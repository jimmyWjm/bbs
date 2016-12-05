//用户退出
function logout() {
    $.getJSON(getCtxPath() + "/admin/ajax/logout", function(data) {
        setTimeout(function() {
            location.reload(true);
        }, 500);
    });
}