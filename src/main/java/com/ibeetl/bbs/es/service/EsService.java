package com.ibeetl.bbs.es.service;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibeetl.bbs.es.annotation.EsEntityType;
import com.ibeetl.bbs.es.annotation.EsOperateType;
import com.ibeetl.bbs.es.entity.BbsIndex;
import com.ibeetl.bbs.es.vo.IndexObject;
import com.ibeetl.bbs.model.*;
import com.ibeetl.bbs.service.BBSService;
import com.ibeetl.bbs.util.EsUtil;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.ext.spring.BeetlGroupUtilConfiguration;
import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.engine.PageQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class EsService{

	private Logger logger = LogManager.getLogger(EsService.class);  
	@Autowired
	private BBSService bbsService;
	@Autowired
	SQLManager sqlManager;
	@Autowired
	private ObjectMapper objectMapper;
	@Value("${elasticsearch.bbs.url}")
	private String bbsUrl;
	@Value("${elasticsearch.bbs.content.url}")
	private String bbsContentUrl;
	@Value("${elasticsearch.bbs.content.search.url}")
	private String bbsContentSearchUrl;

	private GroupTemplate beetlTemplate;
	
	public EsService(@Qualifier("beetlContentTemplateConfig") BeetlGroupUtilConfiguration beetlGroupUtilConfiguration){
		this.beetlTemplate = beetlGroupUtilConfiguration.getGroupTemplate();
	}
	
	
	/**
	 * 公共操作方法
	 * @param entityType
	 * @param operateType
	 * @param id
	 */
	public void editEsIndex(EsEntityType entityType,EsOperateType operateType,Object id) {
		if(operateType == EsOperateType.ADD || operateType == EsOperateType.UPDATE) {
			BbsIndex bbsIndex = this.createBbsIndex(entityType, (Integer)id);
			if(bbsIndex != null) {
				this.saveBbsIndex(bbsIndex);
			}
		}else if(operateType == EsOperateType.DELETE) {
			this.deleteBbsIndex((String)id);
		}

	}
	
	/**
	 * 重构索引
	 */
	public void initIndex() {
		try {
			Request.Delete(bbsUrl).execute().discardContent();
			batchSaveBbsIndex(BbsTopic.class);
			batchSaveBbsIndex(BbsPost.class);
			batchSaveBbsIndex(BbsReply.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 批量插入索引
	 * @param clazz
	 */
	public <T> void batchSaveBbsIndex(Class<T> clazz) {
		int curPage = 1;
		int pageSize = 500;
		List<BbsIndex> indexList = new ArrayList<>();
		
		while(true) {
			int startRow = 1+ (curPage - 1) * pageSize;
			List<T> list = sqlManager.all(clazz, startRow, pageSize);
			if(list != null && list.size() > 0) {
				for (T t: list) {
					BbsIndex bbsIndex = null;
					
					if(t instanceof BbsTopic) {
						BbsTopic topic = (BbsTopic)t ;
						BbsPost firstPost = bbsService.getFirstPost(topic.getId());
						bbsIndex = new BbsIndex(topic.getId(), null, null, topic.getUserId(), topic.getContent(), topic.getCreateTime(),0,0,firstPost!=null?firstPost.getIsAccept():0,topic.getPv());
					}
					if(t instanceof BbsPost) {
						BbsPost post = (BbsPost)t;
						BbsTopic topic = bbsService.getTopic(post.getTopicId());
						bbsIndex = new BbsIndex(post.getTopicId(), post.getId(), null, post.getUserId(), post.getContent(), post.getCreateTime(),post.getPros(),post.getCons(),post.getIsAccept(),topic.getPv());
					}
					if(t instanceof BbsReply) {
						BbsReply reply = (BbsReply)t;
						bbsIndex = new BbsIndex(reply.getTopicId(), reply.getPostId(), reply.getId(), reply.getUserId(), reply.getContent(), reply.getCreateTime(),0,0,0,0);
					} 
					if(bbsIndex == null) {
						logger.error("未定义类型转换");
					}else {
						indexList.add(bbsIndex);
					}
					
				}
				indexList.forEach(this::saveBbsIndex);
				indexList = new ArrayList<>();
				curPage ++;
			}else {
				curPage = 1;
				break;
			}
		}
	}
	
	
	/**
	 * 创建索引对象
	 * @param entityType
	 * @param id
	 * @return
	 */
	public BbsIndex createBbsIndex(EsEntityType entityType,Integer id) {
		
		BbsIndex bbsIndex = null;
		if(entityType == EsEntityType.BbsTopic) {
			BbsTopic topic = bbsService.getTopic(id);
			BbsPost firstPost = bbsService.getFirstPost(topic.getId());
			bbsIndex = new BbsIndex(topic.getId(), null, null, topic.getUserId(), topic.getContent(), topic.getCreateTime(),0,0,firstPost != null ?firstPost.getIsAccept():0,topic.getPv());
		}else if(entityType == EsEntityType.BbsPost) {
			BbsPost post = bbsService.getPost(id);
			BbsTopic topic = bbsService.getTopic(post.getTopicId());
			bbsIndex = new BbsIndex(post.getTopicId(), post.getId(), null, post.getUserId(), post.getContent(), post.getCreateTime(),post.getPros(),post.getCons(),post.getIsAccept(),topic.getPv());
		}else if(entityType == EsEntityType.BbsReply) {
			BbsReply reply = bbsService.getReply(id);
			bbsIndex = new BbsIndex(reply.getTopicId(), reply.getPostId(), reply.getId(), reply.getUserId(), reply.getContent(), reply.getCreateTime(),0,0,0,0);
		}
		if(bbsIndex == null) {
			logger.error("未定义类型转换");
		}
		return bbsIndex;
	}
	
	
	/**
	 * 保存或更新索引
	 * @param bbsIndex
	 */
	public void saveBbsIndex(BbsIndex bbsIndex) {
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String>  httpEntity = new HttpEntity<>(JSON.toJSONString(bbsIndex),headers);

		try {
			Request.Post(bbsContentUrl + bbsIndex)
					.bodyString(JSON.toJSONString(bbsIndex), ContentType.APPLICATION_JSON)
					.execute()
					.discardContent();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 删除索引
	 * @param topicId
	 * @param postId
	 * @param replayId
	 */
	public void deleteBbsIndex(Integer topicId,Integer postId,Integer replayId) {
		String key = EsUtil.getEsKey(topicId, postId, replayId);
		deleteBbsIndex(key);
	}
	private void deleteBbsIndex(String id) {
		try {
			Request.Delete(bbsContentUrl + id)
					.execute()
					.discardContent();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	
	
	/**
	 * 创建所有并返回搜索结果
	 * @param keyword
	 * @param p	当前第几页
	 * @return
	 */
	public PageQuery<IndexObject> getQueryPage(String keyword,int p){
		if(p <= 0) {p = 1;}
		int pageNumber = p;
		long pageSize = PageQuery.DEFAULT_PAGE_SIZE;
		
		if(keyword != null) {
			keyword = this.string2Json(keyword);
		}
		PageQuery<IndexObject> pageQuery = new PageQuery<>(pageNumber, pageSize);
		try {
			Template template = beetlTemplate.getTemplate("/bssContent.html");
			template.binding("pageSize", pageSize);
			template.binding("pageNumber", pageNumber);
			template.binding("keyword", keyword);
			Response response = Request.Post(bbsContentSearchUrl)
					.bodyString(template.render(), ContentType.APPLICATION_JSON)
					.execute();
			String result = response.returnContent().asString(StandardCharsets.UTF_8);

			List<IndexObject> indexObjectList = new ArrayList<>();
			
			JsonNode root = objectMapper.readTree(result);
			long total = root.get("hits").get("total").asLong();

			Iterator<JsonNode> iterator = root.get("hits").get("hits").iterator();
			while(iterator.hasNext()) {
				JsonNode jsonNode = iterator.next();

				double score = jsonNode.get("_score").asDouble();
				BbsIndex index = objectMapper.convertValue(jsonNode.get("_source"), BbsIndex.class);

				index.setContent(jsonNode.get("highlight").get("content").get(0).asText());
				if(index.getTopicId() != null) {
					IndexObject indexObject = null;

					BbsTopic topic = bbsService.getTopic(index.getTopicId());

					BbsUser user = topic.getUser();
					BbsModule module = topic.getModule();

					if(index.getReplyId() != null) {
						indexObject = new IndexObject(topic.getId(), topic.getIsUp(), topic.getIsNice(), user,
								topic.getCreateTime(), topic.getPostCount(), topic.getPv(), module,
								topic.getContent(), index.getContent(), 3, score);

					}else if(index.getPostId() != null) {
						indexObject = new IndexObject(topic.getId(), topic.getIsUp(), topic.getIsNice(), user,
								topic.getCreateTime(), topic.getPostCount(), topic.getPv(), module,
								topic.getContent(), index.getContent(), 2, score);

					}else if(index.getTopicId() != null) {
						String postContent = "";
						BbsPost firstPost = bbsService.getFirstPost(index.getTopicId());
						if(firstPost != null) {
							postContent = firstPost.getContent();
						}
						indexObject = new IndexObject(topic.getId(), topic.getIsUp(), topic.getIsNice(), user,
								topic.getCreateTime(), topic.getPostCount(), topic.getPv(), module,
								index.getContent(),postContent , 1, score);
					}

					indexObjectList.add(indexObject);
				}
			}
			pageQuery.setTotalRow(total);
			pageQuery.setList(indexObjectList);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	
		return pageQuery;
	}
	
	/** 
     * JSON字符串特殊字符处理
     * @param s 
     * @return String 
     */  
    public String string2Json(String s) {        
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<s.length(); i++) {  
            char c = s.charAt(i);    
             switch (c){  
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
