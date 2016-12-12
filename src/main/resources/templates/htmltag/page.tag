@ if(query.totalPage!=1){ //query 是通过标签传入的参数，对应类PageQuery

@ var pageUrl=has(url)?url:pagePatternUrl();

<div class="btn-group">
	@if(query.pageNumber>1){
	<a href="${ctxPath}${pageUrl}${query.pageNumber-1}.html" class="btn btn-white" type="button">上一页</a>
	@}
	
	@if(query.pageNumber!=query.totalPage){
	<a href="${ctxPath}${pageUrl}${query.pageNumber+1}.html" class="btn btn-white" type="button">下一页</a>
	@}
</div>
@}