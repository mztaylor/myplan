function retrieveContent(id, getId, url) {
    jq("#" + id + "_group").load(url + " #" + getId + "_group", null, function (response, status, xhr) {
        if (status === 'success') {
            jq("#" + id + "_group").html( jq(this).html() );
            jq("body").unblock();
        } else {
        	jq("#" + id).html( "Sorry but there was an error: " + xhr.status + " " + xhr.statusText);
        }
    });
}

function truncateField(id) {
    jq("#" + id + "_group ul li").each(function() {
        var margin = parseInt(jq(this).find("span.boxLayoutHorizontalItem span").css("margin-right"), 10)
        var fixed = 0;
        var fields = jq(this).find("span.boxLayoutHorizontalItem span").not(".ellipsis").length;
        jq(this).find("span.boxLayoutHorizontalItem span").not(".ellipsis").each(function() {
        	fixed = fixed + jq(this).width();
        });
        var ellipsis = jq(this).width() - ( fixed + ( margin * fields ) );
        jq(this).find("span.boxLayoutHorizontalItem span").last().css("margin-right", 0);
        jq(this).find("span.boxLayoutHorizontalItem span.ellipsis").width(ellipsis);
    });
}