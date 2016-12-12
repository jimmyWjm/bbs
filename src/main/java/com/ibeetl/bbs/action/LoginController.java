package com.ibeetl.bbs.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.beetl.sql.core.SQLManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.ibeetl.bbs.common.WebUtils;
import com.ibeetl.bbs.model.BbsUser;
import com.ibeetl.bbs.service.BbsUserService;
import com.ibeetl.bbs.util.HashKit;
import com.ibeetl.bbs.util.VerifyCodeUtils;


@Controller
public class LoginController {
	
	@Autowired
	SQLManager sql;

	@Autowired
	BbsUserService bbsUserService;
	
	static final String CODE_NAME = "verCode";
	
	
	@RequestMapping("/bbs/user/login.html")
	public ModelAndView  login(String userName,String password,HttpServletRequest request,HttpServletResponse response){
		
		password = HashKit.md5(password);
		BbsUser user = bbsUserService.getUserAccount(userName, password);
		if(user==null){
			//TODO
			ModelAndView view = new ModelAndView("/user/login.html");
			view.addObject("error","用户不存在");
			return view ;
		}else{
		
			WebUtils.loginUser(request, response, user, true);
//			String url = (String)request.getSession().getAttribute("lastAccess");
//			if(url==null){
				String url = "forward:/bbs/index/1.html";
//			}
			ModelAndView view = new ModelAndView();
			view.setViewName(url);
			return view;
		}
		
	}
	
	@RequestMapping("/bbs/user/register.html")
	public ModelAndView  loginPage(HttpServletRequest request){
		ModelAndView view = new ModelAndView("/register.html");
		return view ;
	}
	
	@RequestMapping("/bbs/user/logout.html")
	public RedirectView  logout(HttpServletRequest request,HttpServletResponse response){
		
		WebUtils.logoutUser(request,response);
		RedirectView view = new RedirectView("/bbs/index/1.html",true);
		return view;
	}
	
	@RequestMapping(value="/bbs/user/doRegister.html",method=RequestMethod.POST)
	public ModelAndView  register(BbsUser user,String code,HttpServletRequest request,HttpServletResponse response){
		HttpSession session = request.getSession(true); 
		String verCode = (String)session.getAttribute(CODE_NAME);
		if(!verCode.equalsIgnoreCase(code)){
			ModelAndView view = new ModelAndView("/register.html");
			view.addObject("error","验证码输入错误");
			view.addObject("user",user);
			return view;
		}
//		BbsUser db = bbsUserService.getUserAccount(user.getUserName(), user.getPassword());
		if(bbsUserService.hasUser(user.getUserName())){
			ModelAndView view = new ModelAndView("/register.html");
			view.addObject("error","用户已经存在");
			view.addObject("user",user);
			
			return view;
		}
		
		String password = HashKit.md5(user.getPassword());
		user.setPassword(password);
		user.setBalance(10);
		user.setLevel(1);
		user.setScore(10);
		user = bbsUserService.setUserAccount(user);
		WebUtils.loginUser(request, response, user, true);
		
		ModelAndView view = new ModelAndView("forward:/bbs/index/1.html");
		return view;
		
	}
	
	@RequestMapping("/bbs/user/authImage")
	public void authImage(HttpServletRequest request,HttpServletResponse response) throws IOException{
		response.setHeader("Pragma", "No-cache"); 
        response.setHeader("Cache-Control", "no-cache"); 
        response.setDateHeader("Expires", 0); 
        response.setContentType("image/jpeg"); 
           
        //生成随机字串 
        String verifyCode = VerifyCodeUtils.generateVerifyCode(4); 
        //存入会话session 
        HttpSession session = request.getSession(true); 
        //删除以前的
        session.removeAttribute(CODE_NAME);
        session.setAttribute(CODE_NAME, verifyCode.toLowerCase()); 
        //生成图片 
        int w = 100, h = 30; 
        VerifyCodeUtils.outputImage(w, h, response.getOutputStream(), verifyCode); 
	
	}
	
	
}
