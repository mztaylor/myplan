/*
#################################################################
    Function: add course to quarter plan view
#################################################################
 */
function fnAddPlanItem (atpId, type, planItemId, courseCode, courseTitle, courseCredits) {
    var item = '\
		<span class="fieldLine boxLayoutVerticalItem clearfix">\
    		<div class="uif-collectionItem" id="' + planItemId + '_div">\
				<span class="fieldLine boxLayoutVerticalItem clearfix">\
					<span class="uif-field uif-link"><a id="' + planItemId + '" href="#" target="" title="' + courseTitle + '" class="uif-field uif-link">' + courseCode + ' (' + courseCredits + ')</a></span>\
					<input name="script" type="hidden" value="jq(\'#\' + \'' + planItemId + '\').click(function(e) { openPopUp(\'' + planItemId + '\',\'add_planned_course\',\'startAddPlannedCourseForm\',\'plan\',{viewId:\'PlannedCourseMenuItem-FormView\',planItemId:\'' + planItemId + '\'},e,null,{width:\'150px\'},{tail:{align:\'top\'},align:\'top\'},false); });">\
				</span>\
			</div>\
		</span>\
	';
    jq(item).prependTo("." + atpId + "-" + type + " .uif-stackedCollectionLayout > span").children(".uif-collectionItem").css({backgroundColor:"#ffffcc"}).hide().fadeIn(250).animate({backgroundColor:"#ffffff"}, 1500, function() {
        runHiddenScripts(planItemId + "_div");
    });

}
/*
#################################################################
    Function: remove course to quarter plan view
#################################################################
 */
function fnRemovePlanItem (atpId, type, planItemId) {
    jq("#" + planItemId).unbind('click');
    jq("." + atpId + "-" + type + " .uif-stackedCollectionLayout .uif-collectionItem #" + planItemId).parents(".uif-collectionItem").parent("span").fadeOut(250, function(){
        jq(this).remove();
    });
}
/*
#################################################################
    Function: update the credits total in the quarter plan view
#################################################################
 */
function fnUpdateCredits (atpId, termCredits) {
    jq("." + atpId + "-planned .myplan-carousel-term-total span.myplan-carousel-term-credits").fadeOut(250, function() {
        jq(this).html(termCredits).fadeIn(250);
    });
}
/*
#################################################################
    Function: swap action button with feedback message
#################################################################
 */
function fnDisplayMessage (message, targetId) {
    jq("#" + targetId).parent().closest('div').fadeOut(250, function() {
        jq(this).addClass("myplan-message-border myplan-message-success").html(message).fadeIn(250);
    });
}