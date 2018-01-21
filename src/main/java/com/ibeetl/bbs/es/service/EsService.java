package com.ibeetl.bbs.es.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.engine.PageQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.ibeetl.bbs.es.annotation.EsEntityType;
import com.ibeetl.bbs.es.annotation.EsOperateType;
import com.ibeetl.bbs.es.entity.BbsIndex;
import com.ibeetl.bbs.es.repository.BbsIndexRepository;
import com.ibeetl.bbs.es.vo.IndexObject;
import com.ibeetl.bbs.model.BbsModule;
import com.ibeetl.bbs.model.BbsPost;
import com.ibeetl.bbs.model.BbsReply;
import com.ibeetl.bbs.model.BbsTopic;
import com.ibeetl.bbs.model.BbsUser;
import com.ibeetl.bbs.service.BBSService;
import com.ibeetl.bbs.service.BbsUserService;
import com.ibeetl.bbs.util.HashKit;

@Service
public class EsService{

	@Autowired
	private BbsIndexRepository bbsIndexRepository;
	@Autowired
	private BBSService bbsService;
	@Autowired
	private BbsUserService bbsUserService;
	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;
	@Autowired
	SQLManager sqlManager;
	
	/**
	 * 公共操作方法
	 * @param entityType
	 * @param operateType
	 * @param id
	 */
	public void editEsIndex(EsEntityType entityType,EsOperateType operateType,Integer id) {
		if(operateType == EsOperateType.ADD || operateType == EsOperateType.UPDATE) {
			BbsIndex bbsIndex = this.createBbsIndex(entityType, id);
			this.saveBbsIndex(bbsIndex);
		}else if(operateType == EsOperateType.DELETE) {
			BbsIndex bbsIndex = this.createBbsIndex(entityType, id);
			this.deleteBbsIndex(bbsIndex.getTopicId(), bbsIndex.getPostId(), bbsIndex.getReplayId());
		}

	}
	
	/**
	 * 重构索引
	 */
	public void initIndex() {
		
		elasticsearchTemplate.deleteIndex("bbs");
		
		batchSaveBbsIndex(BbsTopic.class);
		batchSaveBbsIndex(BbsPost.class);
		batchSaveBbsIndex(BbsReply.class);
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
						bbsIndex = new BbsIndex(topic.getId(), null, null, topic.getUserId(), topic.getContent(), topic.getCreateTime());
					}
					if(t instanceof BbsPost) {
						BbsPost post = (BbsPost)t;
						bbsIndex = new BbsIndex(post.getTopicId(), post.getId(), null, post.getUserId(), post.getContent(), post.getCreateTime());
					}
					if(t instanceof BbsReply) {
						BbsReply reply = (BbsReply)t;
						bbsIndex = new BbsIndex(reply.getTopicId(), reply.getPostId(), reply.getId(), reply.getUserId(), reply.getContent(), reply.getCreateTime());
					} 
					if(bbsIndex == null) {
						throw new RuntimeException("未定义类型转换");
					}
					indexList.add(bbsIndex);
				}
				bbsIndexRepository.saveAll(indexList);
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
			bbsIndex = new BbsIndex(topic.getId(), null, null, topic.getUserId(), topic.getContent(), topic.getCreateTime());
		}else if(entityType == EsEntityType.BbsPost) {
			BbsPost post = bbsService.getPost(id);
			bbsIndex = new BbsIndex(post.getTopicId(), post.getId(), null, post.getUserId(), post.getContent(), post.getCreateTime());
		}else if(entityType == EsEntityType.BbsReply) {
			BbsReply reply = bbsService.getReply(id);
			bbsIndex = new BbsIndex(reply.getTopicId(), reply.getPostId(), reply.getId(), reply.getUserId(), reply.getContent(), reply.getCreateTime());
		}
		if(bbsIndex == null) {
			throw new RuntimeException("未定义类型转换");
		}
		return bbsIndex;
	}
	
	
	/**
	 * 保存或更新索引
	 * @param bbsIndex
	 */
	public void saveBbsIndex(BbsIndex bbsIndex) {
		
		StringBuilder key = new StringBuilder();
		key.append(bbsIndex.getTopicId() != null?bbsIndex.getTopicId().toString():"").append(":");
		key.append(bbsIndex.getPostId() != null?bbsIndex.getPostId().toString():"").append(":");
		key.append(bbsIndex.getReplayId() != null?bbsIndex.getReplayId().toString():"").append(":");
		
		bbsIndex.setId(HashKit.md5(key.toString()));
		bbsIndexRepository.save(bbsIndex);
	}
	/**
	 * 删除索引
	 * @param topicId
	 * @param postId
	 * @param replayId
	 */
	public void deleteBbsIndex(Integer topicId,Integer postId,Integer replayId) {
		StringBuilder key = new StringBuilder();
		key.append(topicId != null?topicId.toString():"").append(":");
		key.append(postId != null?postId.toString():"").append(":");
		key.append(replayId != null?replayId.toString():"").append(":");
		
		bbsIndexRepository.deleteById(HashKit.md5(key.toString()));
	}
	
	/**
	 * 创建所有并返回搜索结果
	 * @param keyword
	 * @param p	当前第几页
	 * @return
	 */
	public PageQuery<IndexObject> getQueryPage(String keyword,int p){
		if(p < 0) {p = 0;}
		int pageNumber = p;
		int pageSize = 3;
		
		PageQuery<IndexObject> pageQuery = new PageQuery<>(pageNumber, pageSize);
		
		PageRequest page = PageRequest.of(pageNumber - 1, pageSize);
		Page<BbsIndex> indexPage = bbsIndexRepository.getByContent(keyword, page);
		List<BbsIndex> indexList = indexPage.getContent();
		
		List<IndexObject> indexObjectList = new ArrayList<>();
		for (BbsIndex index : indexList) {
			if(index.getTopicId() != null) {
				IndexObject indexObject = null;
				
				BbsTopic topic = bbsService.getTopic(index.getTopicId());
				
				BbsUser user = (BbsUser)topic.get("bbsUser");
				BbsModule module = (BbsModule)topic.get("bbsModule");
				
				int score = 1;
				if(index.getReplayId() != null) {
					indexObject = new IndexObject(topic.getId(), topic.getIsUp(), topic.getIsNice(), user.getId(), user.getUserName(), 
							topic.getCreateTime(), topic.getPostCount(), topic.getPv(), module.getId(), module.getName(), 
							topic.getContent(), index.getContent(), 3, score);
					
				}else if(index.getPostId() != null) {
					indexObject = new IndexObject(topic.getId(), topic.getIsUp(), topic.getIsNice(), user.getId(), user.getUserName(), 
							topic.getCreateTime(), topic.getPostCount(), topic.getPv(), module.getId(), module.getName(), 
							topic.getContent(), index.getContent(), 2, score);
					
				}else if(index.getTopicId() != null) {
					indexObject = new IndexObject(topic.getId(), topic.getIsUp(), topic.getIsNice(), user.getId(), user.getUserName(), 
							topic.getCreateTime(), topic.getPostCount(), topic.getPv(), module.getId(), module.getName(), 
							topic.getContent(), "", 1, score);
				}
				
				indexObjectList.add(indexObject);
			}
		}
		
		
		
		pageQuery.setTotalRow(indexPage.getTotalElements());
		pageQuery.setList(indexObjectList);
		
		return pageQuery;
	}
	
}
