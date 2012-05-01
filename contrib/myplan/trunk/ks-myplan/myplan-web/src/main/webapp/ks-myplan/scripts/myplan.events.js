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
					<span class="uif-field uif-link"><a id="' + planItemId + '_' + type + '" href="#" target="" title="' + courseTitle + '" class="uif-field uif-link">' + courseCode + ' (' + courseCredits + ')</a></span>\
					<input name="script" type="hidden" value="jq(\'#\' + \'' + planItemId + '_' + type + '\').click(function(e) { openPopUp(\'' + planItemId + '\',\'add_planned_course\',\'startAddPlannedCourseForm\',\'plan\',{viewId:\'PlannedCourseMenuItem-FormView\',planItemId:\'' + planItemId + '\'},e,\'.uif-collectionItem\',{width:\'150px\'},{tail:{align:\'top\'},align:\'top\',position:\'right\'},false); });">\
				</span>\
			</div>\
		</span>\
	';
    jq(item).prependTo("." + atpId + ".myplan-term-" + type + " .uif-stackedCollectionLayout > span").children(".uif-collectionItem").css({backgroundColor:"#ffffcc"}).hide().fadeIn(250).animate({backgroundColor:"#ffffff"}, 1500, function() {
        runHiddenScripts(planItemId + "_div");
    });

}
/*
#################################################################
    Function: remove course from quarter plan view
#################################################################
 */
function fnRemovePlanItem (atpId, type, planItemId) {
    jq("#" + planItemId).unbind('click');
    jq("." + atpId + ".myplan-term-" + type + " .uif-stackedCollectionLayout .uif-collectionItem #" + planItemId + "_" + type).parents(".uif-collectionItem").parent("span").fadeOut(250, function(){
        jq(this).remove();
    });
}
/*
#################################################################
    Function: remove course from saved courses full details list
#################################################################
 */
function fnRemoveSavedItem (planItemId, cssStyle) {
    jq("." + cssStyle + " #" + planItemId + "_span").parents("li").fadeOut(250, function(){
        jq(this).remove();
    });
}
/*
#################################################################
    Function: update the count of saved courses
#################################################################
 */
function fnUpdateSavedCount (savedItemCount) {
    jq(".myplan-saved-courses-detail .uif-headerField.uif-sectionHeaderField .uif-header strong").fadeOut(250, function() {
	    jq(this).html(savedItemCount - 1).fadeIn(250);
	});
}
/*
#################################################################
    Function: update the credits total in the quarter plan view
#################################################################
 */
function fnUpdateCredits (atpId, termCredits) {
    jq("." + atpId + ".myplan-term-planned .myplan-carousel-term-total span.myplan-carousel-term-credits").fadeOut(250, function() {
        jq(this).html(termCredits).fadeIn(250);
    });
}
/*
#################################################################
    Function: swap action button with feedback message
#################################################################
 */
function fnDisplayMessage (message, cssClass, targetId) {
    jq("#" + targetId).parent().closest('div').fadeOut(250, function() {
        jq(this).addClass("myplan-message-border " + cssClass).html(message).fadeIn(250);
    });
}