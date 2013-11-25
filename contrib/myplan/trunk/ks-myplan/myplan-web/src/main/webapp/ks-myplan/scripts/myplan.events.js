/*
 #################################################################
 Function: add course to quarter plan view
 #################################################################
 */
function planItemTemplate(data) {
    var itemId = data.planItemType + "_" + data.atpId + "_" + data.planItemId;
    var image = jQuery("<img/>").attr("src", "/student/ks-myplan/images/pixel.gif");
    var actionGroup = jQuery("<div/>").attr("class", "uif-horizontalBoxLayout");
    var itemGroup = jQuery("<div/>").attr("class", "uif-horizontalBoxLayout");

    var item = jQuery("<div/>").attr({
        "id": itemId + "_group",
        "class": "uif-verticalBoxGroup uif-collectionItem" + ((data.adviserRecommended == "true") ? " accepted" : ((data.planItemType == "recommended") ? " proposed" : ""))
    });

    var shortTitle = data.planItemShortTitle;

    var title = jQuery("<div/>").attr("class", "itemTitle uif-boxLayoutHorizontalItem").append(shortTitle);
    actionGroup.append(title);

    if (data.sections != null && data.sections != "") {
        var sections = jQuery("<div/>").attr("class", "itemActivities myplan-text-ellipsis uif-boxLayoutHorizontalItem").append(data.sections);
        actionGroup.append(sections);
    }

    if (data.credit != null && data.credit != "") {
        var itemCredit = "(" + data.credit + ")";
        var credit = jQuery("<div/>").attr("class", "itemCredit uif-boxLayoutHorizontalItem").append(itemCredit);
        actionGroup.append(credit);
    }

    var action = jQuery("<div/>").attr("id", itemId).attr({
        "title": ((data.placeHolder == "true" && data.planItemShortTitle == data.planItemLongTitle) ? "" : data.planItemShortTitle) + " " + ((data.sections != null && data.sections != "") ? data.sections + " " : "") + "'" + data.planItemLongTitle + "'" + ((data.placeHolder == "true") ? " placeholder" : ""),
        "class": "uif-horizontalFieldGroup itemAction uif-tooltip uif-boxLayoutHorizontalItem",
        "data-atpid": data.atpId.replace(/-/g, "."),
        "data-planitemid": data.planItemId,
        "data-placeholder": data.placeHolder,
        "data-type": data.planItemType,
        "style": "width:" + ((data.note) ? "103px; padding-right:15px;" : "118px;")
    });

    if (data.placeHolder == "true") {
        item.addClass("placeholder");
    } else {
        action.attr({
            "data-courseid": data.courseId,
            "data-plannedsections": data.sections
        });
    }

    actionGroup.clone().appendTo(action).wrap('<div class="uif-horizontalBoxGroup"/>');

    if (data.showAlert == "true") {
        var alert = jQuery("<div/>").attr({
            "title": data.statusAlert,
            "class": "itemAlert uif-boxLayoutHorizontalItem"
        }).append(image.clone().attr("alt", data.statusAlert));
        itemGroup.append(alert);
        item.addClass("alert");
    }

    itemGroup.append(action);

    var click = "jQuery('#" + itemId + "').click(function(e) { ";

    if (!getConfigParam("adviser")) {
        click += "openMenu('" + data.planItemId + "_" + data.planItemType + "','" + data.planItemType + "_" + ((data.placeHolder == "true") ? "placeholder" : "course" ) + "_menu','" + data.atpId.replace(/-/g, ".") + "',e,'.uif-collectionItem','fl-container-150 uif-boxLayoutHorizontalItem',{tail:{align:'top'},align:'top',position:'right'},false);";
    } else {
        click += "var retrieveData = {action:'plan', viewId:'PlannedCourse-FormView', methodToCall:'startAddPlannedCourseForm', planItemId:'" + data.planItemId + "', atpId:'" + data.atpId.replace(/-/g, ".") + "', pageId:'recommended_dialog_page'" + ((data.placeHolder != "true") ? ", courseId:'" + data.courseId + "'" : "" ) + "}; openPopup('recommended_dialog_page', retrieveData, 'plan', {width:'300px', height:'16px'}, {tail:{hidden:true}, position:'right', align:'middle', close:true}, e);";
    }

    click += "});";

    var script = jQuery("<input/>").attr({
        "type": "hidden",
        "name": "script",
        "data-role": "script"
    }).val(click);

    if (data.note) {
        var note = jQuery("<div/>").attr({
            "id": itemId + "_note",
            "class": "itemNote uif-boxLayoutHorizontalItem uif-tooltip"
        }).append(image.clone());
        itemGroup.append(note);
        var decoded = jQuery("<div/>").html(data.note).text();
        var noteContent;
        if (!getConfigParam("adviser")) {
            noteContent = "<p>" + decoded + "</p><p><a data-planitemtype=" + data.planItemType + " data-planitemid=" + data.planItemId + " data-atpid=" + data.atpId.replace(/-/g, ".") + " onclick=editNote(jQuery(this),event);>Edit Note</a></p>";
        } else {
            noteContent = '&quot;' + decoded + '&quot; - ' + data.adviserName;
        }
        var createTooltip = " createTooltip('" + itemId + "_note', '" + noteContent + "', {position:'top',align:'left',alwaysVisible:false,tail:{align:'left',hidden:false},themePath:'../ks-myplan/jquery-popover/jquerypopover-theme/',themeName:'ksap-" + ((data.planItemType == "recommended") ? "adviser" : "notes") + "',selectable:true,openingSpeed:50,closingSpeed:50,openingDelay:500,closingDelay:0,themeMargins:{total:'17px',difference:'10px'},distance:'0px'},true,true);";
        script.val(script.val() + createTooltip);
    }

    itemGroup.append(script);

    itemGroup.clone().appendTo(item).wrap('<div class="uif-horizontalBoxGroup"/>');

    return item;
}

function fnAddPlanItem(data) {
    var itemId = data.planItemType + "_" + data.atpId + "_" + data.planItemId;
    var collection = jQuery("." + data.atpId + ".myplan-term-" + data.planItemType);

    var size = parseFloat(collection.attr("data-size")) + 1;
    collection.attr("data-size", size);

    if (collection.attr("data-limit") != "false") {
        fnShowHideQuickAddLink(data.atpId, data.planItemType, size);
    }

    planItemTemplate(data).prependTo("." + data.atpId + ".myplan-term-" + data.planItemType + " .uif-stackedCollectionLayout");
    runHiddenScripts(itemId + "_group");
    jQuery("#" + itemId + "_group").css({backgroundColor: "#faf5ca"}).animate({backgroundColor: "#ffffff"}, 1500, "linear", function () {
        jQuery(this).removeAttr("style");
    });
    truncateField(itemId + "_group", true);
}

function fnUpdatePlanItem(data) {
    var itemId = data.planItemType + "_" + data.atpId + "_" + data.planItemId;
    jQuery("#" + itemId + "_group").replaceWith(planItemTemplate(data));
    runHiddenScripts(itemId + "_group");
    jQuery("#" + itemId + "_group").css({backgroundColor: "#faf5ca"}).animate({backgroundColor: "#ffffff"}, 1500, "linear", function () {
        jQuery(this).removeAttr("style");
    });
    truncateField(itemId + "_group", true);
}
function fnUpdateNote(data) {
    var noteId = data.planItemType + "_" + data.atpId + "_" + data.planItemId + "_note";
    jQuery("#" + noteId).off();
    if (data.note) {
        var decoded = jQuery("<div/>").html(data.note).text();
        var createTooltip = "createTooltip('" + noteId + "', ' <p>" + decoded + "</p><p><a data-planitemtype=" + data.planItemType + " data-planitemid=" + data.planItemId + " data-atpid=" + data.atpId.replace(/-/g, ".") + " onclick=editNote(jQuery(this),event);>Edit Note</a></p> ', {position:'top',align:'left',alwaysVisible:false,tail:{align:'left',hidden:false},themePath:'../ks-myplan/jquery-popover/jquerypopover-theme/',themeName:'ksap-notes',selectable:true,width:'250px',openingSpeed:50,closingSpeed:50,openingDelay:500,closingDelay:0,themeMargins:{total:'17px',difference:'10px'},distance:'0px'},true,true);";
        var noteScript = jQuery("input[data-for='" + noteId + "'][data-role='script']")[0];
        jQuery(noteScript).attr("name", "script").removeAttr("script").val(createTooltip);
        evalHiddenScript(jQuery(noteScript));
    } else {
        jQuery("#" + noteId).remove();
    }
}
/*
 #################################################################
 Function: remove course from quarter plan view
 #################################################################
 */
function fnRemovePlanItem(data) {
    var itemId = data.planItemType + "_" + data.atpId + "_" + data.planItemId;
    var collection = jQuery("." + data.atpId + ".myplan-term-" + data.planItemType);
    jQuery("#" + itemId).unbind('click');

    var size = parseFloat(collection.attr("data-size")) - 1;
    collection.attr("data-size", size);

    if (collection.attr("data-limit") != "false") {
        fnShowHideQuickAddLink(data.atpId, data.planItemType, size);
    }

    collection.find("#" + itemId + "_group").fadeOut(250, function () {
        jQuery(this).remove();
    });

    if (size == 0 && collection.attr("data-keep-header") == "false") {
        collection.fadeOut(250, function () {
            jQuery(this).remove();
        });
    }
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
function fnRestoreSearchAddButton(courseId, subject, number) {
    var oTable = jQuery('.myplan-course-search-results-datatable.uif-dataTable').dataTable();
    var oNodes = oTable.fnGetNodes();
    jQuery(oNodes).find("#" + courseId + "_" + subject + "_" + number + "_status").fadeOut(250, function () {
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
        jQuery("." + atpId + ".myplan-term-" + type + " .uif-stackedCollectionLayout .addItemAction").fadeIn(250);
    } else {
        jQuery("." + atpId + ".myplan-term-" + type + " .uif-stackedCollectionLayout .addItemAction").fadeOut(250);
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

/*
 #################################################################
 Function: update style from recommended item
 #################################################################
 */
function fnUpdateRecommendedItem(data) {
    var itemId = data.planItemType + "_" + data.atpId + "_" + data.planItemId;
    jQuery("#" + itemId + "_group").removeClass("proposed").addClass("accepted");
    /*var image = jQuery("<img/>").attr("src", "/student/ks-myplan/images/pixel.gif");
     var accepted = jQuery("<div/>").attr({
     "class": "itemAccepted uif-boxLayoutHorizontalItem"
     }).append(image.clone());
     jQuery("#" + itemId).before(accepted);*/
    jQuery("#" + itemId + "_group").css({backgroundColor: "#faf5ca"}).animate({backgroundColor: "#ffffff"}, 1500, "linear", function () {
        jQuery(this).removeAttr("style");
    });
}
