package com.ibeetl.bbs.util.lucene.entity;


public class SearchResult implements Comparable<SearchResult>{
	
	private String topicId;
	private String topContent;
	private String postId;
	private String postContent;
	private float score;//相似度
	public String getTopicId() {
		return topicId;
	}
	public void setTopicId(String topicId) {
		this.topicId = topicId;
	}
	public String getTopContent() {
		return topContent;
	}
	public void setTopContent(String topContent) {
		this.topContent = topContent;
	}
	public String getPostId() {
		return postId;
	}
	public void setPostid(String postId) {
		this.postId = postId;
	}
	public String getPostContent() {
		return postContent;
	}
	public void setPostContent(String postContent) {
		this.postContent = postContent;
	}
	
	public float getScore() {
		return score;
	}
	public void setScore(float score) {
		this.score = score;
	}
	
	public SearchResult(String topicId, String topContent, String postId, String postContent, float score) {
		super();
		this.topicId = topicId;
		this.topContent = topContent;
		this.postId = postId;
		this.postContent = postContent;
		this.score = score;
	}
	public SearchResult() {
		super();
	}
	@Override
	public int compareTo(SearchResult o) {
		if(this.score < o.getScore()){
			return 1;
		}else if(this.score > o.getScore()){
			return -1;
		}
		return 0;
	}
	
	
	
}
