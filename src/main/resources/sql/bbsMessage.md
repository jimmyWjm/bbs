sample
===
* 注释

	select #use("cols")# from bbs_message where #use("condition")#

cols
===

	id,user_id,topic_id,status

updateSample
===

	`id`=#id#,`user_id`=#userId#,`topic_id`=#topicId#,`status`=#status#

condition
===

	1 = 1  
	@if(!isEmpty(userId)){
	 and `user_id`=#userId#
	@}
	@if(!isEmpty(topicId)){
	 and `topic_id`=#topicId#
	@}
	@if(!isEmpty(status)){
	 and `status`=#status#
	@}
	
