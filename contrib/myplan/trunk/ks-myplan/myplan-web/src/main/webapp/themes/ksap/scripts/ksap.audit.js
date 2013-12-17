function getAuditProgram(param, type) {
    var campus;
    switch (parseFloat(jQuery("input[name='" + type + "Audit.campusParam']:checked").val())) {
        case 306:
            campus = "Seattle";
            break;
        case 310:
            campus = "Bothell";
            break;
        case 323:
            campus = "Tacoma";
            break;
        default:
            campus = null;
    }
    if (param == 'id') {
        return jQuery("select[name='" + type + "Audit.programParam" + campus + "']").val();
    } else {
        return jQuery("select[name='" + type + "Audit.programParam" + campus + "'] option:selected").text();
    }
}

var blockPendingAuditStyle = {
    blockMsgClass: 'auditRun__blockAudit',
    baseZ: 100,
    centerX: false,
    centerY: false,
    message: "",
    fadeIn: 250,
    fadeOut: 250,
    css: {
        top: '0px',
        left: '0px',
        width: '536px',
        color: '#c09853',
        padding: '30px 30px 30px 82px',
        margin: '30px',
        textAlign: 'left',
        border: 'solid 1px #fbeed5',
        backgroundColor: '#fcf8e3'
    },
    overlayCSS: {
        backgroundColor: '#fff',
        opacity: 0.9,
        border: 'none',
        cursor: 'wait'
    }
};

function removeCookie(name) {
    jQuery.cookie(name, null, {expires: new Date().setTime(0)});
}

function setPendingAudit(obj, minutes) {
    if (jQuery.cookie("pendingAudit") == null) {
        var data = {};
        data.expires = new Date();
        data.expires.setTime(data.expires.getTime() + (minutes * 60 * 1000));
        data.programId = getAuditProgram('id', obj.data("audittype"));
        data.programName = getAuditProgram('name', obj.data("audittype"));
        data.recentAuditId = obj.data("recentauditid");
        data.auditType = obj.data("audittype");
        if (typeof data.recentAuditId === 'undefined') data.recentAuditId = '';

        if (data.programId != 'default') {
            blockPendingAudit(data, false);
            jQuery.ajax({
                url: "/student/myplan/audit/status",
                data: {"programId": data.programId, "auditId": data.recentAuditId},
                dataType: "json",
                beforeSend: null,
                success: function (response) {
                    if (response.status == "PENDING") {
                        jQuery.cookie('pendingAudit', JSON.stringify(data), {expires: data.expires});
                        disabledCheck(obj.attr("id"), 'action', function () {
                            return true;
                        });
                        jQuery.event.trigger('REFRESH_AUDITS', data);
                        setUrlHash('modified', 'true');
                    }
                },
                statusCode: { 500: function () {
                    sessionExpired();
                } }
            });
        }
    } else {
        showGrowl("Another audit is currently pending. Please allow audit to complete.", "Running Audit Error", "errorGrowl");
    }
}

function getPendingAudit(id, type, data) {
    var auditData = jQuery.parseJSON(decodeURIComponent(data));
    if (type == auditData.auditType) {
        var component = jQuery("#" + id + " .uif-stackedCollectionLayout");
        if (auditData) {
            var item = jQuery("<div />").addClass("module__item module__item--pending").html('<img src="' + getConfigParam("ksapImageLocation") + 'loader/ajax_small.gif" class="icon"/><span>Auditing</span> ' + auditData.programName);
            component.prepend(item);
            if (component.prev(".module__empty").length > 0) {
                component.prev(".module__empty").remove();
            }
            if ((readUrlParam('viewId') != 'DegreeAudit-FormView' && readUrlParam('viewId') != 'PlanAudit-FormView') && readUrlParam(auditData.auditType + 'Audit.auditId') == false) {
                pollPendingAudit(auditData.programId, auditData.recentAuditId, auditData.auditType);
            }
        }
    }
}

function blockPendingAudit(data, refresh) {
    var id = "audit_section";
    if (data.auditType == "plan") id = "plan_audit_section";
    var elementToBlock = jQuery("#" + id);
    blockPendingAuditStyle.message = 'We are currently auditing your ' + data.auditType + " for '<strong>" + data.programName + "</strong>'.";
    elementToBlock.block(blockPendingAuditStyle);
    jQuery("body").on('AUDIT_COMPLETE AUDIT_ERROR', function (event, data) {
        if (refresh) {
            window.location.assign(window.location.href.split("#")[0]);
        } else {
            setUrlHash('modified', 'true');
            elementToBlock.unblock();
        }
    });
}

function pollPendingAudit(programId, recentAuditId, auditType) {
    var auditData = jQuery.parseJSON(decodeURIComponent(jQuery.cookie("pendingAudit")));
    var title = auditType.charAt(0).toUpperCase() + auditType.slice(1); + " Audit";

    jQuery("body").on('AUDIT_COMPLETE', function (event, data) {
        showGrowl(auditData.programName + " " + title + " is ready to view.", title + " Completed", "infoGrowl");
    });
    jQuery("body").on('AUDIT_ERROR', function (event, data) {
        showGrowl("Your " + title + " was unable to complete.", title + " Error", "errorGrowl");
    });

    jQuery.ajaxPollSettings.pollingType = "interval";
    jQuery.ajaxPollSettings.interval = 250; // polling interval in milliseconds

    jQuery.ajaxPoll({
        url: "/student/myplan/audit/status",
        data: {"programId": programId, "auditId": recentAuditId},
        dataType: "json",
        beforeSend: null,
        successCondition: function (response) {
            return (response.status == 'DONE' || response.status == 'FAILED' || jQuery.cookie("pendingAudit") == null);
        },
        success: function (response) {
            if (jQuery.cookie("pendingAudit") == null || response.status == 'FAILED') {
                jQuery.event.trigger("AUDIT_ERROR", {"auditType": auditType});
            } else {
                jQuery.event.trigger("AUDIT_COMPLETE", {"auditType": auditType});
            }
            /*
            var showAuditGrowl = true;
            if (readUrlParam("viewId") == "DegreeAudit-FormView" || readUrlParam("viewId") == "PlanAudit-FormView") {
                showAuditGrowl = jQuery("body").data("showAuditGrowl");
                if (readUrlParam(auditType + "Audit.auditId") != false) jQuery("body").on('AUDIT_COMPLETE', function (event, data) {
                    showAuditGrowl = false;

                });
            }
            */
        }
    });
}

function collapseReq(obj, onload) {
    var height = 23;
    if (onload) {
        obj.removeClass("expanded").addClass("collapsed").css({
            height: height + "px"
        }).children(".header").find(".title").css({
            height: height + "px"
        }).find(".text:first").css({
            whiteSpace: "nowrap",
            overflow: "hidden",
            textOverflow: "ellipsis"
        });
        obj.find(".header .title .text").not(':first').hide();
    } else {
        obj.removeClass("expanded").addClass("collapsed").animate({
            height: height + "px"
        }, 300, function () {
            jQuery(this).children(".header").find(".title").css({
                height: height + "px"
            }).find(".text:first").css({
                whiteSpace: "nowrap",
                overflow: "hidden",
                textOverflow: "ellipsis"
            });
            jQuery(this).find(".header .title .text").not(':first').hide();
        });
    }
}

function expandReq(obj, onload) {
    var height = obj.data("height");
    if (onload) {
        obj.removeClass("collapsed").addClass("expanded").css({
            height: "auto"
        }).children(".header").find(".title").css({
            height: "auto"
        }).find(".text:first").css({
            whiteSpace: "normal",
            overflow: "auto",
            textOverflow: "ellipsis"
        });
        obj.find(".header .title .text").not(':first').show();
    } else {
        obj.removeClass("collapsed").addClass("expanded").animate({
            height: height + "px"
        }, 300, function() {
            jQuery(this).children(".header").find(".title").css({
                height: "auto"
            }).find(".text:first").css({
                whiteSpace: "normal",
                overflow: "auto",
                textOverflow: "ellipsis"
            });
            jQuery(this).find(".header .title .text").not(':first').show();
        });
    }
}

function initAuditActions() {
    jQuery(".requirement").each(function () {
        jQuery(this).data("height", jQuery(this).height());
        if (jQuery(this).is(".Status_OK")) {
            collapseReq(jQuery(this), true);
        } else {
            expandReq(jQuery(this), true);
        }
    });
    jQuery(".requirement > .header > .toggle, .requirement > .header > .title").click(function (e) {
        var target = (e.target) ? e.target.nodeName.toLowerCase() : e.srcElement.nodeName.toLowerCase();
        if (target != "a") {
            var jRequirement = jQuery(this).parents(".requirement");
            if (jRequirement.hasClass("expanded") && !jRequirement.hasClass("collapsed")) {
                collapseReq(jRequirement, false);
            } else { // if (jRequirement.hasClass("collapsed")) {
                expandReq(jRequirement, false);
            }
        }
    });

    jQuery(".control-toolbar #requirement-status").change(function () {
        var data = jQuery(this).val();

        //jQuery(".requirement").each(function() {
        jQuery(".auditReport .requirement[class*='Status']").not(".Status_NONE").each(function () {

            //if (jQuery(this).hasClass(data) || data == 'all' || jQuery(this).hasClass("Status_NONE") || !jQuery(this).is("div[class*='Status']")) {
            if (data == 'unmet' && jQuery(this).hasClass("Status_OK")) {
                jQuery(this).hide();
            } else {
                jQuery(this).show();
            }
        });

        var jAuditMessage = jQuery(".alert.audit-filtered");
        if (data == "all") {
            jAuditMessage.hide();
        } else {
            jAuditMessage.show();
        }

        jQuery(".section").each(function () {
            var jSectionMessage = jQuery(this).find(".alert.all-reqs-filtered");
            if (jQuery(this).find(".requirement:visible").length > 0) {
                jSectionMessage.hide();
            } else {
                jSectionMessage.show();
            }
        });
    });
}

function validatePlanAudit(obj) {

    var id = "plan_hand_off_container";
    var getId = "plan_audit_hand_off";
    var retrieveData = {
        viewId: "PlanAuditHandOff-FormView",
        methodToCall: "reviewPlanAudit",
        pageId: "plan_audit_hand_off"
    };

    obj.addClass("disabled").attr("disabled", true);

    var retrieveForm = '<form id="retrieveForm" action="audit" method="post" />';
    jQuery("body").append(retrieveForm);

    var blockOptions = {
        centerX: false,
        centerY: false,
        message: '<img src="' + getConfigParam("ksapImageLocation") + 'loader/ajax_small.gif" style="vertical-align:middle; margin-right:10px;"/>Processing request',
        css: {
            top: '0px',
            left: '0px',
            width: '100%',
            height: '16px',
            border: 'none',
            backgroundColor: 'transparent',
            font: 'bold 14px museo-sans, Arial, Helvetica, Verdana, sans-serif',
            padding: '6px 5px',
            margin: '0'
        },
        overlayCSS: {
            backgroundColor: '#fcf9f0',
            opacity: 1,
            border: 'none',
            height: '32px'
        },
        fadeIn: 50,
        fadeOut: 50
    };

    var elementToBlock = jQuery("#plan_audit_actions_container");

    var successCallback = function (htmlContent) {
        setUrlHash('modified', 'true');
        var showHandOffScreen = (jQuery("input#showHandOffScreen_control", htmlContent).val() == "true");

        var component = jQuery("#" + getId, htmlContent);
        if (jQuery("#" + id).length) {
            if (jQuery("#" + getId).length == 0) {
                jQuery("#" + id).append(component);
            } else {
                jQuery("#" + getId).replaceWith(component);
            }
        }

        runHiddenScripts(getId);

        if (jQuery("input[data-role='script'][data-for='" + getId + "']", htmlContent).length > 0) {
            eval(jQuery("input[data-role='script'][data-for='" + getId + "']", htmlContent).val());
        }

        if (showHandOffScreen) {
            jQuery.fancybox({
                helpers: {
                    overlay: null
                },
                parent: "form:first",
                href: "#" + getId,
                beforeLoad: function () {
                    setFancyboxScrollableGroup(115);
                },
                onUpdate: function () {
                    setFancyboxScrollableGroup(115);
                },
                afterClose: function () {
                    var condition = function () {
                        return ((jQuery.cookie("pendingAudit") != null) || (coerceValue("planExists") == false) || (coerceValue("planAudit.campusParam") == "306" && coerceValue("planAudit.programParamSeattle") == "default") || (coerceValue("planAudit.campusParam") == "310" && coerceValue("planAudit.programParamBothell") == "default") || (coerceValue("planAudit.campusParam") == "323" && coerceValue("planAudit.programParamTacoma") == "default"));
                    };
                    disabledCheck(obj.attr("id"), "action", condition);
                }
            });
            elementToBlock.unblock();
        } else {
            jQuery("button#plan_audit_run").click();
            elementToBlock.unblock();
        }

    };

    ksapAjaxSubmitForm(retrieveData, successCallback, elementToBlock, "retrieveForm", blockOptions);
    jQuery("form#retrieveForm").remove();
}

function setFancyboxScrollableGroup(addPadding) {
    var innerHeight = 0;
    jQuery(".fancybox-inner .getHeight").each(function () {
        innerHeight = innerHeight + Math.round(parseFloat(jQuery(this).outerHeight(true))) + 1;
    });
    var maxHeight = jQuery(window).height() - (addPadding + innerHeight);
    jQuery(".fancybox-inner .auditHandOff__scrollGroup").css("max-height", maxHeight + "px");
}

function runPlanAudit(id) {
    if (jQuery("button#" + id).hasClass('disabled')) {
        jQuery("button#" + id).removeClass('disabled');
    }
    jQuery("button#" + id).click();
    jQuery.fancybox.close(true);
}

function indicateViewingAudit(id, type) {
    var open = false;
    var currentAudit = jQuery(".auditReport__html--" + type + " .myplan-audit-report");
    var currentAuditId = currentAudit.attr("auditid");

    jQuery("#" + id + " .module__item").not(".module__item--pending").each(function (index) {
        if (jQuery(this).attr("id").replace("link_", "") == currentAuditId && currentAudit.is(":visible")) {
            if (type == 'degree') {
                jQuery(this).find(".module__itemLabel label").html("Viewing");
            }
            if (type == 'plan') {
                if (index > 1) open = true;
                jQuery(this).addClass("viewing");
            }
        } else {
            if (type == 'degree') {
                jQuery(this).find(".module__itemLabel label").html("View");
            }
            if (type == 'plan') {
                jQuery(this).removeClass("viewing");
            }
        }
    });
    if (open) {
        jQuery("#plan_audit_toggle_link").click();
    }
}