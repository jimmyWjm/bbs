-- MySQL dump 10.13  Distrib 5.6.24, for osx10.8 (x86_64)
--
-- Host: 127.0.0.1    Database: bbs
-- ------------------------------------------------------
-- Server version	5.6.26

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `bbs_message`
--

DROP TABLE IF EXISTS `bbs_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bbs_message` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `topic_id` int(11) DEFAULT NULL,
  `status` tinyint(2) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bbs_message`
--

LOCK TABLES `bbs_message` WRITE;
/*!40000 ALTER TABLE `bbs_message` DISABLE KEYS */;
INSERT INTO `bbs_message` VALUES (1,1,59,1),(2,1,73,1),(3,97,71,1),(4,95,78,1),(5,99,79,1),(6,95,75,0);
/*!40000 ALTER TABLE `bbs_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bbs_module`
--

DROP TABLE IF EXISTS `bbs_module`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bbs_module` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) DEFAULT NULL,
  `detail` varchar(100) DEFAULT NULL,
  `turn` tinyint(2) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bbs_module`
--

LOCK TABLES `bbs_module` WRITE;
/*!40000 ALTER TABLE `bbs_module` DISABLE KEYS */;
INSERT INTO `bbs_module` VALUES (1,'课程','',1),(2,'BBS',NULL,2);
/*!40000 ALTER TABLE `bbs_module` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bbs_post`
--

DROP TABLE IF EXISTS `bbs_post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bbs_post` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `topic_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL DEFAULT '0',
  `content` text NOT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `has_reply` bit(1) NOT NULL DEFAULT b'0',
  `update_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `topicID_P` (`topic_id`),
  KEY `userID_P` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=269 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bbs_post`
--

LOCK TABLES `bbs_post` WRITE;
/*!40000 ALTER TABLE `bbs_post` DISABLE KEYS */;
INSERT INTO `bbs_post` VALUES (201,59,1,'发布一','2016-07-13 02:52:31','\0',NULL),(202,61,1,'<p>&nbsp; &nbsp; &nbsp;dsf df&nbsp;</p>','2016-07-13 03:47:17','\0',NULL),(203,61,1,'<p>&nbsp;<img src=\"/codeweb//bbs/showPic/1468381645615blob.png\" _src=\"/codeweb//bbs/showPic/1468381645615blob.png\"/></p>','2016-07-13 03:47:29','\0',NULL),(209,64,1,'<p>&nbsp; &nbsp; &nbsp;sdf sdfs</p>','2016-07-13 05:25:37','\0',NULL),(210,65,1,'<p>&nbsp; &nbsp; &nbsp;sdfdfsdfsdfsdf</p>','2016-07-13 05:27:06','\0',NULL),(211,66,1,'<p>sdfsdfsd</p>','2016-07-13 05:27:28','\0',NULL),(212,66,1,'<p>&nbsp; &nbsp; &nbsp;dsfsdf&nbsp;</p>','2016-07-13 05:27:47','\0',NULL),(213,66,1,'<p><a href=\"http://127.0.0.1:7700/codeweb/bbs/topic/66-1\" target=\"_blank\" title=\"课程\">http://127.0.0.1:7700/codeweb/bbs/topic/66-1&nbsp;</a></p>','2016-07-13 05:47:07','\0',NULL),(214,67,1,'<h2>第三方斯蒂芬放到</h2><p>dfdfdf<br/></p><p><br/></p><p><br/></p>','2016-07-13 05:49:12','\0',NULL),(215,68,1,'<p><img src=\"/codeweb//bbs/showPic/1468389086446blob.png\" _src=\"/codeweb//bbs/showPic/1468389086446blob.png\"/></p>','2016-07-13 05:51:28','\0',NULL),(216,69,4,'<p><img src=\"/codeweb//bbs/showPic/1468391755464blob.png\" _src=\"/codeweb//bbs/showPic/1468391755464blob.png\" style=\"width: 754px; height: 585px;\"/></p><p><br/></p><p><br/></p><p>ok，试试看了多发点 多发点，明天搞</p>','2016-07-13 06:35:57','\0',NULL),(217,69,4,'<p>&nbsp; &nbsp;<img src=\"/codeweb//bbs/showPic/1468391773228blob.png\" _src=\"/codeweb//bbs/showPic/1468391773228blob.png\" style=\"width: 680px; height: 445px;\"/></p>','2016-07-13 06:36:14','\0',NULL),(218,69,4,'<p>什么时候讲？</p>','2016-07-13 06:37:49','\0',NULL),(219,69,4,'<p><img src=\"/codeweb//bbs/showPic/1468392229548blob.png\" _src=\"/codeweb//bbs/showPic/1468392229548blob.png\" style=\"width: 700px; height: 444px;\"/></p>','2016-07-13 06:43:51','\0',NULL),(220,70,95,'打发第三方\r\n','2016-12-06 12:31:04','\0',NULL),(221,70,95,'### **李宗翰**','2016-12-06 12:32:32','\0',NULL),(225,59,95,'发布2','2016-12-10 14:44:52','\0',NULL),(226,59,95,'发布三','2016-12-10 14:44:58','\0',NULL),(227,59,95,'发布四','2016-12-10 14:45:50','\0',NULL),(228,59,95,'发布无','2016-12-10 14:45:56','\0',NULL),(229,59,95,'发布六','2016-12-10 14:46:03','\0',NULL),(230,59,95,'发布六','2016-12-10 14:57:22','\0',NULL),(231,59,95,'dfsdf  sfsdf ','2016-12-10 15:17:02','\0',NULL),(232,59,95,'sdfsf ','2016-12-10 15:17:27','\0',NULL),(233,59,95,'sdfsf ','2016-12-10 15:19:00','\0',NULL),(234,59,95,'dfdsf ','2016-12-10 16:08:16','\0',NULL),(235,59,95,'df ','2016-12-10 16:08:19','\0',NULL),(237,59,95,'dsfsdfs\r\ndsfdsfsd\r\n[sdfsffdf](http://163.com \"sdfsffdf\")\r\nsdfsdf\r\n## dfdfdfdfdf\r\n','2016-12-11 07:49:39','\0',NULL),(241,59,1,'<pre><code class=\"lang-java\"><br></code></pre>','2016-12-26 13:46:13','\0',NULL),(242,59,1,'<p>sdfsdfsdf</p>','2016-12-26 13:46:21','\0',NULL),(243,59,95,'<p>的说法是否</p><pre><code class=\"lang-java\"> public static void main(String[] args){\r\n&nbsp;&nbsp;&nbsp;&nbsp;\r\n&nbsp;&nbsp;}</code></pre>','2016-12-26 14:01:13','\0',NULL),(262,74,95,'<p>测试我的新手</p>','2017-10-26 11:00:45','\0',NULL),(263,74,95,'<p>测试我的新功能</p>','2017-10-26 11:01:19','\0',NULL),(264,75,95,'<p>beetl 是最好的模板语言 ！！</p>','2018-01-24 12:09:15','\0',NULL),(265,76,95,'<p>作为中国国家主席，习近平在繁忙的公务活动中，不仅展示了他的睿智严谨，还留下了许多风趣幽默的言行。Most people have only seen the serious side of Chinese President Xi Jinping. But he also makes time for some lighthearted moments despite his packed daily schedule. Not only is Xi a statesman, he is also a football fan, a world traveler and occasionally a historian.客串文化讲解员 Witty interpreter“你们看，我的祖先很魁梧吧。<br></p><p><br></p><p>作为中国国家主席，习近平在繁忙的公务活动中，不仅展示了他的睿智严谨，还留下了许多风趣幽默的言行。Most people have only seen the serious side of Chinese President Xi Jinping. But he also makes time for some lighthearted moments despite his packed daily schedule. Not only is Xi a statesman, he is also a football fan, a world traveler and occasionally a historian.客串文化讲解员 Witty interpreter“你们看，我的祖先很魁梧吧。<br></p>','2018-01-24 12:14:06','\0',NULL),(266,77,95,'<p>abc沙发上是否sdfsdf&nbsp;</p>','2018-01-24 12:18:29','\0',NULL);
/*!40000 ALTER TABLE `bbs_post` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bbs_reply`
--

DROP TABLE IF EXISTS `bbs_reply`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bbs_reply` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `topic_id` int(11) NOT NULL DEFAULT '1',
  `post_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL DEFAULT '0',
  `content` varchar(300) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `topicID_R` (`topic_id`),
  KEY `postID_R` (`post_id`),
  KEY `userID_R` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bbs_reply`
--

LOCK TABLES `bbs_reply` WRITE;
/*!40000 ALTER TABLE `bbs_reply` DISABLE KEYS */;
INSERT INTO `bbs_reply` VALUES (3,59,201,1,'三东方闪电','2016-07-13 02:52:40'),(4,59,201,1,'辅导费','2016-07-13 02:52:42'),(5,59,201,1,'第三代','2016-07-13 03:09:03'),(6,61,203,1,'dsfds ','2016-07-13 03:47:33'),(7,61,203,1,'df ','2016-07-13 03:47:35'),(12,66,211,1,'fdfdf','2016-07-13 05:27:52'),(13,68,215,1,'好困难','2016-07-13 05:51:38'),(14,69,216,4,'看着不错','2016-07-13 06:37:30'),(15,69,216,4,'精彩','2016-07-13 06:37:34'),(16,69,218,4,'有时间就讲','2016-07-13 06:43:19'),(17,65,210,1,'sfdsf','2016-12-05 11:38:04'),(18,65,210,1,'dfdfd','2016-12-05 11:38:07'),(19,65,210,1,'sdfdsf','2016-12-05 11:38:54'),(20,65,210,1,'dfdf ','2016-12-05 11:38:56'),(21,70,221,95,'你好','2016-12-06 12:32:38'),(25,59,226,1,'sfsdfsdfssfsdf','2016-12-26 13:45:46'),(26,59,227,95,'kansfsfsfsdfsdfdsfdsfdsfdsf','2017-02-06 13:31:14'),(34,77,266,95,'sfsf  开始算算','2018-01-24 12:52:26');
/*!40000 ALTER TABLE `bbs_reply` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bbs_topic`
--

DROP TABLE IF EXISTS `bbs_topic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bbs_topic` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL DEFAULT '0',
  `module_id` int(11) NOT NULL,
  `post_count` int(11) NOT NULL DEFAULT '1',
  `reply_count` int(11) NOT NULL DEFAULT '0',
  `pv` int(11) NOT NULL DEFAULT '0',
  `content` varchar(150) NOT NULL,
  `emotion` tinyint(2) DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_nice` bit(1) NOT NULL DEFAULT b'0',
  `is_up` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  KEY `moduleID_T` (`module_id`),
  KEY `userID_T` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=78 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bbs_topic`
--

LOCK TABLES `bbs_topic` WRITE;
/*!40000 ALTER TABLE `bbs_topic` DISABLE KEYS */;
INSERT INTO `bbs_topic` VALUES (59,1,2,17,0,87,'再发表一次看看那',NULL,'2016-07-12 16:00:00','\0','\0'),(60,1,2,3,0,16,'地方对双方都',NULL,'2016-07-13 03:45:14','\0','\0'),(61,1,2,2,0,4,'dfdf ',NULL,'2016-07-13 03:47:17','\0','\0'),(64,1,2,1,0,3,'sdfsdf',NULL,'2016-07-13 05:25:37','\0','\0'),(65,1,1,1,0,12,'sfsfs',NULL,'2016-07-13 05:27:06','\0','\0'),(66,1,1,3,0,16,'hello go',NULL,'2016-07-13 05:27:28','\0','\0'),(67,1,2,1,0,2,'',NULL,'2016-07-13 05:49:12','\0','\0'),(68,1,1,1,0,11,'关于什么什么的课程卡缴纳困难是发顺丰的',NULL,'2016-07-13 05:51:28','\0','\0'),(69,4,1,4,0,98,'Zookeeper',NULL,'2016-07-13 06:35:57','','\0'),(70,95,2,2,0,6,'打发第三方\r\n',NULL,'2016-12-06 12:31:04','\0','\0'),(74,95,1,2,0,7,'我的新书1',NULL,'2017-10-26 11:00:45','',''),(75,95,1,1,0,4,'发个帖子看看那',NULL,'2018-01-24 12:09:15','\0','\0'),(76,95,1,1,0,2,'没什么看看',NULL,'2018-01-24 12:14:05','\0','\0'),(77,95,1,3,0,21,'都是方式地方',NULL,'2018-01-24 12:18:29','','');
/*!40000 ALTER TABLE `bbs_topic` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bbs_user`
--

DROP TABLE IF EXISTS `bbs_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bbs_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(255) DEFAULT NULL,
  `password` varchar(32) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `score` int(11) DEFAULT '0' COMMENT '积分',
  `level` int(11) DEFAULT '1' COMMENT '积分换算成等级，新生，老生，班主任，教导主任，校长',
  `balance` int(11) DEFAULT '0' COMMENT '积分余额',
  `corp` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bbs_user`
--

LOCK TABLES `bbs_user` WRITE;
/*!40000 ALTER TABLE `bbs_user` DISABLE KEYS */;
INSERT INTO `bbs_user` VALUES (1,'xxx','e10adc3949ba59abbe56e057f20f883e','xxx',54,1,54,NULL),(4,'李家智','e10adc3949ba59abbe56e057f20f883e',NULL,140,2,0,NULL),(5,'赵晴文','e10adc3949ba59abbe56e057f20f883e','zhaoqingwen@coamc.com',1000,5,0,NULL),(6,'石萌','e10adc3949ba59abbe56e057f20f883e','shimeng@coamc.com',12,1,0,NULL),(95,'admin','e10adc3949ba59abbe56e057f20f883e','xxxx@coamc.com',255,3,255,NULL),(96,'lijiazhi','202cb962ac59075b964b07152d234b70','123@123.com',0,1,NULL,'it'),(97,'hank','e10adc3949ba59abbe56e057f20f883e','hank@163.com',22,1,22,'dfdsf'),(98,'test1','e10adc3949ba59abbe56e057f20f883e','123@123.com',0,0,0,'11'),(99,'test11','f696282aa4cd4f614aa995190cf442fe','test1@163.com',29,1,29,'天天公司'),(100,'adb','e10adc3949ba59abbe56e057f20f883e','xxx@126.com',29,1,29,'cc');
/*!40000 ALTER TABLE `bbs_user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-01-24 21:34:59
