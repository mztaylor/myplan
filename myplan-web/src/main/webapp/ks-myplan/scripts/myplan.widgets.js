/* This is for DOM changes to refresh the view on back to keep the view updated */
if (jQuery("#dirtyView").length > 0 && jQuery("#dirtyView").val() != "CourseSearch-FormView") {
    window.location.href = window.location.href;
}

/*
if(window.location.hash != "" && window.location.hash.indexOf("CourseSearch-FormView") == -1) {

    window.location.href = window.location.href.split('#')[0];
}
 */
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
    if ( jQuery(target).parents(".jquerybubblepopup.jquerybubblepopup-myplan").length > 0 ) {
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

    var popupHtml = jQuery('<div />').attr("id",id + "_popup");

    if (popupStyles) {
        jQuery.each(popupStyles, function(property, value) {
            jQuery(popupHtml).css(property, value);
        });
    }

	var popupOptionsDefault = {
		innerHtml: popupHtml.prop('outerHTML'),
		themePath: '../ks-myplan/jquery-bubblepopup/jquerybubblepopup-theme/',
		manageMouseEvents: true,
		selectable: true,
		tail: {align:'middle', hidden: false},
		position: 'left',
        align: 'center',
        alwaysVisible: false,
        themeMargins: {total:'20px', difference:'5px'},
        themeName:'myplan',
        distance: '0px',
        openingSpeed: 0,
        closingSpeed: 0
	};

    var popupSettings = jQuery.extend(popupOptionsDefault, popupOptions);

    var popupBox;
    var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    if (selector === null) {
        popupBox = jQuery(target);
    } else {
        popupBox = jQuery(target).parents(selector);
    }

    fnCloseAllPopups();

	popupBox.CreateBubblePopup({manageMouseEvents: false});
    popupBox.ShowBubblePopup(popupSettings, false);
    var popupBoxId = popupBox.GetBubblePopupID();
	popupBox.FreezeBubblePopup();

    jQuery(document).on('click', function(e) {
        var tempTarget = (e.target) ? e.target : e.srcElement;
        if ( jQuery(tempTarget).parents("div.jquerybubblepopup.jquerybubblepopup-myplan").length === 0) {
            popupBox.RemoveBubblePopup();
            fnCloseAllPopups();
        }
    });

    var tempForm = jQuery('<form />').attr("id", id + "_form").attr("action", action).attr("method", "post").hide();
    var tempFormInputs = '<div style="display:none;">';
    jQuery.each(retrieveOptions, function(name, value) {
        tempFormInputs += '<input type="hidden" name="' + name + '" value="' + value + '" />';
    });
    tempFormInputs += '</div>';
    jQuery(tempForm).append(tempFormInputs);
    jQuery("body").append(tempForm);

    var elementToBlock = jQuery("#" + id  + "_popup");

	var updateRefreshableComponentCallback = function(htmlContent){
        var component;
        if (jQuery("span#request_status_item_key", htmlContent).length <= 0) {
            component = jQuery("#" + getId, htmlContent);
        } else {
            eval( jQuery("input[data-for='plan_item_action_response_page']", htmlContent).val().replace("#plan_item_action_response_page","body") );
            var sError = jQuery('body').data('validationMessages').serverErrors[0];
            component = jQuery("<div />").html(sError).addClass("myplan-message-border myplan-message-error").width(175);
        }
        elementToBlock.unblock({onUnblock: function(){
            if (jQuery("#" + id  + "_popup").length){
                popupBox.SetBubblePopupInnerHtml(component);
                if (close || typeof close === 'undefined') jQuery("#" + popupBoxId + " .jquerybubblepopup-innerHtml").append('<img src="../ks-myplan/images/btnClose.png" class="myplan-popup-close"/>');
                jQuery("#" + popupBoxId + " img.myplan-popup-close").on('click', function() {
                    popupBox.RemoveBubblePopup();
                    fnCloseAllPopups();
                });
            }
            runHiddenScripts(getId);
        }});
	};

	myplanAjaxSubmitForm(methodToCall, updateRefreshableComponentCallback, {reqComponentId: id, skipViewInit: "false"}, elementToBlock, id);
    jQuery("form#"+ id + "_form").remove();
}




function openMenu(id, getId, e, selector, popupClasses, popupOptions, close) {
    stopEvent(e);

    var popupHtml = jQuery('<div />').attr("id",id + "_popup").attr("class",popupClasses).html( jQuery("#" + getId).html() );

	var popupOptionsDefault = {
		innerHtml: popupHtml.prop('outerHTML'),
		themePath: '../ks-myplan/jquery-bubblepopup/jquerybubblepopup-theme/',
		manageMouseEvents: true,
		selectable: true,
		tail: {align:'middle', hidden: false},
		position: 'left',
        align: 'center',
        alwaysVisible: false,
        themeMargins: {total:'20px', difference:'5px'},
        themeName:'myplan',
        distance: '0px',
        openingSpeed: 0,
        closingSpeed: 0
	};

    var popupSettings = jQuery.extend(popupOptionsDefault, popupOptions);

    var popupBox;
    var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    if (selector === null) {
        popupBox = jQuery(target);
    } else {
        popupBox = jQuery(target).parents(selector);
    }



    fnCloseAllPopups();

	popupBox.CreateBubblePopup({manageMouseEvents: false});
    popupBox.ShowBubblePopup(popupSettings, false);
    var popupBoxId = popupBox.GetBubblePopupID();
	popupBox.FreezeBubblePopup();

    jQuery("#" + id + "_popup a").each(function(){
        var linkId = jQuery(this).attr("id");
        jQuery(this).siblings("input[data-for='"+ linkId + "']").removeAttr("script").attr("name", "script").val(function(index, value) {
            return value.replace("'"+ linkId +"'", "'"+ linkId +"_popup'");
        });
        jQuery(this).attr("id", linkId + "_popup");
        jQuery.each(target.dataset, function(key, value){
            jQuery("a#" + linkId + "_popup").attr("data-" + key, value);
        });
    });

    if (close || typeof close === 'undefined') jQuery("#" + popupBoxId + " .jquerybubblepopup-innerHtml").append('<img src="../ks-myplan/images/btnClose.png" class="myplan-popup-close"/>');

    runHiddenScripts(id + "_popup");

    jQuery(document).on('click', function(e) {
        var tempTarget = (e.target) ? e.target : e.srcElement;
        if ( jQuery(tempTarget).parents("div.jquerybubblepopup.jquerybubblepopup-myplan").length === 0) {
            popupBox.RemoveBubblePopup();
            fnCloseAllPopups();
        }
    });
}







/*
######################################################################################
    Function: Launch generic bubble popup
######################################################################################
 */
function openPopUpForm(id, getId, methodToCall, action, retrieveOptions, e, selector, popupStyles, popupOptions, close) {
    stopEvent(e);

    var popupHtml = jQuery('<div />').attr("id",id + "_popup");

    if (popupStyles) {
        jQuery.each(popupStyles, function(property, value) {
            jQuery(popupHtml).css(property, value);
        });
    }

	var popupOptionsDefault = {
		innerHtml: popupHtml.prop('outerHTML'),
		themePath: '../ks-myplan/jquery-bubblepopup/jquerybubblepopup-theme/',
		manageMouseEvents: true,
		selectable: true,
		tail: {align:'middle', hidden: false},
		position: 'left',
        align: 'center',
        alwaysVisible: false,
        themeMargins: {total:'20px', difference:'5px'},
        themeName:'myplan',
        distance: '0px',
        openingSpeed: 0,
        closingSpeed: 0
	};

    var popupSettings = jQuery.extend(popupOptionsDefault, popupOptions);

    var popupBox;
    var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    if (selector === null) {
        popupBox = jQuery(target);
    } else {
        popupBox = jQuery(target).parents(selector);
    }

    fnCloseAllPopups();

	popupBox.CreateBubblePopup({manageMouseEvents: false});
    popupBox.ShowBubblePopup(popupSettings, false);
    var popupBoxId = popupBox.GetBubblePopupID();
	popupBox.FreezeBubblePopup();

    jQuery(document).on('click', function(e) {
        var tempTarget = (e.target) ? e.target : e.srcElement;
        if ( jQuery(tempTarget).parents("div.jquerybubblepopup.jquerybubblepopup-myplan").length === 0) {
            popupBox.RemoveBubblePopup();
            fnCloseAllPopups();
        }
    });

    var tempForm = jQuery('<form />').attr("id", id + "_form").attr("action", action).attr("method", "post").hide();
    var tempFormInputs = '<div style="display:none;">';
    jQuery.each(retrieveOptions, function(name, value) {
        tempFormInputs += '<input type="hidden" name="' + name + '" value="' + value + '" />';
    });
    tempFormInputs += '</div>';
    jQuery(tempForm).append(tempFormInputs);
    jQuery("body").append(tempForm);

    var elementToBlock = jQuery("#" + id  + "_popup");

	var updateRefreshableComponentCallback = function(htmlContent){
        var component;
        if (jQuery("span#request_status_item_key", htmlContent).length <= 0) {
            component = jQuery("#" + getId, htmlContent);
            var planForm = jQuery('<form />').attr("id", id + "_form").attr("action", "plan").attr("method", "post");
        } else {
            eval( jQuery("input[data-for='plan_item_action_response_page']", htmlContent).val().replace("#plan_item_action_response_page","body") );
            var sError = jQuery('body').data('validationMessages').serverErrors[0];
            component = jQuery("<div />").html(sError).addClass("myplan-message-border myplan-message-error").width(175);
        }
        elementToBlock.unblock({onUnblock: function(){
            if (jQuery("#" + id  + "_popup").length){
                popupBox.SetBubblePopupInnerHtml(component);
                jQuery(".jquerybubblepopup-innerHtml").wrapInner(planForm);
                if (close || typeof close === 'undefined') jQuery("#" + popupBoxId + " .jquerybubblepopup-innerHtml").append('<img src="../ks-myplan/images/btnClose.png" class="myplan-popup-close"/>');
                jQuery("#" + popupBoxId + " img.myplan-popup-close").on('click', function() {
                    popupBox.RemoveBubblePopup();
                    fnCloseAllPopups();
                });
            }
            runHiddenScripts(getId);
        }});
	};

	myplanAjaxSubmitForm(methodToCall, updateRefreshableComponentCallback, {reqComponentId: id, skipViewInit: "false"}, elementToBlock, id);
    jQuery("form#"+ id + "_form").remove();
}










/*
######################################################################################
    Function: Launch plan item bubble popup
######################################################################################
 */
function openPlanItemPopUp(id, getId, retrieveOptions, e, selector, popupOptions, close) {
    stopEvent(e);

    var popupHtml = jQuery('<div />').attr("id",id + "_popup").css({
        width: "300px",
        height: "16px"
    });

	var popupOptionsDefault = {
		innerHtml: popupHtml.prop('outerHTML'),
		themePath: '../ks-myplan/jquery-bubblepopup/jquerybubblepopup-theme/',
		manageMouseEvents: true,
		selectable: true,
		tail: {align:'middle', hidden: false},
		position: 'left',
        align: 'center',
        alwaysVisible: false,
        themeMargins: {total:'20px', difference:'5px'},
        themeName:'myplan',
        distance: '0px',
        openingSpeed: 0,
        closingSpeed: 0
	};

    var popupSettings = jQuery.extend(popupOptionsDefault, popupOptions);

    var popupBox;
    var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    if (selector === null) {
        popupBox = jQuery(target);
    } else {
        popupBox = jQuery(target).parents(selector);
    }

    fnCloseAllPopups();

	popupBox.CreateBubblePopup({manageMouseEvents: false});
    popupBox.ShowBubblePopup(popupSettings, false);
    var popupBoxId = popupBox.GetBubblePopupID();
    fnPositionPopUp(popupBoxId);
	popupBox.FreezeBubblePopup();

    jQuery(document).on('click', function(e) {
        var tempTarget = (e.target) ? e.target : e.srcElement;
        if ( jQuery(tempTarget).parents("div.jquerybubblepopup.jquerybubblepopup-myplan").length === 0) {
            popupBox.RemoveBubblePopup();
            fnCloseAllPopups();
        }
    });

    var tempForm = jQuery('<form />').attr("id", id + "_form").attr("action", "plan").attr("method", "post").hide();
    var tempFormInputs = '<div style="display:none;"><input type="hidden" name="viewId" value="PlannedCourse-FormView" />';
    jQuery.each(retrieveOptions, function(name, value) {
        tempFormInputs += '<input type="hidden" name="' + name + '" value="' + value + '" />';
    });
    tempFormInputs += '</div>';
    jQuery(tempForm).append(tempFormInputs);
    jQuery("body").append(tempForm);

    var elementToBlock = jQuery("#" + id  + "_popup");

	var updateRefreshableComponentCallback = function(htmlContent){
        var component;
        if (jQuery("span#request_status_item_key", htmlContent).length <= 0) {
            component = jQuery("#" + getId, htmlContent);
            var planForm = jQuery('<form />').attr("id", id + "_form").attr("action", "plan").attr("method", "post");
        } else {
            eval( jQuery("input[data-for='plan_item_action_response_page']", htmlContent).val().replace("#plan_item_action_response_page","body") );
            var sError = jQuery('body').data('validationMessages').serverErrors[0];
            component = jQuery("<div />").html(sError).addClass("myplan-message-border myplan-message-error").width(175);
        }
        elementToBlock.unblock({onUnblock: function(){
            if (jQuery("#" + id  + "_popup").length){
                popupBox.SetBubblePopupInnerHtml(component);
                fnPositionPopUp(popupBoxId);
                if ( status != 'error' ) jQuery(".jquerybubblepopup-innerHtml").wrapInner(planForm);
                if (close || typeof close === 'undefined') jQuery("#" + popupBoxId + " .jquerybubblepopup-innerHtml").append('<img src="../ks-myplan/images/btnClose.png" class="myplan-popup-close"/>');
                jQuery("#" + popupBoxId + " img.myplan-popup-close").on('click', function() {
                    popupBox.RemoveBubblePopup();
                    fnCloseAllPopups();
                });
            }
            runHiddenScripts(getId);
        }});
	};

	myplanAjaxSubmitForm("startAddPlannedCourseForm", updateRefreshableComponentCallback, {reqComponentId: id, skipViewInit: "false"}, elementToBlock, id);
    jQuery("form#"+ id + "_form").remove();
}
function openDialog(sText, e, close) {
    stopEvent(e);

    var dialogHtml = jQuery('<div />').html(sText).css({
        width: "300px"
    });

	var popupOptionsDefault = {
		innerHtml: dialogHtml.prop('outerHTML'),
		themePath: '../ks-myplan/jquery-bubblepopup/jquerybubblepopup-theme/',
		manageMouseEvents: true,
		selectable: true,
		tail: {hidden: true},
		position: 'top',
        align: 'center',
        alwaysVisible: false,
        themeMargins: {total:'20px', difference:'5px'},
        themeName:'myplan',
        distance: '0px',
        openingSpeed: 0,
        closingSpeed: 0
	};

    var popupBox = jQuery("body");

    fnCloseAllPopups();

	popupBox.CreateBubblePopup({manageMouseEvents: false});
    popupBox.ShowBubblePopup(popupOptionsDefault, false);
    var popupBoxId = popupBox.GetBubblePopupID();
	popupBox.FreezeBubblePopup();

    if (close || typeof close === 'undefined') jQuery("#" + popupBoxId + " .jquerybubblepopup-innerHtml").append('<img src="../ks-myplan/images/btnClose.png" class="myplan-popup-close"/>');

    fnPositionPopUp(popupBoxId);

    jQuery(document).on('click', function(e) {
        var tempTarget = (e.target) ? e.target : e.srcElement;
        if ( jQuery(tempTarget).parents("div.jquerybubblepopup.jquerybubblepopup-myplan").length === 0) {
            popupBox.RemoveBubblePopup();
            fnCloseAllPopups();
        }
    });

    jQuery("#" + popupBoxId + " img.myplan-popup-close").on('click', function() {
        popupBox.RemoveBubblePopup();
        fnCloseAllPopups();
    });
}

function fnPositionPopUp(popupBoxId) {
    if ( parseFloat(jQuery("#" + popupBoxId).css("top")) < 0 || parseFloat(jQuery("#" + popupBoxId).css("left")) < 0 ) {
        var iTop = ( document.body.scrollTop + ( jQuery(window).height() / 2 ) ) - ( jQuery("#" + popupBoxId).height() / 2 );
        var iLeft = ( document.body.scrollLeft + ( jQuery(window).width() / 2 ) ) - ( jQuery("#" + popupBoxId).width() / 2 );
        jQuery("#" + popupBoxId).css({top: iTop + 'px', left: iLeft + 'px'});
    }
}




function myplanWriteHiddenToForm(propertyName, propertyValue, formId) {
    //removing because of performFinalize bug
    jQuery('input[name="' + escapeName(propertyName) + '"]').remove();

    if (propertyValue.indexOf("'") != -1) {
        jQuery("<input type='hidden' name='" + propertyName + "'" + ' value="' + propertyValue + '"/>').appendTo(jQuery("#"+formId));
    } else {
        jQuery("<input type='hidden' name='" + propertyName + "' value='" + propertyValue + "'/>").appendTo(jQuery("#"+formId));
    }
}
/*
######################################################################################
    Function: Submit
######################################################################################
 */
function myplanAjaxSubmitPlanItem(id, type, methodToCall, e, bDialog) {
    var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    var targetText = ( jQuery.trim( jQuery(target).text() ) != '') ? jQuery.trim( jQuery(target).text() ) : "Error";
    var elementToBlock = (target.nodeName != 'INPUT') ? jQuery(target) : jQuery(target).parent();
    jQuery('input[name="methodToCall"]').remove();
    jQuery('#' + id + '_form input[name="' + type + '"]').remove();
    jQuery('#' + id + '_form input[name="viewId"]').remove();
    jQuery("#" + id + "_form").append('<input type="hidden" name="methodToCall" value="' + methodToCall + '" /><input type="hidden" name="' + type + '" value="' + id + '" /><input type="hidden" name="viewId" value="PlannedCourse-FormView" />');
    var updateRefreshableComponentCallback = function(htmlContent){
        var status = jQuery.trim( jQuery("span#request_status_item_key", htmlContent).text().toLowerCase() );
        eval( jQuery("input[data-for='plan_item_action_response_page']", htmlContent).val().replace("#plan_item_action_response_page","body") );
        elementToBlock.unblock();
        switch (status) {
            case 'success':
                var oMessage = { 'message' : jQuery('body').data('validationMessages').serverInfo[0], 'cssClass':'myplan-message-border myplan-message-success' };
                var json = jQuery.parseJSON( jQuery.trim( jQuery("span#json_events_item_key", htmlContent).text().replace(/\\/g,"") ) );
                for (var key in json) {
                    if (json.hasOwnProperty(key)) {
                        eval('jQuery.publish("' + key + '", [' + JSON.stringify( jQuery.extend(json[key], oMessage) ) + ']);');
                    }
                }
                if (jQuery("#myplanDirty").length === 0) {
                    jQuery("body").append('<form id="myplanDirty"><input type="hidden" id="dirtyView" name="dirtyView" value="'+jQuery("input#viewId").val()+'" </form>');
                }
                /*
                if (window.location.hash == '') {

                    var hash  = new Date().getTime() + '-' + jQuery("input#viewId").val();
                    window.location.hash = hash;
                }
                */
                break;
            case 'error':
                var oMessage = { 'message' : jQuery('body').data('validationMessages').serverErrors[0], 'cssClass':'myplan-message-border myplan-message-error' };
                if (!bDialog) {
                    var sContent = jQuery("<div />").append(oMessage.message).addClass("myplan-message-noborder myplan-message-error").css({"background-color":"#fff","color":"#ff0606","border":"none"});
                    var sHtml = jQuery("<div />").append('<div class="uif-headerField uif-sectionHeaderField"><h3 class="uif-header">' + targetText + '</h3></div>').append(sContent);
                    if ( jQuery("body").HasBubblePopup() ) jQuery("body").RemoveBubblePopup();
                    openDialog(sHtml.html(), e);
                } else {
                    eval('jQuery.publish("ERROR", [' + JSON.stringify( oMessage ) + ']);');
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
    var tempForm = jQuery('<form />').attr("id", id + "_form").attr("action", "plan").attr("method", "post").hide();
    jQuery("body").append(tempForm);
    myplanAjaxSubmitPlanItem(id, type, methodToCall, e, false);
    fnCloseAllPopups();
    jQuery("form#"+ id + "_form").remove();
}
/*
######################################################################################
    Function: Retrieve component content through ajax
######################################################################################
 */
function myplanRetrieveComponent(id, getId, methodToCall, action, retrieveOptions, highlightId) {
    var tempForm = jQuery('<form />').attr("id", id + "_form").attr("action", action).attr("method", "post").hide();
    jQuery.each(retrieveOptions, function(name, value) {
        jQuery(tempForm).append('<input type="hidden" name="' + name + '" value="' + value + '" />');
    });
    jQuery("body").append(tempForm);

    var elementToBlock = jQuery("#" + id );

	var updateRefreshableComponentCallback = function(htmlContent){
		var component = jQuery("#" + getId , htmlContent);
		elementToBlock.unblock({onUnblock: function(){
				// replace component
				if(jQuery("#" + id).length){
					jQuery("#" + id).replaceWith(component);
				}

				runHiddenScripts(getId);

                if(highlightId) {
                	jQuery("[id^='" + highlightId + "']").parents('li').animate( {backgroundColor:"#ffffcc"}, 1 ).animate( {backgroundColor:"#ffffff"}, 3000 );
                }
		}});
	};

    if (!methodToCall) {
        methodToCall = "search";
    }

	myplanAjaxSubmitForm(methodToCall, updateRefreshableComponentCallback, {reqComponentId: id, skipViewInit: "false"}, elementToBlock, id);
    jQuery("form#"+ id + "_form").remove();
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
        var methodToCallInput = jQuery("input[name='methodToCall']");
        if(methodToCallInput.length > 0){
            methodToCall = jQuery("input[name='methodToCall']").val();
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
    jQuery("input[name='methodToCall']").remove();

	if(additionalData != null){
		jQuery.extend(data, additionalData);
	}

    var viewState = jQuery(document).data("ViewState");
    if (!jQuery.isEmptyObject(viewState)) {
        var jsonViewState = jQuery.toJSON(viewState);

        // change double quotes to single because escaping causes problems on URL
        jsonViewState = jsonViewState.replace(/"/g, "'");
        jQuery.extend(data, {clientViewState: jsonViewState});
    }

	var submitOptions = {
			data: data,
			success: function(response){
				var tempDiv = document.createElement('div');
				tempDiv.innerHTML = response;
				var hasError = handleIncidentReport(response);
				if(!hasError){
                    var newServerErrors = jQuery("#errorsFieldForPage_div", tempDiv).clone();
					successCallback(tempDiv);
//                    if(successCallback !== replacePage){
//                        jQuery("#errorsFieldForPage_div").replaceWith(newServerErrors);
//                        runHiddenScripts("errorsFieldForPage_div");
//                    }
				}
				jQuery("#formComplete").empty();
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
                        elementToBlock.block(jQuery.extend(elementBlockingDefaults, elementBlockingSettings));
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
	jQuery.extend(submitOptions, elementBlockingOptions);
	if (formId) {
        var form = jQuery("#" + formId + "_form");
    } else {
        var form = jQuery("#kualiForm");
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
    jQuery("#" + id).each(function() {
        jQuery(this).css("display","block");
        var fixed = margin = 0;
        jQuery(this).find(".uif-boxLayoutHorizontalItem:not(.myplan-text-ellipsis)").each(function() {
        	fixed = fixed + jQuery(this).width();
            margin = margin + Math.ceil(parseFloat(jQuery(this).css("margin-right")));
        });
        var ellipsis = jQuery(this).width() - ( ( fixed + 1 ) + margin );
        jQuery(this).find(".uif-boxLayoutHorizontalItem").last().css("margin-right", 0);
        jQuery(this).find(".uif-boxLayoutHorizontalItem.myplan-text-ellipsis").width(ellipsis);
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
    jQuery("#" + parentId + " > .uif-horizontalBoxLayout > div.uif-boxLayoutHorizontalItem:visible").hide("slide", {
        direction: direction
    }, 100, function() {
        jQuery("#" + parentId + " > .uif-horizontalBoxLayout > div.uif-boxLayoutHorizontalItem").filter("#" + showId).show("slide", {
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
    jQuery("div.jquerybubblepopup.jquerybubblepopup-myplan").remove();
    jQuery(document).off();
}
/*
######################################################################################
    Function:   Build Term Plan View heading
######################################################################################
 */
function fnBuildTitle(aView) {
    var sText = 'Academic Year';
    var aFirst = jQuery.trim( jQuery(aView[0]).find("input[type='hidden'][id^='atpId']").val() ).split(".");
    var aLast = jQuery.trim( jQuery(aView[aView.length-1]).find("input[type='hidden'][id^='atpId']").val() ).split(".");
    jQuery("#planned_courses_detail .myplan-plan-header").html(sText + ' ' + aFirst[3] + '-' + aLast[3]);
}
/*
######################################################################################
    Function:   expand/collapse backup course set within plan view
######################################################################################
 */
function fnToggleBackup(e) {
    stopEvent(e);
    var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    if (!jQuery(target).hasClass("disabled")) {
        var oBackup = jQuery(target).parents(".myplan-term-backup").find(".uif-stackedCollectionLayout");
        var oQuarter = jQuery(target).parents("li");
        var iSpeed = 500;
        var iDefault = 26;
        if (jQuery(target).hasClass("expanded")) {
            var iAdjust = ( oBackup.height() - ( iDefault * 2 ) ) * -1;
            jQuery(target).removeClass("expanded");
            jQuery(target).find("span").html("Show");
        } else {
            var iAdjust = ( oBackup.find("span a").size() * iDefault ) - oBackup.height();
            jQuery(target).addClass("expanded");
            jQuery(target).find("span").html("Hide");
        }
        oBackup.animate({"height": oBackup.height() + iAdjust}, {duration: iSpeed});
        oQuarter.animate({"height": oQuarter.height() + iAdjust}, {duration: iSpeed});
    }
}
/*
######################################################################################
    Function:   expand/collapse backup course set within plan view
######################################################################################
 */
function myplanCreateLightBoxLink(controlId, options) {
    jQuery(function () {
        var showHistory = false;

        // Check if this is called within a light box
        if (!jQuery("#fancybox-frame", parent.document).length) {

            // Perform cleanup when lightbox is closed
            options['onCleanup'] = cleanupClosedLightboxForms;

            options['onComplete'] = function() {
                jQuery('#fancybox-frame').load(function() { // wait for frame to load and then gets it's height
                    jQuery('#fancybox-content').height( jQuery(this).contents().find('body').height()+20 );
                });
            };

            // If this is not the top frame, then create the lightbox
            // on the top frame to put overlay over whole window
            if (top == self) {
                jQuery("#" + controlId).fancybox(options);
            } else {
                jQuery("#" + controlId).click(function (e) {
                    e.preventDefault();
                    top.jQuery.fancybox(options);
                });
            }
        } else {
            //jQuery("#" + controlId).attr('target', '_self');
            showHistory = true;
        }

        // Set the renderedInLightBox = true param
        if (options['href'].indexOf('&renderedInLightBox=true') == -1) {
            options['href'] =  options['href'] + '&renderedInLightBox=true'
                    + '&showHome=false' + '&showHistory=' + showHistory
                    + '&history=' + jQuery('#formHistory\\.historyParameterString').val();
        }
    });
}


(function($) {

	$.fn.characterCount = function(options) {

		var oDefaults = {
			maxLength: 100,
			warningLength: 20,
			classCounter: 'counter',
			classWarning: 'warning'
		};

		var options = $.extend(oDefaults, options);

        function calculate(obj, options) {
			var iCount = $(obj).val().length;
			var iAvailable = options.maxLength - iCount;
            var sValue = $(obj).val();
            if (iCount > options.maxLength) {
                $(obj).val( sValue.substr(0, options.maxLength) );
            }
            if (iAvailable <= options.warningLength && iAvailable >= 0) {
				$('.' + options.classCounter).addClass(options.classWarning);
			} else {
				$('.' + options.classCounter).removeClass(options.classWarning);
			}
			$('.' + options.classCounter).html( '<strong>' + $(obj).val().length + '</strong> / ' + options.maxLength + ' characters' );
		};

		this.each(function() {
			calculate(this, options);
			$(this).keyup(function(){
                calculate(this, options);
            });
			$(this).change(function(){
                calculate(this, options);
            });
		});
	};

})(jQuery);

function fnCreateDate(sData) {
    var jTemp = jQuery(sData);
    jTemp.find("legend, .myplan-sort-remove").remove();
    var sDate = jQuery.trim( jTemp.text() );

    if (sDate.length > 2) {
        return Date.parse(sDate);
    } else {
        return 0;
    }
}

jQuery.fn.dataTableExt.oSort['longdate-asc']  = function(x,y) {
    x = fnCreateDate(x);
    y = fnCreateDate(y);

    return ((x < y) ? -1 : ((x > y) ?  1 : 0));
};
jQuery.fn.dataTableExt.oSort['longdate-desc']  = function(x,y) {
    x = fnCreateDate(x);
    y = fnCreateDate(y);

    return ((x < y) ? 1 : ((x > y) ? -1 : 0));
};