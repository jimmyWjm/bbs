package com.ibeetl.bbs.util;

import javax.servlet.http.HttpServletRequest;

import org.abego.treelayout.internal.util.java.lang.string.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.beetl.core.Context;
import org.beetl.core.Function;
import org.jsoup.select.Evaluator.IsEmpty;
/**
 * 获取翻页的url尾部的参数，比如:
 * url/1.html?keyword="123"
 * ?keyword="123"
 * 
 */
public class PageUrlParameterFunction implements Function {

	@Override
	public String call(Object[] paras, Context ctx) {
			HttpServletRequest  req = (HttpServletRequest)ctx.getGlobal("request");
			String url = req.getRequestURI();
			String parameter = req.getParameter("keyword");
			String para = "";
			if(!StringUtils.isEmpty(parameter)){
				para = "?keyword="+parameter;
			}
			return para;
	}

}
