package com.ibeetl.bbs.es.service;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibeetl.bbs.es.annotation.EsEntityType;
import com.ibeetl.bbs.es.annotation.EsFallback;
import com.ibeetl.bbs.es.annotation.EsOperateType;
import com.ibeetl.bbs.es.config.EsConfig;
import com.ibeetl.bbs.es.entity.BbsIndex;
import com.ibeetl.bbs.es.vo.IndexObject;
import com.ibeetl.bbs.model.*;
import com.ibeetl.bbs.service.BBSService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.engine.PageQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EsService {

    BBSService    bbsService;
    SQLManager    sqlManager;
    ObjectMapper  objectMapper;
    EsConfig      esConfig;
    GroupTemplate beetlTemplate;
    Executor      executor = Executor.newInstance();


    /**
     * 公共操作方法
     */
    @EsFallback
    public void editEsIndex(EsEntityType entityType, EsOperateType operateType, Object id) {
        if (operateType == EsOperateType.ADD || operateType == EsOperateType.UPDATE) {
            BbsIndex bbsIndex = this.createBbsIndex(entityType, (Integer) id);
            if (bbsIndex != null) {
                this.saveBbsIndex(bbsIndex);
            }
        } else if (operateType == EsOperateType.DELETE) {
            this.deleteBbsIndex((String) id);
        }
    }

    public void editEsIndexFallback(EsEntityType entityType, EsOperateType operateType, Object id) {
        log.warn("ES服务[editEsIndex]降级处理...");
    }

    /**
     * 重构索引
     */
    @EsFallback
    public void initIndex() {
        try {
            executor.execute(Request.Delete(esConfig.getUrl())).discardContent();
            batchSaveBbsIndex(BbsTopic.class);
            batchSaveBbsIndex(BbsPost.class);
            batchSaveBbsIndex(BbsReply.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void initIndexFallback() {
        log.warn("ES服务[editEsIndex]降级处理...");
    }

    /**
     * 批量插入索引
     */
    private <T> void batchSaveBbsIndex(Class<T> clazz) {
        int curPage  = 1;
        int pageSize = 500;

        while (true) {
            int     startRow = 1 + (curPage - 1) * pageSize;
            List<T> list     = sqlManager.all(clazz, startRow, pageSize);
            if (list != null && list.size() > 0) {
                List<BbsIndex> indexList = new ArrayList<>();
                for (T t : list) {
                    BbsIndex bbsIndex = null;

                    if (t instanceof BbsTopic) {
                        BbsTopic topic     = (BbsTopic) t;
                        BbsPost  firstPost = bbsService.getFirstPost(topic.getId());
                        bbsIndex = new BbsIndex(topic.getId(), null, null, topic.getUserId(), topic.getContent(), topic.getCreateTime(), 0, 0, firstPost != null ? firstPost.getIsAccept() : 0, topic.getPv());
                    }
                    if (t instanceof BbsPost) {
                        BbsPost  post  = (BbsPost) t;
                        BbsTopic topic = bbsService.getTopic(post.getTopicId());
                        bbsIndex = new BbsIndex(post.getTopicId(), post.getId(), null, post.getUserId(), post.getContent(), post.getCreateTime(), post.getPros(), post.getCons(), post.getIsAccept(), topic.getPv());
                    }
                    if (t instanceof BbsReply) {
                        BbsReply reply = (BbsReply) t;
                        bbsIndex = new BbsIndex(reply.getTopicId(), reply.getPostId(), reply.getId(), reply.getUserId(), reply.getContent(), reply.getCreateTime(), 0, 0, 0, 0);
                    }
                    if (bbsIndex == null) {
                        log.error("未定义类型转换");
                    } else {
                        indexList.add(bbsIndex);
                    }

                }
                indexList.forEach(this::saveBbsIndex);
                curPage++;
            } else {
                break;
            }
        }
    }


    /**
     * 创建索引对象
     */
    public BbsIndex createBbsIndex(EsEntityType entityType, Integer id) {

        BbsIndex bbsIndex = null;
        if (entityType == EsEntityType.BbsTopic) {
            BbsTopic topic     = bbsService.getTopic(id);
            BbsPost  firstPost = bbsService.getFirstPost(topic.getId());
            bbsIndex = new BbsIndex(topic.getId(), null, null, topic.getUserId(), topic.getContent(), topic.getCreateTime(), 0, 0, firstPost != null ? firstPost.getIsAccept() : 0, topic.getPv());
        } else if (entityType == EsEntityType.BbsPost) {
            BbsPost  post  = bbsService.getPost(id);
            BbsTopic topic = bbsService.getTopic(post.getTopicId());
            bbsIndex = new BbsIndex(post.getTopicId(), post.getId(), null, post.getUserId(), post.getContent(), post.getCreateTime(), post.getPros(), post.getCons(), post.getIsAccept(), topic.getPv());
        } else if (entityType == EsEntityType.BbsReply) {
            BbsReply reply = bbsService.getReply(id);
            bbsIndex = new BbsIndex(reply.getTopicId(), reply.getPostId(), reply.getId(), reply.getUserId(), reply.getContent(), reply.getCreateTime(), 0, 0, 0, 0);
        }
        if (bbsIndex == null) {
            log.error("未定义类型转换");
        }
        return bbsIndex;
    }


    /**
     * 保存或更新索引
     */
    private void saveBbsIndex(BbsIndex bbsIndex) {
        try {
            executor.execute(Request.Post(esConfig.getContentUrl() + bbsIndex)
                    .bodyString(JSON.toJSONString(bbsIndex), ContentType.APPLICATION_JSON))
                    .discardContent();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除索引
     */
    private void deleteBbsIndex(String id) {
        try {
            executor.execute(Request.Delete(esConfig.getContentUrl() + id))
                    .discardContent();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 创建所有并返回搜索结果
     */
    @EsFallback
    public PageQuery<IndexObject> getQueryPage(String keyword, int p) {
        if (p <= 0) {
            p = 1;
        }
        int  pageNumber = p;
        long pageSize   = PageQuery.DEFAULT_PAGE_SIZE;

        if (keyword != null) {
            keyword = this.string2Json(keyword);
        }
        PageQuery<IndexObject> pageQuery = new PageQuery<>(pageNumber, pageSize);
        try {
            Template template = beetlTemplate.getTemplate("/bssContent.html");
            template.binding("pageSize", pageSize);
            template.binding("pageNumber", pageNumber);
            template.binding("keyword", keyword);
            String result = executor.execute(Request.Post(esConfig.getContentSearchUrl())
                    .bodyString(template.render(), ContentType.APPLICATION_JSON))
                    .returnContent()
                    .asString(StandardCharsets.UTF_8);

            List<IndexObject> indexObjectList = new ArrayList<>();

            JsonNode root  = objectMapper.readTree(result);
            long     total = root.get("hits").get("total").asLong();

            for (JsonNode jsonNode : root.get("hits").get("hits")) {
                double   score = jsonNode.get("_score").asDouble();
                BbsIndex index = objectMapper.convertValue(jsonNode.get("_source"), BbsIndex.class);

                index.setContent(jsonNode.get("highlight").get("content").get(0).asText());
                if (index.getTopicId() != null) {
                    IndexObject indexObject = null;

                    BbsTopic topic = bbsService.getTopic(index.getTopicId());

                    BbsUser   user   = topic.getUser();
                    BbsModule module = topic.getModule();

                    if (index.getReplyId() != null) {
                        indexObject = new IndexObject(topic.getId(), topic.getIsUp(), topic.getIsNice(), user,
                                topic.getCreateTime(), topic.getPostCount(), topic.getPv(), module,
                                topic.getContent(), index.getContent(), 3, score);

                    } else if (index.getPostId() != null) {
                        indexObject = new IndexObject(topic.getId(), topic.getIsUp(), topic.getIsNice(), user,
                                topic.getCreateTime(), topic.getPostCount(), topic.getPv(), module,
                                topic.getContent(), index.getContent(), 2, score);

                    } else if (index.getTopicId() != null) {
                        String  postContent = "";
                        BbsPost firstPost   = bbsService.getFirstPost(index.getTopicId());
                        if (firstPost != null) {
                            postContent = firstPost.getContent();
                        }
                        indexObject = new IndexObject(topic.getId(), topic.getIsUp(), topic.getIsNice(), user,
                                topic.getCreateTime(), topic.getPostCount(), topic.getPv(), module,
                                index.getContent(), postContent, 1, score);
                    }

                    indexObjectList.add(indexObject);
                }
            }
            pageQuery.setTotalRow(total);
            pageQuery.setList(indexObjectList);
            return pageQuery;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public PageQuery<IndexObject> getQueryPageFallback(String keyword, int p) {
        log.warn("ES服务[getQueryPage]降级处理...");
        if (p <= 0) {
            p = 1;
        }
        int                    pageNumber = p;
        long                   pageSize   = PageQuery.DEFAULT_PAGE_SIZE;
        String                 kw         = keyword.trim().replaceAll("</?\\w+[^>]>", "");
        PageQuery<IndexObject> pageQuery  = new PageQuery<>(pageNumber, pageSize);

        PageQuery<BbsPost> postPage = bbsService.queryPostByContent(kw, pageNumber, pageSize);
        List<IndexObject> indexObjects = Optional.ofNullable(postPage.getList())
                .orElse(Collections.emptyList())
                .stream()
                .peek(post -> post.setContent(post.getContent().replaceAll("</?\\w+[^>]*>", "").toLowerCase()))
                .filter(post -> StringUtils.isNotBlank(post.getContent()))
                .map(post -> {
                    String content = post.getContent();
                    int    index   = Math.max(0, content.indexOf(kw));
                    int    start   = Math.max(index - 100, 0);
                    int    end     = Math.min(index + kw.length() + 100, content.length());
                    return new IndexObject(post.getTopicId(), 0, 0, new BbsUser(post.getUserId(), "注册用户"),
                            post.getCreateTime(), post.getCons(), post.getPros(), null,
                            content.substring(0, Math.min(50, content.length())) + "...",
                            content.substring(start, index) +
                                    "<font color=\"red\">" + kw + "</font>" +
                                    content.substring(index + kw.length(), end),
                            1,
                            1);
                })
                .collect(Collectors.toList());
        pageQuery.setTotalRow(postPage.getTotalRow());
        pageQuery.setList(indexObjects);
        return pageQuery;
    }

    /**
     * JSON字符串特殊字符处理
     */
    private String string2Json(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '/':
                    sb.append("\\/");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

}
