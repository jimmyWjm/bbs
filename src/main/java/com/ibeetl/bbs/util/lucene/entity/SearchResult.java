package com.ibeetl.bbs.util.lucene.entity;


public class SearchResult implements Comparable<SearchResult>{
	
	private String topicId;
	private String content;
	private float score;//相似度
	public String getTopicId() {
		return topicId;
	}
	public void setTopicId(String topicId) {
		this.topicId = topicId;
	}
	
	public float getScore() {
		return score;
	}
	public void setScore(float score) {
		this.score = score;
	}

	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public SearchResult() {
		super();
	}
	
	public SearchResult(String topicId, String content, float score) {
		super();
		this.topicId = topicId;
		this.content = content;
		this.score = score;
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