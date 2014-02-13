function togglePossibleSchedule(calendarObject, sourceObject, targetObject, index) {
    sourceObject.className = ["schedulePossible__event", "schedulePossible--" + (index % 5)];

    if (targetObject.data("selected")) {
        calendarObject.fullCalendar('removeEventSource', sourceObject);
        targetObject.data("selected", false);
    } else {
        calendarObject.fullCalendar('addEventSource', sourceObject);
        targetObject.data("selected", true);
    }
}