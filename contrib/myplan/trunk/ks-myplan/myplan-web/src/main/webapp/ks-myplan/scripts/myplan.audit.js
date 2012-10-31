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
            if (jQuery(this).hasClass(sClass) || sClass == 'all' || jQuery(this).hasClass("Status_NONE")) {
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


/*
jQuery(document).ready(function(){
    //var count = 0;
    jQuery(".requirement .requirement-heading > div").each(function() {
    	var status = jQuery(this).attr("class");
    	switch (status) {
    		case "C":
                jQuery(this).parent(".requirement-heading").addClass("collapsed");
                jQuery(this).parent(".requirement-heading").next(".requirement-body").hide();
    			break;
    		default:
    			jQuery(this).parent(".requirement-heading").addClass("expanded");
    	}
    });
    jQuery(".requirement .requirement-heading").click(function(){
        if ( jQuery(this).hasClass("expanded") ) {
        		jQuery(this).removeClass("expanded").addClass("collapsed");
        		jQuery(this).next(".requirement-body").slideUp("slow");
		} else {
        		jQuery(this).removeClass("collapsed").addClass("expanded");
        		jQuery(this).next(".requirement-body").slideDown("slow");
        }
    });
    jQuery("#requirement_status").change(function() {
        var status = jQuery(this).val();
        jQuery(".requirement").each(function() {
			if ( status =='all' ) {
				jQuery(this).show();
			} else {
				jQuery(this).filter( "."+status ).show();
				jQuery(this).not( "."+status ).hide();
			}
        });
    });
    jQuery("a.jump").click(function(e) {
    	e.preventDefault();
    	var jump = jQuery(this).parents(".requirement-group").next(".requirement-group").offset();
    	if ( jump != null ) {
    		window.scrollTo(jump.left,jump.top);
    	} else {
    		window.scrollTo(0,0);
    	}
    	return false;
    });
    jQuery("a.subrequirement-courses-acceptable-link").click(function(e) {
    	e.preventDefault();
    	//var id = jQuery(this).attr("href");
    	//openPopUp(id+'_popup', id, 'audit', 'audit', {viewId:'DegreeAudit-FormView'}, event, null, {width:'400px'}, {tail:{align:'center', hidden: false}, position: 'bottom'});
    });
});
*/
/*
jQuery(document).ready(function(){
    jQuery(".myplan-audit-report .requirement > .reqText").each(function() {
        jQuery(this).find("br").remove();
    });
    jQuery(".myplan-audit-report .requirement > .status").each(function() {
        if ( jQuery(this).hasClass("statusOK") ) {
            jQuery(this).siblings(".toggler").removeClass("togglerExpanded").addClass("togglerCollapsed")
            jQuery(this).siblings(".reqBody").hide();
        }
    });
    jQuery(".myplan-audit-report .requirement > .reqText").click(function(){
        if ( jQuery(this).siblings(".toggler").hasClass("togglerExpanded") ) {
            jQuery(this).siblings(".toggler").removeClass("togglerExpanded").addClass("togglerCollapsed");
            jQuery(this).siblings(".reqBody").slideUp("400");
        } else {
            jQuery(this).siblings(".toggler").removeClass("togglerCollapsed").addClass("togglerExpanded");
            jQuery(this).siblings(".reqBody").slideDown("400");
        }
    });
    jQuery(".myplan-audit-report .requirement").each(function() {
        var content = jQuery.trim( jQuery(this).text() );
        if (content === '') {
            jQuery(this).remove();
        }
        jQuery(this).find(".status.statusNONE").hide();
        jQuery(this).find(".toggler").not(".togglerExpanded, .togglerCollapsed").hide();
    });
    jQuery(".myplan-audit-report .requirement > .reqBody").each(function() {
        var content = jQuery.trim( jQuery(this).text() );
        if (content === '') {
            jQuery(this).siblings(".toggler").removeClass("togglerExpanded");
            jQuery(this).siblings(".reqText").unbind("click").css("cursor","default");
            jQuery(this).remove();
        }
    });
});
    */