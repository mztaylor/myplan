/*
#################################################################
    Function: add course to quarter plan view
#################################################################
 */
function fnAddPlanItem (atpId, type, planItemId, courseCode, courseTitle, courseCredits) {
    var item = '<div id="' + planItemId + '_div" title="' + courseTitle + '" class="uif-group uif-boxGroup uif-verticalBoxGroup uif-collectionItem uif-boxCollectionItem">' +
                    '<div class="uif-boxLayout uif-verticalBoxLayout clearfix">' +
                        '<div id="' + planItemId + '_' + type + '" class="uif-field uif-fieldGroup uif-horizontalFieldGroup myplan-course-valid">' +
                            '<fieldset>' +
                                '<div class="uif-group uif-boxGroup uif-horizontalBoxGroup">' +
                                    '<div class="uif-boxLayout uif-horizontalBoxLayout clearfix">' +
                                        '<div class="uif-field uif-messageField uif-boxLayoutHorizontalItem uif-boxLayoutHorizontalItem">' +
                                            '<span class="uif-message">' + courseCode + ' (' + courseCredits + ')</span>' +
                                        '</div>' +
                                    '</div>' +
                                '</div>' +
                            '</fieldset>' +
                        '</div>' +
                        '<input name="script" type="hidden" value="jQuery(\'#\' + \'' + planItemId + '_' + type + '\').click(function(e) { openPopUp(\'' + planItemId + '\',\'add_planned_course\',\'startAddPlannedCourseForm\',\'plan\',{viewId:\'PlannedCourseMenuItem-FormView\',dateAdded:\'2012-06-04 10:34:05.21\',planItemId:\'' + planItemId + '\'},e,\'.uif-collectionItem\',{width:\'150px\'},{tail:{align:\'top\'},align:\'top\',position:\'right\'},false); });">' +
                    '</div>' +
                '</div>';

    jq(item).prependTo("." + atpId + ".myplan-term-" + type + " .uif-stackedCollectionLayout").css({backgroundColor:"#ffffcc"}).hide().fadeIn(250).animate({backgroundColor:"#ffffff"}, 1500, function() {
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
    jq("." + atpId + ".myplan-term-" + type + " .uif-stackedCollectionLayout .uif-collectionItem #" + planItemId + "_" + type).parents(".uif-collectionItem").fadeOut(250, function(){
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
    jq("." + atpId + ".myplan-term-planned .myplan-carousel-term-total .myplan-carousel-term-credits span.uif-message").not(".uif-requiredMessage").fadeOut(250, function() {
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