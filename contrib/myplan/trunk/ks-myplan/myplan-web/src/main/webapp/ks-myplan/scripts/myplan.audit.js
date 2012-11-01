function collapseReq(obj, onload) {
    var height = 23;
    if (onload) {
        obj.removeClass("expanded").addClass("collapsed").css({
            height: height + "px"
        }).find(".header .title").css({
            whiteSpace: "nowrap",
            overflow: "hidden",
            height: height + "px"
        });
    } else {
        obj.removeClass("expanded").addClass("collapsed").animate({
            height: height + "px"
        }, 300, function() {
            jQuery(this).find(".header .title").css({
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
        }).find(".header .title").css({
            whiteSpace: "normal",
            overflow: "auto",
            height: "auto"
        });
    } else {
        obj.removeClass("collapsed").addClass("expanded").animate({
            height: height
        }, 300
        ).find(".header .title").css({
            whiteSpace: "normal",
            overflow: "auto",
            height: "auto"
        });
    }
}

function initAuditActions() {

    jQuery(".requirement").each(function() {
        jQuery(this).data("height", jQuery(this).height());
        if (jQuery(this).is(".Status_OK")) {
            collapseReq(jQuery(this), true);
        } else {
            expandReq(jQuery(this), true);
        }
    });
    jQuery(".requirement .toggle, .requirement .title").click(function(e) {
        var jRequirement = jQuery(this).parents(".requirement");
        if (jRequirement.hasClass("expanded")) {
            collapseReq(jRequirement, false);
        } else if (jRequirement.hasClass("collapsed")) {
            expandReq(jRequirement, false);
        }
    });
    jQuery(".control-toolbar #requirement-status").change(function() {
        var sClass = jQuery(this).val();
        jQuery(".requirement").each(function() {
            if (jQuery(this).hasClass(sClass) || sClass == 'all' || jQuery(this).hasClass("Status_NONE") || !jQuery(this).is("div[class*='Status']")) {
                jQuery(this).show();
            } else {
                jQuery(this).hide();
            }
        });

        var jAuditMessage = jQuery(".myplan-status.audit-filtered");
        if (sClass == "all") {
            jAuditMessage.hide();
        } else {
            jAuditMessage.show();
        }

        jQuery(".section").each(function(){
            var jSectionMessage = jQuery(this).find(".myplan-status.all-reqs-filtered");
            if (jQuery(this).find(".requirement:visible").length > 0) {
                jSectionMessage.hide();
            } else {
                jSectionMessage.show();
            }
        });
    });
}