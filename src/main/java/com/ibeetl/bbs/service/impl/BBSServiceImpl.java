package com.ibeetl.bbs.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.engine.PageQuery;
import org.beetl.sql.core.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.ibeetl.bbs.dao.BbsModuleDao;
import com.ibeetl.bbs.dao.BbsPostDao;
import com.ibeetl.bbs.dao.BbsReplyDao;
import com.ibeetl.bbs.dao.BbsTopicDao;
import com.ibeetl.bbs.dao.BbsUserDao;
import com.ibeetl.bbs.es.vo.IndexObject;
import com.ibeetl.bbs.model.BbsMessage;
import com.ibeetl.bbs.model.BbsPost;
import com.ibeetl.bbs.model.BbsReply;
import com.ibeetl.bbs.model.BbsTopic;
import com.ibeetl.bbs.model.BbsUser;
import com.ibeetl.bbs.service.BBSService;
import com.ibeetl.bbs.service.BbsUserService;

@Service
@Transactional
public class BBSServiceImpl implements BBSService {
	@Autowired
	BbsTopicDao topicDao;
	@Autowired
	BbsPostDao postDao;
	@Autowired
	BbsUserDao userDao;
	@Autowired
	BbsModuleDao moduleDao;
	@Autowired
	BbsReplyDao replyDao;
	@Autowired
	SQLManager sql ;
	
	@Autowired
	BbsUserService gitUserService;

	@Override
	public BbsTopic getTopic(Integer topicId) {
		return topicDao.getTopic(topicId);
	}
	
	@Override
	public BbsPost getPost(int id) {
		return postDao.unique(id);
	}
	
	@Override
	public BbsReply getReply(int id) {
		return replyDao.unique(id);
	}
	

	@Override
//	@Cacheable("TOPIC")
	public PageQuery getTopics(PageQuery query) {
		
		topicDao.queryTopic(query);
		return query;
	}
	@Override
//	@Cacheable("MY-MESSAGE")
	public List<BbsTopic> getMyTopics(int userId){
		return topicDao.queryMyMessageTopic(userId);
	}
	@Override
//	@Cacheable("MY-MESSAGE-COUNT")
	public Integer getMyTopicsCount(int userId){
		return topicDao.queryMyMessageTopicCount(userId);
	}
	
	@Override
	public void updateMyTopic(int msgId,int status){
		BbsMessage msg = new BbsMessage();
		msg.setStatus(status);
		msg.setId(msgId);
		sql.updateTemplateById(msg);
		
	}
	@Override
	public BbsMessage makeOneBbsMessage(int userId,int topicId,int status){
		BbsMessage msg = new BbsMessage();
		msg.setUserId(userId);
		msg.setTopicId(topicId);
		List<BbsMessage> list = sql.template(msg);
		if(list.isEmpty()){
			msg.setStatus(status);
			sql.insert(msg,true);
			return msg;
		}else{
			msg =  list.get(0);
			if(msg.getStatus()!=status){
				msg.setStatus(status);
				sql.updateById(msg);
			}
			return msg;
		}
			
	}
	
	@Override
//	@CacheEvict(cacheNames={"MY-MESSAGE","MY-MESSAGE-COUNT"}, allEntries=true)
	public void notifyParticipant(int topicId,int ownerId){
		List<Integer> userIds = topicDao.getParticipantUserId(topicId);
		for(Integer userId:userIds){
			if(userId==ownerId){
				continue;
			}
			//TODO,以后改成批处理,但存在insert&update问题
			makeOneBbsMessage(userId,topicId,1);
		}
	}

	@Override
	public void getHotTopics(PageQuery query) {
		Map paras = new HashMap();
		paras.put("type", "hot");
		query.setParas(paras);
		topicDao.queryTopic(query);
	}

	@Override
	public void getNiceTopics(PageQuery query) {
		Map paras = new HashMap();
		paras.put("type", "nice");
		query.setParas(paras);
		topicDao.queryTopic(query);
//		fillTopic(query);
	}

	@Override
	public void getPosts(PageQuery query) {
//		postDao.getPosts(query, topicId);
		postDao.getPosts(query);
		if(query.getList() != null){
			for (Object topicObj : query.getList()) {
				final BbsPost post = (BbsPost) topicObj;
				List<BbsReply> replys = replyDao.allReply(post.getId());
				post.setReplys (replys);
				
			}
		}
	}

	@Override
	public void saveUser(BbsUser user) {
		userDao.insert(user);
	}

	@Override
	public BbsUser login(BbsUser user) {
		List<BbsUser> users = sql.template(user);
		if(CollectionUtils.isEmpty(users)){
			return null;
		}
		return users.get(0);
	}

	@Override
//	@CacheEvict(cacheNames="TOPIC", allEntries=true)
	public void saveTopic(BbsTopic topic, BbsPost post, BbsUser user) {
		topic.setUserId(user.getId());
		topic.setCreateTime(new Date());
		topicDao.insert(topic, true);
		post.setUserId(user.getId());
		post.setTopicId(topic.getId());
		post.setCreateTime(new Date());
		postDao.insert(post,true);
		gitUserService.addTopicScore(user.getId());
	}

	@Override
//	@CacheEvict(cacheNames="TOPIC", allEntries=true)
	public void savePost(BbsPost post, BbsUser user) {
		post.setUserId(user.getId());
		postDao.insert(post,true);
		gitUserService.addPostScore(user.getId());
	}

	

	@Override
	public void saveReply(BbsReply reply) {
		replyDao.insert(reply,true);
		gitUserService.addReplayScore(reply.getUserId());
	}

	@Override
//	@CacheEvict(cacheNames="TOPIC", allEntries=true)
	public void deleteTopic(int id) {
		sql.deleteById(BbsTopic.class, id);
		postDao.deleteByTopicId(id);
		replyDao.deleteByTopicId(id);
	}

	@Override
//	@CacheEvict(cacheNames="TOPIC", allEntries=true)
	public void deletePost(int id) {
		sql.deleteById(BbsPost.class, id);
		replyDao.deleteByPostId(id);
	}

	@Override
	public Date getLatestPost(int userId) {
		return postDao.getLatestPostDate(userId);
	}

//	@CacheEvict(cacheNames="TOPIC", allEntries=true)
	public void updateTopic(BbsTopic topic){
		sql.updateById(topic);
	}

	@Override
	public BbsPost getFirstPost(Integer topicId) {
		Query<BbsPost> query = sql.query(BbsPost.class);
		List<BbsPost> list = query.andEq("topic_id", topicId)
		    .orderBy("create_time asc").select();
		if(list != null && list.size() > 0) {
			return list.get(0);
		}
		return null;
	}





}
