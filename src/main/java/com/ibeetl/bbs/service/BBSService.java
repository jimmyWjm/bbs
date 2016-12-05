package com.ibeetl.bbs.service;

import org.beetl.sql.core.engine.PageQuery;

import com.ibeetl.bbs.model.BbsPost;
import com.ibeetl.bbs.model.BbsReply;
import com.ibeetl.bbs.model.BbsTopic;
import com.ibeetl.bbs.model.BbsUser;

public interface BBSService {
	BbsTopic getTopic(int id);

	void getTopics(PageQuery query);

	void getHotTopics(PageQuery query);

	void getNiceTopics(PageQuery query);

	void getPosts(PageQuery query);

	void saveUser(BbsUser user);

	BbsUser login(BbsUser user);

	void saveTopic(BbsTopic topic, BbsPost post, BbsUser user);

	void savePost(BbsPost post, BbsUser user);

	void getReplys(PageQuery query);

	void saveReply(BbsReply reply);

	void deleteTopic(int id);

	void deletePost(int id);
}
