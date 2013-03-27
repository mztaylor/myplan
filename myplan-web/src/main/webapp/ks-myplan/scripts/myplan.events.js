/*
 #################################################################
 Function: add course to quarter plan view
 #################################################################
 */
function fnAddPlanItem(atpId, type, planItemId, courseCode, courseTitle, courseCredits, showAlert, termName, timeScheduleOpen, sections) {
    var item = '<div id="' + planItemId + '_div" class="uif-group uif-boxGroup uif-verticalBoxGroup uif-collectionItem uif-boxCollectionItem">' +
        '<div class="uif-boxLayout uif-verticalBoxLayout clearfix">' +
        '<div id="' + planItemId + '_' + type + '" class="uif-field uif-fieldGroup uif-horizontalFieldGroup myplan-course-valid' + ((timeScheduleOpen == "true") ? ' schedule' : '') + '" title="' + courseTitle + '" data-planitemid="' + planItemId + '" data-atpid="' + atpId.replace(/-/g, ".") + '">' +
        '<fieldset>' +
        '<div class="uif-group uif-boxGroup uif-horizontalBoxGroup">' +
        '<div class="uif-boxLayout uif-horizontalBoxLayout clearfix">';
    if (timeScheduleOpen == "true") {
        item += '<div class="' + ((showAlert == "true") ? 'not-scheduled' : 'scheduled') + ' schedule-status uif-imageField uif-boxLayoutHorizontalItem uif-boxLayoutHorizontalItem" title="' + courseCode + ' is' + ((showAlert == "true") ? ' not' : '') + ' scheduled for ' + termName + '">' +
            '<img src="/student/ks-myplan/images/pixel.gif" alt="' + courseCode + ' is' + ((showAlert == "true") ? ' not' : '') + ' scheduled for ' + termName + '" class="uif-image">' +
            '</div>';
    }
    var sectionsDiv = '';
    if(sections){
      sectionsDiv = '<div class="uif-field uif-messageField sections uif-boxLayoutHorizontalItem"> <span class="uif-message">'+
          sections +'</span> </div>';
    }
    item += '<div class="uif-field uif-messageField code uif-boxLayoutHorizontalItem uif-boxLayoutHorizontalItem">' +
        '<span class="uif-message">' + courseCode + '</span>' +
        '</div>' + sectionsDiv +
        '<div class="uif-field uif-messageField credit uif-boxLayoutHorizontalItem uif-boxLayoutHorizontalItem">' +
        '<span class="uif-message">(' + courseCredits + ')</span>' +
        '</div>' +
        '</div>' +
        '</div>' +
        '</fieldset>' +
        '</div>' +
        '<input name="script" type="hidden" value="jQuery(\'#\' + \'' + planItemId + '_' + type + '\').click(function(e) { openMenu(\'' + planItemId + '\', \'' + type + '_menu_items\',null,e,\'.uif-collectionItem\',\'fl-container-150 uif-boxLayoutHorizontalItem\',{tail:{align:\'top\'},align:\'top\',position:\'right\'},false); });">' +
        '</div>' +
        '</div>';
    var size = parseFloat(jQuery("." + atpId + ".myplan-term-" + type).attr("data-size")) + 1;
    jQuery("." + atpId + ".myplan-term-" + type).attr("data-size", size);
    fnShowHideQuickAddLink(atpId, type, size);

    jQuery(item).prependTo("." + atpId + ".myplan-term-" + type + " .uif-stackedCollectionLayout").css({backgroundColor:"#ffffcc"}).hide();
    runHiddenScripts(planItemId + "_div");
    jQuery("#" + planItemId + "_div").fadeIn(250).animate({backgroundColor:"#ffffff"}, 1500);
}
/*
 #################################################################
 Function: remove course from quarter plan view
 #################################################################
 */
function fnRemovePlanItem(atpId, type, planItemId) {
    jQuery("#" + planItemId).unbind('click');
    var size = parseFloat(jQuery("." + atpId + ".myplan-term-" + type).attr("data-size")) - 1;
    jQuery("." + atpId + ".myplan-term-" + type).attr("data-size", size);
    fnShowHideQuickAddLink(atpId, type, size);

    jQuery("." + atpId + ".myplan-term-" + type + " .uif-stackedCollectionLayout .uif-collectionItem #" + planItemId + "_" + type).parents(".uif-collectionItem").fadeOut(250, function () {
        jQuery(this).remove();
    });
}
/*
 #################################################################
 Function: remove course from saved courses list
 #################################################################
 */
function fnRemoveSavedItem(planItemId, cssStyle) {
    jQuery("." + cssStyle + " #" + planItemId).parents("li").fadeOut(250, function () {
        jQuery(this).remove();
    });
}
/*
 #################################################################
 Function: update the count of saved courses
 #################################################################
 */
function fnUpdateSavedCount(savedItemCount) {
    jQuery(".myplan-saved-courses-detail .uif-sectionHeader .uif-headerText strong").fadeOut(250, function () {
        jQuery(this).html(savedItemCount - 1).fadeIn(250);
    });
}
/*
 #################################################################
 Function: update the credits total in the quarter plan view
 #################################################################
 */
function fnUpdateCredits(atpId, termCredits) {
    jQuery("." + atpId + ".myplan-term-planned .myplan-carousel-term-total .credits span.uif-message").fadeOut(250, function () {
        jQuery(this).html(termCredits).fadeIn(250);
    });
}
/*
 #################################################################
 Function: swap action button with feedback message
 #################################################################
 */
function fnDisplayMessage(message, cssClass, targetId, button, full, sameBlock, newId) {
    if (button) {
        if (!sameBlock) {
            if (!full) jQuery("#" + targetId).wrap('<div id="' + newId + '" style="float:left;" />');
            jQuery("#" + targetId).parent("div").fadeOut(250, function () {
                jQuery(this).addClass(cssClass).html(message).fadeIn(250);
            });
        } else {
            jQuery("#" + targetId).fadeOut(250, function () {
                jQuery(this).addClass(cssClass).html('<div id="' + newId + '" style="float:left;">' + message + '</div>').fadeIn(250);
            });
        }
    } else {
        jQuery("#" + targetId).fadeOut(250, function () {
            jQuery(this).addClass(cssClass).html(message).fadeIn(250);
        });
    }
}
/*
 #################################################################
 Function: restore add button for saving courses in search results
 #################################################################
 */
function fnRestoreSearchAddButton(courseId) {
    var oTable = jQuery('.myplan-course-search-results-datatable.uif-dataTable').dataTable();
    var oNodes = oTable.fnGetNodes();
    jQuery(oNodes).find("#" + courseId + "_status").fadeOut(250, function () {
        jQuery(this).removeClass().html('<input type="image" title="Bookmark or Add to Plan" src="/student/ks-myplan/images/pixel.gif" alt="Bookmark or Add to Plan" class="uif-field uif-imageField myplan-add" data-courseid="' + courseId + '" onclick="openMenu(\'' + courseId + '_add\',\'add_course_items\',null,event,null,\'myplan-container-75\',{tail:{align:\'middle\'},align:\'middle\',position:\'right\'},false);" />');
        jQuery(this).fadeIn(250);
    });
}
/*
 #################################################################
 Function: restore add button for saving courses on course details
 #################################################################
 */
function fnRestoreDetailsAddButton(courseId) {
    jQuery("#" + courseId + "_bookmarked").wrap("<div></div>");
    jQuery("#" + courseId + "_bookmarked").parent("div").fadeOut(250, function () {
        jQuery(this).html('<button id="' + courseId + '_addSavedCourse" class="uif-action uif-secondaryActionButton uif-boxLayoutHorizontalItem" onclick="myPlanAjaxPlanItemMove(\'' + courseId + '\', \'courseId\', \'addSavedCourse\', event);">Bookmark Course</button>');
        jQuery(this).siblings("input[data-role='script']").removeAttr("script").attr("name", "script").val("jQuery(document).ready(function () {jQuery('#" + courseId + "_addSavedCourse').subscribe('PLAN_ITEM_ADDED', function (data) {if (data.planItemType === 'wishlist') {fnDisplayMessage(data.message, data.cssClass, data.courseDetails.courseId + '_addSavedCourse', true, false,false);}});});");
        runHiddenScripts();
        jQuery(this).fadeIn(250);
    });
}

/*
 #################################################################
 Function: show or hide the quick add link
 #################################################################
 */
function fnShowHideQuickAddLink(atpId, type, size) {
    if (size < 8) {
        jQuery("." + atpId + ".myplan-term-" + type + " .uif-stackedCollectionLayout .quick-add-cell").fadeIn(250);
    } else {
        jQuery("." + atpId + ".myplan-term-" + type + " .uif-stackedCollectionLayout .quick-add-cell").fadeOut(250);
    }
}

function fnToggleSectionAction(actionId, regId, action, data, primaryPlan) {
    var planItemId = data.planItemId;
    if (primaryPlan) {
        planItemId = data.PrimaryPlanItemId;
    }
    var script;
    var component = jQuery("#" + actionId);
    component.unbind('click');
    switch (action) {
        case "added":
            component.removeClass('myplan-add').addClass('myplan-delete').attr("data-planned", "true").data("planned", true).parent("td").removeClass('myplan-add').addClass('myplan-delete');
            script = "jQuery('#' + '" + actionId + "').click(function(e) { myplanAjaxSubmitSectionItem('" + planItemId + "', 'removeItem', 'plan', {planItemId:'" + planItemId + "',sectionCode:'" + component.data("coursesection") + "',atpId:'" + data.atpId.replace(/-/g, '.') + "',instituteCode:'" + data.InstituteCode + "',registrationCode:'" + regId + "',primary:" + component.data("primary") + ",viewId:'PlannedCourse-FormView'}, e); }); ";
            break;
        case "deleted":
            component.removeClass('myplan-delete').addClass('myplan-add').attr("data-planned", "false").data("planned", false).parent("td").removeClass('myplan-delete').addClass('myplan-add');
            script = "jQuery('#' + '" + actionId + "').click(function(e) { myplanAjaxSubmitSectionItem('" + data.courseDetails.courseId + "', 'addPlannedCourse', 'plan', {courseId:'" + data.courseDetails.courseId + "',sectionCode:'" + component.data("coursesection") + "',atpId:'" + data.atpId.replace(/-/g, '.') + "',instituteCode:'" + data.InstituteCode + "',registrationCode:'" + regId + "',primary:" + component.data("primary") + ",viewId:'PlannedCourse-FormView'}, e);}); ";
            break;
    }
    script += "jQuery('#' + '" + actionId + "').mouseover(function(){ buildHoverText(jQuery(this));}); ";
    updateHiddenScript(actionId, script);
}

function fnRemoveSectionRow(actionId) {
    var row = jQuery("#" + actionId).parents("tr");
    row.fadeOut(250, function () {
        jQuery(this).remove();
    });
}
