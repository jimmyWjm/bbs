package com.ibeetl.bbs.es.config;

import com.alibaba.fastjson.JSONObject;
import com.ibeetl.bbs.es.annotation.EsFallback;
import com.ibeetl.bbs.es.annotation.EsIndexType;
import com.ibeetl.bbs.es.annotation.EsOperateType;
import com.ibeetl.bbs.es.entity.BbsIndex;
import com.ibeetl.bbs.es.service.EsService;
import com.ibeetl.bbs.es.vo.EsIndexTypeData;
import com.ibeetl.bbs.util.EsUtil;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.web.servlet.ModelAndView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@Aspect
public class AOPConfig {

    @Autowired
    private EsService esService;

    private Logger logger = LoggerFactory.getLogger(AOPConfig.class);

    /**
     * ES的切入点
     */
    @Pointcut("@annotation(com.ibeetl.bbs.es.annotation.EsIndexType) || @annotation(com.ibeetl.bbs.es.annotation.EsIndexs)")
    private void anyMethod() {
    }

    @Around("anyMethod()")
    public Object simpleAop(ProceedingJoinPoint pjp) throws Throwable {
        try {
            Signature     sig    = pjp.getSignature();
            Object        target = pjp.getTarget();//代理类
            Method        method = ((MethodSignature) sig).getMethod();//代理方法
            EsIndexType[] types  = method.getAnnotationsByType(EsIndexType.class);

            Map<String, Object> parameters = this.getParameterNames(pjp);

            //获取索引的数据集合
            List<EsIndexTypeData> typeDatas = new ArrayList<>();

            //当操作为删除时，需要提前获取id
            for (EsIndexType index : types) {
                if (index.operateType() == EsOperateType.DELETE) {
                    String key = index.key();

                    Integer id = (Integer) parameters.get(key);
                    if (id == null) {
                        logger.error(target.getClass().getName() + "$" + method.getName() + "：未获取到主键，无法更新索引");
                    } else {
                        BbsIndex        bbsIndex = esService.createBbsIndex(index.entityType(), id);
                        String          md5Id    = EsUtil.getEsKey(bbsIndex.getTopicId(), bbsIndex.getPostId(), bbsIndex.getReplyId());
                        EsIndexTypeData data     = new EsIndexTypeData(index.entityType(), index.operateType(), md5Id);
                        typeDatas.add(data);
                    }
                }
            }
            //调用原方法
            Object o = pjp.proceed();
            //当操作为更新时，可以从返回值中获取id
            for (EsIndexType index : types) {
                if (index.operateType() != EsOperateType.DELETE) {
                    Integer id  = null;
                    String  key = index.key();
                    id = (Integer) parameters.get(key);
                    boolean resultErr = false;
                    if (id == null) {
                        if (o instanceof ModelAndView) {
                            ModelAndView modelAndView = (ModelAndView) o;
                            id = (Integer) modelAndView.getModel().get(key);
                        } else if (o instanceof JSONObject) {
                            JSONObject json = (JSONObject) o;
                            id = json.getInteger(key);
                            resultErr = 1 == json.getInteger("err");
                        }
                    }
                    if (id == null) {
                        if (!resultErr) {
                            logger.error(target.getClass().getName() + "$" + method.getName() + "：未获取到主键，无法更新索引");
                        }

                    } else {
                        EsIndexTypeData data = new EsIndexTypeData(index.entityType(), index.operateType(), id);
                        typeDatas.add(data);
                    }

                }
            }

            //更新索引
            for (EsIndexTypeData esIndexTypeData : typeDatas) {
                esService.editEsIndex(esIndexTypeData.getEntityType(), esIndexTypeData.getOperateType(), esIndexTypeData.getId());
            }

            return o;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 调用ES失败降级的处理（如ES服务挂了）
     */
    @Around("@annotation(com.ibeetl.bbs.es.annotation.EsFallback)")
    public Object fallback(ProceedingJoinPoint pjp) {

        Signature sig    = pjp.getSignature();
        Object    target = pjp.getTarget();//代理类
        Method    method = ((MethodSignature) sig).getMethod();//代理方法

        //调用原方法
        try {
            return pjp.proceed();
        } catch (Throwable throwable) {
            EsFallback fallback = method.getAnnotation(EsFallback.class);
            String methodName = fallback.fallbackMethod();
            if (StringUtils.isBlank(methodName)){
                methodName = method.getName()+"Fallback";
            }
            try {
                Method fallbackMethod = target.getClass().getMethod(methodName, method.getParameterTypes());
                if (fallbackMethod.getReturnType() == method.getReturnType()){
                    method.setAccessible(Boolean.TRUE);
                    return fallbackMethod.invoke(target, pjp.getArgs());
                } else {
                    throw new RuntimeException(throwable);
                }
            } catch (NoSuchMethodException e) {
                //找不到Fallback方法时抛出原异常
                throw new RuntimeException(throwable);
            } catch (IllegalAccessException |InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * 获取方法的参数
     *
     * @param pjp
     * @return
     * @throws Exception
     */
    private Map<String, Object> getParameterNames(ProceedingJoinPoint pjp) throws Exception {

        String[] names = null;//参数名
        Object[] args  = pjp.getArgs();//参数值

        Signature       signature       = pjp.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;

        String   jv  = System.getProperty("java.version");
        String[] jvs = jv.split("\\.");
        if (Integer.parseInt(jvs[0] + jvs[1]) >= 18) {//jdk8直接获取参数名
            names = methodSignature.getParameterNames();
        } else {
            LocalVariableTableParameterNameDiscoverer localVariableTableParameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
            Object                                    target                                    = pjp.getTarget();//代理类
            Method                                    currentMethod                             = target.getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
            names = localVariableTableParameterNameDiscoverer.getParameterNames(currentMethod);
        }
        if (names == null) {
            logger.error(pjp.getTarget().getClass().getName() + "$" + signature.getName() + "：未获取到参数名称列表");
            return Collections.emptyMap();
        } else if (names.length != args.length) {
            logger.error(pjp.getTarget().getClass().getName() + "$" + signature.getName() + "：参数名称列表长度与参数值列表长度不相等");
            return Collections.emptyMap();
        } else {
            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < names.length; i++) {
                map.put(names[i], args[i]);
            }
            return map;
        }
    }


}
