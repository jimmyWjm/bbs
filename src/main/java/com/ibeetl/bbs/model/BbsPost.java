package com.ibeetl.bbs.model;
import org.beetl.sql.core.engine.PageQuery;

import java.math.*;
import java.util.Date;
import java.sql.Timestamp;

/*
* 
* gen by beetlsql 2016-06-13
*/
public class BbsPost  {
	private Integer id ;
	private Integer hasReply ;
	private Integer topicId ;
	private Integer userId ;
	private String content ;
	private Date createTime ;
	private Date updateTime ;
	private PageQuery replyPage;
	private BbsUser user;
	private BbsTopic topic;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getHasReply() {
		return hasReply;
	}

	public void setHasReply(Integer hasReply) {
		this.hasReply = hasReply;
	}

	public Integer getTopicId() {
		return topicId;
	}

	public void setTopicId(Integer topicId) {
		this.topicId = topicId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public PageQuery getReplyPage() {
		return replyPage;
	}

	public void setReplyPage(PageQuery replyPage) {
		this.replyPage = replyPage;
	}

	public BbsUser getUser() {
		return user;
	}

	public void setUser(BbsUser user) {
		this.user = user;
	}

	public BbsTopic getTopic() {
		return topic;
	}

	public void setTopic(BbsTopic topic) {
		this.topic = topic;
	}
}
