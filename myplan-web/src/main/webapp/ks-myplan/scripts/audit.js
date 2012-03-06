jq(document).ready(function(){
    jq(".requirement .requirement-heading > div").each(function() {
    	var status = jq(this).attr("class");
    	switch (status) {
    		case "complete":

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
});