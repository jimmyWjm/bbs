package com.ibeetl.bbs.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.engine.PageQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.ibeetl.bbs.dao.BbsModuleDao;
import com.ibeetl.bbs.dao.BbsPostDao;
import com.ibeetl.bbs.dao.BbsReplyDao;
import com.ibeetl.bbs.dao.BbsTopicDao;
import com.ibeetl.bbs.dao.BbsUserDao;
import com.ibeetl.bbs.model.BbsPost;
import com.ibeetl.bbs.model.BbsReply;
import com.ibeetl.bbs.model.BbsTopic;
import com.ibeetl.bbs.model.BbsUser;
import com.ibeetl.bbs.service.BBSService;
import com.ibeetl.bbs.service.BbsUserService;

@Service
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
	public BbsTopic getTopic(int id) {
		return topicDao.unique(id);
	}
	
	

	@Override
	public void getTopics(PageQuery query) {
		
		topicDao.queryTopic(query);
	}

	@Override
	public void getHotTopics(PageQuery query) {
		Map paras = new HashMap();
		paras.put("type", "hot");
		query.setParas(paras);
		topicDao.queryTopic(query);
//		fillTopic(query);
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
	public void saveTopic(BbsTopic topic, BbsPost post, BbsUser user) {
		topic.setUserId(user.getId());
		topic.setCreateTime(new Date());
		topicDao.insert(topic, true);
		post.setUserId(user.getId());
		post.setTopicId(topic.getId());
		post.setCreateTime(new Date());
		postDao.insert(post);
		gitUserService.addTopicScore(user.getId());
	}

	@Override
	public void savePost(BbsPost post, BbsUser user) {
		post.setUserId(user.getId());
		postDao.insert(post);
		gitUserService.addPostScore(user.getId());
	}

	

	@Override
	public void saveReply(BbsReply reply) {
		replyDao.insert(reply,true);
		gitUserService.addReplayScore(reply.getUserId());
	}

	@Override
	public void deleteTopic(int id) {
		sql.deleteById(BbsTopic.class, id);
		postDao.deleteByTopicId(id);
		replyDao.deleteByTopicId(id);
	}

	@Override
	public void deletePost(int id) {
		sql.deleteById(BbsPost.class, id);
		replyDao.deleteByPostId(id);
	}

	@Override
	public Date getLatestPost(int userId) {
		return postDao.getLatestPostDate(userId);
	}

	

}
