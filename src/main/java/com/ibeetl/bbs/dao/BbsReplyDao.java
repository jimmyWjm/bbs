package com.ibeetl.bbs.dao;

import org.beetl.sql.core.annotatoin.SqlStatement;
import org.beetl.sql.core.annotatoin.SqlStatementType;
import org.beetl.sql.core.engine.PageQuery;
import org.beetl.sql.core.mapper.BaseMapper;

import com.ibeetl.bbs.model.BbsPost;
import com.ibeetl.bbs.model.BbsReply;

public interface BbsReplyDao extends BaseMapper<BbsReply> {
//    @SqlStatement(params="query,postId")
	@SqlStatement(type=SqlStatementType.SELECT)
    void page(PageQuery query);
    @SqlStatement(params="topicId")
    void deleteByTopicId(int topicId);
    @SqlStatement(params="postId")
    void deleteByPostId(int postId);
}
