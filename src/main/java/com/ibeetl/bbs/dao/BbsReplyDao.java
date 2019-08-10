package com.ibeetl.bbs.dao;

import com.ibeetl.bbs.model.BbsReply;
import org.beetl.sql.core.annotatoin.SqlStatement;
import org.beetl.sql.core.mapper.BaseMapper;

import java.util.List;

public interface BbsReplyDao extends BaseMapper<BbsReply> {
    @SqlStatement(params = "postId")
    List<BbsReply> allReply(Integer postId);

    @SqlStatement(params = "topicId")
    void deleteByTopicId(int topicId);

    @SqlStatement(params = "postId")
    void deleteByPostId(int postId);
}
