package com.ibeetl.bbs.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.beetl.sql.core.SQLManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.ibeetl.bbs.common.WebUtils;
import com.ibeetl.bbs.model.BbsUser;
import com.ibeetl.bbs.service.BbsUserService;
import com.ibeetl.bbs.util.HashKit;


@Controller
public class LoginController {
	
	@Autowired
	SQLManager sql;

	@Autowired
	BbsUserService bbsUserService;
	
	
//	@RequestMapping("/user/registerOk.html")
//	public ModelAndView  sync(String key,String userName,String password,HttpServletRequest request,HttpServletResponse response){
//		password = HashKit.md5(password);
//		BbsUser user = bbsUserService.setUserAccount(userName, password);
//		WebUtils.loginUser(request, response, user, true);
//		ModelAndView view = new ModelAndView("redirect:/");
//		UserData.removeKey(key);
//		return view;
//	}
	
	
	@RequestMapping("/user/login.html")
	public ModelAndView  login(String userName,String password,HttpServletRequest request,HttpServletResponse response){
		password = HashKit.md5(password);
		BbsUser user = bbsUserService.getUserAccount(userName, password);
		if(user==null){
			ModelAndView view = new ModelAndView("/user/login.html");
			view.addObject("error","用户不存在");
			return view ;
		}else{
		
			WebUtils.loginUser(request, response, user, true);
			ModelAndView view = new ModelAndView("redirect:/");
			return view;
		}
		
	}
	
	@RequestMapping("/user/loginPage.html")
	public ModelAndView  loginPage(HttpServletRequest request){
		ModelAndView view = new ModelAndView("/user/login.html");
		return view ;
	}
	
	@RequestMapping("/user/logout.html")
	public ModelAndView  logout(HttpServletRequest request,HttpServletResponse response){
		
		WebUtils.logoutUser(response);
		ModelAndView view = new ModelAndView("redirect:/");
		return view;
	}
	
	
	
	
	
}
