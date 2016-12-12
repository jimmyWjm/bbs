queryTopic
===

	
	select  
	@pageTag(){
	t.*,m.name module_name,u.user_name user_name
	@}  
	FROM  bbs_topic t left join bbs_module m
	on t.module_id = m.id  left join bbs_user u on t.user_id=u.id
    where true
     @var type = type!"normal";
     @if(!isEmpty(moduleId)){
     	 and `module_id`=#moduleId#
     @}
     @if("normal" == type){
         order by is_up desc,create_time desc
     @}
     @if("hot" == type){
          order by pv desc
     @}
     @if("nice" == type){
          and is_nice=true order by create_time desc
     @}

queryHotTopic
===
    select  
    @pageTag(){
    *
    @}
    from bbs_topic order by pv desc

queryNiceTopic
===
    select 
    @pageTag(){
    *
    @}
    from bbs_topic where is_nice=true order by create_time desc

getTopicAndPostCount
===

* 根据id查找topic和拥有的post数量

	select t.*,(select count(1) from bbs_post where topic_id=#id#) post_count from bbs_topic t where t.id =#id#

sample
===
* 注释

	select #use("cols")# from bbs_topic where #use("condition")#

cols
===

	id,user_id,module_id,post_count,reply_count,pv,content,emotion,create_time,is_nice,is_up

updateSample
===

	`id`=#id#,`user_id`=#userId#,`module_id`=#moduleId#,`post_count`=#postCount#,`reply_count`=#replyCount#,`pv`=#pv#,`content`=#content#,`emotion`=#emotion#,`create_time`=#createTime#,`is_nice`=#isNice#,`is_up`=#isUp#

condition
===

	1 = 1  
	@if(!isEmpty(userId)){
	 and `user_id`=#userId#
	@}
	@if(!isEmpty(moduleId)){
	 and `module_id`=#moduleId#
	@}
	@if(!isEmpty(postCount)){
	 and `post_count`=#postCount#
	@}
	@if(!isEmpty(replyCount)){
	 and `reply_count`=#replyCount#
	@}
	@if(!isEmpty(pv)){
	 and `pv`=#pv#
	@}
	@if(!isEmpty(content)){
	 and `content`=#content#
	@}
	@if(!isEmpty(emotion)){
	 and `emotion`=#emotion#
	@}
	@if(!isEmpty(createTime)){
	 and `create_time`=#createTime#
	@}
	@if(!isEmpty(isNice)){
	 and `is_nice`=#isNice#
	@}
	@if(!isEmpty(isUp)){
	 and `is_up`=#isUp#
	@}
	
