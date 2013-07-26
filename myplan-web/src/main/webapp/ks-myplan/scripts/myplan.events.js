/*
 #################################################################
 Function: add course to quarter plan view
 #################################################################
 */
function planItemTemplate(data) {
    var item = jQuery("<div/>").attr("id", data.planItemId + "_" + data.planItemType + "_" + data.atpId + "_div").addClass("uif-verticalBoxGroup uif-collectionItem uif-tooltip");

    var group = jQuery("<div/>").attr("id", data.planItemId + "_" + data.planItemType + "_" + data.atpId).attr({
        "title":data.planItemLongTitle,
        "data-atpid":data.atpId.replace(/-/g, "."),
        "data-planitemid":data.planItemId,
        "data-placeholder":data.placeHolder
    }).addClass("uif-horizontalFieldGroup myplan-course-valid uif-boxLayoutVerticalItem clearfix");

    var horizontalBox = jQuery("<div/>").addClass("uif-horizontalBoxLayout clearfix");

    var script = jQuery("<input/>").attr({
        "type":"hidden",
        "name":"script",
        "data-role":"script"
    }).val("jQuery('#" + data.planItemId + "_" + data.planItemType + "_" + data.atpId + "').click(function(e) { openMenu('" + data.planItemId + "_" + data.planItemType + "','" + data.planItemType + "_" + ((data.placeHolder == "true") ? "placeholder" : "course" ) + "_menu','" + data.atpId.replace(/-/g, ".") + "',e,'.uif-collectionItem','fl-container-150 uif-boxLayoutHorizontalItem',{tail:{align:'top'},align:'top',position:'right'},false); });")

    if (data.placeHolder == "true") {
        item.addClass("placeholder");
    } else {
        group.attr("data-courseid", data.courseId).attr("data-plannedsections", data.sections);
    }

    var image = jQuery("<img/>").attr("src", "/student/ks-myplan/images/pixel.gif");

    if (data.showAlert == "true") {
        var alert = jQuery("<div/>").attr("id", data.planItemId + "_alert").addClass("itemAlert uif-boxLayoutHorizontalItem").append(image.attr({
            "title":data.statusAlert,
            "alt":data.statusAlert
        }));
        horizontalBox.append(alert);
        group.addClass("alert");
        //script.val(script.val() + " createTooltip('" + data.planItemId + "_alert', '" + data.statusAlert + "', {position:'top',align:'left',alwaysVisible:false,tail:{align:'left',hidden:false},themePath:'../ks-myplan/jquery-popover/jquerypopover-theme/',themeName:'myplan-help',selectable:true,width:'250px',closingDelay:500,themeMargins:{ total:'17px', difference:'8px' }}, true, true);");
    }
    var title = jQuery("<div/>").addClass("itemTitle uif-boxLayoutHorizontalItem").append(data.planItemShortTitle);
    horizontalBox.append(title);
    if (data.sections != "") {
        var sections = jQuery("<div/>").addClass("itemActivities myplan-text-ellipsis uif-boxLayoutHorizontalItem").append(data.sections);
        horizontalBox.append(sections);
    }
    var credit = jQuery("<div/>").addClass("itemCredit uif-boxLayoutHorizontalItem").append("(" + data.credit + ")");
    horizontalBox.append(credit);
    if (data.note) {
        var note = jQuery("<div/>").addClass("itemNote uif-boxLayoutHorizontalItem").append(image.attr({
            "title":data.note,
            "alt":data.note
        }));
        horizontalBox.append(note);
        // popup for note
    }
    horizontalBox.clone().appendTo(group).wrap("<fieldset/>").wrap('<div class="uif-horizontalBoxGroup"/>');
    group.clone().appendTo(item).wrap('<div class="uif-verticalBoxLayout clearfix"/>').after(script);

    return item;
}

function fnAddPlanItem(data) {
    var size = parseFloat(jQuery("." + data.atpId + ".myplan-term-" + data.planItemType).attr("data-size")) + 1;
    jQuery("." + data.atpId + ".myplan-term-" + data.planItemType).attr("data-size", size);
    fnShowHideQuickAddLink(data.atpId, data.planItemType, size);

    planItemTemplate(data).prependTo("." + data.atpId + ".myplan-term-" + data.planItemType + " .uif-stackedCollectionLayout");
    runHiddenScripts(data.planItemId + "_" + data.planItemType + "_" + data.atpId + "_div");
    jQuery("#" + data.planItemId + "_" + data.planItemType + "_" + data.atpId + "_div").css({backgroundColor:"#ffffcc"}).animate({backgroundColor:"#ffffff"}, 3000);
    truncateField(data.planItemId + "_" + data.planItemType + "_" + data.atpId + "_div", true);
}

function fnUpdatePlanItem(data) {
    jQuery("#" + data.planItemId + "_" + data.planItemType + "_" + data.atpId + "_div").replaceWith(planItemTemplate(data));
    runHiddenScripts(data.planItemId + "_" + data.planItemType + "_" + data.atpId + "_div");
    jQuery("#" + data.planItemId + "_" + data.planItemType + "_" + data.atpId + "_div").css({backgroundColor:"#ffffcc"}).animate({backgroundColor:"#ffffff"}, 3000);
    truncateField(data.planItemId + "_" + data.planItemType + "_" + data.atpId + "_div", true);
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
        jQuery(this).html('<button id="' + courseId + '_addSavedCourse" class="uif-action uif-secondaryActionButton uif-boxLayoutHorizontalItem" onclick="var additionalFormData = {viewId:\'PlannedCourse-FormView\', methodToCall:\'addSavedCourse\', courseId:\'' + courseId + '\'}; submitHiddenForm(\'plan\', additionalFormData, event);">Bookmark Course</button>');
        jQuery(this).siblings("input[data-role='script']").removeAttr("script").attr("name", "script").val("jQuery(document).ready(function () {jQuery('#" + courseId + "_addSavedCourse').on('PLAN_ITEM_ADDED', function (event, data) {if (data.planItemType === 'wishlist') {fnDisplayMessage(data.message, data.cssClass, data.courseId + '_addSavedCourse', true, false,false);}});});");
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
            script = "jQuery('#' + '" + actionId + "').click(function(e) { var additionalFormData = {viewId:'PlannedCourse-FormView', methodToCall:'removeItem', planItemId:'" + planItemId + "', sectionCode:'" + component.data("coursesection") + "', atpId:'" + data.atpId.replace(/-/g, '.') + "', instituteCode:'" + data.InstituteCode + "', registrationCode:'" + regId + "', primary:" + component.data("primary") + "}; submitHiddenForm('plan', additionalFormData, e); }); ";
            break;
        case "deleted":
            component.removeClass('myplan-delete').addClass('myplan-add').attr("data-planned", "false").data("planned", false).parent("td").removeClass('myplan-delete').addClass('myplan-add');
            row.removeClass('myplan-section-planned').next('tr.collapsible').removeClass('myplan-section-planned').next('tr.collapsible').removeClass('myplan-section-planned');
            script = "jQuery('#' + '" + actionId + "').click(function(e) { var additionalFormData = {viewId:'PlannedCourse-FormView', methodToCall:'addUpdatePlanItem', courseId:'" + data.courseId + "', sectionCode:'" + component.data("coursesection") + "', atpId:'" + data.atpId.replace(/-/g, '.') + "', instituteCode:'" + data.InstituteCode + "', registrationCode:'" + regId + "', primary:" + component.data("primary") + "}; submitHiddenForm('plan', additionalFormData, e); }); ";
            if (jQuery("#" + data.courseId + "_toggle").data("hidden")) {
                row.hide().next('tr.collapsible').hide().next('tr.collapsible').hide();
            }
            break;
        case "suspended":
            component.removeClass('myplan-delete').attr("data-planned", "false").data("planned", false).html("--").parent("td").removeClass('myplan-delete');
            row.removeClass('myplan-section-planned');
            script = "jQuery('#' + '" + actionId + "').off('click'); ";
            if (jQuery("#" + data.courseId + "_toggle").data("hidden")) {
                row.hide();
            }
            break;
    }
    if (action != "suspended") script += "jQuery('#' + '" + actionId + "').mouseover(function(){ buildHoverText(jQuery(this)); }); ";
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
