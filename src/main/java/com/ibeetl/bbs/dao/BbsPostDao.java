package com.ibeetl.bbs.dao;

import com.ibeetl.bbs.model.BbsPost;
import org.beetl.sql.core.annotatoin.Sql;
import org.beetl.sql.core.annotatoin.SqlStatement;
import org.beetl.sql.core.annotatoin.SqlStatementType;
import org.beetl.sql.core.engine.PageQuery;
import org.beetl.sql.core.mapper.BaseMapper;

import java.util.Date;

public interface BbsPostDao extends BaseMapper<BbsPost> {
    @SqlStatement(type = SqlStatementType.SELECT)
    void getPosts(PageQuery query);

    @SqlStatement(params = "topicId")
    void deleteByTopicId(int topicId);

    @Sql(value = "select max(create_time) from bbs_post where user_id=? order by id desc ", returnType = Date.class)
    Date getLatestPostDate(int userId);

}
