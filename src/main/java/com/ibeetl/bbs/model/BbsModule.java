package com.ibeetl.bbs.model;
import java.math.*;
import java.util.Arrays;
import java.util.Date;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

/*
* 
* gen by beetlsql 2016-06-13
*/
public class BbsModule  {
	private Integer id ;
	private Integer turn ;
	private String detail ;
	private String name ;
	private Integer readonly;
	private String adminList;

	//只允许特定用法发帖的，通常用于新闻，广告
	private Set<String> adminSet;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getTurn() {
		return turn;
	}

	public void setTurn(Integer turn) {
		this.turn = turn;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getReadonly() {
		return readonly;
	}

	public void setReadonly(Integer readonly) {
		this.readonly = readonly;
	}

	public String getAdminList() {
		return adminList;
	}

	public void setAdminList(String adminList) {
		this.adminList = adminList;
		if(adminList!=null&&adminList.trim().length()!=0){
			this.adminSet = new HashSet(Arrays.asList(adminList.split(",")));
		}


	}

	public boolean  contains(String userName){
		return adminSet.contains(userName);
	}
}
