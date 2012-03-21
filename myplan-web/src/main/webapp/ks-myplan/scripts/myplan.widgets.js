function openCourse(courseId, e) {
    if (!e) var e = window.event;

    if (jq(e.target).parents("#course_details_popup_requisites").length > 0) {
        window.location = "inquiry?methodToCall=start&viewId=CourseDetails-InquiryView&courseId="+courseId;
    } else {
    	openPopUp(courseId, 'course_details_popup', 'start', 'inquiry', {viewId:'CourseDetailsPopup-InquiryView', courseId:courseId}, e, null, {width:'300px'}, {tail:{align:'center', hidden: false}, position: 'bottom'});
    }
}

function openPopUp(id, getId, methodToCall, action, retrieveOptions, e, selector, popupStyles, popupOptions) {
    if (!e) var e = window.event;
    e.stopPropagation();

    var popupHtml = jq('<div />').attr("id",id).attr("class","myplan-popup-box");
    jQuery.each(popupStyles, function(property, value) {
        jq(popupHtml).css(property, value);
    });

	var popupOptionsDefault = {
		innerHtml: jq(popupHtml).clone().wrap('<div>').parent().html(),
		themePath: '../ks-myplan/jquery-bubblepopup/jquerybubblepopup-theme/',
		manageMouseEvents: true,
		selectable: true,
		tail: {align:'middle', hidden: false},
		position: 'left',
        align: 'center',
        alwaysVisible: false
	};

    if (popupOptions) {
        var popupSettings = jQuery.extend(popupOptionsDefault, popupOptions);
    } else {
        var popupSettings = popupOptionsDefault;
    }

    if (selector == null) {
        var popupBox = jq(e.target);
    } else {
        var popupBox = jq(e.target).parents(selector);
    }

    popupBox.attr("class","myplan-popup-target");

    jq(".myplan-popup-target").each(function() {
        jq(this).HideAllBubblePopups();
        jq(this).RemoveBubblePopup();
    });

	popupBox.CreateBubblePopup({manageMouseEvents: false});
    popupBox.ShowBubblePopup(popupSettings, false);
	popupBox.FreezeBubblePopup();
    var popupBoxId = popupBox.GetBubblePopupID();

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

    var elementToBlock = jq("#" + id);

	var updateRefreshableComponentCallback = function(htmlContent){
		var component = jq("#" + getId + "_div", htmlContent);
		elementToBlock.unblock({onUnblock: function(){
            // replace component
            if(jq("#" + id).length){
                popupBox.SetBubblePopupInnerHtml(component.addClass("myplan-popup-box").prepend('<img src="../ks-myplan/images/btnClose.png" class="myplan-popup-close"/>'));
                jq("#" + popupBoxId + " img.myplan-popup-close").click(function() {
                    popupBox.HideAllBubblePopups();
                    popupBox.RemoveBubblePopup();
                });
            }

            runHiddenScripts(getId + "_div");
			}
		});
	};

    if (!methodToCall) {
        methodToCall = "search";
    }

	myplanAjaxSubmitForm(methodToCall, updateRefreshableComponentCallback, {reqComponentId: id, skipViewInit: "false"}, elementToBlock, id);
    jq("form#"+ id + "_form").remove();
}

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
			}
		});
	};

    if (!methodToCall) {
        methodToCall = "search";
    }

	myplanAjaxSubmitForm(methodToCall, updateRefreshableComponentCallback, {reqComponentId: id, skipViewInit: "false"}, elementToBlock, id);
    jq("form#"+ id + "_form").remove();
}

function addSavedCourse(id, methodToCall, action, retrieveOptions, e) {
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
			myplanRetrieveComponent('saved_courses_summary','saved_courses_summary','search','lookup',{viewId:'SavedCoursesSummary-LookupView'},addedId);
			targetId = jq(e.target).attr("id");
            jq("#" + targetId).parent().fadeOut(250, function() {
                if ( jq('#viewId').val() == "CourseSearch-FormView" ) {
                	jq(this).attr("id",courseId+"_span").addClass("fl-text-align-center fl-text-green").html("Saved").fadeIn(250);
                } else {
                    var tempDiv = jq('<div />');
                    jq(tempDiv).attr("id",courseId+"_div").addClass("myplan-message-border myplan-message-success").html("Course successfully added");
                    jq(this).html(tempDiv).fadeIn(250);
                }
			});
		} else {
			// showGrowl('Error updating', 'Error', 'errorGrowl');
		}
	};

    if (!methodToCall) {
        methodToCall = "addSavedCourse";
    }

    myplanAjaxSubmitForm(methodToCall, updateRefreshableComponentCallback, {reqComponentId: id, skipViewInit: "false"}, elementToBlock, id);
    jq("form#" + id + "_form").remove();
}

function removeSavedCourse(id, methodToCall, action, retrieveOptions, courseCode, e) {
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
						targetId = jq(e.target).attr("id");
						if ( jq('#viewId').val() == "SavedCoursesDetail-LookupView" ) {
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
								    var oTable = jq('#course_search_results_datatable').dataTable();
								    var nodes = oTable.fnGetNodes();
                                    if ( jq(nodes).find("#" + removedId + "_span").length ) {
								        jq(nodes).find("#" + removedId + "_span").parent().fadeOut(250, function() {
                                            jq(nodes).find("#" + removedId + "_span").html("<input type=\"image\" id=\""+removedId+"_button\" src=\"/student/ks-myplan/images/btnAdd.png\" alt=\"Save to Your Courses List\" class=\"uif-field uif-imageField\" onclick=\"event.preventDefault();writeHiddenToForm('actionParameters[showHistory]','false');writeHiddenToForm('actionParameters[selectedCollectionPath]','courseSearchResults');writeHiddenToForm('actionParameters[showHome]','false');writeHiddenToForm('showHistory','false'); writeHiddenToForm('showHome','false');writeHiddenToForm('focusId','" + removedId + "_button');writeHiddenToForm('jumpToId','" + removedId + "_button');addSavedCourse('" + removedId + "','addSavedCourse','plan',{viewId:'SavedCoursesListActions-FormView', courseId:'" + removedId + "'},event);\" />");
                                            jq(this).fadeIn(250);
                                        });
                                    }
                                    break;
                                case "CourseDetails-InquiryView":
                                    if ( jq("#" + removedId + "_div").length ) {
                                        jq("#" + removedId + "_div").parent().fadeOut(250, function() {
                                            jq("#" + removedId + "_div").replaceWith("<button id=\"" + removedId + "\" class=\"uif-field uif-action myplan-button myplan-button-gray\" onClick=\"event.preventDefault();writeHiddenToForm('actionParameters[showHistory]','false');writeHiddenToForm('actionParameters[showHome]','false');writeHiddenToForm('showHistory','false');writeHiddenToForm('showHome','false');writeHiddenToForm('focusId','" + removedId + "');writeHiddenToForm('jumpToId','" + removedId + "');addSavedCourse('" + removedId + "','addSavedCourse','plan',{viewId:'SavedCoursesListActions-FormView',courseId:'" + removedId + "'}, event);\">Save to Your Courses List</button>");
                                            jq(this).fadeIn(250);
                                        });
                                    }
                                    break;
                            }
                            jq(e.target).parent().fadeOut(250, function() {
								jq(this).addClass("myplan-message-border myplan-message-success").html("Course successfully removed").fadeIn(250);
							});
                            myplanRetrieveComponent('saved_courses_summary','saved_courses_summary','search','lookup',{viewId:'SavedCoursesSummary-LookupView'});
						}
					}
				};

                if (!methodToCall) {
                    methodToCall = "removeSavedCourse";
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
                                opacity: 0.5
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
function enableToggle(styleClass) {
	jq("." + styleClass).each(function() {
		jq(this).hide();
		var link = jq("<div>Show</div>");
		jq(link).click(function() {
			jq(this).siblings("." + styleClass).slideDown(500);
		});
		jq(this).parent().append(link);
	});
}
function toggleItem() {
	jq(this).click(function() {
		if ( jq(this).hasClass("show") ) {
			jq(this).prev().slideDown(500);
			jq(this).removeClass("show").addClass("hide");
		} else {
			jq(this).prev().slideUp(500);
			jq(this).removeClass("hide").addClass("show");
		}
	});
}