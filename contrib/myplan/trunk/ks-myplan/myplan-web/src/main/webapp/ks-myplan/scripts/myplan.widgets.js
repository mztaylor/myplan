function stopEvent(e) {
    if(!e) var e = window.event;
    if (e.stopPropagation) {
        e.preventDefault();
        e.stopPropagation();
    } else {
        e.returnValue = false;
        e.cancelBubble = true;
    }
    return false;
}

function openCourse(courseId, e) {
    stopEvent(e);
    var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    if ( jq(target).parents(".jquerybubblepopup.jquerybubblepopup-myplan").length > 0 ) {
        window.location = "inquiry?methodToCall=start&viewId=CourseDetails-InquiryView&courseId="+courseId;
    } else {
    	openPlanItemPopUp(courseId,'add_remove_course_popover_page',{courseId:courseId},e,null,{tail:{align:'left'},align:'left',position:'bottom',alwaysVisible:'false'},true);
    }
}
/*
######################################################################################
    Function: Launch generic bubble popup
######################################################################################
 */
function openPopUp(id, getId, methodToCall, action, retrieveOptions, e, selector, popupStyles, popupOptions, close) {
    stopEvent(e);

    var popupHtml = jq('<div />').attr("id",id + "_popup");

    if (popupStyles) {
        jQuery.each(popupStyles, function(property, value) {
            jq(popupHtml).css(property, value);
        });
    }

	var popupOptionsDefault = {
		innerHtml: jq(popupHtml).clone().wrap('<div>').parent().html(),
		themePath: '../ks-myplan/jquery-bubblepopup/jquerybubblepopup-theme/',
		manageMouseEvents: true,
		selectable: true,
		tail: {align:'middle', hidden: false},
		position: 'left',
        align: 'center',
        alwaysVisible: false,
        themeMargins: {total:'20px', difference:'5px'}
	};

    var popupSettings = jQuery.extend(popupOptionsDefault, popupOptions);

    var popupBox;
    var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    if (selector === null) {
        var popupBox = jq(target).addClass("myplan-popup-target");
    } else {
        var popupBox = jq(target).parents(selector).addClass("myplan-popup-target");
    }

    fnCloseAllPopups();

	popupBox.CreateBubblePopup({manageMouseEvents: false});
    popupBox.ShowBubblePopup(popupSettings, false);
    var popupBoxId = popupBox.GetBubblePopupID();
	popupBox.FreezeBubblePopup();

    /*jq(document).mouseup(function(e) {
        if ( jq('#' + popupBoxId).has(e.target).length === 0 ){
            fnCloseAllPopups();
        }
    });*/

    var tempForm = jq('<form />').hide();
	jq(tempForm).attr("id", id + "_form").attr("action", action).attr("method", "post");
    jQuery.each(retrieveOptions, function(name, value) {
        jq(tempForm).append('<input type="hidden" name="' + name + '" value="' + value + '" />');
    });
    jq("body").append(tempForm);

    var elementToBlock = jq("#" + id  + "_popup");

	var updateRefreshableComponentCallback = function(htmlContent){
		var component = jq("#" + getId + "_div", htmlContent);
		elementToBlock.unblock({onUnblock: function(){
            if (jq("#" + id  + "_popup").length){
                popupBox.SetBubblePopupInnerHtml(component);
                if (close || typeof close === 'undefined') jq("#" + popupBoxId + " .jquerybubblepopup-innerHtml").prepend('<img src="../ks-myplan/images/btnClose.png" class="myplan-popup-close"/>');
                jq("#" + popupBoxId + " img.myplan-popup-close").click(function() {
                    fnCloseAllPopups();
                });
            }
            runHiddenScripts(getId + "_div");
			}
		});
	};

	myplanAjaxSubmitForm(methodToCall, updateRefreshableComponentCallback, {reqComponentId: id, skipViewInit: "false"}, elementToBlock, id);
    jq("form#"+ id + "_form").remove();
}
/*
######################################################################################
    Function: Launch plan item bubble popup
######################################################################################
 */
function openPlanItemPopUp(id, getId, retrieveOptions, e, selector, popupOptions, close) {
    stopEvent(e);

    var popupHtml = jq('<div />').attr("id",id + "_popup").css({
        width: "300px",
        height: "16px"
    });

	var popupOptionsDefault = {
		innerHtml: jq(popupHtml).clone().wrap('<div>').parent().html(),
		themePath: '../ks-myplan/jquery-bubblepopup/jquerybubblepopup-theme/',
		manageMouseEvents: true,
		selectable: true,
		tail: {align:'middle', hidden: false},
		position: 'left',
        align: 'center',
        alwaysVisible: false,
        themeMargins: {total:'20px', difference:'5px'}
	};

    var popupSettings = jQuery.extend(popupOptionsDefault, popupOptions);

    var popupBox;
    var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    if (selector === null) {
        var popupBox = jq(target).addClass("myplan-popup-target");
    } else {
        var popupBox = jq(target).parents(selector).addClass("myplan-popup-target");
    }

    fnCloseAllPopups();

	popupBox.CreateBubblePopup({manageMouseEvents: false});
    popupBox.ShowBubblePopup(popupSettings, false);
    var popupBoxId = popupBox.GetBubblePopupID();
    fnPositionPopUp(popupBoxId);
	popupBox.FreezeBubblePopup();

    /*
    jq(document).mouseup(function(e) {
        if ( jq('#' + popupBoxId).has(e.target).length === 0 ){
            fnCloseAllPopups();
        }
    });
    */

    var tempForm = jq('<form />').hide();
	jq(tempForm).attr("id", id + "_form").attr("action", "plan").attr("method", "post");
    jq(tempForm).append('<input type="hidden" name="viewId" value="PlannedCourse-FormView" />');
    jQuery.each(retrieveOptions, function(name, value) {
        jq(tempForm).append('<input type="hidden" name="' + name + '" value="' + value + '" />');
    });
    jq("body").append(tempForm);

    var elementToBlock = jq("#" + id  + "_popup");

	var updateRefreshableComponentCallback = function(htmlContent){
        var status = jq.trim( jq("#request_status_item_key", htmlContent).text().toLowerCase() );
        if ( status === 'error' ) {
            var sError = jq.trim( jq("#errorsFieldForPage_errorMessages ul li:first", htmlContent).text() );
            var oError = jq("<div />").attr("id","error_div").html(sError).addClass("myplan-message-border myplan-message-error");
            var component = jq("<div />").css("padding-right","24px").html(oError);
        } else {
            var component = jq("#" + getId + "_div", htmlContent);
            var planForm = jq('<form />').attr("id", id + "_form").attr("action", "plan").attr("method", "post");
        }
        elementToBlock.unblock({onUnblock: function(){
            if (jq("#" + id  + "_popup").length){
                popupBox.SetBubblePopupInnerHtml(component);
                fnPositionPopUp(popupBoxId);
                if ( status != 'error' ) jq(".jquerybubblepopup-innerHtml").wrapInner(planForm);
                if (close || typeof close === 'undefined') jq("#" + popupBoxId + " .jquerybubblepopup-innerHtml").prepend('<img src="../ks-myplan/images/btnClose.png" class="myplan-popup-close"/>');
                jq("#" + popupBoxId + " img.myplan-popup-close").click(function() {
                    popupBox.HideAllBubblePopups();
                    popupBox.RemoveBubblePopup();
                });
            }
            runHiddenScripts(getId + "_div");
        }});
	};

	myplanAjaxSubmitForm("startAddPlannedCourseForm", updateRefreshableComponentCallback, {reqComponentId: id, skipViewInit: "false"}, elementToBlock, id);
    jq("form#"+ id + "_form").remove();
}
function openDialog(sText, e) {
    stopEvent(e);

    var dialogHtml = jq('<div />').html(sText).css({
        width: "300px"
    });

	var popupOptionsDefault = {
		innerHtml: jq(dialogHtml).clone().wrap('<div>').parent().html(),
		themePath: '../ks-myplan/jquery-bubblepopup/jquerybubblepopup-theme/',
		manageMouseEvents: true,
		selectable: true,
		tail: {hidden: true},
		position: 'top',
        align: 'center',
        alwaysVisible: false,
        themeMargins: {total:'20px', difference:'5px'}
	};

    var popupBox = jq("body");

    fnCloseAllPopups();

	popupBox.CreateBubblePopup({manageMouseEvents: false});
    popupBox.ShowBubblePopup(popupOptionsDefault, false);
    var popupBoxId = popupBox.GetBubblePopupID();
	popupBox.FreezeBubblePopup();

    if (close || typeof close === 'undefined') jq("#" + popupBoxId + " .jquerybubblepopup-innerHtml").prepend('<img src="../ks-myplan/images/btnClose.png" class="myplan-popup-close"/>');

    fnPositionPopUp(popupBoxId);

    jq("html, #" + popupBoxId + " img.myplan-popup-close").click(function() {
		fnCloseAllPopups();
	});
    jq('#' + popupBoxId).click(function(event){
    	event.stopPropagation();
 	});
}

function fnPositionPopUp(popupBoxId) {
    if ( parseFloat(jq("#" + popupBoxId).css("top")) < 0 || parseFloat(jq("#" + popupBoxId).css("left")) < 0 ) {
        var iTop = ( jq(window).height() / 2 ) - ( jq("#" + popupBoxId).height() / 2 );
        var iLeft = ( jq(window).width() / 2 ) - ( jq("#" + popupBoxId).width() / 2 );
        jq("#" + popupBoxId).css({top: iTop + 'px', left: iLeft + 'px'});
    }
}
/*
######################################################################################
    Function: Submit
######################################################################################
 */
function myplanAjaxSubmitPlanItem(id, type, methodToCall, e, bDialog) {
    var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    var targetText = ( jq.trim( jq(target).text() ) != '') ? jq.trim( jq(target).text() ) : "Error";
    var elementToBlock = jq(target);
    jq('input[name="methodToCall"]').remove();
    jq('<input type="hidden" name="methodToCall" value="' + methodToCall + '" />').appendTo(jq("form#" + id + "_form"));
    jq('form#' + id + '_form input[name="' + type + '"]').remove();
    jq('<input type="hidden" name="' + type + '" value="' + id + '" />').appendTo(jq("form#" + id + "_form"));
    jq('form#' + id + '_form input[name="viewId"]').remove();
    jq('<input type="hidden" name="viewId" value="PlannedCourse-FormView" />').appendTo(jq("form#" + id + "_form"));
    var updateRefreshableComponentCallback = function(htmlContent){
        var status = jq.trim( jq("#request_status_item_key", htmlContent).text().toLowerCase() );
        elementToBlock.unblock();
        switch (status) {
            case 'success':
                var oMessage = { 'message' : jq.trim( jq("#errorsFieldForPage_infoMessages ul li:first", htmlContent).text() ), 'cssClass':'myplan-message-border myplan-message-success' };
                var json = jq.parseJSON( jq.trim( jq("#json_events_item_key", htmlContent).text().replace(/\\/g,"") ) );
                for (var key in json) {
                    if (json.hasOwnProperty(key)) {
                        eval('jq.publish("' + key + '", [' + JSON.stringify( jq.extend(json[key], oMessage) ) + ']);');
                    }
                }
                break;
            case 'error':
                var oMessage = { 'message' : jq.trim( jq("#errorsFieldForPage_errorMessages ul li:first", htmlContent).text() ), 'cssClass':'myplan-message-border myplan-message-error' };
                if (!bDialog) {
                    var sContent = jq("<div />").append(oMessage.message).addClass("myplan-message-noborder myplan-message-error").css({"background-color":"transparent","color":"#ff0606","border":"none"});
                    var sHtml = jq("<div />").append('<div class="uif-headerField uif-sectionHeaderField"><h3 class="uif-header">' + targetText + '</h3></div>').append(sContent);
                    openDialog(sHtml.html(), e);
                } else {
                    eval('jq.publish("ERROR", [' + JSON.stringify( oMessage ) + ']);');
                }
                break;
        }
    };
    var blockOptions = {
        message:'<img src="../ks-myplan/images/btnLoader.gif"/>',
        css:{
            width: '100%',
            border: 'none',
            backgroundColor: 'transparent'
        },
        overlayCSS:  {
            backgroundColor: '#fff',
            opacity: 0.6,
            padding: '0px 1px',
            margin: '0px -1px'
        }
    };
    myplanAjaxSubmitForm(methodToCall, updateRefreshableComponentCallback, {reqComponentId: id, skipViewInit: 'false'}, elementToBlock, id, blockOptions);
}
/*Function used for moving the plan Item from planned to backup*/
function myPlanAjaxPlanItemMove(id, type, methodToCall, e) {
    stopEvent(e);
    var tempForm = jq('<form />').hide();
    jq(tempForm).attr("id", id + "_form").attr("action", "plan").attr("method", "post");
    jq("body").append(tempForm);
    myplanAjaxSubmitPlanItem(id, type, methodToCall, e, false);
    fnCloseAllPopups();
    jq("form#"+ id + "_form").remove();
}
/*
######################################################################################
    Function: Retrieve component content through ajax
######################################################################################
 */
function myplanRetrieveComponent(id, getId, methodToCall, action, retrieveOptions, highlightId) {
    var tempForm = jq('<form />').hide();
	jq(tempForm).attr("id", id + "_form").attr("action", action).attr("method", "post");
    jQuery.each(retrieveOptions, function(name, value) {
        jq(tempForm).append('<input type="hidden" name="' + name + '" value="' + value + '" />');
    });
    jq("body").append(tempForm);

    var elementToBlock = jq("#" + id + "_group");

	var updateRefreshableComponentCallback = function(htmlContent){
		var component = jq("#" + getId + "_group", htmlContent);
		elementToBlock.unblock({onUnblock: function(){
				// replace component
				if(jq("#" + id + "_group").length){
					jq("#" + id + "_group").replaceWith(component);
				}

				runHiddenScripts(getId + "_group");

                if(highlightId) {
                	jq("[id^='" + highlightId + "']").parents('li').animate( {backgroundColor:"#ffffcc"}, 1 ).animate( {backgroundColor:"#ffffff"}, 3000 );
                }
		}});
	};

    if (!methodToCall) {
        methodToCall = "search";
    }

	myplanAjaxSubmitForm(methodToCall, updateRefreshableComponentCallback, {reqComponentId: id, skipViewInit: "false"}, elementToBlock, id);
    jq("form#"+ id + "_form").remove();
}
/*
######################################################################################
    Function:   KRAD's ajax submit function modified to allow submission of a form
                other then the kuali form
######################################################################################
 */
function myplanAjaxSubmitForm(methodToCall, successCallback, additionalData, elementToBlock, formId, elementBlockingSettings) {
	var data;
    // methodToCall checks
	if(methodToCall != null){
        data = {methodToCall: methodToCall, renderFullView: false};
	}
	else{
        var methodToCallInput = jq("input[name='methodToCall']");
        if(methodToCallInput.length > 0){
            methodToCall = jq("input[name='methodToCall']").val();
        }
        //check to see if methodToCall is still null
        if(methodToCall == null || methodToCall === ""){
            data = {renderFullView: false};
        }
        else{
            data = {methodToCall: methodToCall, renderFullView: false};
        }
	}
    // remove this since the methodToCall was passed in or extracted from the page, to avoid issues
    jq("input[name='methodToCall']").remove();

	if(additionalData != null){
		jq.extend(data, additionalData);
	}

    var viewState = jq(document).data("ViewState");
    if (!jq.isEmptyObject(viewState)) {
        var jsonViewState = jq.toJSON(viewState);

        // change double quotes to single because escaping causes problems on URL
        jsonViewState = jsonViewState.replace(/"/g, "'");
        jq.extend(data, {clientViewState: jsonViewState});
    }

	var submitOptions = {
			data: data,
			success: function(response){
				var tempDiv = document.createElement('div');
				tempDiv.innerHTML = response;
				var hasError = handleIncidentReport(response);
				if(!hasError){
                    var newServerErrors = jq("#errorsFieldForPage_div", tempDiv).clone();
					successCallback(tempDiv);
                    if(successCallback !== replacePage){
                        jq("#errorsFieldForPage_div").replaceWith(newServerErrors);
                        runHiddenScripts("errorsFieldForPage_div");
                    }
				}
				jq("#formComplete").html("");
			},
            error: function(jqXHR, textStatus) {
                alert( "Request failed: " + textStatus );
            }
	};

	if(elementToBlock != null && elementToBlock.length){
		var elementBlockingOptions = {
				beforeSend: function() {
					if(elementToBlock.hasClass("unrendered")){
						elementToBlock.append('<img src="' + getConfigParam("kradImageLocation") + 'loader.gif" alt="Loading..." /> Loading...');
						elementToBlock.show();
					}
					else{
						var elementBlockingDefaults = {
			                message: '<img src="../ks-myplan/images/ajaxLoader.gif" alt="loading..." />',
			                fadeIn:  0,
			                fadeOut:  0,
			                overlayCSS:  {
                                backgroundColor: '#fff',
                                opacity: 0
                            },
			                css: {
                                border: 'none',
                                width: '16px',
                                top: '0px',
                                left: '0px'
    						}
			            };
                        elementToBlock.block(jq.extend(elementBlockingDefaults, elementBlockingSettings));
					}
				},
				complete: function(){
					elementToBlock.unblock();
				},
				error: function(){
					if(elementToBlock.hasClass("unrendered")){
						elementToBlock.hide();
					}
					else{
						elementToBlock.unblock();
					}
				}
		};
	}
	jq.extend(submitOptions, elementBlockingOptions);
	if (formId) {
        var form = jq("#" + formId + "_form");
    } else {
        var form = jq("#kualiForm");
    }
	form.ajaxSubmit(submitOptions);
}
/*
######################################################################################
    Function:   Truncate (ellipse) a single horizontally aligned item so all items
                fit on one line.
######################################################################################
 */
function truncateField(id) {
    jq("[id^='" + id + "']").each(function() {
        jq(this).css("display","block");
        var margin = Math.ceil(parseFloat(jq(this).find("span.boxLayoutHorizontalItem span").css("margin-right")));
        var fixed = 0;
        var fields = jq(this).find("span.boxLayoutHorizontalItem span").not(".myplan-text-ellipsis").length;
        jq(this).find("span.boxLayoutHorizontalItem span").not(".myplan-text-ellipsis").each(function() {
        	fixed = fixed + jq(this).width();
        });
        var ellipsis = jq(this).width() - ( ( fixed + 1 ) + ( margin * fields ) );
        jq(this).find("span.boxLayoutHorizontalItem span").last().css("margin-right", 0);
        jq(this).find("span.boxLayoutHorizontalItem span.myplan-text-ellipsis").width(ellipsis);
    });
}
/*
######################################################################################
    Function:   Slide into view hidden horizontally aligned items specifying the id
                of the item being brought into view.
######################################################################################
 */
function fnPopoverSlider(showId, parentId, direction) {
    var newDirection;
    if (direction === 'left') {
        newDirection = 'right';
    } else {
        newDirection = 'left';
    }
    jq("#" + parentId + "_group > .uif-horizontalBoxLayout > .boxLayoutHorizontalItem > div:visible").hide("slide", {
        direction: direction
    }, 100, function() {
        jq("#" + parentId + "_group > .uif-horizontalBoxLayout > .boxLayoutHorizontalItem > div").filter("#" + showId + "_div").show("slide", {
            direction: newDirection
        }, 100, function() {});
    });
}
/*
######################################################################################
    Function:   Close all bubble popups
######################################################################################
 */
function fnCloseAllPopups() {
    jq("div.jquerybubblepopup.jquerybubblepopup-myplan").remove();
    jq("*").each(function() {
        if ( jq(this).HasBubblePopup() ) {
            jq(this).HideAllBubblePopups();
            jq(this).RemoveBubblePopup();
        }
    });
}
/*
######################################################################################
    Function:   Build Term Plan View heading
######################################################################################
 */
function fnBuildTitle(aView, termSelector, headerSelector) {
    var sText = 'Academic Year';
    var sFirst = jq.trim ( jq(aView[0]).find("." + termSelector).text() );
    var sLast = jq.trim ( jq(aView[aView.length-1]).find("." + termSelector).text() );
    jq("#" + headerSelector + " .myplan-plan-header").html(sText + ' ' + sFirst.substr(-4) + '-' + sLast.substr(-4));
}
/*
######################################################################################
    Function:   expand/collapse backup course set within plan view
######################################################################################
 */
function fnToggleBackup(e) {
    stopEvent(e);
    var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    if (!jq(target).hasClass("disabled")) {
        var oBackup = jq(target).parents(".myplan-term-backup").find(".uif-stackedCollectionLayout");
        var oQuarter = jq(target).parents("li");
        var iSpeed = 500;
        var iDefault = 26;
        if (jq(target).hasClass("expanded")) {
            var iAdjust = ( oBackup.height() - ( iDefault * 2 ) ) * -1;
            jq(target).removeClass("expanded");
            jq(target).find("span").html("Show");
        } else {
            var iAdjust = ( oBackup.find("span a").size() * iDefault ) - oBackup.height();
            jq(target).addClass("expanded");
            jq(target).find("span").html("Hide");
        }
        oBackup.animate({"height": oBackup.height() + iAdjust}, {duration: iSpeed});
        oQuarter.animate({"height": oQuarter.height() + iAdjust}, {duration: iSpeed});
    }
}



























function addSavedCourse(id, methodToCall, action, retrieveOptions, e) {
    var targetId = (e.currentTarget) ? e.currentTarget.id : e.srcElement.id;
    var tempForm = jq('<form />').hide();
	jq(tempForm).attr("id", id + "_form").attr("action", action).attr("method", "post");
	jQuery.each(retrieveOptions, function(name, value) {
        jq(tempForm).append('<input type="hidden" name="' + name + '" value="' + value + '" />');
    });
    jq("body").append(tempForm);

    var elementToBlock = jq("#" + id + "_form");

	var updateRefreshableComponentCallback = function(htmlContent){
		var component = jq("#add_plan_item_key", htmlContent);
        var courseId =  jq.trim( jq( jq("#course_id_item_key", htmlContent) ).text() );
		var addedId = jq.trim( jq(component).text() );
        if (addedId.length > 0) {
			elementToBlock.unblock();
            jq("#" + targetId).parent().fadeOut(250, function() {
                jq("#" + targetId).hide();
                if ( jq('#viewId').val() === "CourseSearch-FormView" ) {
                    var tempDiv = jq('<div />').attr("id",courseId+"_saved").addClass("fl-text-green").html("Saved");
                } else {
                    var tempDiv = jq('<div />').attr("id",courseId+"_saved").addClass("myplan-message-border myplan-message-success").html("Course successfully added");
                }
                jq("#" + targetId).parent().append(tempDiv);
                jq(this).fadeIn(250);
			});
            myplanRetrieveComponent('saved_courses_summary','saved_courses_summary','search','lookup',{viewId:'SavedCoursesSummary-LookupView'},addedId);
		}
	};

    myplanAjaxSubmitForm(methodToCall, updateRefreshableComponentCallback, {reqComponentId: id, skipViewInit: "false"}, elementToBlock, id);
    jq("form#" + id + "_form").remove();
}

function removeSavedCourse(id, methodToCall, action, retrieveOptions, courseCode, e) {
    var targetId = (e.currentTarget) ? e.currentTarget.id : e.srcElement.id;
    var dialogConfirm = jq('<div />');
	jq(dialogConfirm).attr("id","dialog-confirm").attr("title","Delete Course");
	jq(dialogConfirm).html('<p>Are you sure that you want to delete <strong>' + courseCode + '</strong>?</p><p>Once deleted, you cannot undo.</p>');
	jq(dialogConfirm).dialog({
		resizable: false,
		height: 160,
		width: 350,
		modal: false,
		buttons: {
			"Yes": function () {
				var tempForm = jq('<form />').hide();
                jq(tempForm).attr("id", id + "_form").attr("action", action).attr("method", "post");
                jQuery.each(retrieveOptions, function(name, value) {
                    jq(tempForm).append('<input type="hidden" name="' + name + '" value="' + value + '" />');
                });
                jq("body").append(tempForm);

				var elementToBlock = jq("#" + id + "_form");

				var updateRefreshableComponentCallback = function(htmlContent){
                    var component = jq("[id='remove_plan_item_key']", htmlContent);
					var removedId = jq.trim( jq(component).text() );
        			if (removedId.length > 0) {
						elementToBlock.unblock();
						if ( jq('#viewId').val() === "SavedCoursesDetail-LookupView" ) {
							var planListItem = jq("#" + targetId).parents("li").children();
							jq(planListItem).fadeOut(250, function() {
								jq(this).parent().html('<div class="myplan-message-noborder myplan-action-success fl-text-green fl-text-bold fl-font-size-120"><span class="fl-text-black">' + courseCode + '</span> has been deleted successfully!</div>').fadeIn(250).delay(2000).fadeOut(250);
							});
							var planItemCount = jq(".myplan-saved-courses-detail .uif-headerField.uif-sectionHeaderField .uif-header strong");
							jq(planItemCount).fadeOut(250, function() {
								jq(this).html(planItemCount.text() - 1).fadeIn(250);
							});
						} else {
                            switch ( jq('#viewId').val() ) {
                                case "CourseSearch-FormView":
								    if ( jq('#course_search_results_datatable').length ) {
                                        var oTable = jq('#course_search_results_datatable').dataTable();
                                        var nodes = oTable.fnGetNodes();
                                        if ( jq(nodes).find("#" + removedId + "_span").length ) {
                                            jq(nodes).find("#" + removedId + "_span").fadeOut(250, function() {
                                                if ( jq.trim( jq(this).text() ) === "In List" ) {
                                                    jq(this).parent().attr("id", removedId + "_cell");
                                                    jq(this).html("<input type=\"image\" id=\"" + removedId + "\" src=\"/student/ks-myplan/images/btnAdd.png\" alt=\"Save to Your Courses List\" class=\"uif-field uif-imageField\" />");
                                                    jq(this).after("<input name=\"script\" type=\"hidden\" value=\"jq('#' + '" + removedId + "').click(function(e){e.preventDefault();writeHiddenToForm('actionParameters[showHistory]','false');writeHiddenToForm('actionParameters[selectedCollectionPath]','courseSearchResults');writeHiddenToForm('actionParameters[showHome]','false');writeHiddenToForm('showHistory','false');writeHiddenToForm('showHome','false');writeHiddenToForm('focusId','" + removedId + "');writeHiddenToForm('jumpToId','" + removedId + "');addSavedCourse('" + removedId + "','addSavedCourse','plan',{viewId:'SavedCoursesListActions-FormView',courseId:'" + removedId + "'},e);});\" script=\"first_run\">");
                                                    runHiddenScripts(removedId + "_cell");
                                                } else {
                                                    jq(this).find("#" + removedId + "_saved").remove();
                                                    jq(this).find("#" + removedId).show();
                                                }
                                                jq(this).fadeIn(250);
                                            });
                                        }
                                    }
                                    break;
                                case "CourseDetails-InquiryView":
                                    if ( jq("#" + removedId + "_div").length ) {
                                        jq("#" + removedId + "_div").parent().fadeOut(250, function() {
                                            jq("#" + removedId + "_div").replaceWith("<button id=\"" + removedId + "\" class=\"uif-field uif-action myplan-button myplan-button-gray\" onClick=\"event.preventDefault();writeHiddenToForm('actionParameters[showHistory]','false');writeHiddenToForm('actionParameters[showHome]','false');writeHiddenToForm('showHistory','false');writeHiddenToForm('showHome','false');writeHiddenToForm('focusId','" + removedId + "');writeHiddenToForm('jumpToId','" + removedId + "');addSavedCourse('" + removedId + "','addSavedCourse','plan',{viewId:'SavedCoursesListActions-FormView',courseId:'" + removedId + "'},event);\">Save to Your Courses List</button>");
                                            jq(this).fadeIn(250);
                                        });
                                    }
                                    break;
                            }
                            jq("#" + targetId).parent().fadeOut(250, function() {
								jq(this).addClass("myplan-message-border myplan-message-success").html("Course successfully removed").fadeIn(250);
							});
                            myplanRetrieveComponent('saved_courses_summary','saved_courses_summary','search','lookup',{viewId:'SavedCoursesSummary-LookupView'});
						}
					}
				};

                if (!methodToCall) {
                    methodToCall = "removeItem";
                }

				myplanAjaxSubmitForm(methodToCall, updateRefreshableComponentCallback, {reqComponentId: id, skipViewInit: "false"}, elementToBlock, id);
				jq("form#" + id + "_form").remove();
				jq(this).dialog("close");
			},
			"No": function () {
				jq(this).dialog("close");
			}
		}
	});
}