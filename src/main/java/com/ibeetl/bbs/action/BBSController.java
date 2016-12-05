package com.ibeetl.bbs.action;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.engine.PageQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.ibeetl.bbs.common.WebUtils;
import com.ibeetl.bbs.model.BbsPost;
import com.ibeetl.bbs.model.BbsReply;
import com.ibeetl.bbs.model.BbsTopic;
import com.ibeetl.bbs.model.BbsUser;
import com.ibeetl.bbs.service.BBSService;
import com.ibeetl.bbs.service.BbsUserService;

@Controller
public class BBSController {

	@Autowired
	SQLManager sql;
	@Autowired
	BbsUserService gitUserService;


	@Autowired
	BBSService bbsService;


	@Autowired
	WebUtils webUtils;
	
	static String filePath = null;
	static {
		filePath = System.getProperty("user.dir");
		File file = new File("upload",filePath);
		file.mkdirs();
	}

	@RequestMapping("/bbs/share")
	public RedirectView share(HttpServletRequest request){
		return new RedirectView("/bbs/topic/module/1-1.html");
	}
	
	@RequestMapping("/bbs/index")
	public RedirectView index(HttpServletRequest request){
		return new RedirectView("/bbs/index/1.html");
	}

	@RequestMapping("/bbs/index/{p}.html")
	public ModelAndView  index(@PathVariable int p){
		ModelAndView view = new ModelAndView();
		view.setViewName("/index.html");
		PageQuery query = new PageQuery(p, null);
		bbsService.getTopics(query);
		view.addObject("topicPage", query);
		return view;
	}

	@RequestMapping("/bbs/topic/hot")
	public RedirectView hotTopic(){
		return new RedirectView("/bbs/topic/hot/1");
	}

	@RequestMapping("/bbs/topic/hot/{p}")
	public ModelAndView hotTopic(@PathVariable int p){
		 ModelAndView view = new ModelAndView();
		view.setViewName("/bbs/index.html");
		PageQuery query = new PageQuery(p, null);
		bbsService.getHotTopics(query);
		view.addObject("topicPage", query);
		return view;
	}

	@RequestMapping("/bbs/topic/nice")
	public RedirectView niceTopic(){
		return new RedirectView("/bbs/topic/nice/1");
	}

	@RequestMapping("/bbs/topic/nice/{p}")
	public ModelAndView niceTopic(@PathVariable int p, ModelAndView view){
		view.setViewName("/bbs/index.html");
		PageQuery query = new PageQuery(p, null);
		bbsService.getNiceTopics(query);
		view.addObject("topicPage", query);
		return view;
	}

	@RequestMapping("/bbs/topic/{id}-{p}.html")
	public ModelAndView topic(@PathVariable final int id, @PathVariable int p){
		ModelAndView view = new  ModelAndView();
		view.setViewName("/detail.html");
		PageQuery query = new PageQuery(p, new HashMap(){{put("topicId", id);}});
		bbsService.getPosts(query);
		view.addObject("postPage", query);
		BbsTopic topic = bbsService.getTopic(id);
		topic.setPv(topic.getPv() + 1);
		sql.updateById(topic);
		view.addObject("topic", topic);
		return view;
	}

	@RequestMapping("/bbs/topic/module/{id}-{p}.html")
	public ModelAndView module(@PathVariable final int id, @PathVariable int p){
		ModelAndView view = new ModelAndView();
		view.setViewName("/bbs/index.html");
		PageQuery query = new PageQuery(p, new HashMap(){{put("moduleId", id+"");}});
		bbsService.getTopics(query);
		view.addObject("topicPage", query);
		return view;
	}

	@RequestMapping("/bbs/topic/add")
	public ModelAndView addTopic(ModelAndView view){
		view.setViewName("/bbs/topic/add.html");
		return view;
	}

	@RequestMapping("/bbs/topic/save")
	public RedirectView saveTopic(BbsTopic topic, BbsPost post, String topicContent, HttpServletRequest request, HttpServletResponse response){
		topic.setIsNice(0);
		topic.setIsUp(0);
		topic.setPv(1);
		topic.setPostCount(1);
		topic.setReplyCount(0);
		post.setHasReply(0);
		topic.setContent(topicContent);
		bbsService.saveTopic(topic, post, webUtils.currentUser(request, response));
		return new RedirectView("/bbs/topic/"+topic.getId()+"-1");
	}

	@RequestMapping("/bbs/post/save")
	public RedirectView savePost(BbsPost post, HttpServletRequest request, HttpServletResponse response){
		post.setHasReply(0);
		post.setCreateTime(new Date());
		bbsService.savePost(post, webUtils.currentUser(request, response));
		BbsTopic topic = bbsService.getTopic(post.getTopicId());
		topic.setPostCount(topic.getPostCount() + 1);
		sql.updateById(topic);
		return new RedirectView("/bbs/topic/"+post.getTopicId()+"-1");
	}

	@RequestMapping("/bbs/reply/{postId}-{p}")
	public ModelAndView listReply(@PathVariable final int postId, @PathVariable int p, ModelAndView view){
		PageQuery query = new PageQuery(p, new HashMap(){{put("postId", postId);}});
		bbsService.getReplys(query);
		view.addObject("replyPage", query);
		view.addObject("postId", postId);
		view.setViewName("/bbs/reply/_reply.html");
		return view;
	}

	@RequestMapping("/bbs/reply/save")
	public ModelAndView saveReply(BbsReply reply, HttpServletRequest request, HttpServletResponse response){
		ModelAndView view = new ModelAndView("/common/replyItem.html");
		
		BbsUser user = webUtils.currentUser(request, response);
		reply.setUserId(user.getId());
		reply.setPostId(reply.getPostId());
		reply.setCreateTime(new Date());
		bbsService.saveReply(reply);
		reply.setUser(user);
		view.addObject("reply",reply);
		return view;
	}

	@RequestMapping("/bbs/user/{id}")
	public ModelAndView saveUser(ModelAndView view, @PathVariable int id){
		view.setViewName("/bbs/user/user.html");
		BbsUser user = sql.unique(BbsUser.class, id);
		view.addObject("user", user);
		return view;
	}

	// ============== 上传文件路径：项目根目录 upload
	@RequestMapping("/bbs/upload")
	@ResponseBody
	public Map<String, Object> upload(@RequestParam("imgFile") MultipartFile file, HttpServletRequest request, HttpServletResponse response){
		String rootPath = filePath;
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			BbsUser user = webUtils.currentUser(request, response);
			if (null == user) {
				map.put("error", 1);
				map.put("message", "上传出错，请先登陆！");
				return map;
			}
			String newName = System.currentTimeMillis() + file.getOriginalFilename();
			
			String filePath = rootPath + "/upload/" + newName;
			FileCopyUtils.copy(file.getBytes(), new File(filePath));
			map.put("url", "/bbs/showPic/" + newName);
			map.put("error", 0);
			return map;
		} catch (Exception e) {
			map.put("error", 1);
			map.put("message", "上传出错！");
		}
		return map;
	}
	
	@RequestMapping("/bbs/showPic/{path}.{ext}")
	public void showPic(@PathVariable String path, @PathVariable String ext,HttpServletRequest request, HttpServletResponse response){
		String rootPath = filePath;
		
		try {
			String filePath = rootPath + "/upload/" + path+"."+ext;
			FileInputStream fins = new FileInputStream(filePath);
			response.setContentType("image/jpeg");
			FileCopyUtils.copy(fins, response.getOutputStream());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ;
	}

	// ======================= admin

	

	@RequestMapping("/bbs/admin/topic/nice/{id}")
	public ModelAndView editNiceTopic(ModelAndView view,@PathVariable int id){
		BbsTopic db = bbsService.getTopic(id);
		Integer nice = db.getIsNice();
		if(nice>0){
			db.setIsNice(0);
		}else{
			db.setIsNice(1);
		}
		sql.updateById(db);
		view.setView(new RedirectView("/bbs/index"));
		return view;
	}
	
	
	@RequestMapping("/bbs/admin/topic/up/{id}")
	public ModelAndView editUpTopic(ModelAndView view,@PathVariable int id){
		BbsTopic db = bbsService.getTopic(id);
		Integer up = db.getIsUp();
		if(up>0){
			db.setIsUp(0);
		}else{
			db.setIsUp(1);
		}
		sql.updateById(db);
		view.setView(new RedirectView("/bbs/index"));
		return view;
	}

	

	@RequestMapping("/bbs/admin/topic/delete/{id}")
	public ModelAndView deleteTopic(ModelAndView view, @PathVariable int id){
		bbsService.deleteTopic(id);
		view.setView(new RedirectView("/bbs/index"));
		return view;
	}

	@RequestMapping("/bbs/admin/post/{p}")
	public ModelAndView adminPosts(ModelAndView view, @PathVariable int p){
		view.setViewName("/bbs/admin/postList.html");
		PageQuery query = new PageQuery(p, new HashMap(){{put("isAdmin", true);}});
		bbsService.getPosts(query);
		view.addObject("postPage", query);
		return view;
	}

	@RequestMapping("/bbs/admin/post/edit/{id}")
	public ModelAndView editPost(ModelAndView view, @PathVariable int id){
		view.setViewName("/bbs/post/edit.html");
		view.addObject("post", sql.unique(BbsPost.class, id));
		return view;
	}

	@RequestMapping("/bbs/admin/post/update")
	public ModelAndView updatePost(ModelAndView view, BbsPost post){
		BbsPost db = sql.unique(BbsPost.class, post.getId());
		db.setContent(post.getContent());
		sql.updateById(db);
		view.setView(new RedirectView("/bbs/topic/"+db.getTopicId()+"-1"));
		return view;
	}

	@RequestMapping("/bbs/admin/post/delete/{id}")
	public ModelAndView deletePost(ModelAndView view, @PathVariable int id){
		bbsService.deletePost(id);
		view.setView(new RedirectView("/bbs/admin/post/1"));
		return view;
	}

	@RequestMapping("/bbs/admin/reply/{p}")
	public ModelAndView adminReplys(ModelAndView view, @PathVariable int p){
		view.setViewName("/bbs/admin/replyList.html");
		PageQuery query = new PageQuery(p, new HashMap(){{put("isAdmin", true);}});
		bbsService.getReplys(query);
		view.addObject("replyPage", query);
		return view;
	}

	@RequestMapping("/bbs/admin/reply/delete/{id}")
	public ModelAndView deleteReply(ModelAndView view, @PathVariable int id){
		sql.deleteById(BbsReply.class, id);
		view.setView(new RedirectView("/bbs/admin/reply/1"));
		return view;
	}
	
	

}
