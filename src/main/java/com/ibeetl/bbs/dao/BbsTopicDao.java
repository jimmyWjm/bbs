package com.ibeetl.bbs.dao;

import org.beetl.sql.core.annotatoin.SqlStatement;
import org.beetl.sql.core.annotatoin.SqlStatementType;
import org.beetl.sql.core.engine.PageQuery;
import org.beetl.sql.core.mapper.BaseMapper;

import com.ibeetl.bbs.model.BbsTopic;

public interface BbsTopicDao extends BaseMapper<BbsTopic> {
	void queryTopic(PageQuery query);
	
}
