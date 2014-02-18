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
    } else {
        calendarObj.fullCalendar('addEventSource', sourceObject);
    }
}

function toggleSaveSchedule(uniqueId, methodToCall, event) {
    event.stopPropagation();
    jQuery("#kualiForm").ajaxSubmit({
        data : {
            methodToCall: methodToCall,
            uniqueId: uniqueId
        },
        dataType : 'json',
        success : function(response, textStatus, jqXHR) {
            customRetrieveComponent('saved_schedules_summary','saved_schedules_summary','search','lookup',{viewId:'SavedSchedulesSummary-LookupView',termId:jQuery('#schedule_build_termId_control').val(),learningPlanId:jQuery('#schedule_build_learningPlanId_control').val()},null,{css:{right:'0px',top:'0px',width:'16px',height:'16px',lineHeight:'16px',border:'none'}});
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
    if (weekends != calendarObj.fullCalendar('option', 'weekends')) {
        calendarObj.fullCalendar('destroy');
        KsapSbCalendarOptions.weekends = weekends;
        calendarObj.fullCalendar(KsapSbCalendarOptions);
    } else {
        calendarObj.fullCalendar('removeEvents');
    }
}