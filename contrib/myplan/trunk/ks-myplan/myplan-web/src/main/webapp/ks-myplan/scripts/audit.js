jq(document).ready(function(){
    jq(".requirement .requirement-heading").click(function(){
        jq(this).toggleClass("collapsed");
        jq(this).next(".requirement-body").slideToggle("slow");
    });
});