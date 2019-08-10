package com.ibeetl.bbs.action;

import com.alibaba.fastjson.JSONObject;
import com.ibeetl.bbs.common.WebUtils;
import com.ibeetl.bbs.config.BbsConfig;
import com.ibeetl.bbs.config.CaffeineConfig;
import com.ibeetl.bbs.es.annotation.EsEntityType;
import com.ibeetl.bbs.es.annotation.EsIndexType;
import com.ibeetl.bbs.es.annotation.EsOperateType;
import com.ibeetl.bbs.es.service.EsService;
import com.ibeetl.bbs.es.vo.IndexObject;
import com.ibeetl.bbs.model.BbsMessage;
import com.ibeetl.bbs.model.BbsModule;
import com.ibeetl.bbs.model.BbsPost;
import com.ibeetl.bbs.model.BbsReply;
import com.ibeetl.bbs.model.BbsTopic;
import com.ibeetl.bbs.model.BbsUser;
import com.ibeetl.bbs.service.BBSService;
import com.ibeetl.bbs.service.BbsUserService;
import com.ibeetl.bbs.util.AddressUtil;
import com.ibeetl.bbs.util.DateUtil;
import com.ibeetl.bbs.util.Functions;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.engine.PageQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BBSController {

    SQLManager          sql;
    BbsUserService      gitUserService;
    BBSService          bbsService;
    WebUtils            webUtils;
    Functions           functionUtil;
    EsService           esService;
    CacheManager        cacheManager;
    BbsConfig           bbsConfig;
    HttpServletRequest  request;
    HttpServletResponse response;

    static String filePath = null;

    static {
        filePath = System.getProperty("user.dir");
        File file = new File("upload", filePath);
        if (!file.exists()) {
            file.mkdirs();
        }

    }

    @RequestMapping("/bbs/share")
    public ModelAndView share() {
        return new ModelAndView("forward:/bbs/topic/module/1-1.html");
    }

    @GetMapping({"/bbs/index", "/bbs/index/{p}.html"})
    public String index(@PathVariable(required = false) Integer p, String keyword) {
        if (p == null) {
            p = 1;
        }
        String view;
        if (StringUtils.isBlank(keyword)) {
            view = "/index.html";
            PageQuery query = new PageQuery(p, null);
            //因为用了spring boot缓存,sb是用返回值做缓存,所以service再次返回了pageQuery以缓存查询结果
            query = bbsService.getTopics(query);
            request.setAttribute("topicPage", query);
            request.setAttribute("pageName", "首页综合");
        } else {
            view = "/lucene/lucene.html";
            //查询索引
            PageQuery<IndexObject> searcherKeywordPage = this.esService.getQueryPage(keyword, p);
            request.setAttribute("searcherPage", searcherKeywordPage);
            request.setAttribute("pageName", keyword);
            request.setAttribute("resultnum", searcherKeywordPage.getTotalRow());
        }
        return view;
    }

    @RequestMapping("/bbs/myMessage.html")
    public ModelAndView myPage() {
        ModelAndView view = new ModelAndView();
        view.setViewName("/message.html");
        BbsUser        user = webUtils.currentUser();
        List<BbsTopic> list = bbsService.getMyTopics(user.getId());
        view.addObject("list", list);
        return view;
    }

    @RequestMapping("/bbs/my/{p}.html")
    public RedirectView openMyTopic(@PathVariable int p) {
        BbsUser    user    = webUtils.currentUser();
        BbsMessage message = bbsService.makeOneBbsMessage(user.getId(), p, 0);
        this.bbsService.updateMyTopic(message.getId(), 0);
        return new RedirectView(request.getContextPath() + "/bbs/topic/" + p + "-1.html");
    }

    @RequestMapping("/bbs/topic/hot")
    public RedirectView hotTopic() {
        return new RedirectView("/bbs/topic/hot/1");
    }

    @RequestMapping("/bbs/topic/hot/{p}")
    public ModelAndView hotTopic(@PathVariable int p) {
        ModelAndView view = new ModelAndView();
        view.setViewName("/bbs/index.html");
        PageQuery query = new PageQuery(p);
        query = bbsService.getHotTopics(query);
        view.addObject("topicPage", query);
        return view;
    }

    @RequestMapping("/bbs/topic/nice")
    public ModelAndView niceTopic() {
        return new ModelAndView("forward:/bbs/topic/nice/1");
    }

    @RequestMapping("/bbs/topic/nice/{p}")
    public ModelAndView niceTopic(@PathVariable int p, ModelAndView view) {
        view.setViewName("/bbs/index.html");
        PageQuery query = new PageQuery(p, null);
        query = bbsService.getNiceTopics(query);
        view.addObject("topicPage", query);
        return view;
    }

    @RequestMapping("/bbs/topic/{id}-{p}.html")
    @EsIndexType(entityType = EsEntityType.BbsTopic, operateType = EsOperateType.UPDATE)
    public ModelAndView topic(@PathVariable final int id, @PathVariable int p) {
        ModelAndView view = new ModelAndView();
        view.setViewName("/detail.html");
        PageQuery query = new PageQuery(p);
        query.setPara("topicId", id);
        query = bbsService.getPosts(query);
        view.addObject("postPage", query);

        BbsTopic topic    = bbsService.getTopic(id);
        BbsTopic template = new BbsTopic();
        template.setId(id);
        template.setPv(topic.getPv() + 1);
        sql.updateTemplateById(template);

        view.addObject("topic", topic);
        return view;
    }

    @RequestMapping("/bbs/topic/module/{id}-{p}.html")
    public ModelAndView module(@PathVariable final Integer id, @PathVariable Integer p) {
        ModelAndView view = new ModelAndView();
        view.setViewName("/index.html");
        PageQuery query = new PageQuery<>(p);
        query.setPara("moduleId", id);
        query = bbsService.getTopics(query);
        view.addObject("topicPage", query);
        if (query.getList().size() > 0) {
            BbsTopic bbsTopic = (BbsTopic) query.getList().get(0);
            view.addObject("pageName", bbsTopic.getTails().get("moduleName"));
            view.addObject("module", this.bbsService.getModule(id));
        }
        return view;
    }

    @RequestMapping("/bbs/topic/add.html")
    public ModelAndView addTopic(ModelAndView view) {

        view.setViewName("/post.html");
        return view;
    }

    /**
     * 文章发布改为Ajax方式提交更友好
     */
    @ResponseBody
    @PostMapping("/bbs/topic/save")
    @EsIndexType(entityType = EsEntityType.BbsTopic, operateType = EsOperateType.ADD, key = "tid")
    @EsIndexType(entityType = EsEntityType.BbsPost, operateType = EsOperateType.ADD, key = "pid")
    public JSONObject saveTopic(BbsTopic topic, BbsPost post, String code, String title, String postContent) {
        //@TODO， 防止频繁提交
        BbsUser user = webUtils.currentUser();
//		Date lastPostTime = bbsService.getLatestPost(user.getId());
//		long now = System.currentTimeMillis();
//		long temp = lastPostTime.getTime();
//		if(now-temp<1000*10){
//			//10秒之内的提交都不处理
//			throw new RuntimeException("提交太快，处理不了，上次提交是 "+lastPostTime);
//		}

        HttpSession session = request.getSession(true);
        String      verCode = (String) session.getAttribute(LoginController.POST_CODE_NAME);


        JSONObject result = new JSONObject();
        result.put("err", 1);
        if (user == null) {
            result.put("msg", "请先登录后再继续！");
            return result;
        }

        if (!verCode.equals(code)) {
            result.put("msg", "验证码不正确");
            return result;
        }

        if (title.length() < 5 || postContent.length() < 10) {
            //客户端需要完善
            result.put("msg", "标题或内容太短！");
            return result;
        }


        BbsModule module = this.bbsService.getModule(topic.getModuleId());

        if (!isAllowAdd(module)) {
            result.put("msg", "板块 [" + module.getName() + "] 普通用户只能浏览");
            return result;
        }
        //4个小时的提交总数
        Date lastPost = DateUtil.getDate(new Date(), bbsConfig.getTopicCountMinutes());
        int  count    = bbsService.getTopicCount(user, lastPost);
        if (count >= bbsConfig.getTopicCount()) {
            String msg = AddressUtil.getIPAddress(request) + " " + user.getUserName() + " 提交主题太频繁，稍后再提交，紧急问题入群";
            result.put("msg", msg);
            System.out.println(msg);
            return result;
        }
        topic.setIsNice(0);
        topic.setIsUp(0);
        topic.setPv(1);
        topic.setPostCount(1);
        topic.setReplyCount(0);
        post.setHasReply(0);
        topic.setContent(title);
        post.setContent(postContent);
        bbsService.saveTopic(topic, post, user);

        result.put("err", 0);
        result.put("tid", topic.getId());
        result.put("pid", post.getId());
        result.put("msg", "/bbs/topic/" + topic.getId() + "-1.html");


        return result;
    }

    private boolean isAllowAdd(BbsModule module) {
        return functionUtil.allowPost(module, request, response);
    }

    @ResponseBody
    @RequestMapping("/bbs/post/save")
    @EsIndexType(entityType = EsEntityType.BbsPost, operateType = EsOperateType.ADD)
    public JSONObject savePost(BbsPost post) {
        JSONObject result = new JSONObject();
        result.put("err", 1);
        if (post.getContent().length() < 5) {
            result.put("msg", "内容太短，请重新编辑！");
        } else {
            post.setHasReply(0);
            post.setCreateTime(new Date());
            BbsUser user = webUtils.currentUser();
            bbsService.savePost(post, user);
            BbsTopic topic     = bbsService.getTopic(post.getTopicId());
            int      totalPost = topic.getPostCount() + 1;
            topic.setPostCount(totalPost);
            bbsService.updateTopic(topic);

            bbsService.notifyParticipant(topic.getId(), user.getId());

            int pageSize = (int) PageQuery.DEFAULT_PAGE_SIZE;
            int page     = (totalPost / pageSize) + (totalPost % pageSize == 0 ? 0 : 1);
            result.put("msg", "/bbs/topic/" + post.getTopicId() + "-" + page + ".html");
            result.put("err", 0);
            result.put("id", post.getId());
        }
        return result;
    }


    /**
     * 回复评论改为Ajax方式提升体验
     */
    @ResponseBody
    @PostMapping("/bbs/reply/save")
    @EsIndexType(entityType = EsEntityType.BbsReply, operateType = EsOperateType.ADD)
    public JSONObject saveReply(BbsReply reply) {
        JSONObject result = new JSONObject();
        result.put("err", 1);
        BbsUser user = webUtils.currentUser();
        if (user == null) {
            result.put("msg", "未登录用户！");
        } else if (reply.getContent().length() < 2) {
            result.put("msg", "回复内容太短，请修改!");
        } else {
            reply.setUserId(user.getId());
            reply.setPostId(reply.getPostId());
            reply.setCreateTime(new Date());
            bbsService.saveReply(reply);
            reply.set("bbsUser", user);
            reply.setUser(user);
            result.put("msg", "评论成功！");
            result.put("err", 0);

            BbsTopic topic = bbsService.getTopic(reply.getTopicId());
            bbsService.notifyParticipant(reply.getTopicId(), user.getId());
            result.put("id", reply.getId());
        }
        return result;
    }

    @RequestMapping("/bbs/user/{id}")
    public ModelAndView saveUser(ModelAndView view, @PathVariable int id) {
        view.setViewName("/bbs/user/user.html");
        BbsUser user = sql.unique(BbsUser.class, id);
        view.addObject("user", user);
        return view;
    }


    // ============== 上传文件路径：项目根目录 upload
    @RequestMapping("/bbs/upload")
    @ResponseBody
    public Map<String, Object> upload(@RequestParam("editormd-image-file") MultipartFile file) {
        String              rootPath = filePath;
        Map<String, Object> map      = new HashMap<String, Object>();
        map.put("success", false);
        try {
            BbsUser user = webUtils.currentUser();
            if (null == user) {
                map.put("error", 1);
                map.put("msg", "上传出错，请先登录！");
                return map;
            }
            //从剪切板粘贴上传没有后缀名，通过此方法可以获取后缀名
            Matcher matcher = Pattern.compile("^image/(.+)$", Pattern.CASE_INSENSITIVE).matcher(Objects.requireNonNull(file.getContentType()));
            if (matcher.find()) {
                String newName   = UUID.randomUUID().toString() + System.currentTimeMillis() + "." + matcher.group(1);
                String filePaths = rootPath + "/upload/";
                File   fileout   = new File(filePaths);
                if (!fileout.exists()) {
                    fileout.mkdirs();
                }
                FileCopyUtils.copy(file.getBytes(), new File(filePaths + newName));
                map.put("file_path", request.getContextPath() + "/bbs/showPic/" + newName);
                map.put("msg", "图片上传成功！");
                map.put("success", true);
                return map;
            } else {
                map.put("success", "不支持的上传文件格式！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            map.put("msg", "图片上传出错！");
        }
        return map;
    }

    @RequestMapping("/bbs/showPic/{path}.{ext}")
    public void showPic(@PathVariable String path, @PathVariable String ext) {
        String rootPath = filePath;

        try {
            String          filePath = rootPath + "/upload/" + path + "." + ext;
            FileInputStream fins     = new FileInputStream(filePath);
            response.setContentType("image/jpeg");
            FileCopyUtils.copy(fins, response.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ======================= admin


    @ResponseBody
    @PostMapping("/bbs/admin/topic/nice/{id}")
    @EsIndexType(entityType = EsEntityType.BbsTopic, operateType = EsOperateType.UPDATE)
    public JSONObject editNiceTopic(@PathVariable int id) {
        JSONObject result = new JSONObject();
        if (!webUtils.isAdmin(request, response)) {
            //如果有非法使用，不提示具体信息，直接返回null
            result.put("err", 1);
            result.put("msg", "呵呵~~");
        } else {
            BbsTopic db   = bbsService.getTopic(id);
            Integer  nice = db.getIsNice();
            db.setIsNice(nice > 0 ? 0 : 1);
            bbsService.updateTopic(db);
            result.put("err", 0);
            result.put("msg", "success");
        }
        return result;
    }

    @ResponseBody
    @PostMapping("/bbs/admin/topic/up/{id}")
    @EsIndexType(entityType = EsEntityType.BbsTopic, operateType = EsOperateType.UPDATE)
    public JSONObject editUpTopic(@PathVariable int id) {
        JSONObject result = new JSONObject();
        if (!webUtils.isAdmin(request, response)) {
            //如果有非法使用，不提示具体信息，直接返回null
            result.put("err", 1);
            result.put("msg", "呵呵~~");
        } else {
            BbsTopic db = bbsService.getTopic(id);
            Integer  up = db.getIsUp();
            db.setIsUp(up > 0 ? 0 : 1);
            bbsService.updateTopic(db);
            result.put("err", 0);
            result.put("msg", "success");
        }
        return result;
    }


    @ResponseBody
    @PostMapping("/bbs/admin/topic/delete/{id}")
    @EsIndexType(entityType = EsEntityType.BbsTopic, operateType = EsOperateType.DELETE)
    public JSONObject deleteTopic(@PathVariable int id) {
        JSONObject result = new JSONObject();
        if (!webUtils.isAdmin(request, response)) {
            //如果有非法使用，不提示具体信息，直接返回null
            result.put("err", 1);
            result.put("msg", "呵呵~~");
        } else {
            bbsService.deleteTopic(id);
            result.put("err", 0);
            result.put("msg", "success");
        }
        return result;
    }

    @ResponseBody
    @PostMapping("/bbs/admin/topic/deleteUser/{id}")
    @EsIndexType(entityType = EsEntityType.BbsTopic, operateType = EsOperateType.DELETE)
    public JSONObject deleteTopicOwner(@PathVariable int id) {
        JSONObject result = new JSONObject();
        if (!webUtils.isAdmin(request, response)) {
            //如果有非法使用，不提示具体信息，直接返回null
            result.put("err", 1);
            result.put("msg", "呵呵~~");
        } else {
            BbsTopic topic  = bbsService.getTopic(id);
            Integer  userId = topic.getUserId();
            this.gitUserService.removeUser(userId);
            result.put("err", 0);
            result.put("msg", "success");
        }
        return result;
    }

    @RequestMapping("/bbs/admin/post/{p}")
    public String adminPosts(@PathVariable int p) {
        PageQuery query = new PageQuery(p);
        query.setPara("isAdmin", true);
        bbsService.getPosts(query);
        request.setAttribute("postPage", query);
        return "/bbs/admin/postList.html";
    }

    @RequestMapping("/bbs/admin/post/edit/{id}.html")
    public String editPost(@PathVariable int id) {
        BbsPost post = sql.unique(BbsPost.class, id);
        request.setAttribute("post", post);
        request.setAttribute("topic", sql.unique(BbsTopic.class, post.getTopicId()));
        return "/postEdit.html";
    }

    /**
     * ajax方式编辑内容
     */
    @ResponseBody
    @RequestMapping("/bbs/admin/post/update")
    @EsIndexType(entityType = EsEntityType.BbsPost, operateType = EsOperateType.UPDATE)
    public JSONObject updatePost(BbsPost post) {
        JSONObject result = new JSONObject();
        result.put("err", 1);
        if (post.getContent().length() < 10) {
            result.put("msg", "输入的内容太短，请重新编辑！");
        } else {
            BbsPost db = sql.unique(BbsPost.class, post.getId());
            if (canUpdatePost(db)) {
                db.setContent(post.getContent());
                bbsService.updatePost(db);
                result.put("id", post.getId());
                result.put("msg", "/bbs/topic/" + db.getTopicId() + "-1.html");
                result.put("err", 0);
            } else {
                result.put("msg", "不是自己发表的内容无法编辑！");
            }
        }
        return result;
    }

    /**
     * ajax方式删除内容
     */
    @ResponseBody
    @RequestMapping("/bbs/admin/post/delete/{id}")
    @EsIndexType(entityType = EsEntityType.BbsPost, operateType = EsOperateType.DELETE)
    public JSONObject deletePost(@PathVariable int id) {
        JSONObject result = new JSONObject();
        BbsPost    post   = sql.unique(BbsPost.class, id);
        if (canUpdatePost(post)) {
            bbsService.deletePost(id);
            result.put("err", 0);
            result.put("msg", "删除成功！");
        } else {
            result.put("err", 1);
            result.put("msg", "不是自己发表的内容无法删除！");
        }
        return result;
    }


    @ResponseBody
    @PostMapping("/bbs/admin/reply/delete/{id}")
    @EsIndexType(entityType = EsEntityType.BbsReply, operateType = EsOperateType.DELETE)
    public JSONObject deleteReply(@PathVariable int id) {

        JSONObject result = new JSONObject();
        if (canDeleteReply(id)) {
            bbsService.deleteReplay(id);
            result.put("err", 0);
            result.put("msg", "success");
        } else {
            result.put("err", 1);
            result.put("msg", "无法删除他人的回复");
        }
        return result;
    }

    private boolean canDeleteReply(Integer replyId) {

        BbsUser  user  = this.webUtils.currentUser();
        BbsReply reply = bbsService.getReply(replyId);
        if (reply.getUserId().equals(user.getId())) {
            return true;
        }
        //如果是admin
        return user.getUserName().equals("admin");
    }

    private boolean canUpdatePost(BbsPost post) {

        BbsUser user = this.webUtils.currentUser();
        if (post.getUserId().equals(user.getId())) {
            return true;
        }
        //如果是admin
        return user.getUserName().equals("admin");
    }

    /**
     * 初始化索引
     */
    @ResponseBody
    @RequestMapping("/bbs/admin/es/init")
    public JSONObject initEsIndex() {
        JSONObject result = new JSONObject();
        if (!webUtils.isAdmin(request, response)) {
            //如果有非法使用，不提示具体信息，直接返回null
            result.put("err", 1);
            result.put("msg", "呵呵~~");
        } else {
            esService.initIndex();
            result.put("err", 0);
            result.put("msg", "ES初始化成功");
        }
        return result;
    }

    /**
     * 踩或顶 评论
     */
    @PostMapping("/bbs/post/support/{postId}")
    @ResponseBody
    @EsIndexType(entityType = EsEntityType.BbsPost, operateType = EsOperateType.UPDATE)
    public JSONObject updatePostSupport(@PathVariable Integer postId, @RequestParam Integer num) {
        JSONObject result = new JSONObject();
        result.put("err", 1);
        BbsUser user = webUtils.currentUser();
        if (user == null) {
            result.put("msg", "未登录用户！");
        } else {
            BbsPost post = bbsService.getPost(postId);

            Cache        cache        = cacheManager.getCache(CaffeineConfig.Caches.postSupport.name());
            ValueWrapper valueWrapper = Objects.requireNonNull(cache).get(user.getId() + ":" + post.getId());
            if (valueWrapper != null && valueWrapper.get() != null) {
                result.put("err", 1);
                result.put("msg", "请勿频繁点赞，休息一下吧~~~");
            } else {
                if (num == 0) {
                    int cons = post.getCons() != null ? post.getCons() : 0;
                    post.setCons(++cons);
                    result.put("data", post.getCons());
                } else {
                    int pros = post.getPros() != null ? post.getPros() : 0;
                    post.setPros(++pros);
                    result.put("data", post.getPros());
                }
                bbsService.updatePost(post);

                result.put("id", post.getId());
                result.put("err", 0);
                cache.put(user.getId() + ":" + post.getId(), 1);
            }
        }
        return result;
    }

    /**
     * 提问人或管理员是否已采纳
     */
    @PostMapping("/bbs/user/post/accept/{postId}")
    @ResponseBody
    @EsIndexType(entityType = EsEntityType.BbsPost, operateType = EsOperateType.UPDATE)
    public JSONObject updatePostAccept(@PathVariable Integer postId) {
        JSONObject result = new JSONObject();
        result.put("err", 1);
        BbsUser user = webUtils.currentUser();
        BbsPost post = bbsService.getPost(postId);
        if (user == null || post == null || !webUtils.isAdmin(request, response) || !user.getId().equals(post.getUserId())) {
            result.put("err", 1);
            result.put("msg", "无法操作");
        } else {

            post.setIsAccept((post.getIsAccept() == null || post.getIsAccept() == 0) ? 1 : 0);
            result.put("data", post.getIsAccept());
            bbsService.updatePost(post);
            result.put("err", 0);
            result.put("id", post.getId());
        }
        return result;
    }
}
