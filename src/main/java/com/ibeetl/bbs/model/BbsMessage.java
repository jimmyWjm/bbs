package com.ibeetl.bbs.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/*
 *
 * gen by beetlsql 2016-12-27
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BbsMessage {
    Integer id;
    Integer status;
    Integer topicId;
    Integer userId;

}
