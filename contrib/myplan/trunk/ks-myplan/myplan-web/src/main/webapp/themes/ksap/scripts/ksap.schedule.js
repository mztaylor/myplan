function togglePossibleSchedule(calendarObj, targetObj, index, uniqueId) {
    var sourceObject = jQuery.extend(
        targetObj.data("events"),
        {"className": ["schedulePossible__event", "schedulePossible--" + (index % 5)]}
    );

    for (var i = 0; i < sourceObject.events.length; i++) {
        sourceObject.events[i].title = (index + 1).toString();
    }

    var selected = calendarObj.fullCalendar('clientEvents', uniqueId).length > 0;

    if (selected) {
        calendarObj.fullCalendar('removeEventSource', sourceObject);
        targetObj.find(".schedulePossible__save").hide();
    } else {
        calendarObj.fullCalendar('addEventSource', sourceObject);
        targetObj.find(".schedulePossible__save").show();
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

function hasWeekends() {
    var possible = jQuery(".schedulePossible__carousel .schedulePossible__option.schedulePossible__option--hasWeekend").length;
    var saved = jQuery(".scheduleSaved .scheduleSaved__item.scheduleSaved__item--hasWeekend").length;

    return (possible + saved) > 0;
}

function getTimeFrame(selection, defaultHour, maxFlag) {
    var returnHour = defaultHour;

    for (var i = 0; i < selection.length; i++) {
        var date = new Date(jQuery(selection[i]).data((maxFlag ? "maxtime":"mintime")));
        var tempHour = date.getHours();
        if (date.getMinutes() > 0 && maxFlag) tempHour = tempHour + 1;
        if (tempHour > returnHour) returnHour = tempHour;
    }

    return returnHour;
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

function refreshScheduleCalendar(calendarObj) {
    var weekends = hasWeekends();
    calendarObj.fullCalendar('destroy');
    KsapSbCalendarOptions.weekends = weekends;
    KsapSbCalendarOptions.minTime = getTimeFrame(jQuery(".schedulePossible__option"), KsapSbCalendarOptions.minTime, false);
    KsapSbCalendarOptions.maxTime = getTimeFrame(jQuery(".schedulePossible__option"), KsapSbCalendarOptions.maxTime, true);
    calendarObj.fullCalendar(KsapSbCalendarOptions);
    /*
    if (weekends != calendarObj.fullCalendar('option', 'weekends')) {

    } else {
        calendarObj.fullCalendar('removeEvents');
    }
    */
}
