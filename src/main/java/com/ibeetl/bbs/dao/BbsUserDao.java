package com.ibeetl.bbs.dao;

import java.util.List;

import org.beetl.sql.core.annotatoin.Sql;
import org.beetl.sql.core.annotatoin.SqlStatement;
import org.beetl.sql.core.mapper.BaseMapper;

import com.ibeetl.bbs.model.BbsUser;


public interface BbsUserDao extends BaseMapper<BbsUser> {
	
		
		List<BbsUser> getScoreTop(Integer max);
		
		List<BbsUser> getLevelTop(Integer max);
		@Sql("select count(1) from bbs_user where ip=? "
				+ "and DATE_FORMAT(register_time,'%Y-%m-%d') = ?")
		public int getIpCount(String ip,String date);
}
