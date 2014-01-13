var popupOptionsDefault = {
    themePath: "../themes/ksap/images/popover-theme/",
    manageMouseEvents: true,
    selectable: true,
    tail: {align: "middle", hidden: false},
    position: "left",
    align: "center",
    alwaysVisible: false,
    themeMargins: {total: "20px", difference: "5px"},
    themeName: "default",
    distance: "0px",
    openingSpeed: 5,
    closingSpeed: 5
};

/**
 * Open a course item popup by id, mostly used in linking course codes in audit reports and course reqs text (course linkifier)
 *
 * @param courseId - GUID string of course to open in popup
 * @param e - An object containing data that will be passed to the event handler.
 */
function openCourse(courseId, courseCd, e) {
    stopEvent(e);
    var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    if (jQuery(target).parents(".jquerypopover.jquerypopover-default").length > 0) {
        window.location = "inquiry?methodToCall=start&viewId=CourseDetails-InquiryView&courseId=" + courseId + "&courseCd=" + courseCd.replace("&", "%26");
    } else {
        var retrieveData = {action: "plan", viewId: "PlannedCourse-FormView", methodToCall: "startAddPlannedCourseForm", courseId: courseId, code: courseCd};
        var popupStyle = {width: "300px", height: "16px"};
        var popupOptions = {tail: {align: "left"}, align: "left", position: "bottom", close: true};
        openPopup("add_remove_course_popover_page", retrieveData, "plan", popupStyle, popupOptions, e);
    }
}
/**
 * Open a popup which loads via ajax a separate view's component
 *
 * @param getId - Id of the component from the separate view to select to insert into popup.
 * @param retrieveData - Object of data used to passed to generate the separate view.
 * @param formAction - The action param of the popup inner form.
 * @param popupStyle - Object of css styling to apply to the initial inner div of the popup (will be replaced with remote component)
 * @param popupOptions - Object of settings to pass to the Bubble Popup jQuery Plugin.
 * @param e - An object containing data that will be passed to the event handler.
 */
function openPopup(getId, retrieveData, formAction, popupStyle, popupOptions, e) {
    stopEvent(e);
    fnCloseAllPopups();

    var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    var popupItem = (typeof popupOptions.selector == "undefined") ? jQuery(target) : jQuery(target).parents(popupOptions.selector);

    if (!popupItem.HasPopOver()) popupItem.CreatePopOver({manageMouseEvents: false});
    var popupSettings = jQuery.extend(popupOptionsDefault, popupOptions);
    var popupHtml = jQuery('<div />').attr("id", "KSAP-Popover");
    if (popupStyle) {
        jQuery.each(popupStyle, function (property, value) {
            popupHtml.css(property, value);
        });
    } else {
        popupHtml.css({
            width: "300px",
            height: "16px"
        });
    }
    popupSettings.innerHtml = popupHtml.wrap("<div>").parent().clone().html();

    popupItem.ShowPopOver(popupSettings, false);
    popupItem.FreezePopOver();

    var popupId = popupItem.GetPopOverID();

    fnPositionPopUp(popupId);
    clickOutsidePopOver(popupId, popupItem);

    var retrieveForm = '<form id="retrieveForm" action="' + retrieveData.action + '" method="post" />'
    jQuery("body").append(retrieveForm);

    var elementToBlock = jQuery("#KSAP-Popover");

    var successCallback = function (htmlContent) {
        var component;
        if (jQuery("#requestStatus", htmlContent).length <= 0) {
            var popupForm = jQuery('<form />').attr("id", "popupForm").attr("action", formAction).attr("method", "post");
            component = jQuery("#" + getId, htmlContent).wrap(popupForm).parent();
        } else {
            var pageId = jQuery("#pageId", htmlContent).val();
            var errorMessage = jQuery("#" + pageId, htmlContent).data("validation_messages").serverErrors[0];
            component = jQuery('<div style="width:300px;" />').append('<h4>Error</h4>').append(jQuery("<div />").addClass("alert alert-error alert-unboxed uif-boxLayoutVerticalItem").html(errorMessage));
        }
        if (jQuery("#KSAP-Popover").length) {
            popupItem.SetPopOverInnerHtml(component);
            fnPositionPopUp(popupId);
            if (popupOptions.close || typeof popupOptions.close === 'undefined') jQuery("#" + popupId + " .jquerypopover-innerHtml").append('<img src="' + getConfigParam("ksapImageLocation") + 'icons/close.png" class="popover__close"/>');
            jQuery("#" + popupId + " img.popover__close").on('click', function () {
                popupItem.HidePopOver();
                fnCloseAllPopups();
            });
        }
        runHiddenScripts(getId);
        elementToBlock.unblock();
    };

    ksapAjaxSubmitForm(retrieveData, successCallback, elementToBlock, "retrieveForm");
    jQuery("form#retrieveForm").remove();
}
/**
 *
 *
 * @param id -
 * @param getId - Id of the component from the separate view to select to insert into popup.
 * @param atpId -
 * @param e - An object containing data that will be passed to the event handler.
 * @param selector -
 * @param popupClasses -
 * @param popupOptions - Object of settings to pass to the Bubble Popup jQuery Plugin.
 * @param close -
 */
function openMenu(id, getId, atpId, e, selector, popupClasses, popupOptions, close) {
    stopEvent(e);

    var popupBox;
    var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    if (selector === null) {
        popupBox = jQuery(target);
    } else {
        popupBox = jQuery(target).parents(selector);
    }

    fnCloseAllPopups();

    if (!popupBox.HasPopOver()) popupBox.CreatePopOver({manageMouseEvents: false});

    if (atpId != null) {
        var openForPlanning = popupBox.parents(".planYear__term").data("openforplanning");
        if (!openForPlanning && getId != "academic_course_menu") {
            if (getId == "planned_placeholder_menu" || getId == "backup_placeholder_menu") {
                getId = "completed_placeholder_menu";
            }
            if (getId == "planned_course_menu" || getId == "backup_course_menu") {
                getId = "completed_plan_items_menu";
            }
        }
    }
    var popupHtml = jQuery('<div />').attr("id", id + "_popup").attr("class", popupClasses).html(jQuery("#" + getId).html());

    var popupSettings = jQuery.extend(popupOptionsDefault, popupOptions);
    popupSettings.innerHtml = popupHtml.wrap("<div>").parent().clone().html();

    popupBox.ShowPopOver(popupSettings, false);
    var popupBoxId = popupBox.GetPopOverID();
    popupBox.FreezePopOver();

    jQuery("#" + id + "_popup a").each(function () {
        var linkId = jQuery(this).attr("id");
        jQuery(this).siblings("input[data-for='" + linkId + "']").removeAttr("script").attr("name", "script").val(function (index, value) {
            return value.replace("'#" + linkId + "'", "'#" + linkId + "_popup'");
        });
        jQuery(this).attr("id", linkId + "_popup");
        jQuery.each(jQuery(target).data(), function (key, value) {
            jQuery("#" + linkId + "_popup").attr("data-" + key, value);
        });
    });

    runHiddenScripts(id + "_popup");

    clickOutsidePopOver(popupBoxId, popupBox);
}
/**
 *
 *
 * @param sText -
 * @param e - An object containing data that will be passed to the event handler.
 * @param close -
 */
function openDialog(sText, e, close) {
    stopEvent(e);

    var popupBox = jQuery("body");

    fnCloseAllPopups();

    if (!popupBox.HasPopOver()) popupBox.CreatePopOver({manageMouseEvents: false});

    popupOptionsDefault.tail.hidden = true;
    popupOptionsDefault.innerHtml = '<div style="width:300px;">' + sText + '</div>';

    popupBox.ShowPopOver(popupOptionsDefault, false);
    var popupBoxId = popupBox.GetPopOverID();
    popupBox.FreezePopOver();

    if (close || typeof close === 'undefined') jQuery("#" + popupBoxId + " .jquerypopover-innerHtml").append('<img src="' + getConfigParam("ksapImageLocation") + 'icons/close.png" class="popover__close"/>');

    fnPositionPopUp(popupBoxId);

    clickOutsidePopOver(popupBoxId, popupBox);

    jQuery("#" + popupBoxId + " img.popover__close").on('click', function () {
        popupBox.HidePopOver();
        fnCloseAllPopups();
    });
}

function fnPositionPopUp(popupBoxId) {
    if (parseFloat(jQuery("#" + popupBoxId).css("top")) < 0 || parseFloat(jQuery("#" + popupBoxId).css("left")) < 0) {
        var top = (document.documentElement && document.documentElement.scrollTop) || document.body.scrollTop;
        var left = (document.documentElement && document.documentElement.scrollLeft) || document.body.scrollLeft;
        var iTop = ( top + ( jQuery(window).height() / 2 ) ) - ( jQuery("#" + popupBoxId).height() / 2 );
        var iLeft = ( left + ( jQuery(window).width() / 2 ) ) - ( jQuery("#" + popupBoxId).width() / 2 );
        jQuery("#" + popupBoxId).css({top: iTop + 'px', left: iLeft + 'px'});
    }
}

/*
 ######################################################################################
 Function:   Slide into view hidden horizontally aligned items specifying the id
 of the item being brought into view.
 ######################################################################################
 */
function fnPopoverSlider(showId, parentId, direction) {
    var newDirection;
    if (direction === 'left') {
        newDirection = 'right';
    } else {
        newDirection = 'left';
    }
    jQuery("#" + parentId + " > .uif-horizontalBoxLayout > div.uif-boxLayoutHorizontalItem:visible").hide("slide", {
        direction: direction
    }, 100, function () {
        jQuery("#" + parentId + " > .uif-horizontalBoxLayout > div.uif-boxLayoutHorizontalItem").filter("#" + showId).show("slide", {
            direction: newDirection
        }, 100, function () {
        });
    });
}

function clickOutsidePopOver(popoverId, element) {
    jQuery("body").on("click", function (e) {
        var tempTarget = (e.target) ? e.target : e.srcElement;
        if (jQuery(tempTarget).parents("#" + popoverId).length === 0) {
            fnCloseAllPopups();
            jQuery("body").off("click");
        }
    });
}
/*
 ######################################################################################
 Function:   Close all bubble popups
 ######################################################################################
 */
function fnCloseAllPopups() {
    if (jQuery("body").HasPopOver()) {
        jQuery("body").HidePopOver();
        jQuery("body").RemovePopOver();
    }
    jQuery("div.jquerypopover[id^='jquerypopover']").remove();
    jQuery("body").off("click");
}

/**
 *
 */
function editNote(obj, e) {
    var planItemId = obj.data("planitemid");
    var atpId = obj.data("atpid");
    var planItemType = obj.data("planitemtype");
    var backupFlag = (planItemType == "backup");
    jQuery("#" + planItemType + "-" + atpId + "-" + planItemId + "-note").HideBubblePopup();
    var retrieveData = {
        action: 'plan',
        viewId: 'PlannedCourse-FormView',
        methodToCall: 'startAddPlannedCourseForm',
        planItemId: planItemId,
        atpId: atpId,
        backup: backupFlag,
        pageId: 'edit_note_page'
    };
    var popupOptions = {
        tail: {
            hidden: true
        },
        align: 'top',
        close: true,
        selector: "body"
    };
    openPopup('edit_note_page', retrieveData, 'plan', null, popupOptions, e);
}