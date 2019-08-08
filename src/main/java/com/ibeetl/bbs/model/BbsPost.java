package com.ibeetl.bbs.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import org.beetl.sql.core.TailBean;

import java.util.Date;
import java.util.List;

/*
 *
 * gen by beetlsql 2016-06-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BbsPost extends TailBean {

    Integer id;
    Integer hasReply;
    Integer topicId;
    Integer userId;
    String  content;
    Date    createTime;
    Date    updateTime;

    Integer pros     = 0;//顶次数
    Integer cons     = 0;//踩次数
    Integer isAccept = 0;//0：未采纳，1：采纳


    List<BbsReply> replys;

}
