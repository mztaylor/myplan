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

function toggleSaveSchedule(uniqueId, methodToCall) {
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

function hasWeekend(calendarObj) {
    var possible = jQuery(".schedulePossible__carousel li .schedulePossible__option.schedulePossible__option--hasWeekend").length;
    var saved = jQuery(".schedulePossible__carousel li .schedulePossible__option.schedulePossible__option--hasWeekend").length;

    return (possible + saved) > 0;
}