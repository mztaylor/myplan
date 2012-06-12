jq(document).ready(function(){
    //var count = 0;
    jq(".requirement .requirement-heading > div").each(function() {
    	/*
    	if (count == 3) {
    		jq(this).attr("class","C");
    	}
    	++count;
    	*/
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