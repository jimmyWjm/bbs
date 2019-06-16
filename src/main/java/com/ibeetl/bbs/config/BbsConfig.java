package com.ibeetl.bbs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 每天用户能做的操作个数，如果大于配置个数，需要验证码
 */
@ConfigurationProperties(prefix = "bbs.user", ignoreUnknownFields = true)
@Configuration
public class BbsConfig {


  Integer registerSameIp = 2 ;
  Integer topicCount=2;
  Integer topicCountMinutes = 1;
  Integer postCount=2;

  public Integer getRegisterSameIp() {
    return registerSameIp;
  }

  public void setRegisterSameIp(Integer registerSameIp) {
    this.registerSameIp = registerSameIp;
  }

  public Integer getTopicCount() {
    return topicCount;
  }

  public void setTopicCount(Integer topicCount) {
    this.topicCount = topicCount;
  }

  public Integer getPostCount() {
    return postCount;
  }

  public void setPostCount(Integer postCount) {
    this.postCount = postCount;
  }

  public Integer getTopicCountMinutes() {
    return topicCountMinutes;
  }

  public void setTopicCountMinutes(Integer topicCountMinutes) {
    this.topicCountMinutes = topicCountMinutes;
  }
}
