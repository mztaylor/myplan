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

function openCourse(courseId, e, enrolled, quarter, credits) {
    stopEvent(e);
    var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    if ( jq(target).parents("#course_details_popup_requisites").length > 0 ) {
        window.location = "inquiry?methodToCall=start&viewId=CourseDetails-InquiryView&courseId="+courseId;
    } else {
    	openPlanItemPopUp(courseId,'add_remove_course_popover_page',{courseId:courseId},e,null,{tail:{align:'top'},align:'top',position:'right',alwaysVisible:'false'},true);
        /*
        if ( enrolled ) {
            //var messaging = "You are currently enrolled in this course.";
            openPlanItemPopUp(courseId,'add_remove_course_popover_page',{courseId:courseId},e,null,{tail:{align:'top'},align:'top'},true);
        } else if (quarter != null && credits != null)  {
            //var messaging = "You already took this course on "+quarter+" and received "+credits;
            openPlanItemPopUp(courseId,'add_remove_course_popover_page',{courseId:courseId},e,null,{tail:{align:'top'},align:'top'},true);
        } else {
            openPlanItemPopUp(courseId,'add_remove_course_popover_page',{courseId:courseId},e,null,{tail:{align:'top'},align:'top'},true);
        }
        */
    }
}
/*
######################################################################################
    Function: Launch generic bubble popup
######################################################################################
 */
function openPopUp(id, getId, methodToCall, action, retrieveOptions, e, selector, popupStyles, popupOptions, close, messaging) {
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

    jq(".myplan-popup-target").each(function() {
        jq(this).HideAllBubblePopups();
        jq(this).RemoveBubblePopup();
    });

	popupBox.CreateBubblePopup({manageMouseEvents: false});
    popupBox.ShowBubblePopup(popupSettings, false);
    var popupBoxId = popupBox.GetBubblePopupID();
    //jq("#" + popupBoxId).css({top:'0px', left:'0px'});
	popupBox.FreezeBubblePopup();

    jq("html").click(function() {
		popupBox.HideAllBubblePopups();
		popupBox.RemoveBubblePopup();
	});
    jq('#' + popupBoxId).click(function(event){
    	event.stopPropagation();
 	});

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
                    popupBox.HideAllBubblePopups();
                    popupBox.RemoveBubblePopup();
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

    jq(".myplan-popup-target").each(function() {
        jq(this).HideAllBubblePopups();
        jq(this).RemoveBubblePopup();
    });

	popupBox.CreateBubblePopup({manageMouseEvents: false});
    popupBox.ShowBubblePopup(popupSettings, false);
    var popupBoxId = popupBox.GetBubblePopupID();
    fnPositionPopUp(popupBoxId);
	popupBox.FreezeBubblePopup();

    jq("html").click(function() {
		popupBox.HideAllBubblePopups();
		popupBox.RemoveBubblePopup();
	});
    jq('#' + popupBoxId).click(function(event){
    	event.stopPropagation();
 	});

    var tempForm = jq('<form />').hide();
	jq(tempForm).attr("id", id + "_form").attr("action", "plan").attr("method", "post");
    jq(tempForm).append('<input type="hidden" name="viewId" value="PlannedCourse-FormView" />');
    jQuery.each(retrieveOptions, function(name, value) {
        jq(tempForm).append('<input type="hidden" name="' + name + '" value="' + value + '" />');
    });
    jq("body").append(tempForm);

    var elementToBlock = jq("#" + id  + "_popup");

	var updateRefreshableComponentCallback = function(htmlContent){
		var component = jq("#" + getId + "_div", htmlContent);
        var planForm = jq('<form />').attr("id", id + "_form").attr("action", "plan").attr("method", "post");
        elementToBlock.unblock({onUnblock: function(){
            if (jq("#" + id  + "_popup").length){
                popupBox.SetBubblePopupInnerHtml(component);
                fnPositionPopUp(popupBoxId);
                jq(".jquerybubblepopup-innerHtml").wrapInner(planForm);
                if (close || typeof close === 'undefined') jq("#" + popupBoxId + " .jquerybubblepopup-innerHtml").prepend('<img src="../ks-myplan/images/btnClose.png" class="myplan-popup-close"/>');
                jq("#" + popupBoxId + " img.myplan-popup-close").click(function() {
                    popupBox.HideAllBubblePopups();
                    popupBox.RemoveBubblePopup();
                });
            }
            runHiddenScripts(getId + "_div");
			}
		});
	};

	myplanAjaxSubmitForm("startAddPlannedCourseForm", updateRefreshableComponentCallback, {reqComponentId: id, skipViewInit: "false"}, elementToBlock, id);
    jq("form#"+ id + "_form").remove();
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
function myplanAjaxSubmitPlanItem(id, type, methodToCall) {
    var tempDiv = jq("<div />").hide();
    jq("body").append(tempDiv);
    var elementToBlock = tempDiv;
    jq('input[name="methodToCall"]').remove();
    jq('<input type="hidden" name="methodToCall" value="' + methodToCall + '" />').appendTo(jq("form#" + id + "_form"));
    jq('<input type="hidden" name="' + type + '" value="' + id + '" />').appendTo(jq("form#" + id + "_form"));
    jq('<input type="hidden" name="viewId" value="PlannedCourse-FormView" />').appendTo(jq("form#" + id + "_form"));
    var updateRefreshableComponentCallback = function(htmlContent){
        elementToBlock.unblock();
        var status = jq.trim( jq("#request_status_item_key", htmlContent).text().toLowerCase() );
        switch (status) {
            case 'success':
                var oMessage = { 'message' : jq.trim( jq("#errorsFieldForPage_infoMessages ul li:first", htmlContent).text() ), 'cssClass':'myplan-message-success' };
                var json = jq.parseJSON( jq.trim( jq("#json_events_item_key", htmlContent).text().replace(/\\/g,"") ) );
                for (var key in json) {
                    if (json.hasOwnProperty(key)) {
                        eval('jq.publish("' + key + '", [' + JSON.stringify( jq.extend(json[key], oMessage) ) + ']);');
                    }
                }
                break;
            case 'error':
                // grab message and display message
                break;
        }
    };
    myplanAjaxSubmitForm(methodToCall, updateRefreshableComponentCallback, {reqComponentId: id, skipViewInit: 'false'}, elementToBlock, id);
    tempDiv.remove();
}
/*Function used for moving the plan Item from planned to backup*/
function myPlanAjaxPlanItemMove(id, getId, retrieveOptions, e, methodToCall) {
    stopEvent(e);
    var tempForm = jq('<form />').hide();
    jq(tempForm).attr("id", id + "_form").attr("action", "plan").attr("method", "post");
    jq("body").append(tempForm);
    myplanAjaxSubmitPlanItem(id, 'planItemId', methodToCall);
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
function myplanAjaxSubmitForm(methodToCall, successCallback, additionalData, elementToBlock, formId) {
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
						elementToBlock.append('<img src="' + getConfigParam("kradImageLocation") + 'loader.gif" alt="working..." /> Loading...');
						elementToBlock.show();
					}
					else{
						elementToBlock.block({
			                message: '<img src="../ks-myplan/images/ajaxLoader.gif" alt="loading..." />', // ' <img src="' + getConfigParam("kradImageLocation") + 'loading-bars.gif" alt="working..." /> Updating...',
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
			            });
					}
				},
				complete: function(){
					// note that if you want to unblock simultaneous with showing the new retrieval
					// you must do so in the successCallback
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