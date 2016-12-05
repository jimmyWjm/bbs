package com.ibeetl.bbs.dao;

import org.beetl.sql.core.annotatoin.SqlStatement;
import org.beetl.sql.core.annotatoin.SqlStatementType;
import org.beetl.sql.core.engine.PageQuery;
import org.beetl.sql.core.mapper.BaseMapper;

import com.ibeetl.bbs.model.BbsPost;

public interface BbsPostDao extends BaseMapper<BbsPost> {
	@SqlStatement(type=SqlStatementType.SELECT)
    void getPosts(PageQuery query);
    @SqlStatement(params="topicId")
    void deleteByTopicId(int topicId);
}
