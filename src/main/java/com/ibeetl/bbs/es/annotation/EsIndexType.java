package com.ibeetl.bbs.es.annotation;

import java.lang.annotation.*;

/**
 * 创建索引的注解
 * @author yangkebiao
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Repeatable(EsIndexs.class)
public @interface EsIndexType {
	
	EsEntityType entityType();					//实体类型
	EsOperateType operateType();			//操作类型
	String key() default "id";					//获取主键的名称
	
}
