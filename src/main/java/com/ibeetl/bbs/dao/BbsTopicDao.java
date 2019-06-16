package com.ibeetl.bbs.dao;

import java.util.Date;
import java.util.List;

import org.beetl.sql.core.annotatoin.Sql;
import org.beetl.sql.core.annotatoin.SqlStatement;
import org.beetl.sql.core.engine.PageQuery;
import org.beetl.sql.core.mapper.BaseMapper;

import com.ibeetl.bbs.model.BbsTopic;

public interface BbsTopicDao extends BaseMapper<BbsTopic> {
	void queryTopic(PageQuery query);
	List<BbsTopic> queryMyMessageTopic(int userId);
	
	Integer queryMyMessageTopicCount(int userId);
	
	List<Integer> getParticipantUserId(Integer topicId);
	
	BbsTopic getTopic(Integer topicId);

	@Sql("SELECT count(1) FROM bbs_topic where user_id =? and create_time>? ")
	int getTopicCount(Integer userId, Date before);
	
}
