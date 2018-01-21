package com.ibeetl.bbs.es.config;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.ibeetl.bbs.es.annotation.EsIndexType;
import com.ibeetl.bbs.es.annotation.EsIndexs;
import com.ibeetl.bbs.es.service.EsService;

@Configuration
@Aspect
public class AOPConfig {
	
	@Autowired
	private EsService esService;
	
	@Pointcut("@annotation(com.ibeetl.bbs.es.annotation.EsIndexType) || @annotation(com.ibeetl.bbs.es.annotation.EsIndexs)")  
	private void anyMethod(){}//定义ES的切入点  
	
	@Around("anyMethod()")
	public Object simpleAop(ProceedingJoinPoint pjp) throws Throwable{
		try {
	        Signature sig = pjp.getSignature();
	        MethodSignature msig = (MethodSignature) sig;//代理方法
	        Object target = pjp.getTarget();//代理类
	        Method method = target.getClass().getMethod(msig.getName(), msig.getParameterTypes());
	        EsIndexType esIndexType = method.getAnnotation(EsIndexType.class);
	        EsIndexs esIndexs = method.getAnnotation(EsIndexs.class);
	        
	        Object o = pjp.proceed();//调用原方法
	        
	        List<EsIndexType> types = new ArrayList<>();
	        if(esIndexs != null){
	        	types.addAll(Arrays.asList(esIndexs.value()));
	        }
	        if(esIndexType != null){
	        	types.add(esIndexType);
	        }
	        if(types.size() > 0){
	        	for (EsIndexType index : types) {
	        		Integer id = null;
					String key = index.key();
					
			       Map<String, Object> parameterNames = this.getParameterNames(pjp);
		        	id = (Integer)parameterNames.get(key);
	        		if(id == null) {
	        			if(o instanceof ModelAndView) {
	        				ModelAndView modelAndView = (ModelAndView)o;
	    	    			id = (Integer)modelAndView.getModel().get(key);
	        			}else if(o instanceof JSONObject) {
	        				JSONObject json = (JSONObject)o;
	        				id = json.getInteger(key);
	        			}
	    			}
	        		if(id == null) {
	        			throw new RuntimeException(target.getClass().getName()+"$"+msig.getName()+"：未获取到主键，无法更新索引");
	        		}
		        	esService.editEsIndex(index.entityType(), index.operateType(), id);
				}
	        }
	        
			return o;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 获取方法的参数
	 * @param pjp
	 * @return
	 * @throws Exception
	 */
	private Map<String, Object> getParameterNames(ProceedingJoinPoint pjp) throws Exception {
		
		String[] names = null;//参数名
		Object[] args = pjp.getArgs();//参数值
		
		Signature signature = pjp.getSignature();  
        MethodSignature methodSignature = (MethodSignature) signature;  
		
		String jv = System.getProperty("java.version");
		String[] jvs = jv.split("\\.");
		if(Integer.parseInt(jvs[0]+jvs[1]) >= 18) {//jdk8直接获取参数名
	        names = methodSignature.getParameterNames(); 
		}else {
			LocalVariableTableParameterNameDiscoverer localVariableTableParameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
	        Object target = pjp.getTarget();//代理类
	        Method currentMethod = target.getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
	        names = localVariableTableParameterNameDiscoverer.getParameterNames(currentMethod);
		}
		if(names == null) {
			throw new RuntimeException(pjp.getTarget().getClass().getName()+"$"+signature.getName()+"：未获取到参数名称列表");
		}
		if(names.length != args.length ) {
			throw new RuntimeException(pjp.getTarget().getClass().getName()+"$"+signature.getName()+"：参数名称列表长度与参数值列表长度不相等");
		}
		Map<String, Object> map = new HashMap<>();
		for (int i = 0; i < names.length; i++) {
			map.put(names[i], args[i]);
		}
		
		return map;
	}
	

}
