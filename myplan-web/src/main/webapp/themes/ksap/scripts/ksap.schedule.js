function togglePossibleSchedule(calendarObj, targetObj, index, uniqueId, hasTBA) {
    var classes = (typeof calendarObj.data("class-selections") == "string") ? calendarObj.data("class-selections").split(",") : calendarObj.data("class-selections");
    var tempClass;
    var sourceObject = targetObj.data("events");
    var selected = calendarObj.fullCalendar('clientEvents', uniqueId).length > 0;

    for (var i = 0; i < sourceObject.events.length; i++) {
        sourceObject.events[i].title = (index + 1).toString();
        sourceObject.events[i].start = sourceObject.events[i].start + index;
    }

    if (selected) {
        calendarObj.fullCalendar('removeEventSource', sourceObject);
        targetObj.removeClass(function(index, css) {
            tempClass = (css.match(/schedulePossible\-\-([a-z]*)\b/g) || []).join(" ");
            return tempClass;
        }).find(".schedulePossible__save").hide();
        if (hasTBA) {
            jQuery("#possible-tba-" + uniqueId).removeClass(tempClass).hide();
        }
        classes.splice(0, 0, tempClass);
    } else {
        if (classes.length === 0) return false;
        sourceObject.className = ["schedulePossible__event", classes[0]];
        calendarObj.fullCalendar('addEventSource', sourceObject);
        targetObj.addClass(classes[0]).find(".schedulePossible__save").show();
        if (hasTBA) {
            jQuery("#possible-tba-" + uniqueId).addClass(classes[0]).show();
        }
        classes.splice(0, 1);
    }

    calendarObj.data("class-selections", classes);

    var tbaCount = jQuery(".schedulePossible__tba .schedulePossible__tbaItem").filter(function() {
        return jQuery(this).css('display') != 'none';
    }).length;

    if (tbaCount > 0) {
        jQuery(".schedulePossible__tba").show();
    } else {
        jQuery(".schedulePossible__tba").hide();
    }
}

function toggleSaveSchedule(uniqueId, methodToCall, event) {
    event.stopPropagation();
    // var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    jQuery("#kualiForm").ajaxSubmit({
        data : {
            methodToCall: methodToCall,
            uniqueId: uniqueId
        },
        dataType : 'json',
        success : function(response, textStatus, jqXHR) {
            jQuery.event.trigger("SAVED_SCHEDULE_" + methodToCall.toUpperCase(), response);
        },
        error : function(jqXHR, textStatus, errorThrown) {
            if (textStatus == "parsererror")
                textStatus = "JSON Parse Error";
            showGrowl(errorThrown, jqXHR.status + " " + textStatus);
        }
    });
}

function hidePossibleScheduleEvents(viewArr, calendarObj) {
    for (var i = 0; i < viewArr.length; i++) {
        var sourceObject = jQuery(viewArr[i]).find(".schedulePossible__option").data("events");
        var selected = calendarObj.fullCalendar('clientEvents', sourceObject.uniqueId).length > 0;
        if (selected) {
            calendarObj.fullCalendar('removeEventSource', sourceObject);
        }
    }
}

function addReservedScheduleOption(methodToCall, e) {
    var form = jQuery("#popupForm");
    form.ajaxSubmit({
        data: ksapAdditionalFormData({
            methodToCall : methodToCall
        }),
        dataType: 'json',
        success: function(response, textStatus, jqXHR){
            var container = jQuery("div.scheduleReserved__container");
            var template = jQuery("#sb-reserved-item-template").wrap('<div/>').parent().html();
            template = template.replace(/id="sb-reserved-item-template"/gi, "");
            template = template.replace(/__KSAP_ID__/gi, response.id);
            template = template.replace(/__KSAP_DAYSTIMES__/gi, response.daysTimes);
            var item = jQuery(template);
            container.append(item);
            item.attr("data-events", JSON.stringify(response));
            item.show();
            fnCloseAllPopups();
            jQuery.event.trigger("REFRESH_POSSIBLE_SCHEDULES");
        },
        error: function(jqXHR, textStatus, errorThrown) {
            if (textStatus == "parsererror")
                textStatus = "Parse Error in response";
            showGrowl(errorThrown, jqXHR.status + " " + textStatus);
            fnCloseAllPopups();
        }
    });
}

function removeReservedScheduleOption(uniqueId, e) {
    var form = jQuery("#kualiForm");
    form.ajaxSubmit({
        data: ksapAdditionalFormData({
            methodToCall : "remove",
            uniqueId : uniqueId
        }),
        dataType: 'json',
        success: function(response, textStatus, jqXHR) {
            var target = (e.currentTarget) ? jQuery(e.currentTarget) : jQuery(e.srcElement);
            target.parents(".scheduleReserved__item").remove();
            jQuery.event.trigger("REFRESH_POSSIBLE_SCHEDULES");
        },
        error: function(jqXHR, textStatus, errorThrown) {
            if (textStatus == "parsererror")
                textStatus = "JSON Parse Error";
            showGrowl(errorThrown, jqXHR.status + " " + textStatus);
        }
    });
}

function appendReservedScheduleOptions(calendarObj, parentSelector, itemSelector) {
    var items = jQuery(parentSelector).find(itemSelector);
    for (var i = 0; i < items.length; i++) {
        var sourceObject = jQuery.extend(
            jQuery(items[i]).data("events"),
            {"className": ["scheduleReserved__event"]});
        calendarObj.fullCalendar('addEventSource', sourceObject);
    }
}

function buildActivitiesContent(popoverContent, templateId) {
    var popoverTemplate = jQuery("#" + templateId).wrap('<div/>').parent().html();
    var regex = new RegExp('id="' + templateId + '"', "gi");
    popoverTemplate = popoverTemplate.replace(regex, "");
    popoverTemplate = popoverTemplate.replace(/__KSAP_COURSECD__/gi, popoverContent.courseCd);
    popoverTemplate = popoverTemplate.replace(/__KSAP_COURSEID__/gi, popoverContent.courseId);
    popoverTemplate = popoverTemplate.replace(/__KSAP_COURSETITLE__/gi, popoverContent.courseTitle);

    var popoverHtml = jQuery(popoverTemplate);
    var activitiesTemplate = popoverHtml.find("table tbody").html();
    popoverHtml.find("table tbody").empty();

    for (var i = 0; i < popoverContent.activities.length; i++) {
        var tempActivitiesTemplate = activitiesTemplate;
        tempActivitiesTemplate = tempActivitiesTemplate.replace(/__KSAP_SECTIONCD__/gi, popoverContent.activities[i].sectionCd);
        var tempMeetingDays = "", tempMeetingTimes = "";
        for (var n = 0; n < popoverContent.activities[i].meetings.length; n++) {
            if (n != 0) {
                tempMeetingDays += "<br />";
                tempMeetingTimes += "<br />";
            }
            tempMeetingDays += popoverContent.activities[i].meetings[n].meetingTime;
            tempMeetingTimes += popoverContent.activities[i].meetings[n].meetingTime;
        }
        tempActivitiesTemplate = tempActivitiesTemplate.replace(/__KSAP_MEETINGDAYS__/gi, tempMeetingDays);
        tempActivitiesTemplate = tempActivitiesTemplate.replace(/__KSAP_MEETINGTIMES__/gi, tempMeetingTimes);
    }

    popoverHtml.find("table tbody").html(tempActivitiesTemplate);

    return popoverHtml.show().wrap('<div/>').parent().html();
}

