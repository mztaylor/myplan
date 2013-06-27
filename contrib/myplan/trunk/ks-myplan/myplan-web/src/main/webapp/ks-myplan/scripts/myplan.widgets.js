var popupOptionsDefault = {
    themePath:'../ks-myplan/jquery-popover/jquerypopover-theme/',
    manageMouseEvents:true,
    selectable:true,
    tail:{align:'middle', hidden:false},
    position:'left',
    align:'center',
    alwaysVisible:false,
    themeMargins:{total:'20px', difference:'5px'},
    themeName:'myplan',
    distance:'0px',
    openingSpeed:50,
    closingSpeed:50
};

function readUrlHash(key) {
    if (window.location.href.split("#")[1]) {
        var aHash = window.location.href.split("#")[1].replace('#', '').split('&');
        var oHash = {};
        jQuery.each(aHash, function (index, value) {
            oHash[value.split('=')[0]] = value.split('=')[1];
        });
        if (oHash[key]) {
            if (decodeURIComponent(oHash[key]) == "true" || decodeURIComponent(oHash[key]) == "false") {
                return (decodeURIComponent(oHash[key]) == "true");
            } else {
                return decodeURIComponent(oHash[key]);
            }
        } else {
            return false;
        }
    } else {
        return false;
    }
}

function setUrlHash(key, value) {
    var aHash = [];
    if (window.location.href.split("#")[1]) {
        aHash = window.location.href.split("#")[1].replace('#', '').split('&');
    }
    var oHash = {};
    if (aHash.length > 0) {
        jQuery.each(aHash, function (index, value) {
            oHash[decodeURIComponent(value.split('=')[0])] = decodeURIComponent(value.split('=')[1]);
        });
        var oTemp = {};
        oTemp[key] = value;
        jQuery.extend(oHash, oTemp);
    } else {
        oHash[key] = value;
    }
    aHash = [];
    for (var key in oHash) {
        if (key !== "" && oHash[key] !== "") aHash.push(encodeURIComponent(key) + "=" + encodeURIComponent(oHash[key]));
    }
    window.location.replace("#" + aHash.join("&"));
}

function readUrlParam(key) {
    var aParams = window.location.search.replace('?', '').split('&');
    var oParams = {};
    jQuery.each(aParams, function (index, value) {
        oParams[value.split('=')[0]] = value.split('=')[1];
    });
    if (oParams[key]) {
        return decodeURIComponent(oParams[key]);
    } else {
        return false;
    }
}

function setUrlParam(key, value) {
    var aParams = [];
    if (window.location.search) {
        aParams = window.location.search.replace('?', '').split('&');
    }
    var oParams = {};
    if (aParams.length > 0) {
        jQuery.each(aParams, function (index, value) {
            oParams[value.split('=')[0]] = value.split('=')[1];
        });
        var oTemp = {};
        oTemp[key] = value;
        jQuery.extend(oParams, oTemp);
    } else {
        oParams[key] = value;
    }
    aParams = [];
    for (var key in oParams) {
        if (key != "" && oParams[key] != "") aParams.push(encodeURIComponent(key) + "=" + encodeURIComponent(oParams[key]));
    }
    window.location.replace(window.location.protocol + "//" + window.location.host + window.location.pathname + "?" + aParams.join("&"));
}

/* This is for DOM changes to refresh the view on back to keep the view updated */
if (readUrlHash("modified")) {
    var url = window.location.href;
    var aHash = window.location.href.split("#")[1].replace("#", "").split("&");
    aHash.splice("modified=true", 1);
    window.location.assign(url.split("#")[0] + ((aHash.length > 0) ? "#" + aHash.join("&") : ""));
}

jQuery(document).ready(function () {
    jQuery("head").append('<!--[if ie 9]><style type="text/css" media="screen"> \
        button.uif-primaryActionButton,button.uif-secondaryActionButton, \
        button.uif-primaryActionButton:hover,button.uif-secondaryActionButton:hover,\
        button.uif-primaryActionButton[disabled="true"],\
        button.uif-primaryActionButton[disabled="disabled"],\
        button.uif-primaryActionButton[disabled="true"]:hover,\
        button.uif-primaryActionButton[disabled="disabled"]:hover,\
        button.uif-secondaryActionButton[disabled="true"],\
        button.uif-secondaryActionButton[disabled="disabled"],\
        button.uif-secondaryActionButton[disabled="true"]:hover,\
        button.uif-secondaryActionButton[disabled="disabled"]:hover{ \
            filter:none !important;}</style><![endif]-->\
    ');
});

function sessionExpired() {
    window.location = '/student/myplan/sessionExpired';
}

function stopEvent(e) {
    if (!e) var e = window.event;
    if (e.stopPropagation) {
        e.preventDefault();
        e.stopPropagation();
    } else {
        e.returnValue = false;
        e.cancelBubble = true;
    }
    return false;
}

function openDocument(url) {
    var newUrl;
    if (url.substring(0,4) == "http") {
        newUrl = url;
    } else {
        newUrl = window.location.protocol + "//" + window.location.host + window.location.pathname.substring(0, window.location.pathname.lastIndexOf("/")) + "/" + url;
    }
    if (newUrl == window.location.href) {
        window.location.reload(true);
    } else {
        window.location.assign(newUrl);
    }
}

function openCourse(courseId, e) {
    stopEvent(e);
    var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    if (jQuery(target).parents(".jquerypopover.jquerypopover-myplan").length > 0) {
        window.location = "inquiry?methodToCall=start&viewId=CourseDetails-InquiryView&courseId=" + courseId;
    } else {
        openPlanItemPopUp(courseId, 'add_remove_course_popover_page', {courseId:courseId}, e, null, {tail:{align:'left'}, align:'left', position:'bottom', alwaysVisible:'false'}, true);
    }
}
/*
 ######################################################################################
 Function: Launch generic bubble popup
 ######################################################################################
 */
function openPopUp(id, getId, methodToCall, action, retrieveOptions, e, selector, popupStyles, popupOptions, close) {
    stopEvent(e);

    var popupBox;
    var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    if (selector === null) {
        popupBox = jQuery(target);
    } else {
        popupBox = jQuery(target).parents(selector);
    }

    fnCloseAllPopups();

    if (!popupBox.HasPopOver()) popupBox.CreatePopOver({manageMouseEvents:false});

    var popupHtml = jQuery('<div />').attr("id", id + "_popup");
    if (popupStyles) {
        jQuery.each(popupStyles, function (property, value) {
            jQuery(popupHtml).css(property, value);
        });
    }

    var popupSettings = jQuery.extend(popupOptionsDefault, popupOptions);
    popupSettings.innerHtml = popupHtml.wrap("<div>").parent().clone().html();

    popupBox.ShowPopOver(popupSettings, false);
    var popupBoxId = popupBox.GetPopOverID();
    popupBox.FreezePopOver();

    clickOutsidePopOver(popupBoxId, popupBox);

    var tempForm = jQuery('<form />').attr("id", id + "_form").attr("action", action).attr("method", "post").hide();
    var tempFormInputs = '<div style="display:none;">';
    jQuery.each(retrieveOptions, function (name, value) {
        tempFormInputs += '<input type="hidden" name="' + name + '" value="' + value + '" />';
    });
    tempFormInputs += '</div>';
    jQuery(tempForm).append(tempFormInputs);
    jQuery("body").append(tempForm);

    var elementToBlock = jQuery("#" + id + "_popup");

    var updateRefreshableComponentCallback = function (htmlContent) {
        var component;
        if (jQuery("span#request_status_item_key_control", htmlContent).length <= 0) {
            component = jQuery("#" + getId, htmlContent);
        } else {
            eval(jQuery("input[data-for='plan_item_action_response_page']", htmlContent).val().replace("#plan_item_action_response_page", "body"));
            var sError = '<img src="/student/ks-myplan/images/pixel.gif" alt="" class="icon"><span class="message">' + jQuery('body').data('validationMessages').serverErrors[0] + '</span>';
            component = jQuery("<div />").html(sError).addClass("myplan-feedback error").width(175);
        }
        elementToBlock.unblock({onUnblock:function () {
            if (jQuery("#" + id + "_popup").length) {
                popupBox.SetPopOverInnerHtml(component);
                if (close || typeof close === 'undefined') jQuery("#" + popupBoxId + " .jquerypopover-innerHtml").append('<img src="../ks-myplan/images/btnClose.png" class="myplan-popup-close"/>');
                jQuery("#" + popupBoxId + " img.myplan-popup-close").on('click', function () {
                    popupBox.HidePopOver();
                    fnCloseAllPopups();
                });
            }
            runHiddenScripts(getId);
        }});
    };

    myplanAjaxSubmitForm(methodToCall, updateRefreshableComponentCallback, {reqComponentId:id, skipViewInit:"false"}, elementToBlock, id);
    jQuery("form#" + id + "_form").remove();
}


function openMenu(id, getId, atpId, e, selector, popupClasses, popupOptions, close) {
    stopEvent(e);

    var popupBox;
    var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    if (selector === null) {
        popupBox = jQuery(target);
    } else {
        popupBox = jQuery(target).parents(selector);
    }

    fnCloseAllPopups();

    if (!popupBox.HasPopOver()) popupBox.CreatePopOver({manageMouseEvents:false});

    if (atpId != null) {
        var openForPlanning = jQuery('input[id^="' + atpId + '_plan_status"]').val();
        if (openForPlanning == "false" && getId != "completed_menu_items") {
            getId = "completed_backup_menu_items";
        }
    }
    var popupHtml = jQuery('<div />').attr("id", id + "_popup").attr("class", popupClasses).html(jQuery("#" + getId).html());

    var popupSettings = jQuery.extend(popupOptionsDefault, popupOptions);
    popupSettings.innerHtml = popupHtml.wrap("<div>").parent().clone().html();

    popupBox.ShowPopOver(popupSettings, false);
    var popupBoxId = popupBox.GetPopOverID();
    popupBox.FreezePopOver();

    jQuery("#" + id + "_popup a").each(function () {
        var linkId = jQuery(this).attr("id");
        jQuery(this).siblings("input[data-for='" + linkId + "']").removeAttr("script").attr("name", "script").val(function (index, value) {
            return value.replace("'" + linkId + "'", "'" + linkId + "_popup'");
        });
        jQuery(this).attr("id", linkId + "_popup");
        jQuery.each(jQuery(target).data(), function (key, value) {
            jQuery("#" + linkId + "_popup").attr("data-" + key, value);
        });
    });

    runHiddenScripts(id + "_popup");

    clickOutsidePopOver(popupBoxId, popupBox);
}

/*
 ######################################################################################
 Function: Launch generic bubble popup
 ######################################################################################
 */
function openPopUpForm(id, getId, methodToCall, action, retrieveOptions, e, selector, popupStyles, popupOptions, close) {
    stopEvent(e);

    var popupBox;
    var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    if (selector === null) {
        popupBox = jQuery(target);
    } else {
        popupBox = jQuery(target).parents(selector);
    }

    fnCloseAllPopups();

    if (!popupBox.HasPopOver()) popupBox.CreatePopOver({manageMouseEvents:false});

    var popupHtml = jQuery('<div />').attr("id", id + "_popup");

    if (popupStyles) {
        jQuery.each(popupStyles, function (property, value) {
            jQuery(popupHtml).css(property, value);
        });
    }

    var popupSettings = jQuery.extend(popupOptionsDefault, popupOptions);
    popupSettings.innerHtml = popupHtml.wrap("<div>").parent().clone().html();

    popupBox.ShowPopOver(popupSettings, false);
    var popupBoxId = popupBox.GetPopOverID();
    popupBox.FreezePopOver();

    clickOutsidePopOver(popupBoxId, popupBox);

    var tempForm = jQuery('<form />').attr("id", id + "_form").attr("action", action).attr("method", "post").hide();
    var tempFormInputs = '<div style="display:none;">';
    jQuery.each(retrieveOptions, function (name, value) {
        tempFormInputs += '<input type="hidden" name="' + name + '" value="' + value + '" />';
    });
    tempFormInputs += '</div>';
    jQuery(tempForm).append(tempFormInputs);
    jQuery("body").append(tempForm);

    var elementToBlock = jQuery("#" + id + "_popup");

    var updateRefreshableComponentCallback = function (htmlContent) {
        var component;
        if (jQuery("span#request_status_item_key_control", htmlContent).length <= 0) {
            component = jQuery("#" + getId, htmlContent);
            var planForm = jQuery('<form />').attr("id", id + "_form").attr("action", "plan").attr("method", "post");
        } else {
            eval(jQuery("input[data-for='plan_item_action_response_page']", htmlContent).val().replace("#plan_item_action_response_page", "body"));
            var sError = '<img src="/student/ks-myplan/images/pixel.gif" alt="" class="icon"><span class="message">' + jQuery('body').data('validationMessages').serverErrors[0] + '</span>';
            component = jQuery("<div />").html(sError).addClass("myplan-feedback error").width(175);
        }
        elementToBlock.unblock({onUnblock:function () {
            if (jQuery("#" + id + "_popup").length) {
                popupBox.SetPopOverInnerHtml(component);
                jQuery("#" + popupBoxId + " .jquerypopover-innerHtml").wrapInner(planForm);
                if (close || typeof close === 'undefined') jQuery("#" + popupBoxId + " .jquerypopover-innerHtml").append('<img src="../ks-myplan/images/btnClose.png" class="myplan-popup-close"/>');
                jQuery("#" + popupBoxId + " img.myplan-popup-close").on('click', function () {
                    popupBox.HidePopOver();
                    fnCloseAllPopups();
                });
            }
            runHiddenScripts(getId);
        }});
    };

    myplanAjaxSubmitForm(methodToCall, updateRefreshableComponentCallback, {reqComponentId:id, skipViewInit:"false"}, elementToBlock, id);
    jQuery("form#" + id + "_form").remove();
}


/*
 ######################################################################################
 Function: Launch plan item bubble popup
 ######################################################################################
 */
function openPlanItemPopUp(id, getId, retrieveOptions, e, selector, popupOptions, close) {
    stopEvent(e);

    var popupBox;
    var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    if (selector === null) {
        popupBox = jQuery(target);
    } else {
        popupBox = jQuery(target).parents(selector);
    }

    fnCloseAllPopups();

    if (!popupBox.HasPopOver()) popupBox.CreatePopOver({manageMouseEvents:false});

    var popupHtml = jQuery('<div />').attr("id", id + "_popup").css({
        width:"300px",
        height:"16px"
    });

    var popupSettings = jQuery.extend(popupOptionsDefault, popupOptions);
    popupSettings.innerHtml = popupHtml.wrap("<div>").parent().clone().html();

    popupBox.ShowPopOver(popupSettings, false);
    var popupBoxId = popupBox.GetPopOverID();
    fnPositionPopUp(popupBoxId);
    popupBox.FreezePopOver();

    clickOutsidePopOver(popupBoxId, popupBox);

    var tempForm = jQuery('<form />').attr("id", id + "_form").attr("action", "plan").attr("method", "post").hide();
    var tempFormInputs = '<div style="display:none;"><input type="hidden" name="viewId" value="PlannedCourse-FormView" />';
    jQuery.each(retrieveOptions, function (name, value) {
        tempFormInputs += '<input type="hidden" name="' + name + '" value="' + value + '" />';
    });
    tempFormInputs += '</div>';
    jQuery(tempForm).append(tempFormInputs);
    jQuery("body").append(tempForm);

    var elementToBlock = jQuery("#" + id + "_popup");

    var updateRefreshableComponentCallback = function (htmlContent) {
        var component;
        if (jQuery("span#request_status_item_key_control", htmlContent).length <= 0) {
            component = jQuery("#" + getId, htmlContent);
            var planForm = jQuery('<form />').attr("id", id + "_form").attr("action", "plan").attr("method", "post");
        } else {
            eval(jQuery("input[data-for='plan_item_action_response_page']", htmlContent).val().replace("#plan_item_action_response_page", "body"));
            var sError = '<img src="/student/ks-myplan/images/pixel.gif" alt="" class="icon"><span class="message">' + jQuery('body').data('validationMessages').serverErrors[0] + '</span>';
            component = jQuery("<div />").html(sError).addClass("myplan-feedback error").width(175);
        }
        elementToBlock.unblock({onUnblock:function () {
            if (jQuery("#" + id + "_popup").length) {
                popupBox.SetPopOverInnerHtml(component);
                fnPositionPopUp(popupBoxId);
                if (status != 'error') jQuery("#" + popupBoxId + " .jquerypopover-innerHtml").wrapInner(planForm);
                if (close || typeof close === 'undefined') jQuery("#" + popupBoxId + " .jquerypopover-innerHtml").append('<img src="../ks-myplan/images/btnClose.png" class="myplan-popup-close"/>');
                jQuery("#" + popupBoxId + " img.myplan-popup-close").on('click', function () {
                    popupBox.HidePopOver();
                    fnCloseAllPopups();
                });
            }
            runHiddenScripts(getId);
        }});
    };

    myplanAjaxSubmitForm("startAddPlannedCourseForm", updateRefreshableComponentCallback, {reqComponentId:id, skipViewInit:"false"}, elementToBlock, id);
    jQuery("form#" + id + "_form").remove();
}
function openDialog(sText, e, close) {
    stopEvent(e);

    var popupBox = jQuery("body");

    fnCloseAllPopups();

    if (!popupBox.HasPopOver()) popupBox.CreatePopOver({manageMouseEvents:false});

    popupOptionsDefault.tail.hidden = true;
    popupOptionsDefault.innerHtml = '<div style="width:300px;">' + sText + '</div>';

    popupBox.ShowPopOver(popupOptionsDefault, false);
    var popupBoxId = popupBox.GetPopOverID();
    popupBox.FreezePopOver();

    if (close || typeof close === 'undefined') jQuery("#" + popupBoxId + " .jquerypopover-innerHtml").append('<img src="../ks-myplan/images/btnClose.png" class="myplan-popup-close"/>');

    fnPositionPopUp(popupBoxId);

    clickOutsidePopOver(popupBoxId, popupBox);

    jQuery("#" + popupBoxId + " img.myplan-popup-close").on('click', function () {
        popupBox.HidePopOver();
        fnCloseAllPopups();
    });
}

function fnPositionPopUp(popupBoxId) {
    if (parseFloat(jQuery("#" + popupBoxId).css("top")) < 0 || parseFloat(jQuery("#" + popupBoxId).css("left")) < 0) {
        var top = (document.documentElement && document.documentElement.scrollTop) || document.body.scrollTop;
        var left = (document.documentElement && document.documentElement.scrollLeft) || document.body.scrollLeft;
        var iTop = ( top + ( jQuery(window).height() / 2 ) ) - ( jQuery("#" + popupBoxId).height() / 2 );
        var iLeft = ( left + ( jQuery(window).width() / 2 ) ) - ( jQuery("#" + popupBoxId).width() / 2 );
        jQuery("#" + popupBoxId).css({top:iTop + 'px', left:iLeft + 'px'});
    }
}


function myplanWriteHiddenToForm(propertyName, propertyValue, formId) {
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
 Function: Submit
 ######################################################################################
 */
function myplanAjaxSubmitPlanItem(id, type, methodToCall, e, bDialog) {
    var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    var targetText = ( jQuery.trim(jQuery(target).text()) != '') ? jQuery.trim(jQuery(target).text()) : "Error";
    var elementToBlock = (target.nodeName != 'INPUT') ? jQuery(target) : jQuery(target).parent();
    jQuery('input[name="methodToCall"]').remove();
    jQuery('#' + id + '_form input[name="' + type + '"]').remove();
    jQuery('#' + id + '_form input[name="viewId"]').remove();
    jQuery("#" + id + "_form").append('<input type="hidden" name="methodToCall" value="' + methodToCall + '" /><input type="hidden" name="' + type + '" value="' + id + '" /><input type="hidden" name="viewId" value="PlannedCourse-FormView" />');
    var updateRefreshableComponentCallback = function (htmlContent) {
        var status = jQuery.trim(jQuery("span#request_status_item_key_control", htmlContent).text().toLowerCase());
        eval(jQuery("input[data-for='plan_item_action_response_page']", htmlContent).val().replace("#plan_item_action_response_page", "body"));
        elementToBlock.unblock();
        switch (status) {
            case 'success':
                var oMessage = { 'message':'<img src="/student/ks-myplan/images/pixel.gif" alt="" class="icon"><span class="message">' + jQuery('body').data('validationMessages').serverInfo[0] + '</span>', 'cssClass':'myplan-feedback success' };
                var json = jQuery.parseJSON(jQuery.trim(jQuery("span#json_events_item_key_control", htmlContent).text()));
                for (var key in json) {
                    if (json.hasOwnProperty(key)) {
                        eval('jQuery.publish("' + key + '", [' + JSON.stringify(jQuery.extend(json[key], oMessage)) + ']);');
                    }
                }
                setUrlHash('modified', 'true');
                break;
            case 'error':
                var oMessage = { 'message':'<img src="/student/ks-myplan/images/pixel.gif" alt="" class="icon"><span class="message">' + jQuery('body').data('validationMessages').serverErrors[0] + '</span>', 'cssClass':'myplan-feedback error' };
                if (!bDialog) {
                    var sContent = jQuery("<div />").append(oMessage.message).addClass("myplan-feedback error").css({"background-color":"#fff"});
                    var sHtml = jQuery("<div />").append('<div class="uif-headerField uif-sectionHeaderField"><h3 class="uif-header">' + targetText + '</h3></div>').append(sContent);
                    if (jQuery("body").HasPopOver()) jQuery("body").HidePopOver();
                    openDialog(sHtml.html(), e);
                } else {
                    eval('jQuery.publish("ERROR", [' + JSON.stringify(oMessage) + ']);');
                }
                break;
        }
    };
    var blockOptions = {
        message:'<img src="../ks-myplan/images/btnLoader.gif"/>',
        css:{
            width:'100%',
            border:'none',
            backgroundColor:'transparent'
        },
        overlayCSS:{
            backgroundColor:'#fff',
            opacity:0.6,
            padding:'0px 1px',
            margin:'0px -1px'
        }
    };
    myplanAjaxSubmitForm(methodToCall, updateRefreshableComponentCallback, {reqComponentId:id, skipViewInit:'false'}, elementToBlock, id, blockOptions);
}

/*Function used for moving the plan Item from planned to backup*/
function myPlanAjaxPlanItemMove(id, type, methodToCall, e) {
    stopEvent(e);
    var tempForm = jQuery('<form />').attr("id", id + "_form").attr("action", "plan").attr("method", "post").hide();
    jQuery("body").append(tempForm);
    myplanAjaxSubmitPlanItem(id, type, methodToCall, e, false);
    fnCloseAllPopups();
    jQuery("form#" + id + "_form").remove();
}

function myplanAjaxSubmitSectionItem(id, methodToCall, action, formData, e) {
    stopEvent(e);
    fnCloseAllPopups();
    var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    var targetText = ( jQuery.trim(jQuery(target).text()) != '') ? jQuery.trim(jQuery(target).text()) : "Error";
    var elementToBlock = (target.nodeName != 'INPUT') ? jQuery(target) : jQuery(target).parent();
    var tempForm = '<form id="' + id + '_form" action="' + action + '" method="post" style="display:none;">';
    tempForm += '<input type="hidden" name="methodToCall" value="' + methodToCall + '" />';
    jQuery.each(formData, function (name, value) {
        tempForm += '<input type="hidden" name="' + name + '" value="' + value + '" />';
    });
    tempForm += '</form>';
    jQuery("body").append(tempForm);

    var updateRefreshableComponentCallback = function (htmlContent) {
        var status = jQuery.trim(jQuery("span#request_status_item_key_control", htmlContent).text().toLowerCase());
        eval(jQuery("input[data-for='plan_item_action_response_page']", htmlContent).val().replace("#plan_item_action_response_page", "body"));
        elementToBlock.unblock();
        switch (status) {
            case 'success':
                var oMessage = { 'message':'<img src="/student/ks-myplan/images/pixel.gif" alt="" class="icon"><span class="message">' + jQuery('body').data('validationMessages').serverInfo[0] + '</span>', 'cssClass':'myplan-feedback success' };
                var json = jQuery.parseJSON(jQuery.trim(jQuery("span#json_events_item_key_control", htmlContent).text()));
                for (var key in json) {
                    if (json.hasOwnProperty(key)) {
                        eval('jQuery.publish("' + key + '", [' + JSON.stringify(jQuery.extend(json[key], oMessage)) + ']);');
                    }
                }
                setUrlHash('modified', 'true');
                break;
            case 'error':
                var oMessage = { 'message':'<img src="/student/ks-myplan/images/pixel.gif" alt="" class="icon"><span class="message">' + jQuery('body').data('validationMessages').serverErrors[0] + '</span>', 'cssClass':'myplan-feedback error' };
                var sContent = jQuery("<div />").append(oMessage.message).addClass("myplan-feedback error").css({"background-color":"#fff"});
                var sHtml = jQuery("<div />").append('<div class="uif-headerField uif-sectionHeaderField"><h3 class="uif-header">' + targetText + '</h3></div>').append(sContent);
                if (jQuery("body").HasPopOver()) jQuery("body").HidePopOver();
                openDialog(sHtml.html(), e, true);
                break;
        }
    };
    var blockOptions = {
        message:'<img src="../ks-myplan/images/btnLoader.gif"/>',
        css:{
            width:'100%',
            border:'none',
            backgroundColor:'transparent'
        },
        overlayCSS:{
            backgroundColor:'#fff',
            opacity:0.6,
            padding:'0px 1px',
            margin:'0px -1px'
        }
    };
    myplanAjaxSubmitForm(methodToCall, updateRefreshableComponentCallback, {reqComponentId:id, skipViewInit:'false'}, elementToBlock, id, blockOptions);
    jQuery("form#" + id + "_form").remove();
}
/*
 ######################################################################################
 Function: Retrieve component content through ajax
 ######################################################################################
 */
function myplanRetrieveComponent(id, getId, methodToCall, action, retrieveOptions, highlightId, elementBlockingSettings) {
    var tempForm = '<form id="' + id + '_form" action="' + action + '" method="post" style="display:none;">'; //jQuery('<form />').attr("id", id + "_form").attr("action", action).attr("method", "post").hide();
    jQuery.each(retrieveOptions, function (name, value) {
        tempForm += '<input type="hidden" name="' + name + '" value="' + value + '" />';
    });
    tempForm += '</form>';
    jQuery("body").append(tempForm);

    var elementToBlock = jQuery("#" + id);

    var updateRefreshableComponentCallback = function (htmlContent) {
        var component = jQuery("#" + getId, htmlContent);
        elementToBlock.unblock({onUnblock:function () {
            // replace component
            if (jQuery("#" + id).length) {
                jQuery("#" + id).replaceWith(component);
            }

            runHiddenScripts(getId);

            if (jQuery("input[data-role='script'][data-for='" + getId + "']", htmlContent).length > 0) {
                eval(jQuery("input[data-role='script'][data-for='" + getId + "']", htmlContent).val());
            }

            if (highlightId) {
                jQuery("[id^='" + highlightId + "']").parents('li').animate({backgroundColor:"#ffffcc"}, 1).animate({backgroundColor:"#ffffff"}, 1500, function () {
                    jQuery(this).removeAttr("style");
                });
            }
        }});
    };

    if (!methodToCall) {
        methodToCall = "search";
    }

    myplanAjaxSubmitForm(methodToCall, updateRefreshableComponentCallback, {reqComponentId:id, skipViewInit:"false"}, elementToBlock, id, elementBlockingSettings);
    jQuery("form#" + id + "_form").remove();
}
/*
 ######################################################################################
 Function:   KRAD's ajax submit function modified to allow submission of a form
 other then the kuali form
 ######################################################################################
 */
function myplanAjaxSubmitForm(methodToCall, successCallback, additionalData, elementToBlock, formId, elementBlockingSettings) {
    var data = {};

    // methodToCall checks
    if (methodToCall == null) {
        var methodToCallInput = jQuery("input[name='methodToCall']");
        if (methodToCallInput.length > 0) {
            methodToCall = jQuery("input[name='methodToCall']").val();
        }
    }

    // check to see if methodToCall is still null
    if (methodToCall != null || methodToCall !== "") {
        data.methodToCall = methodToCall;
    }

    data.renderFullView = false;

    // remove this since the methodToCall was passed in or extracted from the page, to avoid issues
    jQuery("input[name='methodToCall']").remove();

    if (additionalData != null) {
        jQuery.extend(data, additionalData);
    }

    var viewState = jQuery(document).data(kradVariables.VIEW_STATE);
    if (!jQuery.isEmptyObject(viewState)) {
        var jsonViewState = jQuery.toJSON(viewState);

        // change double quotes to single because escaping causes problems on URL
        jsonViewState = jsonViewState.replace(/"/g, "'");
        jQuery.extend(data, {clientViewState:jsonViewState});
    }

    var submitOptions = {
        data:data,
        success:function (response) {
            var tempDiv = document.createElement('div');
            tempDiv.innerHTML = response;
            var hasError = checkForIncidentReport(response);
            if (!hasError) {
                successCallback(tempDiv);
            }
            jQuery("#formComplete").empty();
        },
        error:function (jqXHR, textStatus) {
            alert("Request failed: " + textStatus);
        }
    };

    if (elementToBlock != null && elementToBlock.length) {
        var elementBlockingOptions = {
            beforeSend:function () {
                if (elementToBlock.hasClass("unrendered")) {
                    elementToBlock.append('<img src="' + getConfigParam("kradImageLocation") + 'loader.gif" alt="Loading..." /> Loading...');
                    elementToBlock.show();
                }
                else {
                    var elementBlockingDefaults = {
                        baseZ:500,
                        message:'<img src="../ks-myplan/images/ajaxLoader16.gif" alt="loading..." />',
                        fadeIn:0,
                        fadeOut:0,
                        overlayCSS:{
                            backgroundColor:'#fff',
                            opacity:0
                        },
                        css:{
                            border:'none',
                            width:'16px',
                            top:'0px',
                            left:'0px'
                        }
                    };
                    elementToBlock.block(jQuery.extend(elementBlockingDefaults, elementBlockingSettings));
                }
            },
            complete:function () {
                elementToBlock.unblock();
            },
            error:function () {
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
    var form;
    if (formId) {
        form = jQuery("#" + formId + "_form");
    } else {
        form = jQuery("#kualiForm");
    }
    form.ajaxSubmit(submitOptions);
}
/*
 ######################################################################################
 Function:   Truncate (ellipse) a single horizontally aligned item so all items
 fit on one line.
 ######################################################################################
 */
function truncateField(id, margin, floated) {
    jQuery("#" + id + " .uif-horizontalFieldGroup").each(function () {
        var itemSelector = ".uif-horizontalBoxGroup > .uif-horizontalBoxLayout > .uif-boxLayoutHorizontalItem";
        if (jQuery(this).find(itemSelector + ".myplan-text-ellipsis").length != 0) {
            jQuery(this).css("display", "block");
            var fixed = 0;
            jQuery(this).find(itemSelector + ":not(.myplan-text-ellipsis)").each(function () {
                fixed = fixed + jQuery(this).width();
            });
            var ellipsis = jQuery(this).width() - ( ( fixed + 1 ) + margin );
            if (!floated) {
                jQuery(this).find(itemSelector + ".myplan-text-ellipsis").width(ellipsis);
            } else {
                if (jQuery(this).find(itemSelector + ".myplan-text-ellipsis").width() >= ellipsis) {
                    jQuery(this).find(itemSelector + ".myplan-text-ellipsis").width(ellipsis);
                }
            }
        }
    });
}
function indicateViewingAudit(id, type) {
    var open = false;
    var currentAudit = jQuery("." + type + ".auditHtml .myplan-audit-report");
    var currentAuditId = currentAudit.attr("auditid");

    jQuery("#" + id + " .uif-collectionItem").each(function (index) {
        if (jQuery(this).attr("id") == currentAuditId && currentAudit.is(":visible")) {
            if (type == 'degreeAudit') {
                jQuery(this).find(".uif-label label").html("Viewing");
            }
            if (type == 'planAudit') {
                if (index > 1) open = true;
                jQuery(this).addClass("viewing");
            }
        } else {
            if (type == 'degreeAudit') {
                jQuery(this).find(".uif-label label").html("View");
            }
            if (type == 'planAudit') {
                jQuery(this).removeClass("viewing");
            }
        }
    });
    if (open) {
        jQuery("#plan_audit_toggle_link").click();
    }
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
        direction:direction
    }, 100, function () {
        jQuery("#" + parentId + " > .uif-horizontalBoxLayout > div.uif-boxLayoutHorizontalItem").filter("#" + showId).show("slide", {
            direction:newDirection
        }, 100, function () {
        });
    });
}

function clickOutsidePopOver(popoverId, element) {
    jQuery("body").on("click", function (e) {
        var tempTarget = (e.target) ? e.target : e.srcElement;
        if (jQuery(tempTarget).parents("#" + popoverId).length === 0) {
            element.HidePopOver();
            jQuery("body").off("click");
        }
    });
}
/*
 ######################################################################################
 Function:   Close all bubble popups
 ######################################################################################
 */
function fnCloseAllPopups() {
    if (jQuery("body").HasPopOver()) {
        jQuery("body").HidePopOver();
        jQuery("body").RemovePopOver();
    }
    jQuery("div.jquerypopover.jquerypopover-myplan").remove();
    jQuery("body").off("click");
}
/*
 ######################################################################################
 Function:   Build Term Plan View heading
 ######################################################################################
 */
function fnBuildTitle(aView) {
    var sText = 'Academic Year';
    var aFirst = jQuery.trim(jQuery(aView[0]).find("div:hidden[id^='plan_base_atpId']").text()).split(".");
    var aLast = jQuery.trim(jQuery(aView[aView.length - 1]).find("div:hidden[id^='plan_base_atpId']").text()).split(".");
    jQuery("#planned_courses_detail .myplan-plan-header").html(sText + ' ' + aFirst[3] + '-' + aLast[3]);
    var navigationAtpId = jQuery.trim(jQuery(aView[0]).find("div:hidden[id^='single_quarter_atpId']").text());
    //fnQuarterNavigation(navigationAtpId, 'single_quarter_button', 'planned_courses_detail');
    var quarterLink = "inquiry?methodToCall=start&viewId=SingleTerm-InquiryView&term_atp_id=" + navigationAtpId;
    jQuery("#single_quarter_button").attr("href", quarterLink);
}

/*
 ######################################################################################
 Function:   Dynamically Builds the Quarter Button Action
 ######################################################################################
 */
function fnQuarterNavigation(atpId, component) {
    if (atpId == "") {
        jQuery("#" + component).attr("href", "#").addClass('disabled').click(function (e) {
            e.preventDefault();
        });
    }
}
/*
 function fnQuarterNavigation(navigationAtpId, button, targetId) {
 if (navigationAtpId == "") {
 jQuery("#" + button).addClass('disabled');
 } else {
 jQuery("#" + button).unbind('click');
 var message = "<p><img src=\"../ks-myplan/images/ajaxAuditRunning32.gif\" alt=\"loading...\"/></p><p>Please wait while we fetch your quarter...</p>";
 var scriptValue = "jQuery('#" + button + "').click(function(e) {e.preventDefault();" +
 "if(jQuery(this).hasClass('disabled')){ return false;}myplanRetrieveComponent('" + targetId + "'," +
 "'single_quarter_items','start','inquiry', " +
 "{viewId:'SingleTerm-InquiryView',term_atp_id:'" + navigationAtpId + "'}, null, " +
 "{message:'" + message + "', fadeIn:0, fadeOut:0, overlayCSS:{backgroundColor:'#000',opacity:0.5,cursor:'wait'}," +
 " css:{left: '180px !important', top: '20px !important',backgroundColor:'#fffdd7', border:'solid 1px #ffd14c', borderRadius:'15px','-webkit-border-radius':'15px','-moz-border-radius':'15px'," +
 " width:'230px', textAlign:'center', padding:'20px'}});});"
 jQuery("input[data-for='" + button + "'][data-role='script']").attr('value', scriptValue);
 runHiddenScripts(button);
 }
 }
 */

function fnOneYearButtonAction() {
    var focusAtp = jQuery('#hidden_focusAtpId span.uif-readOnlyContent').text().trim();
    var retrieveOptions = {viewId:'PlannedCourses-LookupView', focusAtpId:focusAtp};
    myplanRetrieveComponent('single_quarter_items', 'planned_courses_detail', 'search', 'lookup', retrieveOptions, null, {message:'<p><img src="../ks-myplan/images/ajaxAuditRunning32.gif" alt="loading..." /></p><p>Please wait while we are fetching your plan...</p>', fadeIn:0, fadeOut:0, overlayCSS:{backgroundColor:'#000', opacity:0.5, cursor:'wait'}, css:{left:'180px !important', top:'20px !important', backgroundColor:'#fffdd7', border:'solid 1px #ffd14c', borderRadius:'15px', '-webkit-border-radius':'15px', '-moz-border-radius':'15px', width:'230px', textAlign:'center', padding:'20px'}});
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
        if (!jQuery(".fancybox-wrap", parent.document).length) {

            // Perform cleanup when lightbox is closed
            options['beforeClose'] = cleanupClosedLightboxForms;

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
            options['href'] = options['href'] + '&renderedInLightBox=true'
                + '&showHome=false' + '&showHistory=' + showHistory
                + '&history=' + jQuery('#formHistory\\.historyParameterString').val();
        }
    });
}

function myplanLightBoxLink(href, options, e) {
    stopEvent(e);
    var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    options['autoHeight'] = true;
    options['href'] = href;
    //options['beforeClose'] = cleanupClosedLightboxForms;
    top.jQuery.fancybox(options);
}

function auditButtonState(buttonId, blockAudit) {
    var button = jQuery("button#" + buttonId);
    var type = button.data("audittype");
    var disabled;
    if (jQuery.cookie("myplan_audit_running") || blockAudit) {
        disabled = true;
    } else {
        var id = getAuditProgram("id", type);
        if (id) {
            disabled = (id == "default");
        } else {
            disabled = true;
        }
    }
    if (disabled) {
        button.addClass("disabled").attr("disabled", true);
    } else {
        button.removeClass("disabled").attr("disabled", false);
    }
}

function getAuditProgram(param, type) {
    var campus;
    switch (parseFloat(jQuery("#" + type + "_param_campus input:checked").val())) {
        case 306:
            campus = "seattle";
            break;
        case 310:
            campus = "bothell";
            break;
        case 323:
            campus = "tacoma";
            break;
        default:
            campus = null;
    }
    if (param == 'id') {
        return jQuery("#" + type + "_param_program select#" + type + "_programs_" + campus + "_control").val();
    } else {
        return jQuery("#" + type + "_param_program select#" + type + "_programs_" + campus + "_control option:selected").text();
    }
}

var blockPendingAuditStyle = {
    message:'<img src="../ks-myplan/images/ajaxAuditRunning32.gif" alt="" class="icon"/><div class="heading">We are currently running your degree audit for \'<span class="programName"></span>\'.</div><div class="content">Audits may take 1-5 minutes to load. Feel free to leave this page to explore MyPlan further while your audit is running. You will receive a browser notification when your report is complete.</div>',
    fadeIn:250,
    fadeOut:250,
    css:{
        padding:'30px 30px 30px 82px',
        margin:'30px',
        width:'auto',
        textAlign:'left',
        border:'solid 1px #ffd14c',
        backgroundColor:'#fffdd7',
        'border-radius':'15px',
        '-webkit-border-radius':'15px',
        '-moz-border-radius':'15px'
    },
    overlayCSS:{
        backgroundColor:'#fff',
        opacity:0.85,
        border:'none',
        cursor:'wait'
    }
};

var replaceBlockPendingAudit;

function changeLoadingMessage(selector, programName) {
    replaceBlockPendingAudit = setInterval(function () {
        setLoadingMessage(selector, programName)
    }, 100);
}

function setLoadingMessage(selector, programName) {
    if (jQuery(selector + ' div.blockUI.blockMsg.blockElement').length > 0) {
        fnAddLoadingText(selector, programName);
    }
}

function fnAddLoadingText(selector, programName) {
    clearInterval(replaceBlockPendingAudit);
    jQuery(selector + " div.blockUI.blockOverlay").css(blockPendingAuditStyle.overlayCSS);
    jQuery(selector + " div.blockUI.blockMsg.blockElement").html(blockPendingAuditStyle.message).css(blockPendingAuditStyle.css).data("growl", "false");
    jQuery(selector + " div.blockUI.blockMsg.blockElement .programName").text(programName);
}

function removeCookie() {
    jQuery.cookie("myplan_audit_running", null, {expires:new Date().setTime(0)});
}

function setPendingAudit(obj, minutes) {
    if (jQuery.cookie('myplan_audit_running') == null) {
        var data = {};

        data.expires = new Date();
        data.expires.setTime(data.expires.getTime() + (minutes * 60 * 1000));
        data.programId = getAuditProgram('id', obj.data("audittype"));
        data.programName = getAuditProgram('name', obj.data("audittype"));
        data.recentAuditId = obj.data("recentauditid");
        data.auditType = obj.data("audittype");
        if (typeof data.recentAuditId === 'undefined') data.recentAuditId = '';

        if (data.programId != 'default') {
            changeLoadingMessage('.myplan-audit-report', data.programName);
            jQuery.ajax({
                url:"/student/myplan/audit/status",
                data:{"programId":data.programId, "auditId":data.recentAuditId},
                dataType:"json",
                beforeSend:null,
                success:function (response) {
                    if (response.status == "PENDING") {
                        jQuery.cookie('myplan_audit_running', JSON.stringify(data), {expires:data.expires});
                        auditButtonState(obj.attr("id"));
                        jQuery.publish('REFRESH_AUDITS', [
                            {"type":data.auditType}
                        ]);
                    }
                },
                statusCode:{ 500:function () {
                    sessionExpired();
                } }
            });
        }
    } else {
        showGrowl("Another audit is currently pending. Please allow audit to complete.", "Running Audit Error", "errorGrowl");
    }
}

function getPendingAudit(id, type) {
    if (jQuery.cookie('myplan_audit_running')) {
        var data = jQuery.parseJSON(decodeURIComponent(jQuery.cookie('myplan_audit_running')));
        if (type == data.auditType) {
            var component = jQuery("#" + id + " .uif-stackedCollectionLayout");
            if (data) {
                var item = jQuery("<div />").addClass("uif-collectionItem pending").html('<img src="../ks-myplan/images/ajaxPending16.gif" class="icon"/><span class="title">Running <span class="program">' + data.programName + '</span></span>');
                component.prepend(item);
                pollPendingAudit(data.programId, data.recentAuditId, data.auditType);
            }
            if (component.prev(".ksap-emptyCollection").length > 0) {
                component.prev(".ksap-emptyCollection").remove();
            }
        }
    }
}

function blockPendingAudit(data) {
    var id = "audit_section";
    if (data.auditType == "plan") id = "plan_audit_section";
    var elementToBlock = jQuery("#" + id);
    elementToBlock.block(blockPendingAuditStyle);
    jQuery("#" + id + " div.blockUI.blockMsg.blockElement").data("growl", "true");
    jQuery("#" + id + " div.blockUI.blockMsg.blockElement .programName").text(data.programName);
    jQuery("#" + id).subscribe('AUDIT_COMPLETE', function () {
        window.location.assign(window.location.href.split("#")[0] + "#" + data.auditType + "_audit_block_tab");
        window.location.reload(true);
    });
}

function pollPendingAudit(programId, recentAuditId, auditType) {
    jQuery.ajaxPollSettings.pollingType = "interval";
    jQuery.ajaxPollSettings.interval = 250; // polling interval in milliseconds

    jQuery.ajaxPoll({
        url:"/student/myplan/audit/status",
        data:{"programId":programId, "auditId":recentAuditId},
        dataType:"json",
        beforeSend:null,
        successCondition:function (response) {
            return (response.status == 'DONE' || response.status == 'FAILED' || jQuery.cookie("myplan_audit_running") == null);
        },
        success:function (response) {
            var growl = true;
            if (readUrlParam("viewId") == "DegreeAudit-FormView") {
                growl = jQuery(".myplan-audit-report div.blockUI.blockMsg.blockElement").data("growl");
                if (readUrlParam(auditType + "Audit.auditId") != false) jQuery("body").subscribe('AUDIT_COMPLETE', function () {
                    setUrlParam(auditType + "Audit.auditId", "");
                });
            }
            var title = "Degree Audit";
            var text = "audit";
            if (auditType == "plan") {
                title = "Plan Review";
                text = "review";
            }
            if (jQuery.cookie("myplan_audit_running") == null || response.status == 'FAILED') {
                if (growl) showGrowl("Your " + text + " was unable to complete.", title + " Error", "errorGrowl");
            } else {
                var data = jQuery.parseJSON(decodeURIComponent(jQuery.cookie("myplan_audit_running")));
                if (growl) showGrowl(data.programName + " " + text + " is ready to view.", title + " Completed", "infoGrowl");
            }
            jQuery.cookie("myplan_audit_running", null, {expires:new Date().setTime(0)});
            jQuery.publish("AUDIT_COMPLETE", [
                {"type":auditType}
            ]);
        }
    });
}

function buttonState(parentId, buttonId) {
    var disabled = false;
    var button = jQuery("button#" + buttonId);
    jQuery("#" + parentId + " .myplan-required").each(function () {
        var value;
        if (jQuery(this).val()) {
            value = jQuery(this).val().replace(/\n/g, '');
        } else {
            value = "";
        }
        if (value == "" || value == "default") {
            disabled = true;
        }
    });
    if (disabled) {
        button.addClass("disabled").attr("disabled", disabled);
    } else {
        button.removeClass("disabled").attr("disabled", disabled);
    }
}

(function ($) {

    $.fn.characterCount = function (options) {

        var oDefaults = {
            maxLength:100,
            warningLength:20,
            classCounter:'counter',
            classWarning:'warning'
        };

        var options = $.extend(oDefaults, options);

        function calculate(obj, options) {
            var iCount = $(obj).val().length;
            var iAvailable = options.maxLength - iCount;
            var sValue = $(obj).val();
            if (iCount > options.maxLength) {
                $(obj).val(sValue.substr(0, options.maxLength));
            }
            if (iAvailable <= options.warningLength && iAvailable >= 0) {
                $('.' + options.classCounter).addClass(options.classWarning);
            } else {
                $('.' + options.classCounter).removeClass(options.classWarning);
            }
            $('.' + options.classCounter).html('<strong>' + $(obj).val().length + '</strong> / ' + options.maxLength + ' characters');
        }

        ;

        this.each(function () {
            calculate(this, options);
            $(this).keyup(function () {
                calculate(this, options);
            });
            $(this).change(function () {
                calculate(this, options);
            });
        });
    };

})(jQuery);

function fnCreateDate(sData) {
    var jTemp = jQuery(sData);
    jTemp.find("legend, .myplan-sort-remove").remove();
    var sDate = jQuery.trim(jTemp.text());

    if (sDate.length > 2) {
        return Date.parse(sDate);
    } else {
        return 0;
    }
}

jQuery.fn.dataTableExt.oSort['longdate-asc'] = function (x, y) {
    x = fnCreateDate(x);
    y = fnCreateDate(y);

    return ((x < y) ? -1 : ((x > y) ? 1 : 0));
};
jQuery.fn.dataTableExt.oSort['longdate-desc'] = function (x, y) {
    x = fnCreateDate(x);
    y = fnCreateDate(y);

    return ((x < y) ? 1 : ((x > y) ? -1 : 0));
};
Array.max = function (array) {
    return Math.max.apply(Math, array);
};


/*Quick Add*/

function openQuickAddPopUp(id, getId, retrieveOptions, e, selector, popupOptions, close) {
    stopEvent(e);

    var popupBox;
    var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    if (selector === null) {
        popupBox = jQuery(target);
    } else {
        popupBox = jQuery(target).parents(selector);
    }

    fnCloseAllPopups();

    if (!popupBox.HasPopOver()) popupBox.CreatePopOver({manageMouseEvents:false});

    var popupHtml = jQuery('<div />').attr("id", id + "_popup").css({
        width:"300px", //width:"350px",
        // TODO: Keep at 300px until 1.4 tagged and trunk moves to MyPlan 1.5
        height:"16px"
    });

    var popupSettings = jQuery.extend(popupOptionsDefault, popupOptions);
    popupSettings.innerHtml = popupHtml.wrap("<div>").parent().clone().html()

    popupBox.ShowPopOver(popupSettings, false);
    var popupBoxId = popupBox.GetPopOverID();
    fnPositionPopUp(popupBoxId);
    popupBox.FreezePopOver();

    clickOutsidePopOver(popupBoxId, popupBox);

    var tempForm = jQuery('<form />').attr("id", id + "_form").attr("action", "quickAdd").attr("method", "post").hide();
    var tempFormInputs = '<div style="display:none;"><input type="hidden" name="viewId" value="QuickAdd-FormView" />';
    jQuery.each(retrieveOptions, function (name, value) {
        tempFormInputs += '<input type="hidden" name="' + name + '" value="' + value + '" />';
    });
    tempFormInputs += '</div>';
    jQuery(tempForm).append(tempFormInputs);
    jQuery("body").append(tempForm);

    var elementToBlock = jQuery("#" + id + "_popup");

    var updateRefreshableComponentCallback = function (htmlContent) {
        var component;
        if (jQuery("span#request_status_item_key_control", htmlContent).length <= 0) {
            component = jQuery("#" + getId, htmlContent);
            var quickAddForm = jQuery('<form />').attr("id", id + "_form").attr("action", "quickAdd").attr("method", "post");
        } else {
            eval(jQuery("input[data-for='quick_add_action_response_page']", htmlContent).val().replace("#quick_add_action_response_page", "body"));
            var sError = '<img src="../ks-myplan/images/pixel.gif" alt="" class="icon"/><span class="message">' + jQuery('body').data('validationMessages').serverErrors[0] + '</span>';
            component = jQuery("<div />").html(sError).addClass("myplan-feedback error").width(175);
        }
        elementToBlock.unblock({onUnblock:function () {
            if (jQuery("#" + id + "_popup").length) {
                popupBox.SetPopOverInnerHtml(component);
                fnPositionPopUp(popupBoxId);
                if (status != 'error') jQuery("#" + popupBoxId + " .jquerypopover-innerHtml").wrapInner(quickAddForm);
                if (close || typeof close === 'undefined') jQuery("#" + popupBoxId + " .jquerypopover-innerHtml").append('<img src="../ks-myplan/images/btnClose.png" class="myplan-popup-close"/>');
                jQuery("#" + popupBoxId + " img.myplan-popup-close").on('click', function () {
                    popupBox.HidePopOver();
                    fnCloseAllPopups();
                });
            }
            runHiddenScripts(getId);
        }});
    };

    myplanAjaxSubmitForm("start", updateRefreshableComponentCallback, {reqComponentId:id, skipViewInit:"false"}, elementToBlock, id);
    jQuery("form#" + id + "_form").remove();
}

function myplanAjaxSubmitQuickAdd(id, submitOptions, methodToCall, e, bDialog) {
    var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    var targetText = ( jQuery.trim(jQuery(target).text()) != '') ? jQuery.trim(jQuery(target).text()) : "Error";
    var elementToBlock = (target.nodeName != 'INPUT') ? jQuery(target) : jQuery(target).parent();
    jQuery('input[name="methodToCall"]').remove();
    jQuery('#' + id + '_form input[name="viewId"]').remove();
    var formInputs = '<div style="display:none;"><input type="hidden" name="methodToCall" value="' + methodToCall + '" /><input type="hidden" name="viewId" value="QuickAdd-FormView" />';
    jQuery.each(submitOptions, function (name, value) {
        formInputs += '<input type="hidden" name="' + name + '" value="' + value + '" />';
        jQuery('#' + id + '_form input[name="' + name + '"]').remove();
    });
    formInputs += '</div>';
    jQuery("#" + id + "_form").append(formInputs);
    var updateRefreshableComponentCallback = function (htmlContent) {
        var status = jQuery.trim(jQuery("span#request_status_item_key_control", htmlContent).text().toLowerCase());
        eval(jQuery("input[data-for='quick_add_action_response_page']", htmlContent).val().replace("#quick_add_action_response_page", "body"));
        elementToBlock.unblock();
        switch (status) {
            case 'success':
                var oMessage = { 'message':'<img src="../ks-myplan/images/pixel.gif" alt="" class="icon"><span class="message">' + jQuery('body').data('validationMessages').serverInfo[0] + '</span>', 'cssClass':'myplan-feedback success' };
                var json = jQuery.parseJSON(jQuery.trim(jQuery("span#json_events_item_key_control", htmlContent).text()));
                for (var key in json) {
                    if (json.hasOwnProperty(key)) {
                        eval('jQuery.publish("' + key + '", [' + JSON.stringify(jQuery.extend(json[key], oMessage)) + ']);');
                    }
                }
                setUrlHash('modified', 'true');
                break;
            case 'error':
                var oMessage = { 'message':'<img src="../ks-myplan/images/pixel.gif" alt="" class="icon"><span class="message">' + jQuery('body').data('validationMessages').serverErrors[0] + '</span>', 'cssClass':'myplan-feedback error' };
                eval('jQuery.publish("ERROR", [' + JSON.stringify(oMessage) + ']);');
                break;
        }
    };
    var blockOptions = {
        message:'<img src="../ks-myplan/images/btnLoader.gif"/>',
        css:{
            width:'100%',
            border:'none',
            backgroundColor:'transparent'
        },
        overlayCSS:{
            backgroundColor:'#fff',
            opacity:0.6,
            padding:'0px 1px',
            margin:'0px -1px'
        }
    };
    myplanAjaxSubmitForm(methodToCall, updateRefreshableComponentCallback, {reqComponentId:id, skipViewInit:'false'}, elementToBlock, id, blockOptions);
}
function autoCompleteText(atpId) {
    var sQuery = jQuery("input[id='search_text_box_control']").val();
    var emptySuggestions = ["No courses Found"];
    jQuery("#search_text_box_control").autocomplete({source:function (request, response) {
        jQuery.ajax({
            url:"/student/myplan/quickAdd/autoSuggestions?courseCd=" + sQuery + "&atpId=" + atpId,
            type:"GET",
            beforeSend:null,
            data:"list=" + '',
            dataType:"json",
            error:function () {
                jQuery("#search_text_box_control").autocomplete({source:emptySuggestions});
            },
            success:function (data) {
                if (data.aaData.length > 0) {
                    response(data.aaData);
                }
                else {
                    response(emptySuggestions)
                }
            }
        });
    }
    });
    jQuery(document).ajaxStart(jQuery.unblockUI).ajaxStop(jQuery.unblockUI);

}

function toggleComponentContent(obj, sectionId, selector, expandText, collapseText) {
    var action = jQuery(obj);
    if (typeof action.data("hidden") == "undefined") {
        action.data("hidden", true);
    }
    if (action.data("hidden")) {
        jQuery("#" + sectionId).find(selector).show();
        action.text(collapseText).data("hidden", false);
    } else {
        jQuery("#" + sectionId).find(selector).hide();
        action.text(expandText).data("hidden", true);
    }
}


function expandCurriculumComments(actionComponent, expandText, collapseText) {
    var curriculumMessage = jQuery(actionComponent).parent().find('.curriculum-comment');
    if (curriculumMessage.is(":visible")) {
        curriculumMessage.slideUp(250, function () {
            if (expandText) {
                jQuery(actionComponent).text(expandText);
            }
        });
    } else {
        curriculumMessage.slideDown(250, function () {
            if (collapseText) {
                jQuery(actionComponent).text(collapseText);
            }
        });
    }
}

function expandPlanAuditSummary(selector, expandText, collapseText) {
    if (jQuery(selector).is(":visible")) {
        jQuery(selector).each(function () {
            jQuery(this).attr('style', 'display:none').slideUp(250)
        });
        if (expandText) {
            jQuery('#plan_audit_toggle_link').text(expandText);
        }
    } else {
        jQuery(selector).each(function () {
            jQuery(this).attr('style', 'display:block').slideDown(250)
        });
        if (collapseText) {
            jQuery('#plan_audit_toggle_link').text(collapseText);
        }
    }

}

function myplanGetSectionEnrollment(url, retrieveOptions, componentId) {
    var elementToBlock = jQuery(".myplan-enrl-data").parent();
    if (componentId) elementToBlock = jQuery("#" + componentId + " .myplan-enrl-data").parent();
    jQuery.ajax({
        url:url,
        data:retrieveOptions,
        dataType:"json",
        beforeSend:function () {
            elementToBlock.block({
                message:'<img src="../ks-myplan/images/ajaxLoader16.gif" alt="Fetching enrollment data..." />',
                fadeIn:0,
                fadeOut:0,
                overlayCSS:{
                    backgroundColor:'#fff',
                    opacity:0
                },
                css:{
                    border:'none',
                    width:'16px',
                    top:'0px',
                    left:'0px'
                }
            });
        },
        error:function () {
            elementToBlock.fadeOut(250);
            elementToBlock.each(function () {
                jQuery(this).css("text-align", "center").find("img.myplan-enrl-data").addClass("alert").attr("alt", "Oops, couldn't fetch the data. Refresh the page.").attr("title", "Oops, couldn't fetch the data. Refresh the page.");
            });
            elementToBlock.fadeIn(250);
            elementToBlock.unblock();
        },
        success:function (response) {
            elementToBlock.fadeOut(250);
            jQuery.each(response, function (sectionId, enrlObject) {
                var message = "<strong>" + enrlObject.enrollCount + "</strong> / " + enrlObject.enrollMaximum;
                var title = enrlObject.enrollCount + " enrolled out of " + enrlObject.enrollMaximum;
                if (enrlObject.enrollEstimate) {
                    message += "E";
                    title += " estimated";
                }
                title += " limit. Updated few minutes ago."
                var data = jQuery("<span />").addClass("myplan-enrl-data").attr("title", title).html(message);
                jQuery("#" + sectionId + " .myplan-enrl-data").replaceWith(data);
            });
            elementToBlock.fadeIn(250);
            elementToBlock.unblock();
        }
    });
}

function updateHiddenScript(id, script) {
    jQuery("#" + id).unbind();
    var input = jQuery("input[data-for='" + id + "'][data-role='script']");
    input.removeAttr("script").attr("name", "script").val(script);
    runScriptsForId(id);
}

function switchFetchAction(actionId, toggleId) {
    var script = "jQuery('#' + '" + actionId + "').click(function(e){ toggleSections('" + actionId + "', '" + toggleId + "', 'myplan-section-planned', 'Show all scheduled sections', 'Hide non-selected sections'); });";
    updateHiddenScript(actionId, script);
    jQuery("#" + actionId).text("Hide non-selected sections").removeAttr("data-hidden").data("hidden", false);
}

function toggleSections(actionId, toggleId, showClass, showText, hideText) {
    var group = jQuery("#" + toggleId + " table tbody tr.row").not("." + showClass);
    var action = jQuery("#" + actionId);
    if (action.data("hidden")) {
        group.each(function () {
            var toggle = jQuery(this).find("a[id^='toggle_']");
            if (toggle.data("hidden") || typeof toggle.data("hidden") == "undefined") {
                jQuery(this).show();
            } else {
                jQuery(this).show().next("tr.collapsible").show().next("tr.collapsible").show();
            }
        });
        jQuery(".myplan-quarter-detail .activityInstitutionHeading").show();
        action.text(hideText).data("hidden", false);
    } else {
        group.each(function () {
            var toggle = jQuery(this).find("a[id^='toggle_']");
            if (toggle.data("hidden") || typeof toggle.data("hidden") == "undefined") {
                jQuery(this).hide();
            } else {
                jQuery(this).hide().next("tr.collapsible").hide().next("tr.collapsible").hide();
            }
        });
        jQuery(".myplan-quarter-detail .activityInstitutionHeading").hide();
        action.text(showText).data("hidden", true);
    }
}

function toggleSectionDetails(sectionRow, obj, expandText, collapseText) {
    if (typeof obj.data("hidden") == "undefined") {
        obj.data("hidden", true);
    }
    var collapsibleRow = sectionRow.next("tr.collapsible");
    if (obj.data("hidden")) {
        sectionRow.find("td").first().attr("rowspan", "3");
        sectionRow.find("td").last().attr("rowspan", "3");
        collapsibleRow.show().next("tr.collapsible").show();
        obj.text(collapseText).data("hidden", false);
    } else {
        sectionRow.find("td").first().attr("rowspan", "1");
        sectionRow.find("td").last().attr("rowspan", "1");
        collapsibleRow.hide().next("tr.collapsible").hide();
        obj.text(expandText).data("hidden", true);
    }
}

function toggleRegisteredDetails(sectionRow, obj) {
    var collapsibleRow = sectionRow.next("tr.collapsible");
    if (collapsibleRow.is(":visible")) {
        obj.parents("td").attr("rowspan", "1");
        collapsibleRow.hide();
        obj.find("img.uif-image").toggleClass("expanded");
    } else {
        obj.parents("td").attr("rowspan", "2");
        collapsibleRow.show();
        obj.find("img.uif-image").toggleClass("expanded");
    }
}

function buildHoverText(obj) {
    var message = '';
    var temp = '';
    // condition to check whether section is primary or secondary
    if (obj.data("primary")) {
        // Primary sections
        // condition to check if planned or not planned
        if (obj.data("planned")) {
            var secondarySections = [];
            // Find list of secondary sections associated
            jQuery("div[data-courseid='" + obj.data("courseid") + "'][data-primarysection='" + obj.data("coursesection") + "'][data-planned='true'][data-primary='false']").each(function () {
                secondarySections.push(jQuery(this).data("coursesection"));
            });
            // Build string of secondary sections associated
            if (secondarySections.length > 0) {
                if (secondarySections.length == 1) {
                    temp = " and " + secondarySections.join();
                } else {
                    // commas separated string of secondary sections
                    temp = ", " + secondarySections.slice(0, -1).join(", ") + ", and " + secondarySections[secondarySections.length - 1];
                }
            }
            // Text should give "Delete {primary section} {list of secondary sections if any exist}"
            message = "Delete " + obj.data("coursesection") + temp;
        } else {
            // Text should give "Add {primary section}"
            message = "Add " + obj.data("coursesection");
        }
    } else {
        // Secondary sections
        // condition to check if planned or not planned
        if (obj.data("planned")) {
            // Text should give "Delete {secondary section}"
            message = "Delete " + obj.data("coursesection");
        } else {
            // Text should give "Add {secondary section} and {primary section if not planned}"
            if (!jQuery("div[data-courseid='" + obj.data("courseid") + "'][data-coursesection='" + obj.data("primarysection") + "']").data("planned")) {
                temp = " and " + obj.data("primarysection");
            }
            message = "Add " + obj.data("coursesection") + temp;
        }
    }
    obj.attr("title", message).find("img.uif-image").attr("alt", message);
}


function ksapCreateSuggest(controlId, options, queryFieldId, queryParameters, localSource, suggestOptions) {
    if (localSource) {
        options.source = suggestOptions;
    }
    else {
        options.source = function (request, response) {
            var queryData = {};

            queryData.methodToCall = 'performFieldSuggest';
            queryData.ajaxRequest = true;
            queryData.ajaxReturnType = 'update-none';
            queryData.formKey = jQuery("input#formKey").val();
            queryData.viewId = jQuery("input#viewId").val();
            queryData.queryTerm = request.term;
            queryData.queryFieldId = queryFieldId;

            for (var parameter in queryParameters) {
                queryData['queryParameter.' + parameter] = coerceValue(queryParameters[parameter]);
            }

            jQuery.ajax({
                url:jQuery("form#kualiForm").attr("action"),
                dataType:"json",
                beforeSend:null,
                complete:null,
                error:null,
                data:queryData,
                success:function (data) {
                    response(data.resultData);
                }
            });
        };
    }

    jQuery(document).ready(function () {
        jQuery("#" + controlId).autocomplete(options);
    });
}