$(function(){

});
function saveReply(v){
    if($(v).parent().parent().children('input[type=text]').val().trim() == ''){
        alert('内容不能为空');
        return false;
    }
    $.post(
        $(v).attr("data"),
        $(v).parent().parent().parent().parent().parent().serialize(),
        function(data){
            if(data == "error"){
                alert("评论不能为空，且不超过200字 :(");
            }else{
                var input = $(v).closest(".mail-body").html();
                $(v).closest(".replay").html(data+input);
               
            }
        },
        "html"
    );
    return false;
}
function replyPaginate(v){
    $.post(
        $(v).attr("data"),
        function(data){
            $(v).closest(".-reply-list").html(data).hide().fadeIn(2000);
        },
        "html"
    );
}
function replyThisUser(v){
    var footer = $(v).closest(".-post-footer").find("textarea").focus().val("回复 @" + $(v).attr("data") + " ");
}
function login(v){
    var passwordInput = $(v).closest("form").find("input[name='password']");
    passwordInput.val(md5(passwordInput.val()));
    return true;
}