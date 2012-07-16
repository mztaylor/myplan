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

    jQuery(item).prependTo("." + atpId + ".myplan-term-" + type + " .uif-stackedCollectionLayout").css({backgroundColor:"#ffffcc"}).hide().fadeIn(250).animate({backgroundColor:"#ffffff"}, 1500, function() {
        runHiddenScripts(planItemId + "_div");
    });
}
/*
#################################################################
    Function: remove course from quarter plan view
#################################################################
 */
function fnRemovePlanItem (atpId, type, planItemId) {
    jQuery("#" + planItemId).unbind('click');
    jQuery("." + atpId + ".myplan-term-" + type + " .uif-stackedCollectionLayout .uif-collectionItem #" + planItemId + "_" + type).parents(".uif-collectionItem").fadeOut(250, function(){
        jQuery(this).remove();
    });
}
/*
#################################################################
    Function: remove course from saved courses list
#################################################################
 */
function fnRemoveSavedItem (planItemId, cssStyle) {
    jQuery("." + cssStyle + " #" + planItemId).parents("li").fadeOut(250, function(){
        jQuery(this).remove();
    });
}
/*
#################################################################
    Function: update the count of saved courses
#################################################################
 */
function fnUpdateSavedCount (savedItemCount) {
    jQuery(".myplan-saved-courses-detail .uif-sectionHeader .uif-headerText strong").fadeOut(250, function() {
	    jQuery(this).html(savedItemCount - 1).fadeIn(250);
	});
}
/*
#################################################################
    Function: update the credits total in the quarter plan view
#################################################################
 */
function fnUpdateCredits (atpId, termCredits) {
    jQuery("." + atpId + ".myplan-term-planned .myplan-carousel-term-total .myplan-carousel-term-credits span.uif-message").not(".uif-requiredMessage").fadeOut(250, function() {
        jQuery(this).html(termCredits).fadeIn(250);
    });
}
/*
#################################################################
    Function: swap action button with feedback message
#################################################################
 */
function fnDisplayMessage (message, cssClass, targetId, button, full) {
    if (button) {
        if (!full) jQuery("#" + targetId).wrap("<div></div>");
        jQuery("#" + targetId).parent("div").fadeOut(250, function() {
            jQuery(this).addClass(cssClass).html(message).fadeIn(250);
        });
    } else {
        jQuery("#" + targetId).fadeOut(250, function() {
            jQuery(this).addClass(cssClass).html(message).fadeIn(250);
        });
    }
}
/*
#################################################################
    Function: restore add button for saving courses in search results
#################################################################
 */
function fnRestoreSearchAddButton (courseId) {
    var oTable = jQuery('.myplan-course-search-results-datatable.uif-dataTable').dataTable();
    var oNodes = oTable.fnGetNodes();
    jQuery(oNodes).find("#" + courseId + "_status").fadeOut(250, function() {
        jQuery(this).html('<input type="image" title="Bookmark This Course" src="/student/ks-myplan/images/btnAdd.png" alt="Bookmark This Course" class="uif-field uif-imageField" onclick="myPlanAjaxPlanItemMove(\''+courseId+'\', \'courseId\', \'addSavedCourse\', event);" />');
        jQuery(this).fadeIn(250);
    });
}
/*
#################################################################
    Function: restore add button for saving courses on course details
#################################################################
 */
function fnRestoreDetailsAddButton (courseId) {
    jQuery("#" + courseId + "_bookmarked").wrap("<div></div>");
    jQuery("#" + courseId + "_bookmarked").parent("div").fadeOut(250, function() {
        jQuery(this).replaceWith('<button id="'+ courseId +'_addSavedCourse" class="uif-action uif-primaryActionButton myplan-button myplan-button-gray uif-boxLayoutHorizontalItem onclick="myPlanAjaxPlanItemMove(\''+ courseId +'\', \'courseId\', \'addSavedCourse\', event);">Bookmark Course</button>');
        jQuery(this).append("jQuery(document).ready(function () {jQuery('#"+ courseId +"_addSavedCourse').subscribe('PLAN_ITEM_ADDED', function (data) {if (data.planItemType === 'wishlist') {fnDisplayMessage(data.message, data.cssClass, data.courseDetails.courseId + '_addSavedCourse', true, false);}});});");
        runHiddenScripts();
        jQuery(this).fadeIn(250);
    });
}