/*
#################################################################
    Function: add course to quarter plan view
#################################################################
 */
function fnAddPlanItem (atpId, type, planItemId, courseCode, courseTitle, courseCredits) {
    var item = '\
		<span class="fieldLine boxLayoutVerticalItem clearfix">\
    		<div class="uif-collectionItem" id="' + planItemId + '_div">\
				<span class="fieldLine boxLayoutVerticalItem clearfix">\
					<span class="uif-field uif-link"><a id="' + planItemId + '_' + type + '" href="#" target="" title="' + courseTitle + '" class="uif-field uif-link">' + courseCode + ' (' + courseCredits + ')</a></span>\
					<input name="script" type="hidden" value="jq(\'#\' + \'' + planItemId + '_' + type + '\').click(function(e) { openPopUp(\'' + planItemId + '\',\'add_planned_course\',\'startAddPlannedCourseForm\',\'plan\',{viewId:\'PlannedCourseMenuItem-FormView\',planItemId:\'' + planItemId + '\'},e,\'.uif-collectionItem\',{width:\'150px\'},{tail:{align:\'top\'},align:\'top\',position:\'right\'},false); });">\
				</span>\
			</div>\
		</span>\
	';
    jq(item).prependTo("." + atpId + ".myplan-term-" + type + " .uif-stackedCollectionLayout > span").children(".uif-collectionItem").css({backgroundColor:"#ffffcc"}).hide().fadeIn(250).animate({backgroundColor:"#ffffff"}, 1500, function() {
        runHiddenScripts(planItemId + "_div");
    });

}
/*
#################################################################
    Function: remove course from quarter plan view
#################################################################
 */
function fnRemovePlanItem (atpId, type, planItemId) {
    jq("#" + planItemId).unbind('click');
    jq("." + atpId + ".myplan-term-" + type + " .uif-stackedCollectionLayout .uif-collectionItem #" + planItemId + "_" + type).parents(".uif-collectionItem").parent("span").fadeOut(250, function(){
        jq(this).remove();
    });
}
/*
#################################################################
    Function: add course to saved courses list
#################################################################
 */
function fnAddSavedItem (planItemId, courseId, courseCode, courseTitle, courseCredits) {
    var item = '\
		<li>\
	        <div class="uif-group uif-boxGroup uif-verticalBoxGroup uif-boxSection">\
                <div>\
                    <div id="' + planItemId + '_div" class="uif-boxLayout uif-verticalBoxLayout fieldLine clearfix">\
                        <span class="fieldLine boxLayoutVerticalItem clearfix">\
                            <span id="' + planItemId + '_span" class="uif-field uif-fieldGroup uif-horizontalFieldGroup">\
                            <div class="uif-group uif-boxGroup uif-verticalBoxGroup">\
                                <div>\
                                    <div style="float:left;" class="uif-boxLayout uif-verticalBoxLayout fieldLine clearfix">\
                                        <span class="boxLayoutHorizontalItem ">\
                                            <span class="uif-field uif-messageField fl-text-bold">' + courseCode + '</span>\
                                        </span>\
                                        <span class="boxLayoutHorizontalItem ">\
                                            <span class="uif-field uif-link myplan-text-ellipsis">\
                                                <a id="' + planItemId + '_saved" href="#" target="" title="' + courseTitle + '" class="uif-field uif-link myplan-text-ellipsis">' + courseTitle + '</a>\
                                            </span>\
                                            <input name="script" type="hidden" value="jq(\'#\' +\'124_line0\').click(function(e) { openPlanItemPopUp(\'' + courseId + '\',\'add_remove_course_popover_page\',{courseId:\'' + courseId + '\'},e,\'li\',{tail:{align:\'top\'},align:\'top\'}); });">\
                                        </span>\
                                        <span class="boxLayoutHorizontalItem ">\
                                            <span class="uif-field uif-messageField">(' + courseCredits + ')</span>\
                                        </span>\
                                    </div>\
                                </div>\
                            </div>\
                            </span>\
                            <input name="script" type="hidden" value="jq(document).ready(function(){truncateField(\'' + planItemId + '\');});">\
                        </span>\
                    </div>\
                </div>\
            </div>\
        </li>\
    ';
    jq(item).prependTo(".myplan-saved-courses-summary ul").css({backgroundColor:"#ffffcc"}).hide().fadeIn(250).animate({backgroundColor:"#ffffff"}, 1500, function() {
        runHiddenScripts(planItemId + "_div");
    });

}
/*
#################################################################
    Function: remove course from saved courses list
#################################################################
 */
function fnRemoveSavedItem (planItemId, cssStyle) {
    jq("." + cssStyle + " #" + planItemId + "_span").parents("li").fadeOut(250, function(){
        jq(this).remove();
    });
}
/*
#################################################################
    Function: update the count of saved courses
#################################################################
 */
function fnUpdateSavedCount (savedItemCount) {
    jq(".myplan-saved-courses-detail .uif-headerField.uif-sectionHeaderField .uif-header strong").fadeOut(250, function() {
	    jq(this).html(savedItemCount - 1).fadeIn(250);
	});
}
/*
#################################################################
    Function: update the credits total in the quarter plan view
#################################################################
 */
function fnUpdateCredits (atpId, termCredits) {
    jq("." + atpId + ".myplan-term-planned .myplan-carousel-term-total span.myplan-carousel-term-credits").fadeOut(250, function() {
        jq(this).html(termCredits).fadeIn(250);
    });
}
/*
#################################################################
    Function: swap action button with feedback message
#################################################################
 */
function fnDisplayMessage (message, cssClass, targetId, buttons) {
    if (buttons) {
        jq("#" + targetId).parent().parent("div").fadeOut(250, function() {
            jq(this).addClass(cssClass).html(message).fadeIn(250);
        });
    } else {
        jq("#" + targetId).parent().fadeOut(250, function() {
            jq(this).addClass(cssClass).html(message).fadeIn(250);
        });
    }
}
/*
#################################################################
    Function: restore add button for saving courses in search results
#################################################################
 */
function fnRestoreSearchAddButton (courseId) {
    var oTable = jq('#course_search_results_datatable').dataTable();
    var oNodes = oTable.fnGetNodes();
    jq(oNodes).find("#" + courseId + "_save_span").removeClass().fadeOut(250, function() {
        if ( jq.trim( jq(this).text() ) === 'In List') {
            var sOriginalScript = jq(this).parent().find("input[type='hidden']").attr("value");
            var sAppendScript = "jq('#' + '" + courseId + "_save').click(function(e){e.preventDefault();myPlanAjaxPlanItemMove('" + courseId + "', 'courseId', 'addSavedCourse', e);});";
            jq(this).parent().find("input[type='hidden']").attr("name","script").attr("value", sOriginalScript + " " + sAppendScript);
        }
        jq(this).html("<input type=\"image\" id=\"" + courseId + "_save\" src=\"/student/ks-myplan/images/btnAdd.png\" alt=\"Save to Your Courses List\" class=\"uif-field uif-imageField\" />");
        runHiddenScripts();
        jq(this).fadeIn(250);
    });
}
/*
#################################################################
    Function: restore add button for saving courses on course details
#################################################################
 */
function fnRestoreDetailsAddButton (courseId) {
    jq("#" + courseId + "_div").parent().fadeOut(250, function() {
        jq("#" + courseId + "_div").replaceWith("<button id=\"" + courseId + "_addSavedCourse\" class=\"uif-field uif-action myplan-button myplan-button-gray\" onClick=\"event.preventDefault(); myPlanAjaxPlanItemMove('" + courseId + "', 'courseId', 'addSavedCourse', event);\">Save to Your Courses List</button>");
        jq("#" + courseId + "_addSavedCourse").after('<input name="script" type="hidden" value="jq(document).ready(function() {jq(\'#' + courseId + '_addSavedCourse\').subscribe(\'PLAN_ITEM_ADDED\', function(data){ if (data.planItemType === \'wishlist\') { fnDisplayMessage(data.message, data.cssClass, \'' + courseId + '_addSavedCourse\', false); } });});" />');
        runHiddenScripts();
        jq(this).fadeIn(250);
    });
}