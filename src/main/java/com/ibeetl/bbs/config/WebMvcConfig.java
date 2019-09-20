package com.ibeetl.bbs.config;

import com.ibeetl.bbs.common.WebUtils;
import com.ibeetl.bbs.model.BbsUser;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

@Configuration
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebMvcConfig implements WebMvcConfigurer {

    WebUtils webUtils;

    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptorAdapter() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                                     Object handler) throws Exception {

                String requestURI = request.getServletPath();
                if (webUtils.currentUser() == null) {
                    //未登陆用户，记录访问地址，登陆后可以直接跳转到此页面
                    if (!requestURI.contains("/bbs/user/login")) {
                        request.getSession(true).setAttribute("lastAccess", requestURI);
                    }
                }
                if (requestURI.contains("/bbs/admin/") || requestURI.contains("/bbs/topic/add")) {
                    BbsUser user = webUtils.currentUser();
                    if (user == null) {
                        response.sendRedirect(request.getContextPath() + "/user/loginPage");
                        return false;
                    }
                }
                return true;
            }
        }).addPathPatterns("/bbs/**");
    }

    /**
     * 跨域访问
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://ibeetl.com", "http://www.ibeetl.com")
                .allowedMethods("*");

    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //外部静态资源的处理（用于图片上传后的URL映射）
        registry.addResourceHandler("/bbs/showPic/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + File.separator + "upload" + File.separator);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        //无逻辑处理的
        registry.addViewController("/bbs/share").setViewName("forward:/bbs/topic/module/1");
        registry.addViewController("/bbs/topic/add").setViewName("/post.html");
    }


    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        //开启允许URL添加后缀也能访问到相应的Controller，以便兼容旧版本
        configurer.setUseSuffixPatternMatch(Boolean.TRUE);
    }
}
