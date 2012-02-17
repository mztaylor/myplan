function openPopUp(id, getId, action, methodToCall, viewId, e, itemId) {
    e.stopPropagation();

	var optionsPopUp = {
		innerHtml: '<div id="' + id + '" style="width: 300px; min-height: 16px;"><div></div></div>',
		manageMouseEvents: true,
		selectable: true,
		tail: {align:'top',hidden: false},
		distance: '0px',
		openingSpeed: 0,
        closingSpeed: 0,
		position: 'left',
		themePath: '../ks-myplan/jquery-bubblepopup/jquerybubblepopup-theme/',
		themeName: 'grey'
	};

	//var targetId = jq(e.target).attr("id");
	//var popUpBox = jq("#" + targetId).parents("li");
	var popUpBox = jq(e.target).parents("li");
	popUpBox.CreateBubblePopup({manageMouseEvents: false});
	var popUpBoxId = popUpBox.GetBubblePopupID();
	popUpBox.ShowBubblePopup(optionsPopUp, false);
	popUpBox.FreezeBubblePopup();

	jq("html").click(function() {
		popUpBox.HideAllBubblePopups();
		popUpBox.RemoveBubblePopup();
	});


    var tempForm = jq('<form></form>');
	jq(tempForm).attr("id", id + "_form").attr("action", action).attr("method", "post");
	jq(tempForm).append('<input type="hidden" name="viewId" value="' + viewId + '" />');
    if (itemId) {
        jq(tempForm).append('<input type="hidden" name="planItemId" value="' + itemId + '" />');
    }
    jq(tempForm).hide();
    jq("body").append(tempForm);

    var elementToBlock = jq("#" + id);

	var updateRefreshableComponentCallback = function(htmlContent){

		var component = jq("#" + getId, htmlContent);
		elementToBlock.unblock({onUnblock: function(){
				// replace component
				if(jq("#" + id + ">div").length){
					//jq("#" + id).animate({height: +"px"}, 250);
					jq("#" + id + ">div").replaceWith(component);
				}

				runHiddenScripts(id + ">div");
			}
		});
	};

    if (!methodToCall) {
        methodToCall = "start";
    }

	myplanAjaxSubmitForm(methodToCall, updateRefreshableComponentCallback, {reqComponentId: id, skipViewInit: "false"}, elementToBlock, id);
    jq("form#"+ id + "_form").remove();
}

function loadSavedCoursesList(id, getId, viewId, methodToCall, action, highlightId) {
    var tempForm = jq('<form></form>');
	jq(tempForm).attr("id", id + "_form").attr("action", action).attr("method", "post");
	jq(tempForm).append('<input type="hidden" name="viewId" value="' + viewId + '" />').hide();
    jq("body").append(tempForm);

    var elementToBlock = jq("#" + id);

	var updateRefreshableComponentCallback = function(htmlContent){
		var component = jq("#" + getId, htmlContent);
		elementToBlock.unblock({onUnblock: function(){
				// replace component
				if(jq("#" + id + ">div").length){
					jq("#" + id + ">div").replaceWith(component);
				}

				runHiddenScripts(id + ">div");

                if(highlightId) {
                	jq("[id^='" + highlightId + "']").parents('li').animate( { backgroundColor: "#ffffcc" }, 1 ).animate( { backgroundColor: "#ffffff" }, 3000 );
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

function addSavedCourse(id, action, methodToCall, viewId, courseId, e) {
	var tempForm = jq('<form></form>');
	jq(tempForm).attr("id", id + "_form").attr("action", action).attr("method", "post");
    jq(tempForm).append('<input type="hidden" name="viewId" value="' + viewId + '" /><input type="hidden" name="courseId" value="' + courseId + '" />').hide();
    jq("body").append(tempForm);

    var elementToBlock = jq("#" + id + "_form");

	var updateRefreshableComponentCallback = function(htmlContent){
		var component = jq("[id='add_plan_item_key']", htmlContent);
		var addedId = jq.trim( jq(component).text() );
		// TODO: Add validation for if there was an error adding - to be added later
        if (addedId.length > 0) {
			elementToBlock.unblock();
			loadSavedCoursesList('watch_list_group','saved_courses_summary_div','savedCoursesSummaryView','search','lookup', addedId);
			targetId = jq(e.target).attr("id");
            jq("#" + targetId).parent().fadeOut(250, function() {
				jq(this).addClass("fl-text-align-center fl-text-green").html("Saved").fadeIn(250);
                // If on course details page
                // jq(this).addClass("").html("This course is in your Watch List").fadeIn(250);
			});
		} else {
			// showGrowl('Error updating', 'Error', 'errorGrowl');
		}
	};

    if (!methodToCall) {
        methodToCall = "addItem";
    }

    myplanAjaxSubmitForm(methodToCall, updateRefreshableComponentCallback, {reqComponentId: id, skipViewInit: "false"}, elementToBlock, id);
    jq("form#" + id + "_form").remove();
}

function removeSavedCourse(id, action, methodToCall, viewId, planItemId, courseCode, e) {
	var dialogConfirm = jq('<div></div>');
	jq(dialogConfirm).attr("id","dialog-confirm").attr("title","Delete Course");
	jq(dialogConfirm).html('<p>Are you sure that you want to delete <strong>' + courseCode + '</strong>?</p><p>Once deleted, you cannot undo.</p>');
	jq(dialogConfirm).dialog({
		resizable: false,
		height: 160,
		width: 350,
		dialogClass: 'dialogWithDropShadow',
		modal: false,
		buttons: {
			"Yes": function () {
				var tempForm = jq('<form></form>');
                jq(tempForm).attr("id", id + "_form").attr("action", action).attr("method", "post");
                jq(tempForm).append('<input type="hidden" name="viewId" value="' + viewId + '" /><input type="hidden" name="planItemId" value="' + planItemId + '" />');
                jq(tempForm).hide();
				jq("body").append(tempForm);

				var elementToBlock = jq("#" + id + "_form");

				var updateRefreshableComponentCallback = function(htmlContent){
					// TODO: Add validation for if there was an error deleting - to be added later
                    if ( jq("#remove_plan_item_result_group .kr-errorsField ul.errorLines li", htmlContent).size() > 0 ) {
                        var errorText = '<ul>' + jq("#remove_plan_item_result_group .kr-errorsField ul.errorLines", htmlContent).html() + '</ul>';
                        showGrowl(errorText, 'Delete Item Error', 'errorGrowl');
                    } else {
                        elementToBlock.unblock();
                        targetId = jq(e.target).attr("id");
                        var planListItem = jq("#" + targetId).parents("li").children();
                        jq(planListItem).fadeOut(250, function() {
                            jq(this).parent().html('<div class="msg-success fl-text-green fl-text-bold"><span class="fl-text-black">' + courseCode + '</span> has been deleted successfully!</div>').fadeIn(250).delay(2000).fadeOut(250);
                        });
                        var planItemCount = jq(".myplan-saved-courses-detail .uif-headerField.uif-sectionHeaderField .uif-header strong");
                        jq(planItemCount).fadeOut(250, function() {
                            jq(this).html(planItemCount.text() - 1).fadeIn(250);
                        });
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

function myplanAjaxSubmitForm(methodToCall, successCallback, additionalData, elementToBlock, id) {
	var data;
    //methodToCall checks
	if(methodToCall != null){
		data = {methodToCall: methodToCall, renderFullView: false, viewId: 'savedCoursesSummaryView'};
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
    //remove this since the methodToCall was passed in or extracted from the page, to avoid issues
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
                                width: '100%',
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
	var form = jq("#" + id + "_form");
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
        var ellipsis = jq(this).width() - ( fixed + ( margin * fields ) );
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