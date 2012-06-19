/*
jq(document).ready(function(){
    //var count = 0;
    jq(".requirement .requirement-heading > div").each(function() {
    	var status = jq(this).attr("class");
    	switch (status) {
    		case "C":
                jq(this).parent(".requirement-heading").addClass("collapsed");
                jq(this).parent(".requirement-heading").next(".requirement-body").hide();
    			break;
    		default:
    			jq(this).parent(".requirement-heading").addClass("expanded");
    	}
    });
    jq(".requirement .requirement-heading").click(function(){
        if ( jq(this).hasClass("expanded") ) {
        		jq(this).removeClass("expanded").addClass("collapsed");
        		jq(this).next(".requirement-body").slideUp("slow");
		} else {
        		jq(this).removeClass("collapsed").addClass("expanded");
        		jq(this).next(".requirement-body").slideDown("slow");
        }
    });
    jq("#requirement_status").change(function() {
        var status = jq(this).val();
        jq(".requirement").each(function() {
			if ( status =='all' ) {
				jq(this).show();
			} else {
				jq(this).filter( "."+status ).show();
				jq(this).not( "."+status ).hide();
			}
        });
    });
    jq("a.jump").click(function(e) {
    	e.preventDefault();
    	var jump = jq(this).parents(".requirement-group").next(".requirement-group").offset();
    	if ( jump != null ) {
    		window.scrollTo(jump.left,jump.top);
    	} else {
    		window.scrollTo(0,0);
    	}
    	return false;
    });
    jq("a.subrequirement-courses-acceptable-link").click(function(e) {
    	e.preventDefault();
    	//var id = jq(this).attr("href");
    	//openPopUp(id+'_popup', id, 'audit', 'audit', {viewId:'DegreeAudit-FormView'}, event, null, {width:'400px'}, {tail:{align:'center', hidden: false}, position: 'bottom'});
    });
});
*/

jq(document).ready(function(){
    jq(".myplan-audit-report .requirement > .reqText").each(function() {
        jq(this).find("br").remove();
    });
    jq(".myplan-audit-report .requirement > .status").each(function() {
        if ( jq(this).hasClass("statusOK") ) {
            jq(this).siblings(".toggler").removeClass("togglerExpanded").addClass("togglerCollapsed")
            jq(this).siblings(".reqBody").hide();
        }
    });
    jq(".myplan-audit-report .requirement > .reqText").click(function(){
        if ( jq(this).siblings(".toggler").hasClass("togglerExpanded") ) {
            jq(this).siblings(".toggler").removeClass("togglerExpanded").addClass("togglerCollapsed");
            jq(this).siblings(".reqBody").slideUp("400");
        } else {
            jq(this).siblings(".toggler").removeClass("togglerCollapsed").addClass("togglerExpanded");
            jq(this).siblings(".reqBody").slideDown("400");
        }
    });
    jq(".myplan-audit-report .requirement").each(function() {
        var content = jq.trim( jq(this).text() );
        if (content === '') {
            jq(this).remove();
        }
        jq(this).find(".status.statusNONE").hide();
        jq(this).find(".toggler").not(".togglerExpanded, .togglerCollapsed").hide();
    });
    jq(".myplan-audit-report .requirement > .reqBody").each(function() {
        var content = jq.trim( jq(this).text() );
        if (content === '') {
            jq(this).siblings(".toggler").removeClass("togglerExpanded");
            jq(this).siblings(".reqText").unbind("click").css("cursor","default");
            jq(this).remove();
        }
    });
});