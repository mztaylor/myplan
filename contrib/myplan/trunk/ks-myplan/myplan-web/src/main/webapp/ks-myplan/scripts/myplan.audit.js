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