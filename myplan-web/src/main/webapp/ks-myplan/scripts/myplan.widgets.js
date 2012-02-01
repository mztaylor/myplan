function loadSavedCoursesList(id, getId, viewId, methodToCall, action, highlightId) {
    jq("body").append('<form id="' + id + '_form" action="' + action + '" method="post"><input type="hidden" name="viewId" value="' + viewId + '" /></form>');
    myplanRetrieveComponent(id, getId, methodToCall, highlightId);
    jq("form#"+ id + "_form").remove();
}

function myplanRetrieveComponent(id, incomingId, methodToCall, highlightId){
	var elementToBlock = jq("#" + id);

	var updateRefreshableComponentCallback = function(htmlContent){
		var component = jq("#" + incomingId, htmlContent);

        var displayWithId = id;
        if (id.indexOf('_attribute') > 0) {
            displayWithId = id.replace('_attribute', '');
        }

		// special label handling, if any
		var theLabel = jq("#" + displayWithId + "_label_span", htmlContent);
		if(jq(".displayWith-" + displayWithId).length && theLabel.length){
			theLabel.addClass("displayWith-" + displayWithId);
			jq("span.displayWith-" + displayWithId).replaceWith(theLabel);
			component.remove("#" + displayWithId + "_label_span");
		}

		elementToBlock.unblock({onUnblock: function(){
                var origColor = jq(component).css("background-color");
                jq(component).css("background-color", "");
                jq(component).addClass("uif-progressiveDisclosure-highlight");

				// replace component
				if(jq("#" + id + ">div").length){
					jq("#" + id + ">div").replaceWith(component);
				}

				runHiddenScripts(id + ">div");
                if(origColor == ""){
                    origColor = "transparent";
                }

                jq("#" + id).animate({backgroundColor: origColor}, 5000);

                if(highlightId) {
                	jq("[id^='" + highlightId + "']").parents('li').animate( { backgroundColor: "#ffffcc" }, 1 ).animate( { backgroundColor: "#ffffff" }, 3000 );
                }
			}
		});

		jq(".displayWith-" + displayWithId).show();
	};

    if (!methodToCall) {
        methodToCall = "updateComponent";
    }

	myplanAjaxSubmitForm(methodToCall, updateRefreshableComponentCallback, {reqComponentId: id, skipViewInit: "false"}, elementToBlock, id);
}

function myplanAjaxSubmitForm(methodToCall, successCallback, additionalData, elementToBlock, id) {
	var data;
    //methodToCall checks
	if(methodToCall != null){
		data = {methodToCall: methodToCall, renderFullView: false, viewId: 'SavedCoursesSummaryView'};
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
			                message: '<img src="' + getConfigParam("kradImageLocation") + 'loader.gif" alt="working..." /> Updating...',
			                fadeIn:  400,
			                fadeOut:  800
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

function addSavedCourse(id, action, methodToCall, viewId, courseId) {
    // plan?methodToCall=addItem&viewId=savedCoursesListActionsView&courseId=

    var tempForm = jq('<form id="' + id + '_form" action="' + action + '" method="post"><input type="hidden" name="viewId" value="' + viewId + '" /><input type="hidden" name="courseId" value="' + courseId + '" /></form>');
    jq(tempForm).hide();
    jq("body").append(tempForm);

    var elementToBlock = jq("#" + id + "_form");

	var updateRefreshableComponentCallback = function(htmlContent){
		var component = jq("[id^='add_to_plan_item_key']", htmlContent);
		var idAdded = jq.trim( jq(component).text() );
		if (idAdded.length > 0) {
			elementToBlock.unblock();
			loadSavedCoursesList('watch_list_group','saved_courses_summary_div','SavedCoursesSummaryView','search','lookup', idAdded);

		} else {
			// showGrowl('Error updating', 'Error', 'errorGrowl');
		}
	};

    myplanAjaxSubmitForm(methodToCall, updateRefreshableComponentCallback, {reqComponentId: id, skipViewInit: "false"}, elementToBlock, id);
    jq("form#" + id + "_form").remove();
}

function truncateField(id) {
    jq("[id^='" + id + "']").each(function() {
        jq(this).css("display","block");
        var margin = Math.ceil(parseFloat(jq(this).find("span.boxLayoutHorizontalItem span").css("margin-right")));
        var fixed = 0;
        var fields = jq(this).find("span.boxLayoutHorizontalItem span").not(".ellipsis").length;
        jq(this).find("span.boxLayoutHorizontalItem span").not(".ellipsis").each(function() {
        	fixed = fixed + jq(this).width();
        });
        var ellipsis = jq(this).width() - ( fixed + ( margin * fields ) );
        jq(this).find("span.boxLayoutHorizontalItem span").last().css("margin-right", 0);
        jq(this).find("span.boxLayoutHorizontalItem span.ellipsis").width(ellipsis);
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