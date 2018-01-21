package com.ibeetl.bbs.es.entity;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import com.ibeetl.bbs.util.HashKit;

@Document(indexName="bbs",type="content")
public class BbsIndex implements Serializable{

	private static final long serialVersionUID = 7588021529563246352L;
	
	@Id
	private String id;
	private Integer topicId;
	private Integer postId;
	private Integer replayId;
	private Integer userId;
	private String content;
	private Date createTime;
	
	public String getId() {
		if(this.id == null) {
			StringBuilder key = new StringBuilder();
			key.append(topicId != null?topicId.toString():"").append(":");
			key.append(postId != null?postId.toString():"").append(":");
			key.append(replayId != null?replayId.toString():"").append(":");
			return HashKit.md5(key.toString());
		}
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getTopicId() {
		return topicId;
	}

	public void setTopicId(Integer topicId) {
		this.topicId = topicId;
	}

	public Integer getPostId() {
		return postId;
	}

	public void setPostId(Integer postId) {
		this.postId = postId;
	}

	public Integer getReplayId() {
		return replayId;
	}

	public void setReplayId(Integer replayId) {
		this.replayId = replayId;
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

	public BbsIndex() {
		super();
	}

	public BbsIndex(Integer topicId, Integer postId, Integer replayId, Integer userId,
			String content, Date createTime) {
		super();
		this.topicId = topicId;
		this.postId = postId;
		this.replayId = replayId;
		this.userId = userId;
		this.content = content;
		this.createTime = createTime;
	}

	
	
}
