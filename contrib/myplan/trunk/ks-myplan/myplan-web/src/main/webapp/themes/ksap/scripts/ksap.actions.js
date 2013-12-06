/**
 *
 *
 * @param additionalFormData -
 * @param type -
 * @param methodToCall -
 * @param e - An object containing data that will be passed to the event handler.
 */
function submitHiddenForm(formAction, additionalFormData, e) {
    stopEvent(e);
    var form = jQuery('<form />').attr("id", "popupForm").attr("action", formAction).attr("method", "post");
    jQuery("body").append(form);
    submitPopupForm(additionalFormData, e, true);
    fnCloseAllPopups();
    jQuery("form#popupForm").remove();
}
/**
 *
 *
 * @param id -
 * @param type -
 * @param methodToCall -
 * @param e - An object containing data that will be passed to the event handler.
 */
function submitPopupForm(additionalFormData, e, bDialog) {
    var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    var targetText = ( jQuery.trim(jQuery(target).text()) != '') ? jQuery.trim(jQuery(target).text()) : "Error";
    var elementToBlock = (target.nodeName != 'INPUT') ? jQuery(target) : jQuery(target).parent();
    var successCallback = function (htmlContent) {
        var pageId = jQuery("#pageId", htmlContent).val();
        var status = jQuery.trim(jQuery("#requestStatus", htmlContent).text().toLowerCase());
        var data = {};
        data.messages = jQuery("#" + pageId, htmlContent).data("validation_messages");
        data.cssClasses = "alert alert-" + status;
        switch (status) {
            case 'success':
                data.message = data.messages.serverInfo[0];
                var json = jQuery.parseJSON(jQuery.trim(jQuery("#jsonEvents", htmlContent).text()));
                for (var key in json) {
                    if (json.hasOwnProperty(key)) {
                        eval('jQuery.event.trigger("' + key + '", ' + JSON.stringify(jQuery.extend(json[key], data)) + ');');
                    }
                }
                setUrlHash('modified', 'true');
                break;
            case 'error':
                data.message = data.messages.serverErrors[0];
                if (bDialog) {
                    var sContent = jQuery("<div />").append(data.message).addClass(data.cssClasses + " alert-unboxed uif-boxLayoutVerticalItem");
                    var sHtml = jQuery("<div />").append('<h4>' + targetText + '</h4>').append(sContent);
                    if (jQuery("body").HasPopOver()) jQuery("body").HidePopOver();
                    openDialog(sHtml.html(), e);
                } else {
                    eval('jQuery.event.trigger("ERROR", ' + JSON.stringify(data) + ');');
                }
                break;
        }
    };
    var blockOptions = {
        centerX: true,
        centerY: true,
        message: '<img src="' + getConfigParam("ksapImageLocation") + 'loader/ajax_small.gif"/>',
        css: {
            width: '100%',
            border: 'none',
            backgroundColor: 'transparent',
            width: (elementToBlock.outerWidth() - 2) + "px",
            height: (elementToBlock.outerHeight() - 2) + "px",
            lineHeight: (elementToBlock.outerHeight() - 4) + "px"
        },
        overlayCSS: {
            backgroundColor: '#fff',
            opacity: 0.3,
            padding: '0px',
            margin: '0px',
            top: '-1px',
            left: '-1px',
            width: (elementToBlock.outerWidth()) + "px",
            height: (elementToBlock.outerHeight()) + "px"
        }
    };
    ksapAjaxSubmitForm(additionalFormData, successCallback, elementToBlock, "popupForm", blockOptions);
}
/**
 *
 *
 * @param id -
 * @param type -
 * @param methodToCall -
 * @param e - An object containing data that will be passed to the event handler.
 */
function ksapAjaxSubmitForm(data, successCallback, elementToBlock, formId, blockingSettings) {
    var submitOptions = {
        data: data,
        success: function (response) {
            var tempDiv = document.createElement('div');
            tempDiv.innerHTML = response;
            var hasError = checkForIncidentReport(response);
            var isSessionExpired = (jQuery("title", tempDiv).text() == "Session Expired");
            if (!hasError) successCallback(tempDiv);
            if (isSessionExpired) sessionExpired();
            jQuery("#formComplete").empty();
        },
        error: function (jqXHR, textStatus) {
            alert("Request failed: " + textStatus);
        }
    };

    if (elementToBlock != null && elementToBlock.length) {
        var elementBlockingOptions = {
            beforeSend: function () {
                if (elementToBlock.hasClass("unrendered")) {
                    elementToBlock.append('<img src="' + getConfigParam("kradImageLocation") + 'loader.gif" alt="Loading..." /> Loading...');
                    elementToBlock.show();
                }
                else {
                    var elementBlockingDefaults = {
                        baseZ: 100,
                        centerY: false,
                        centerX: false,
                        message: '<img src="' + getConfigParam("ksapImageLocation") + 'loader/ajax_refresh.gif" alt="loading..." />',
                        fadeIn: 0,
                        fadeOut: 0,
                        overlayCSS: {
                            backgroundColor: '#fff',
                            opacity: 0
                        },
                        css: {
                            border: 'none',
                            width: '16px',
                            height: '16px',
                            lineHeight: '16px',
                            top: '0px',
                            left: '0px'
                        }
                    };
                    elementToBlock.block(jQuery.extend(elementBlockingDefaults, blockingSettings));
                }
            },
            complete: function () {
                elementToBlock.unblock();
            },
            error: function () {
                if (elementToBlock.hasClass("unrendered")) {
                    elementToBlock.hide();
                }
                else {
                    elementToBlock.unblock();
                }
            }
        };
    }
    jQuery.extend(submitOptions, elementBlockingOptions);
    var form = jQuery("#" + ((formId) ? formId : "kualiForm"));
    form.ajaxSubmit(submitOptions);
}


function addHiddenDataToForm(propertyName, propertyValue, formId) {
    //removing because of performFinalize bug
    jQuery('input[name="' + escapeName(propertyName) + '"]').remove();

    if (propertyValue.indexOf("'") != -1) {
        jQuery("<input type='hidden' name='" + propertyName + "'" + ' value="' + propertyValue + '"/>').appendTo(jQuery("#" + formId));
    } else {
        jQuery("<input type='hidden' name='" + propertyName + "' value='" + propertyValue + "'/>").appendTo(jQuery("#" + formId));
    }
}


/*
 ######################################################################################
 Function: Retrieve component content through ajax
 ######################################################################################
 */
function customRetrieveComponent(id, getId, methodToCall, action, retrieveOptions, highlightId, elementBlockingSettings) {
    var tempForm = '<form id="' + id + '_form" action="' + action + '" method="post" style="display:none;">';
    jQuery.each(retrieveOptions, function (name, value) {
        tempForm += '<input type="hidden" name="' + name + '" value="' + value + '" />';
    });
    tempForm += '</form>';
    jQuery("body").append(tempForm);

    var elementToBlock = jQuery("#" + id);

    var updateRefreshableComponentCallback = function (htmlContent) {
        var component = jQuery("#" + getId, htmlContent);

        // replace component
        if (jQuery("#" + id).length) {
            jQuery("#" + id).replaceWith(component);
        }

        runHiddenScripts(getId);

        if (jQuery("input[data-role='script'][data-for='" + getId + "']", htmlContent).length > 0) {
            eval(jQuery("input[data-role='script'][data-for='" + getId + "']", htmlContent).val());
        }

        if (highlightId) {
            jQuery("[id^='" + highlightId + "']").animate({backgroundColor: "#faf5ca"}, 1).animate({backgroundColor: "#ffffff"}, 1500, function () {
                jQuery(this).removeAttr("style");
            });
        }

        elementToBlock.unblock();
    };

    var data = {
        'methodToCall': methodToCall,
        'renderFullView': false,
        'reqComponentId': id,
        'skipViewInit': "false"
    };

    var viewState = jQuery(document).data(kradVariables.VIEW_STATE);
    if (!jQuery.isEmptyObject(viewState)) {
        var jsonViewState = jQuery.toJSON(viewState);
        // change double quotes to single because escaping causes problems on URL
        jsonViewState = jsonViewState.replace(/"/g, "'");
        jQuery.extend(data, {clientViewState: jsonViewState});
    }

    ksapAjaxSubmitForm(data, updateRefreshableComponentCallback, elementToBlock, id + "_form", elementBlockingSettings);

    //myplanAjaxSubmitForm(methodToCall, updateRefreshableComponentCallback, {reqComponentId: id, skipViewInit: "false"}, elementToBlock, id, elementBlockingSettings);
    jQuery("form#" + id + "_form").remove();
}

function getActivityEnrollment(url, retrieveOptions, componentId) {
    var elementToBlock = jQuery(".courseActivities__enrlData");
    if (componentId) elementToBlock = jQuery("#" + componentId + ".courseActivities__enrlData");
    jQuery.ajax({
        url: url,
        data: retrieveOptions,
        dataType: "json",
        beforeSend: function () {
            elementToBlock.block({
                message: '<img src="' + getConfigParam("ksapImageLocation") + 'loader/ajax_refresh.gif" alt="Fetching enrollment data..." />',
                fadeIn: 0,
                fadeOut: 0,
                overlayCSS: {
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
        },
        error: function () {
            elementToBlock.fadeOut(250);
            elementToBlock.each(function () {
                var image = jQuery("<img/>").attr({
                    'src': getConfigParam("ksapImageLocation") + "icons/warning.png",
                    'alt': "Oops, couldn't fetch the data. Refresh the page.",
                    'title': "Oops, couldn't fetch the data. Refresh the page."
                });
                jQuery(this).css("text-align", "center").html(image);
            });
            elementToBlock.fadeIn(250);
            elementToBlock.unblock();
        },
        success: function (response) {
            elementToBlock.fadeOut(250);
            jQuery.each(response, function (sectionId, enrlObject) {
                if (enrlObject.status && (enrlObject.status == "open" || enrlObject.status == "closed")) {
                    jQuery("#" + sectionId + ".courseActivities__enrlData").html('<div class="courseActivities__enrlData--' + enrlObject.status + '">' + enrlObject.status + '</div>');
                } else {
                    jQuery("#" + sectionId + ".courseActivities__enrlData").html('<div>--</div>');
                }
                var message = "<strong>" + enrlObject.enrollCount + "</strong> / " + enrlObject.enrollMaximum;
                var title = enrlObject.enrollCount + " enrolled out of " + enrlObject.enrollMaximum;
                if (enrlObject.enrollEstimate) {
                    message += "E";
                    title += " estimated";
                }
                title += " limit. Updated few minutes ago.";
                var data = jQuery("<div />").attr({
                    'title': title,
                    'class': 'courseActivities__enrlData--light'
                }).html(message);
                jQuery("#" + sectionId + ".courseActivities__enrlData").append(data);
            });
            elementToBlock.fadeIn(250);
            elementToBlock.unblock();
        }
    });
}