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

var pendingPlanAuditHeadingText = 'We are currently auditing your plan for \'<span class="programName"></span>\'.';
var pendingDegreeAuditHeadingText = 'We are currently auditing your degree for \'<span class="programName"></span>\'.';

var blockPendingAuditStyle = {
    message: '<img src="../ks-myplan/images/ajaxAuditRunning32.gif" alt="" class="icon"/><div class="heading"></div>',
    fadeIn: 250,
    fadeOut: 250,
    css: {
        padding: '30px 30px 30px 82px',
        margin: '30px',
        width: 'auto',
        textAlign: 'left',
        border: 'solid 1px #ffd14c',
        backgroundColor: '#fffdd7',
        'border-radius': '15px',
        '-webkit-border-radius': '15px',
        '-moz-border-radius': '15px'
    },
    overlayCSS: {
        backgroundColor: '#fff',
        opacity: 0.85,
        border: 'none',
        cursor: 'wait'
    }
};

var replaceBlockPendingAudit;

function changeLoadingMessage(selector, programName, auditType) {
    replaceBlockPendingAudit = setInterval(function () {
        setLoadingMessage(selector, programName, auditType)
    }, 100);
}

function setLoadingMessage(selector, programName, auditType) {
    if (jQuery(selector + ' div.blockUI.blockMsg.blockElement').length > 0) {
        fnAddLoadingText(selector, programName, auditType);
    }
}

function fnAddLoadingText(selector, programName, auditType) {
    clearInterval(replaceBlockPendingAudit);
    jQuery(selector + " div.blockUI.blockOverlay").css(blockPendingAuditStyle.overlayCSS);
    jQuery(selector + " div.blockUI.blockMsg.blockElement").html(blockPendingAuditStyle.message).css(blockPendingAuditStyle.css).data("growl", "false");
    if (auditType == "plan") {
        jQuery(selector + " div.blockUI.blockMsg.blockElement .heading").html(pendingPlanAuditHeadingText);
    } else {
        jQuery(selector + " div.blockUI.blockMsg.blockElement .heading").html(pendingDegreeAuditHeadingText);
    }
    jQuery(selector + " div.blockUI.blockMsg.blockElement .programName").text(programName);
}

function removeCookie() {
    jQuery.cookie("myplan_audit_running", null, {expires: new Date().setTime(0)});
}

function setPendingAudit(obj, minutes) {
    if (jQuery.cookie("myplan_audit_running") == null) {
        var data = {};
        data.expires = new Date();
        data.expires.setTime(data.expires.getTime() + (minutes * 60 * 1000));
        data.programId = getAuditProgram('id', obj.data("audittype"));
        data.programName = getAuditProgram('name', obj.data("audittype"));
        data.recentAuditId = obj.data("recentauditid");
        data.auditType = obj.data("audittype");
        if (typeof data.recentAuditId === 'undefined') data.recentAuditId = '';

        if (data.programId != 'default') {
            changeLoadingMessage('.myplan-audit-report', data.programName, data.auditType);
            jQuery.ajax({
                url: "/student/myplan/audit/status",
                data: {"programId": data.programId, "auditId": data.recentAuditId},
                dataType: "json",
                beforeSend: null,
                success: function (response) {
                    if (response.status == "PENDING") {
                        jQuery.cookie('myplan_audit_running', JSON.stringify(data), {expires: data.expires});
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

function getPendingAudit(id, type) {
    if (jQuery.cookie('myplan_audit_running')) {
        var data = jQuery.parseJSON(decodeURIComponent(jQuery.cookie('myplan_audit_running')));
        if (type == data.auditType) {
            var component = jQuery("#" + id + " .uif-stackedCollectionLayout");
            if (data) {
                var item = jQuery("<div />").addClass("module__item module__item--pending").html('<img src="../ks-myplan/images/ajaxPending16.gif" class="icon"/><span>Auditing</span> ' + data.programName);
                component.prepend(item);
                pollPendingAudit(data.programId, data.recentAuditId, data.auditType);
            }
            if (component.prev(".ksap-emptyCollection").length > 0) {
                component.prev(".ksap-emptyCollection").remove();
            }
        }
    }
}

function blockPendingAudit(data) {
    var id = "audit_section";
    if (data.auditType == "plan") id = "plan_audit_section";
    var elementToBlock = jQuery("#" + id);
    elementToBlock.block(blockPendingAuditStyle);
    jQuery("#" + id + " div.blockUI.blockMsg.blockElement").data("growl", "true");
    if (data.auditType == "plan") {
        jQuery("#" + id + " div.blockUI.blockMsg.blockElement .heading").html(pendingPlanAuditHeadingText);
    } else {
        jQuery("#" + id + " div.blockUI.blockMsg.blockElement .heading").html(pendingDegreeAuditHeadingText);
    }
    jQuery("#" + id + " div.blockUI.blockMsg.blockElement .programName").text(data.programName);
    jQuery("#" + id).on('AUDIT_COMPLETE', function (event, data) {
        window.location.assign(window.location.href.split("#")[0]);
    });
}

function pollPendingAudit(programId, recentAuditId, auditType) {
    jQuery.ajaxPollSettings.pollingType = "interval";
    jQuery.ajaxPollSettings.interval = 250; // polling interval in milliseconds

    jQuery.ajaxPoll({
        url: "/student/myplan/audit/status",
        data: {"programId": programId, "auditId": recentAuditId},
        dataType: "json",
        beforeSend: null,
        successCondition: function (response) {
            return (response.status == 'DONE' || response.status == 'FAILED' || jQuery.cookie("myplan_audit_running") == null);
        },
        success: function (response) {
            var growl = true;
            if (readUrlParam("viewId") == "DegreeAudit-FormView") {
                growl = jQuery(".myplan-audit-report div.blockUI.blockMsg.blockElement").data("growl");
                if (readUrlParam(auditType + "Audit.auditId") != false) jQuery("body").on('AUDIT_COMPLETE', function (event, data) {
                    setUrlParam(auditType + "Audit.auditId", "");
                });
            }
            var title = "Degree Audit";
            if (auditType == "plan") {
                title = "Plan Audit";
            }
            if (jQuery.cookie("myplan_audit_running") == null || response.status == 'FAILED') {
                if (growl) showGrowl("Your " + title + " was unable to complete.", title + " Error", "errorGrowl");
            } else {
                var data = jQuery.parseJSON(decodeURIComponent(jQuery.cookie("myplan_audit_running")));
                if (growl) showGrowl(data.programName + " " + title + " is ready to view.", title + " Completed", "infoGrowl");
            }
            jQuery.cookie("myplan_audit_running", null, {expires: new Date().setTime(0)});
            jQuery.event.trigger("AUDIT_COMPLETE", {"auditType": auditType});
        }
    });
}

function collapseReq(obj, onload) {
    var height = 23;
    if (onload) {
        obj.removeClass("expanded").addClass("collapsed").css({
            height: height + "px"
        }).children(".header").find(".title").css({
                whiteSpace: "nowrap",
                overflow: "hidden",
                height: height + "px"
            });
    } else {
        obj.removeClass("expanded").addClass("collapsed").animate({
            height: height + "px"
        }, 300, function () {
            jQuery(this).children(".header").find(".title").css({
                whiteSpace: "nowrap",
                overflow: "hidden",
                height: height + "px"
            });
        });
    }
}

function expandReq(obj, onload) {
    var height = obj.data("height");
    if (onload) {
        obj.removeClass("collapsed").addClass("expanded").css({
            height: "auto"
        }).children(".header").find(".title").css({
                whiteSpace: "normal",
                overflow: "auto",
                height: "auto"
            });
    } else {
        obj.removeClass("collapsed").addClass("expanded").animate({
                height: height
            }, 300
        ).children(".header").find(".title").css({
                whiteSpace: "normal",
                overflow: "auto",
                height: "auto"
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
        jQuery(".myplan-audit-report .requirement[class*='Status']").not(".Status_NONE").each(function () {

            //if (jQuery(this).hasClass(data) || data == 'all' || jQuery(this).hasClass("Status_NONE") || !jQuery(this).is("div[class*='Status']")) {
            if (data == 'unmet' && jQuery(this).hasClass("Status_OK")) {
                jQuery(this).hide();
            } else {
                jQuery(this).show();
            }
        });

        var jAuditMessage = jQuery(".myplan-status.audit-filtered");
        if (data == "all") {
            jAuditMessage.hide();
        } else {
            jAuditMessage.show();
        }

        jQuery(".section").each(function () {
            var jSectionMessage = jQuery(this).find(".myplan-status.all-reqs-filtered");
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
        message: '<img src="../ks-myplan/images/btnLoader.gif" style="vertical-align:middle; margin-right:10px;"/>Processing request',
        css: {
            width: '100%',
            border: 'none',
            backgroundColor: 'transparent',
            font: 'bold 14px museo-sans, Arial, Helvetica, Verdana, sans-serif',
            padding: '5px'
        },
        overlayCSS: {
            backgroundColor: '#fcf9f0',
            opacity: 1,
            border: 'solid 1px #fcf9f0'
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
                        return ((jQuery.cookie("myplan_audit_running") != null) || (coerceValue("planExists") == false) || (coerceValue("planAudit.campusParam") == "306" && coerceValue("planAudit.programParamSeattle") == "default") || (coerceValue("planAudit.campusParam") == "310" && coerceValue("planAudit.programParamBothell") == "default") || (coerceValue("planAudit.campusParam") == "323" && coerceValue("planAudit.programParamTacoma") == "default"));
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

    ksapAjaxSubmitForm(retrieveData, successCallback, elementToBlock, "retrieveForm");
    jQuery("form#retrieveForm").remove();
}

function setFancyboxScrollableGroup(addPadding) {
    var innerHeight = 0;
    jQuery(".fancybox-inner .ksap-getHeight").each(function () {
        innerHeight = innerHeight + Math.round(parseFloat(jQuery(this).outerHeight(true))) + 1;
    });
    var maxHeight = jQuery(window).height() - (addPadding + innerHeight);
    jQuery(".fancybox-inner .ksap-scrollableGroup").css("max-height", maxHeight + "px");
}

function runPlanAudit(id) {
    if (jQuery("button#" + id).hasClass('disabled')) {
        jQuery("button#" + id).removeClass('disabled');
    }
    jQuery("button#" + id).click();
    jQuery.fancybox.close(true);
}