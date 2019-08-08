package com.ibeetl.bbs.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import org.beetl.sql.core.TailBean;

import java.io.Serializable;
import java.util.Date;

/*
 *
 * gen by beetlsql 2016-06-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BbsReply extends TailBean implements Serializable {
    private static final long serialVersionUID = 4351960407287422152L;

    Integer  id;
    Integer  postId;
    Integer  topicId;
    Integer  userId;
    String   content;
    Date     createTime;
    BbsUser  user;
    BbsTopic topic;
}
