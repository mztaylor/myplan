/*
 #################################################################
 Function: add course to quarter plan view
 #################################################################
 */
function fnAddPlanItem(atpId, type, planItemId, courseCode, courseTitle, courseCredits, showAlert, termName, timeScheduleOpen, sections, statusAlert) {
    var item = '<div id="' + planItemId + '_' + type + '_' + atpId + '_div" class="uif-group uif-boxGroup uif-verticalBoxGroup uif-collectionItem uif-boxCollectionItem">' +
        '<div class="uif-boxLayout uif-verticalBoxLayout clearfix">' +
        '<div id="' + planItemId + '_' + type + '_' + atpId + '" class="uif-field uif-fieldGroup uif-horizontalFieldGroup myplan-course-valid ' + ((showAlert == "true") ? 'alert' : '') + '" title="' + courseTitle + '" data-planitemid="' + planItemId + '" data-atpid="' + atpId.replace(/-/g, ".") + '">' +
        '<fieldset>' +
        '<div class="uif-group uif-boxGroup uif-horizontalBoxGroup">' +
        '<div class="uif-boxLayout uif-horizontalBoxLayout clearfix">';
    if (statusAlert.length > 0) {
        item += '<div class="' + ((showAlert == "true") ? 'alert-icon' : '') + ' uif-field uif-imageField uif-boxLayoutHorizontalItem " title="' + statusAlert + '">' +
            '<img src="/student/ks-myplan/images/pixel.gif" alt="' + statusAlert + '" class="uif-image"/>' +
            '</div>';
    }
    var sectionsDiv = '';
    if (sections) {
        sectionsDiv = '<div class="uif-field uif-messageField sections uif-boxLayoutHorizontalItem myplan-text-ellipsis"> <span class="uif-message">' +
            sections + '</span></div>';
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
        '<input name="script" type="hidden" value="jQuery(\'#\' + \'' + planItemId + '_' + type + '_' + atpId + '\').click(function(e) { openMenu(\'' + planItemId + '\', \'' + type + '_menu_items\',null,e,\'.uif-collectionItem\',\'fl-container-150 uif-boxLayoutHorizontalItem\',{tail:{align:\'top\'},align:\'top\',position:\'right\'},false); });"/>' +
        '</div>' +
        '</div>';
    var size = parseFloat(jQuery("." + atpId + ".myplan-term-" + type).attr("data-size")) + 1;
    jQuery("." + atpId + ".myplan-term-" + type).attr("data-size", size);
    fnShowHideQuickAddLink(atpId, type, size);

    jQuery(item).prependTo("." + atpId + ".myplan-term-" + type + " .uif-stackedCollectionLayout").css({backgroundColor:"#ffffcc"}).hide();
    runHiddenScripts(planItemId + "_" + type + "_" + atpId + "_div");

    jQuery("#" + planItemId + "_" + type + "_" + atpId + "_div").show().animate({backgroundColor:"#ffffff"}, 3000);
    truncateField(planItemId + "_" + type + "_" + atpId + "_div", 12, true);
}
/*
 #################################################################
 Function: remove course from quarter plan view
 #################################################################
 */
function fnRemovePlanItem(atpId, type, planItemId) {
    jQuery("#" + planItemId + "_" + type + "_" + atpId).unbind('click');
    var size = parseFloat(jQuery("." + atpId + ".myplan-term-" + type).attr("data-size")) - 1;
    jQuery("." + atpId + ".myplan-term-" + type).attr("data-size", size);
    fnShowHideQuickAddLink(atpId, type, size);

    jQuery("." + atpId + ".myplan-term-" + type + " .uif-stackedCollectionLayout .uif-collectionItem #" + planItemId + "_" + type + "_" + atpId).parents(".uif-collectionItem").fadeOut(250, function () {
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
    jQuery("." + atpId + ".myplan-term-planned .myplan-carousel-term-total .credits.uif-messageField").fadeOut(250, function () {
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
    var row = component.parents('tr.row');
    component.unbind('click');
    switch (action) {
        case "added":
            component.removeClass('myplan-add').addClass('myplan-delete').attr("data-planned", "true").data("planned", true).parent("td").removeClass('myplan-add').addClass('myplan-delete');
            row.addClass('myplan-section-planned').next('tr.collapsible').addClass('myplan-section-planned').next('tr.collapsible').addClass('myplan-section-planned');
            script = "jQuery('#' + '" + actionId + "').click(function(e) { myplanAjaxSubmitSectionItem('" + planItemId + "', 'removeItem', 'plan', {planItemId:'" + planItemId + "',sectionCode:'" + component.data("coursesection") + "',atpId:'" + data.atpId.replace(/-/g, '.') + "',instituteCode:'" + data.InstituteCode + "',registrationCode:'" + regId + "',primary:" + component.data("primary") + ",viewId:'PlannedCourse-FormView'}, e); }); ";
            break;
        case "deleted":
            component.removeClass('myplan-delete').addClass('myplan-add').attr("data-planned", "false").data("planned", false).parent("td").removeClass('myplan-delete').addClass('myplan-add');
            row.removeClass('myplan-section-planned').next('tr.collapsible').removeClass('myplan-section-planned').next('tr.collapsible').removeClass('myplan-section-planned');
            script = "jQuery('#' + '" + actionId + "').click(function(e) { myplanAjaxSubmitSectionItem('" + data.courseDetails.courseId + "', 'addPlannedCourse', 'plan', {courseId:'" + data.courseDetails.courseId + "',sectionCode:'" + component.data("coursesection") + "',atpId:'" + data.atpId.replace(/-/g, '.') + "',instituteCode:'" + data.InstituteCode + "',registrationCode:'" + regId + "',primary:" + component.data("primary") + ",viewId:'PlannedCourse-FormView'}, e);}); ";
            if (jQuery("#" + data.courseDetails.courseId + "_toggle").data("hidden")) {
                row.hide().next('tr.collapsible').hide().next('tr.collapsible').hide();
            }
            break;
        case "suspended":
            component.removeClass('myplan-delete').attr("data-planned", "false").data("planned", false).html("--").parent("td").removeClass('myplan-delete');
            row.removeClass('myplan-section-planned');
            script = "jQuery('#' + '" + actionId + "').off('click'); ";
            if (jQuery("#" + data.courseDetails.courseId + "_toggle").data("hidden")) {
                row.hide();
            }
            break;
    }
    if (action != "suspended") script += "jQuery('#' + '" + actionId + "').mouseover(function(){ buildHoverText(jQuery(this));}); ";
    updateHiddenScript(actionId, script);
}

function fnRemoveSectionRow(actionId) {
    var row = jQuery("#" + actionId).parents("tr.row");
    row.remove().next("tr.collapsible").remove().next("tr.collapsible").remove();
}

function fnUpdateQuarterViewCredits(termCredits) {
    var currentCredits = jQuery(".myplan-credit-total .myplan-credit-value span.uif-message");
    if (currentCredits.text() != termCredits) {
        currentCredits.fadeOut(250, function () {
            jQuery(this).text(termCredits).fadeIn(250);
        });
    }
}
