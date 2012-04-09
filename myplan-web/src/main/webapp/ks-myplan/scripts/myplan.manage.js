function fnBuildTitle(aView, termSelector, headerSelector) {
    var sText = 'Academic Year';
    var sFirst = jq.trim ( jq(aView[0]).find("." + termSelector).text() );
    var sLast = jq.trim ( jq(aView[aView.length-1]).find("." + termSelector).text() );
    jq("#" + headerSelector + " .uif-header").html(sText + ' ' + sFirst.substr(-4) + '-' + sLast.substr(-4));
}

jq(document).ready(function() {
    jq(".myplan-carousel-list .uif-stackedCollectionLayout").each(function() {
        var slots = 8;
        var count = jq(this).find("span > span.fieldLine.boxLayoutVerticalItem.clearfix").size();
        if (count < slots) {
            for (var i = count; i < slots; i++) {
                jq(this).append('<span class="fieldLine boxLayoutVerticalItem clearfix"><div class="uif-group uif-boxGroup uif-verticalBoxGroup uif-collectionItem uif-boxCollectionItem">&nbsp;</div></span>');
            }
        } /* else if (count > slots) {
            jq(this).find('span:gt(' + (slots - 1) + ')').hide();
        } */
    }); /*
    jq(".quarter .quarter_backup_group").each(function() {
        var slots = 2;
        var count = jq(this).find("span").size();
        if (count <= slots) {
            jq(this).next(".quarter_backup_footer").html("&nbsp;").addClass("disabled");
        } else {
            jq(this).next(".quarter_backup_footer").html("<span>Show</span> " + (count - slots) + " more courses");
        }
        if (count < slots) {
            for (var i = count; i < slots; i++) {
                jq(this).append("<span>&nbsp;</span>");
            }
        }
    }); */
    jq("#planned_courses_detail_list_group").jCarouselLite({
        btnNext: ".myplan-carousel-next",
        btnPrev: ".myplan-carousel-prev",
        scroll: 4,
        afterEnd: function(a) { fnBuildTitle(a, 'uif-groupHeader', 'planned_courses_header_div') },
        initCallback: function(a) { fnBuildTitle(a, 'uif-groupHeader', 'planned_courses_header_div') }
    });
    jq(".quarter_backup_footer").click(function() {
        if (!jq(this).hasClass("disabled")) {
            var oBackup = jq(this).prev(".quarter_backup_group");
            var oQuarter = jq(this).parents("li");
            var iSpeed = 500;
            if (jq(this).hasClass("expanded")) {
                var iAdjust = ( oBackup.height() - 46 ) * -1;
                jq(this).removeClass("expanded");
                jq(this).find("span").html("Show");
            } else {
                var iAdjust = oBackup[0].scrollHeight - oBackup.height();
                jq(this).addClass("expanded");
                jq(this).find("span").html("Hide");
            }
            oBackup.animate({"height": oBackup.height() + iAdjust}, {duration: iSpeed});
            oQuarter.animate({"height": oQuarter.height() + iAdjust}, {duration: iSpeed});
        }
    });
});