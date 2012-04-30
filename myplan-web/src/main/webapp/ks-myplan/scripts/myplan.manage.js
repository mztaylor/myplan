function fnBuildTitle(aView, termSelector, headerSelector) {
    var sText = 'Academic Year';
    var sFirst = jq.trim ( jq(aView[0]).find("." + termSelector).text() );
    var sLast = jq.trim ( jq(aView[aView.length-1]).find("." + termSelector).text() );
    jq("#" + headerSelector + " .myplan-plan-header").html(sText + ' ' + sFirst.substr(-4) + '-' + sLast.substr(-4));
}

function fnExpandBackup(e) {
    stopEvent(e);
    var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    if (!jq(target).hasClass("disabled")) {
        var oBackup = jq(target).parents(".myplan-term-backup").find(".uif-stackedCollectionLayout");
        var oQuarter = jq(target).parents("li");
        var iSpeed = 500;
        var iDefault = 26;
        if (jq(target).hasClass("expanded")) {
            var iAdjust = ( oBackup.height() - ( iDefault * 2 ) ) * -1;
            jq(target).removeClass("expanded");
            jq(target).find("span").html("Show");
        } else {
            var iAdjust = ( oBackup.find("span a").size() * iDefault ) - oBackup.height();
            jq(target).addClass("expanded");
            jq(target).find("span").html("Hide");
        }
        oBackup.animate({"height": oBackup.height() + iAdjust}, {duration: iSpeed});
        oQuarter.animate({"height": oQuarter.height() + iAdjust}, {duration: iSpeed});
    }
}
jq(document).ready(function() {
    /*
    jq(".myplan-carousel-list .uif-stackedCollectionLayout").each(function() {
        var slots = 8;
        var count = jq(this).find("span > span.fieldLine.boxLayoutVerticalItem.clearfix").size();
        if (count < slots) {
            for (var i = count; i < slots; i++) {
                jq(this).append('<span class="fieldLine boxLayoutVerticalItem clearfix"><div class="uif-group uif-boxGroup uif-verticalBoxGroup uif-collectionItem uif-boxCollectionItem">&nbsp;</div></span>');
            }
        } else if (count > slots) {
            jq(this).find('span:gt(' + (slots - 1) + ')').hide();
        }
    }); */ /*
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
    if ( jq("#planned_courses_detail_list_group ul:not(.errorLines) li").length > 0 ) {
        jq("#planned_courses_detail_list_group").jCarouselLite({
            btnNext: ".myplan-carousel-next",
            btnPrev: ".myplan-carousel-prev",
            scroll: 4,
            afterEnd: function(a) { fnBuildTitle(a, 'uif-groupHeader', 'planned_courses_detail_group'); },
            initCallback: function(a) { fnBuildTitle(a, 'uif-groupHeader', 'planned_courses_detail_group'); }
        });
    }
});