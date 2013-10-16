/*
 ######################################################################################
 Function:   Truncate (ellipse) a single horizontally aligned item so all items
 fit on one line.
 ######################################################################################
 */
function truncateField(id, floated) {
    jQuery("#" + id + " .uif-horizontalFieldGroup").each(function () {
        var itemSelector = ".uif-horizontalBoxGroup > .uif-horizontalBoxLayout > .uif-boxLayoutHorizontalItem";
        var ellipsisItem = jQuery(this).find(itemSelector + ".ellipsisItem");
        if (ellipsisItem.length != 0) {
            jQuery(this).css("display", "block");
            var fixed = 0;
            jQuery(this).find(itemSelector + ":not(.ellipsisItem)").each(function () {
                fixed = fixed + jQuery(this).outerWidth(true);
            });
            var available = jQuery(this).width() - ( fixed + ( ellipsisItem.outerWidth(true) - ellipsisItem.width() ) + 1 );
            ellipsisItem.css("white-space", "nowrap");
            if (!floated) {
                ellipsisItem.width(available);
            } else {
                if (ellipsisItem.width() >= available) {
                    ellipsisItem.width(available);
                }
            }
        }
    });
}
function indicateViewingAudit(id, type) {
    var open = false;
    var currentAudit = jQuery("." + type + ".auditHtml .myplan-audit-report");
    var currentAuditId = currentAudit.attr("auditid");

    jQuery("#" + id + " .uif-collectionItem").not(".pending").each(function (index) {
        if (jQuery(this).attr("id").replace("link_", "") == currentAuditId && currentAudit.is(":visible")) {
            if (type == 'degreeAudit') {
                jQuery(this).find(".uif-label label").html("Viewing");
            }
            if (type == 'planAudit') {
                if (index > 1) open = true;
                jQuery(this).addClass("viewing");
            }
        } else {
            if (type == 'degreeAudit') {
                jQuery(this).find(".uif-label label").html("View");
            }
            if (type == 'planAudit') {
                jQuery(this).removeClass("viewing");
            }
        }
    });
    if (open) {
        jQuery("#plan_audit_toggle_link").click();
    }
}

/*
 ######################################################################################
 Function:   Build Term Plan View heading
 ######################################################################################
 */
function fnBuildTitle(aView) {
    var sText = 'Academic Year';
    var aFirst = jQuery.trim(jQuery(aView[0]).find("div:hidden[id^='plan_base_atpId']").text()).split(".");
    var aLast = jQuery.trim(jQuery(aView[aView.length - 1]).find("div:hidden[id^='plan_base_atpId']").text()).split(".");
    jQuery("#planned_courses_detail .myplan-plan-header").html(sText + ' ' + aFirst[3] + '-' + aLast[3]);
    var navigationAtpId = jQuery.trim(jQuery(aView[0]).find("div:hidden[id^='single_quarter_atpId']").text());
    //fnQuarterNavigation(navigationAtpId, 'single_quarter_button', 'planned_courses_detail');
    var quarterLink = "inquiry?methodToCall=start&viewId=SingleTerm-InquiryView&term_atp_id=" + navigationAtpId;
    jQuery("#single_quarter_button").attr("href", quarterLink);
}

/*
 ######################################################################################
 Function:   Dynamically Builds the Quarter Button Action
 ######################################################################################
 */
function fnQuarterNavigation(atpId, component) {
    if (atpId == "") {
        jQuery("#" + component).attr("href", "#").addClass('disabled').click(function (e) {
            e.preventDefault();
        });
    }
}
/*
 ######################################################################################
 Function:   expand/collapse backup course set within plan view
 ######################################################################################
 */
function myplanCreateLightBoxLink(controlId, options) {
    jQuery(function () {
        var showHistory = false;

        // Check if this is called within a light box
        if (!jQuery(".fancybox-wrap", parent.document).length) {

            // Perform cleanup when lightbox is closed
            options['beforeClose'] = cleanupClosedLightboxForms;

            // If this is not the top frame, then create the lightbox
            // on the top frame to put overlay over whole window
            if (top == self) {
                jQuery("#" + controlId).fancybox(options);
            } else {
                jQuery("#" + controlId).click(function (e) {
                    e.preventDefault();
                    top.jQuery.fancybox(options);
                });
            }
        } else {
            //jQuery("#" + controlId).attr('target', '_self');
            showHistory = true;
        }

        // Set the renderedInLightBox = true param
        if (options['href'].indexOf('&renderedInLightBox=true') == -1) {
            options['href'] = options['href'] + '&renderedInLightBox=true'
                + '&showHome=false' + '&showHistory=' + showHistory
                + '&history=' + jQuery('#formHistory\\.historyParameterString').val();
        }
    });
}

function myplanLightBoxLink(href, options, e) {
    stopEvent(e);
    var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    options['autoHeight'] = true;
    options['href'] = href;
    //options['beforeClose'] = cleanupClosedLightboxForms;
    top.jQuery.fancybox(options);
}

function toggleSections(actionId, toggleId, showClass, showText, hideText) {
    var group = jQuery("#" + toggleId + " table tbody tr.row").not("." + showClass);
    var action = jQuery("#" + actionId);
    if (action.data("hidden")) {
        group.each(function () {
            var toggle = jQuery(this).find("a[id^='toggle_']");
            if (toggle.data("hidden") || typeof toggle.data("hidden") == "undefined") {
                jQuery(this).show();
            } else {
                jQuery(this).show().next("tr.collapsible").show().next("tr.collapsible").show();
            }
        });
        jQuery(".myplan-quarter-detail .activityInstitutionHeading").show();
        action.text(hideText).data("hidden", false);
    } else {
        group.each(function () {
            var toggle = jQuery(this).find("a[id^='toggle_']");
            if (toggle.data("hidden") || typeof toggle.data("hidden") == "undefined") {
                jQuery(this).hide();
            } else {
                jQuery(this).hide().next("tr.collapsible").hide().next("tr.collapsible").hide();
            }
        });
        jQuery(".myplan-quarter-detail .activityInstitutionHeading").hide();
        action.text(showText).data("hidden", true);
    }
}

function toggleSectionDetails(sectionRow, obj, expandText, collapseText) {
    if (typeof obj.data("hidden") == "undefined") {
        obj.data("hidden", true);
    }
    var collapsibleRow = sectionRow.next("tr.collapsible");
    if (obj.data("hidden")) {
        sectionRow.find("td").first().attr("rowspan", "3");
        sectionRow.find("td").last().attr("rowspan", "3");
        collapsibleRow.show().next("tr.collapsible").show();
        obj.text(collapseText).data("hidden", false);
    } else {
        sectionRow.find("td").first().attr("rowspan", "1");
        sectionRow.find("td").last().attr("rowspan", "1");
        collapsibleRow.hide().next("tr.collapsible").hide();
        obj.text(expandText).data("hidden", true);
    }
}

function toggleRegisteredDetails(sectionRow, obj) {
    var collapsibleRow = sectionRow.next("tr.collapsible");
    if (collapsibleRow.is(":visible")) {
        obj.parents("td").attr("rowspan", "1");
        collapsibleRow.hide();
        obj.find("img.uif-image").toggleClass("expanded");
    } else {
        obj.parents("td").attr("rowspan", "2");
        collapsibleRow.show();
        obj.find("img.uif-image").toggleClass("expanded");
    }
}

function toggleComponentContent(obj, sectionId, selector, expandText, collapseText) {
    var action = jQuery(obj);
    if (typeof action.data("hidden") == "undefined") {
        action.data("hidden", true);
    }
    if (action.data("hidden")) {
        jQuery("#" + sectionId).find(selector).show();
        action.text(collapseText).data("hidden", false);
    } else {
        jQuery("#" + sectionId).find(selector).hide();
        action.text(expandText).data("hidden", true);
    }
}

function expandCurriculumComments(actionComponent, expandText, collapseText) {
    var curriculumMessage = jQuery(actionComponent).parent().find('.curriculum-comment');
    if (curriculumMessage.is(":visible")) {
        curriculumMessage.slideUp(250, function () {
            if (expandText) {
                jQuery(actionComponent).text(expandText);
            }
        });
    } else {
        curriculumMessage.slideDown(250, function () {
            if (collapseText) {
                jQuery(actionComponent).text(collapseText);
            }
        });
    }
}

function expandPlanAuditSummary(selector, expandText, collapseText) {
    if (jQuery(selector).is(":visible")) {
        jQuery(selector).each(function () {
            jQuery(this).attr('style', 'display:none').slideUp(250)
        });
        if (expandText) {
            jQuery('#plan_audit_toggle_link').text(expandText);
        }
    } else {
        jQuery(selector).each(function () {
            jQuery(this).attr('style', 'display:block').slideDown(250)
        });
        if (collapseText) {
            jQuery('#plan_audit_toggle_link').text(collapseText);
        }
    }
}

function buildHoverText(obj) {
    var message = '';
    var temp = '';
    // condition to check whether section is primary or secondary
    if (obj.data("primary")) {
        // Primary sections
        // condition to check if planned or not planned
        if (obj.data("planned")) {
            var secondarySections = [];
            // Find list of secondary sections associated
            jQuery("div[data-courseid='" + obj.data("courseid") + "'][data-primarysection='" + obj.data("coursesection") + "'][data-planned='true'][data-primary='false']").each(function () {
                secondarySections.push(jQuery(this).data("coursesection"));
            });
            // Build string of secondary sections associated
            if (secondarySections.length > 0) {
                if (secondarySections.length == 1) {
                    temp = " and " + secondarySections.join();
                } else {
                    // commas separated string of secondary sections
                    temp = ", " + secondarySections.slice(0, -1).join(", ") + ", and " + secondarySections[secondarySections.length - 1];
                }
            }
            // Text should give "Delete {primary section} {list of secondary sections if any exist}"
            message = "Delete " + obj.data("coursesection") + temp;
        } else {
            // Text should give "Add {primary section}"
            message = "Add " + obj.data("coursesection");
        }
    } else {
        // Secondary sections
        // condition to check if planned or not planned
        if (obj.data("planned")) {
            // Text should give "Delete {secondary section}"
            message = "Delete " + obj.data("coursesection");
        } else {
            // Text should give "Add {secondary section} and {primary section if not planned}"
            if (!jQuery("div[data-courseid='" + obj.data("courseid") + "'][data-coursesection='" + obj.data("primarysection") + "']").data("planned")) {
                temp = " and " + obj.data("primarysection");
            }
            message = "Add " + obj.data("coursesection") + temp;
        }
    }
    obj.attr("title", message).find("img.uif-image").attr("alt", message);
}

function buildTooltip(id, content, position, align, delay, speed) {
    var my;
    var offset = 20 - (jQuery("#" + id).width() / 2);
    switch (align) {
        case "left":
            my = "left-" + offset;
            break;
        case "right":
            my = "right+" + offset;
            break;
    }
    switch (position) {
        case "top":
            my += " bottom-2";
            break;
        case "right":
            my += " top+2";
            break;
    }
    jQuery("#" + id).tooltip({
        content: content.replace(/\s+/g, " "),
        tooltipClass: position + align,
        show: {
            delay: delay,
            duration: speed,
            easing: speed
        },
        position: {
            my: my,
            at: align + ' ' + position,
            of: jQuery("#" + id)
        },
        close: function (event, ui) {
            ui.tooltip.hover(
                function () {
                    jQuery(this).stop(true).fadeTo(speed, 1);
                },
                function () {
                    jQuery(this).fadeOut(speed, function () {
                        jQuery(this).remove();
                    })
                }
            );
        }
    });
}

function fnCreateDate(sData) {
    var jTemp = jQuery(sData);
    jTemp.find("legend, .myplan-sort-remove").remove();
    var sDate = jQuery.trim(jTemp.text());
    if (sDate.length > 2) {
        return Date.parse(sDate);
    } else {
        return 0;
    }
}

function updateHiddenScript(id, script) {
    jQuery("#" + id).unbind();
    var input = jQuery("input[data-for='" + id + "'][data-role='script']");
    input.removeAttr("script").attr("name", "script").val(script);
    runScriptsForId(id);
}

function switchFetchAction(actionId, toggleId) {
    var script = "jQuery('#' + '" + actionId + "').click(function(e){ toggleSections('" + actionId + "', '" + toggleId + "', 'myplan-section-planned', 'Show all scheduled sections', 'Hide non-selected sections'); });";
    updateHiddenScript(actionId, script);
    jQuery("#" + actionId).text("Hide non-selected sections").removeAttr("data-hidden").data("hidden", false);
}

function buttonState(parentId, buttonId) {
    var disabled = false;
    var button = jQuery("button#" + buttonId);
    jQuery("#" + parentId + " .myplan-required").each(function () {
        var value;
        if (jQuery(this).val()) {
            value = jQuery(this).val().replace(/\n/g, '');
        } else {
            value = "";
        }
        if (value == "" || value == "default") {
            disabled = true;
        }
    });
    if (disabled) {
        button.addClass("disabled").attr("disabled", disabled);
    } else {
        button.removeClass("disabled").attr("disabled", disabled);
    }
}

function disabledCheck(disableCompId, disableCompType, condition) {
    if (disableCompType == "radioGroup" || disableCompType == "checkboxGroup") {
        if (condition()) {
            jQuery("input[id^='" + disableCompId + "']").prop("disabled", true);
        }
        else {
            jQuery("input[id^='" + disableCompId + "']").prop("disabled", false);
        }
    }
    else {
        var disableControl = jQuery("#" + disableCompId);
        if (condition()) {
            disableControl.prop("disabled", true);
            disableControl.addClass("disabled");
            if (disableCompType === "actionLink" || disableCompType === "action") {
                disableControl.attr("tabIndex", "-1");
            }
        }
        else {
            disableControl.prop("disabled", false);
            disableControl.removeClass("disabled");
            if (disableCompType === "actionLink" || disableCompType === "action") {
                disableControl.attr("tabIndex", "0");
            }
        }
    }
}

/*
 #################################################################
 Function: add course to quarter plan view
 #################################################################
 */
function planItemTemplate(data) {
    var itemId = data.planItemType + "_" + data.atpId + "_" + data.planItemId;
    var image = jQuery("<img/>").attr("src", "../themes/ksap/images/pixel.gif");
    var actionGroup = jQuery("<div/>").attr("class", "uif-horizontalBoxLayout");
    var itemGroup = jQuery("<div/>").attr("class", "uif-horizontalBoxLayout");

    var item = jQuery("<div/>").attr({
        "id": itemId + "_group",
        "class": "uif-verticalBoxGroup uif-collectionItem"
    });

    var title = jQuery("<div/>").attr("class", "itemTitle uif-boxLayoutHorizontalItem").append(data.planItemShortTitle);
    actionGroup.append(title);

    if (data.sections != null && data.sections != "") {
        var sections = jQuery("<div/>").attr("class", "itemActivities myplan-text-ellipsis uif-boxLayoutHorizontalItem").append(data.sections);
        actionGroup.append(sections);
    }

    if (data.credit != null && data.credit != "") {
        var credit = jQuery("<div/>").attr("class", "itemCredit uif-boxLayoutHorizontalItem").append("(" + data.credit + ")");
        actionGroup.append(credit);
    }

    var action = jQuery("<div/>").attr("id", itemId).attr({
        "title": data.planItemShortTitle + " " + ((data.sections != null && data.sections != "") ? data.sections + " " : "") + "'" + data.planItemLongTitle + "'",
        "class": "uif-horizontalFieldGroup itemAction uif-tooltip uif-boxLayoutHorizontalItem",
        "data-atpid": data.atpId.replace(/-/g, "."),
        "data-planitemid": data.planItemId,
        "data-placeholder": data.placeHolder,
        "style": "width:" + ((data.note) ? "99px; padding-right: 16px;" : "115px;")
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

    var script = jQuery("<input/>").attr({
        "type": "hidden",
        "name": "script",
        "data-role": "script"
    }).val("jQuery('#" + itemId + "').click(function(e) { openMenu('" + data.planItemId + "_" + data.planItemType + "','" + data.planItemType + "_" + ((data.placeHolder == "true") ? "placeholder" : "course" ) + "_menu','" + data.atpId.replace(/-/g, ".") + "',e,'.uif-collectionItem','fl-container-150 uif-boxLayoutHorizontalItem',{tail:{align:'top'},align:'top',position:'right'},false); });")

    if (data.note) {
        var note = jQuery("<div/>").attr({
            "id": itemId + "_note",
            "class": "itemNote uif-boxLayoutHorizontalItem uif-tooltip"
        }).append(image.clone());
        itemGroup.append(note);
        var decoded = jQuery("<div/>").html(data.note).text();
        var createTooltip = " createTooltip('" + itemId + "_note', ' <p>" + decoded + "</p><p><a data-planitemtype=" + data.planItemType + " data-planitemid=" + data.planItemId + " data-atpid=" + data.atpId.replace(/-/g, ".") + " onclick=editNote(jQuery(this),event);>Edit Note</a></p> ', {position:'top',align:'left',alwaysVisible:false,tail:{align:'left',hidden:false},themePath:'../ks-myplan/jquery-popover/jquerypopover-theme/',themeName:'ksap-notes',selectable:true,width:'250px',openingSpeed:50,closingSpeed:50,openingDelay:500,closingDelay:0,themeMargins:{total:'17px',difference:'10px'},distance:'0px'},true,true);";
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
    var createTooltip = "createTooltip('" + noteId + "', ' <p>" + data.note + "</p><p><a data-planitemtype=" + data.planItemType + " data-planitemid=" + data.planItemId + " data-atpid=" + data.atpId.replace(/-/g, ".") + " onclick=editNote(jQuery(this),event);>Edit Note</a></p> ', {position:'top',align:'left',alwaysVisible:false,tail:{align:'left',hidden:false},themePath:'../ks-myplan/jquery-popover/jquerypopover-theme/',themeName:'ksap-notes',selectable:true,width:'250px',openingSpeed:50,closingSpeed:50,openingDelay:500,closingDelay:0,themeMargins:{total:'17px',difference:'10px'},distance:'0px'},true,true);";
    var noteScript = jQuery("input[data-for='" + noteId + "'][data-role='script']")[0];
    jQuery(noteScript).attr("name", "script").removeAttr("script").val(createTooltip);
    evalHiddenScript(jQuery(noteScript));
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

function replaceAction(actionId, replacement) {
    jQuery("#" + actionId).parent("div").fadeOut(250, function() {
        jQuery(this).html(replacement).fadeIn(250);
    });
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
        jQuery(this).removeClass().html('<input type="image" title="Bookmark or Add to Plan" src="../themes/ksap/images/pixel.gif" alt="Bookmark or Add to Plan" class="uif-field uif-imageField myplan-add" data-courseid="' + courseId + '" onclick="openMenu(\'' + courseId + '_add\',\'add_course_items\',null,event,null,\'myplan-container-75\',{tail:{align:\'middle\'},align:\'middle\',position:\'right\'},false);" />');
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

(function ($) {
    // TODO remove publish method after old audits have been purged as audit FTL inline scripted a publish call
    $.publish = function (event) {
        return true;
    };
    $.fn.characterCount = function (options) {
        var oDefaults = {
            maxLength: 999,
            warningLength: 10,
            classCounter: "counter",
            classWarning: "warning"
        };

        function calculate(obj, options) {
            var iCount = $(obj).val().length;
            var iAvailable = options.maxLength - iCount;
            var sValue = $(obj).val();
            if (iCount > options.maxLength) {
                $(obj).val(sValue.substr(0, options.maxLength));
            }
            if (iAvailable <= options.warningLength && iAvailable >= 0) {
                $('.' + options.classCounter).addClass(options.classWarning);
            } else {
                $('.' + options.classCounter).removeClass(options.classWarning);
            }
            $('.' + options.classCounter).html("<strong>" + iAvailable + "</strong>" + " character" + ((iAvailable != 1) ? "s" : "") + " remaining");
        }

        this.each(function () {
            if (typeof $(this).attr("maxlength") != "undefined") {
                oDefaults.maxLength = parseInt($(this).attr("maxlength"));
            }
            var options = $.extend(oDefaults, options);
            calculate(this, options);
            $(this).on("keyup paste cut contextmenu change mouseout blur", function (e) {
                calculate(this, options);
            });
        });
    };
})(jQuery);

jQuery.fn.dataTableExt.oSort['longdate-asc'] = function (x, y) {
    x = fnCreateDate(x);
    y = fnCreateDate(y);

    return ((x < y) ? -1 : ((x > y) ? 1 : 0));
};

jQuery.fn.dataTableExt.oSort['longdate-desc'] = function (x, y) {
    x = fnCreateDate(x);
    y = fnCreateDate(y);

    return ((x < y) ? 1 : ((x > y) ? -1 : 0));
};

Array.max = function (array) {
    return Math.max.apply(Math, array);
};
