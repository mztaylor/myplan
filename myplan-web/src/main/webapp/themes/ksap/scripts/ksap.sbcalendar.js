var KsapSbCalendar = {
    initialize: function (element) {
        this.appendReservedSchedules(jQuery('.scheduleReserved__container').find('.scheduleReserved__item'));
        this.cssClasses = this.widget.data("selection-classes").split(",");
        this.limit = parseFloat(this.widget.data("selection-limit"));
        if(typeof KsapScheduleBuild.registeredSchedule.events !== "undefined" && KsapScheduleBuild.registeredSchedule.events.length > 0) {
            this.addSchedule(KsapScheduleBuild.registeredSchedule, "scheduleBuilder__registered");
        }
        if (element) element.click();
    },

    getCssClass: function (index) {
        if (index == null) index = 0;
        return this.cssClasses[index];
    },

    addCssClass: function (cssClass) {
        if (cssClass) this.cssClasses.splice(0, 0, cssClass);
    },

    removeCssClass: function (cssClass) {
        if (cssClass) this.cssClasses.splice(this.cssClasses.indexOf(cssClass), 1);
    },

    getNumAvailable: function () {
        return this.limit;
    },

    showTba: function (id, cssClass) {
        jQuery("#tba-" + id).addClass(cssClass).show();
        this.toggleTbaSection();
    },

    hideTba: function (id, cssClass) {
        jQuery("#tba-" + id).removeClass(cssClass).hide();
        this.toggleTbaSection();
    },

    toggleTbaSection: function () {
        var numTba = jQuery(".scheduleBuilder__tba .scheduleBuilder__tbaItem").filter(function () {
            return jQuery(this).css('display') != 'none';
        }).length;

        if (numTba > 0) {
            jQuery(".scheduleBuilder__tba").show();
        } else {
            jQuery(".scheduleBuilder__tba").hide();
        }
    },

    addSchedule: function (source, cssClasses) {
        for (var i = 0; i < source.events.length; i++) {
            var event = jQuery.extend(source.events[i], {className: cssClasses})
            this.widget.fullCalendar('renderEvent', event);
        }
        return source;
    },

    removeSchedule: function (source) {
        this.widget.fullCalendar('removeEvents', function (event) {
            return (source.uniqueId === event.parentUniqueid);
        });
        return source;
    },

    getScheduleEvents: function (uniqueId) {
        return this.widget.fullCalendar('clientEvents', function (event) {
            return (event.parentUniqueid === uniqueId);
        });
    },

    toggleCalendarSchedule: function (id, scheduleNumber, target, hasTba, isSaved) {
        if (target.hasClass("schedulePossible__option--saved")) return false;
        var source = target.data("source");
        var isSelected = this.getScheduleEvents(id).length > 0;

        if (isSelected) {
            // Possible Schedule is active in calendar
            var cssClass = (target.attr("class").match(/scheduleCalendar\-\-([a-z]*)\b/g) || []).join(" ");
            this.removeSchedule(source);
            if (!isSaved) {
                target.removeClass(cssClass);
                this.addCssClass(cssClass);
            }
            this.limit++;
            if (hasTba) this.hideTba(id, cssClass);
        } else {
            // Possible Schedule is NOT active in calendar
            if (this.getNumAvailable() === 0) {
                var message = jQuery("#sb_error_max_schedules").clone().wrap('<div/>').parent().html();
                openDialog(jQuery(message).show().wrap("<div/>").parent().html(), null, true);
                return false;
            }
            for (var i = 0; i < source.events.length; i++) {
                if (!source.events[i].tbd) {
                    source.events[i].title = (!isSaved) ? scheduleNumber.toString() : "P" + scheduleNumber.toString();
                    if (typeof source.events[i].start === "number") {
                        source.events[i].start = new Date((source.events[i].start + ((!isSaved) ? scheduleNumber : 30 + scheduleNumber)) * 1000).toString();
                    } else {
                        source.events[i].start = new Date(source.events[i].start.setSeconds((!isSaved) ? scheduleNumber : 30 + scheduleNumber)).toString();
                    }
                }
            }
            var cssClass = isSaved ? "scheduleCalendar--saved" : this.getCssClass();
            this.addSchedule(source, [cssClass]);
            if (!isSaved) {
                target.addClass(cssClass);
                this.removeCssClass(cssClass);
            }
            this.limit--;
            if (hasTba) this.showTba(id, cssClass);
        }
    },

    toggleSaveSchedule: function (id, methodToCall, event) {
        stopEvent(event);
        jQuery("#kualiForm").ajaxSubmit({
            data: {
                methodToCall: methodToCall,
                uniqueId: id
            },
            dataType: 'json',
            success: function (response, textStatus, jqXHR) {
                if (response.success) {
                    jQuery.event.trigger("SAVED_SCHEDULE_" + ((methodToCall === "save") ? "SAVE" : "REMOVE"), response);
                } else if (!response.success && response.limitReached) {
                    var messageHtml = jQuery("#sb_error_max_pinned").clone().show().wrap('<div/>').parent().html();
                    openDialog(messageHtml, null, true);
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                if (textStatus == "parsererror")
                    textStatus = "JSON Parse Error";
                showGrowl(errorThrown, jqXHR.status + " " + textStatus);
            }
        });
    },

    updateAddedSavedSchedule: function (data) {
        var recentlyAdded;
        for (var i = 0; i < data.savedSchedules.length; i++) {
            if (data.savedSchedules[i].recentlyAdded) recentlyAdded = data.savedSchedules[i];
        }
        var possibleSchedule = jQuery("#possible-schedule-" + recentlyAdded.uniqueId);
        var possibleTba = jQuery("#tba-" + recentlyAdded.uniqueId);
        var cssClass = (possibleSchedule.attr("class").match(/scheduleCalendar\-\-([a-z]*)\b/g) || []).join(" ");
        possibleSchedule.removeClass(cssClass).addClass("schedulePossible__option--saved").attr("data-saved", recentlyAdded.id);
        this.addCssClass(cssClass);
        if (possibleTba.length > 0) {
            possibleTba.removeClass(cssClass).addClass("scheduleCalendar--saved").attr("data-saved", recentlyAdded.id).find(".scheduleBuilder__tbaItemIndex").text(recentlyAdded.index);
        }
        var scheduleEvents = this.getScheduleEvents(recentlyAdded.uniqueId);
        for (var i = 0; i < scheduleEvents.length; i++) {
            scheduleEvents[i].title = recentlyAdded.index;
            scheduleEvents[i].className = ["scheduleCalendar--saved"];
            this.widget.fullCalendar("updateEvent", scheduleEvents[i]);
        }
    },

    updateRemovedSavedSchedule: function (data) {
        jQuery.fancybox.close(true);
        fnCloseAllPopups();
        var possibleSchedule = jQuery('.schedulePossible__option[data-saved=' + data.scheduleIdRemoved + ']');
        var tba = jQuery('.scheduleBuilder__tbaItem[data-saved=' + data.scheduleIdRemoved + ']');
        if (possibleSchedule.length > 0) possibleSchedule.removeClass('schedulePossible__option--saved').removeData('saved');
        if (tba.length > 0) {
            tba.removeClass('scheduleCalendar--saved').removeData('saved').hide().find(".scheduleBuilder__tbaItemIndex").text(tba.data("index"));
            this.toggleTbaSection();
        }
        var scheduleEvents = this.getScheduleEvents(data.uniqueIdRemoved);
        if (scheduleEvents.length > 0) {
            this.limit++;
            this.widget.fullCalendar("removeEvents", function (event) {
                return (event.parentUniqueid === data.uniqueIdRemoved);
            });
        }
        if (data.savedSchedules) {
            for (var i = 0; i < data.savedSchedules.length; i++) {
                var scheduleEvents = this.getScheduleEvents(data.savedSchedules[i].uniqueId);
                var possibleTba = jQuery('.scheduleBuilder__tbaItem[data-saved=' + data.savedSchedules[i].id + ']');
                for (var n = 0; n < scheduleEvents.length; n++) {
                    scheduleEvents[n].title = data.savedSchedules[i].index;
                    this.widget.fullCalendar("updateEvent", scheduleEvents[n]);
                }
                if (possibleTba.length > 0) possibleTba.find(".scheduleBuilder__tbaItemIndex").text(data.savedSchedules[i].index);
            }
        }
    },

    appendReservedSchedules: function (selection) {
        for (var i = 0; i < selection.length; i++) {
            var source = jQuery(selection[i]).data("source");
            this.widget.fullCalendar('addEventSource', source);
            this.addSchedule(source, ["scheduleReserved__event"]);
        }
    },

    highlightActiveEvents: function (calEvent) {
        var courseEvents = this.widget.fullCalendar('clientEvents', function (event) {
            if (typeof event.popoverContent === "undefined") return false;
            return (calEvent.popoverContent.courseId === event.popoverContent.courseId &&
                calEvent.popoverContent.courseCd === event.popoverContent.courseCd &&
                calEvent.parentUniqueid === event.parentUniqueid);
        });

        for (var i = 0; i < courseEvents.length; i++) {
            jQuery("#possible_schedule_event" + courseEvents[i]._id + ".fc-event").addClass("scheduleCalendar--highlight");
        }
    },

    clearActiveEvents: function () {
        for (var i = 0; i < this.widget.fullCalendar('clientEvents').length; i++) {
            jQuery("#possible_schedule_event" + this.widget.fullCalendar('clientEvents')[i]._id + ".fc-event").removeClass("scheduleCalendar--highlight");
        }
    }
};