var KsapSbCalendar = {
    initialize : function (element) {
        this.appendReservedSchedules(jQuery('.scheduleReserved__container').find('.scheduleReserved__item'));
        this.cssClasses = this.widget.data("selection-classes").split(",");
        this.limit = parseFloat(this.widget.data("selection-limit"));
        this.plannedActivities = (this.widget.data("planned-activities") ? this.widget.data("planned-activities") : {});
        if (element) element.click();
    },

    getCssClass : function (index) {
        if (index == null) index = 0;
        return this.cssClasses[index];
    },

    addCssClass : function (cssClass) {
        if (cssClass) this.cssClasses.splice(0, 0, cssClass);
    },

    removeCssClass : function (cssClass) {
        if (cssClass) this.cssClasses.splice(this.cssClasses.indexOf(cssClass), 1);
    },

    getNumAvailable : function () {
        return this.limit;
    },

    showTba : function (id, cssClass) {
        jQuery("#tba-" + id).addClass(cssClass).show();
        this.toggleTbaSection();
    },

    hideTba : function (id, cssClass) {
        jQuery("#tba-" + id).removeClass(cssClass).hide();
        this.toggleTbaSection();
    },

    toggleTbaSection : function () {
        var numTba = jQuery(".scheduleBuilder__tba .scheduleBuilder__tbaItem").filter(function() {
            return jQuery(this).css('display') != 'none';
        }).length;

        if (numTba > 0) {
            jQuery(".scheduleBuilder__tba").show();
        } else {
            jQuery(".scheduleBuilder__tba").hide();
        }
    },

    addSchedule : function (source, cssClasses) {
        for (var i = 0; i < source.events.length; i++) {
            var event = jQuery.extend(source.events[i], {className: cssClasses})
            this.widget.fullCalendar('renderEvent', event);
        }
        return source;
    },

    removeSchedule : function (source) {
        this.widget.fullCalendar('removeEvents', function(event) {
            return (source.uniqueId === event.parentUniqueid);
        });
        return source;
    },

    getScheduleEvents : function (uniqueId) {
        return this.widget.fullCalendar('clientEvents', function(event) {
            return (event.parentUniqueid === uniqueId);
        });
    },

    toggleCalendarSchedule : function (id, scheduleNumber, target, hasTba, isSaved) {
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
            if (this.getNumAvailable() === 0) return false;
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

    toggleSaveSchedule : function (id, methodToCall, event) {
        stopEvent(event);
        jQuery("#kualiForm").ajaxSubmit({
            data : {
                methodToCall: methodToCall,
                uniqueId: id
            },
            dataType : 'json',
            success : function(response, textStatus, jqXHR) {
                if (response.success) jQuery.event.trigger("SAVED_SCHEDULE_" + ((methodToCall === "save") ? "SAVE" : "REMOVE"), response);

                // Add condition for error message when save limit is reached.
            },
            error : function(jqXHR, textStatus, errorThrown) {
                if (textStatus == "parsererror")
                    textStatus = "JSON Parse Error";
                showGrowl(errorThrown, jqXHR.status + " " + textStatus);
            }
        });
    },

    updateAddedSavedSchedule : function (data) {
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

    updateRemovedSavedSchedule : function (data) {
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

    addReservedSchedule : function () {
        var form = jQuery("#popupForm");
        form.ajaxSubmit({
            data: ksapAdditionalFormData({
                methodToCall: "createReservedTime"
            }),
            dataType: 'json',
            success: function(response, textStatus, jqXHR){
                var container = jQuery("div.scheduleReserved__container");
                var template = jQuery("#sb-reserved-item-template").wrap("<div/>").parent().html();
                template = template.replace(/id="sb-reserved-item-template"/gi, "");
                template = template.replace(/__KSAP_ID__/gi, response.id);
                template = template.replace(/__KSAP_TERM_ID__/gi, response.termId);
                template = template.replace(/__KSAP_DAYSTIMES__/gi, response.daysTimes);
                var item = jQuery(template).attr("id", "reserved-item-" + response.id).attr("data-source", JSON.stringify(response)).show();
                container.append(item);
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
    },

    editReservedSchedule : function (id) {
        var form = jQuery("#popupForm");
        form.ajaxSubmit({
            data: ksapAdditionalFormData({
                id : id,
                methodToCall : "updateReservedTime"
            }),
            dataType: 'json',
            success: function(response, textStatus, jqXHR){
                var container = jQuery("div.scheduleReserved__container");
                var template = jQuery("#sb-reserved-item-template").wrap('<div/>').parent().html();
                template = template.replace(/id="sb-reserved-item-template"/gi, "");
                template = template.replace(/__KSAP_ID__/gi, response.id);
                template = template.replace(/__KSAP_TERM_ID__/gi, response.termId);
                template = template.replace(/__KSAP_DAYSTIMES__/gi, response.daysTimes);
                var item = jQuery(template).attr("id", "reserved-item-" + response.id).attr("data-source", JSON.stringify(response)).show();
                container.find("#reserved-item-" + response.id).replaceWith(item);
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
    },

    removeReservedSchedule : function (id, event) {
        var form = jQuery("#kualiForm");
        form.ajaxSubmit({
            data: ksapAdditionalFormData({
                methodToCall: "removeReservedTime",
                uniqueId: id
            }),
            dataType: 'json',
            success: function(response, textStatus, jqXHR) {
                var target = (event.currentTarget) ? jQuery(event.currentTarget) : jQuery(event.srcElement);
                target.parents(".scheduleReserved__item").remove();
                jQuery.event.trigger("REFRESH_POSSIBLE_SCHEDULES");
            },
            error: function(jqXHR, textStatus, errorThrown) {
                if (textStatus == "parsererror")
                    textStatus = "JSON Parse Error";
                showGrowl(errorThrown, jqXHR.status + " " + textStatus);
            }
        });
    },

    appendReservedSchedules : function (selection) {
        for (var i = 0; i < selection.length; i++) {
            var source = jQuery(selection[i]).data("source");
            this.widget.fullCalendar('addEventSource', source);
            this.addSchedule(source, ["scheduleReserved__event"]);
        }
    },

    getPopoverHtml : function (popoverContent) {
        var popoverTemplate = jQuery("#sb-popover-content-template").wrap('<div/>').parent().html();
        popoverTemplate = popoverTemplate.replace(/sb-popover-content-template/gi, "event_popover");
        var regex = new RegExp(' id="u[0-9]*"', "gi");
        popoverTemplate = popoverTemplate.replace(regex, "");
        popoverTemplate = popoverTemplate.replace(/__KSAP_COURSECD__/gi, popoverContent.courseCd);
        popoverTemplate = popoverTemplate.replace(/__KSAP_COURSEID__/gi, popoverContent.courseId);
        popoverTemplate = popoverTemplate.replace(/__KSAP_COURSETITLE__/gi, popoverContent.courseTitle);

        var popoverHtml = jQuery(popoverTemplate);
        var activitiesTemplate = popoverHtml.find("td:nth-child(4),td:nth-child(5)").addClass("activityDetail").parents("tbody").html();
        popoverHtml.find("table tbody").empty();

        for (var i = 0; i < popoverContent.activities.length; i++) {
            var tempActivitiesTemplate = activitiesTemplate;
            tempActivitiesTemplate = tempActivitiesTemplate.replace(/__KSAP_SECTIONCD__/gi, popoverContent.activities[i].sectionCd);
            var tempMeetingDay = "", tempMeetingTime = "", tempMeetingLocation ="";
            if (popoverContent.activities[i].meetings.length === 0) {
                tempMeetingTime = "To be arranged";
            } else {
                for (var n = 0; n < popoverContent.activities[i].meetings.length; n++) {
                    if (n != 0) {
                        tempMeetingDay += "<br />";
                        tempMeetingTime += "<br />";
                        tempMeetingLocation += "<br />";
                    }
                    tempMeetingDay += popoverContent.activities[i].meetings[n].meetingDay;
                    tempMeetingTime += popoverContent.activities[i].meetings[n].meetingTime;
                    if (popoverContent.activities[i].meetings[n].buildingUrl !== "") tempMeetingLocation += '<a href="' + popoverContent.activities[i].meetings[n].buildingUrl + '" target="_blank">'
                    tempMeetingLocation += popoverContent.activities[i].meetings[n].building + " " + popoverContent.activities[i].meetings[n].location;
                    if (popoverContent.activities[i].meetings[n].buildingUrl !== "") tempMeetingLocation += '</a>';
                }
            }
            tempActivitiesTemplate = tempActivitiesTemplate.replace(/__KSAP_MEETINGDAY__/gi, tempMeetingDay);
            tempActivitiesTemplate = tempActivitiesTemplate.replace(/__KSAP_MEETINGTIME__/gi, tempMeetingTime);
            tempActivitiesTemplate = tempActivitiesTemplate.replace(/__KSAP_MEETINGLOCATION__/gi, tempMeetingLocation);
            tempActivitiesTemplate = tempActivitiesTemplate.replace(/__KSAP_REGISTRATIONCODE__/gi, popoverContent.activities[i].registrationCode);
            tempActivitiesTemplate = tempActivitiesTemplate.replace(/__KSAP_SUMMERTERM__/gi, "");//popoverContent.activities[i].instituteCd);
            tempActivitiesTemplate = tempActivitiesTemplate.replace(/__KSAP_INSTITUTECD__/gi, popoverContent.activities[i].instituteCd);
            tempActivitiesTemplate = tempActivitiesTemplate.replace(/__KSAP_ENROLLRESTRICTION__/gi, (popoverContent.activities[i].enrollRestriction ? "K" : ""));
            tempActivitiesTemplate = tempActivitiesTemplate.replace(/__KSAP_ENROLLSTATUS__/gi, popoverContent.activities[i].enrollStatus);
            tempActivitiesTemplate = tempActivitiesTemplate.replace(/__KSAP_ENROLLSTATE__/gi, popoverContent.activities[i].enrollState);

            tempActivitiesTemplate = tempActivitiesTemplate.replace(/__KSAP_ACTIVITYID__/gi, popoverContent.activities[i].activityId);
            tempActivitiesTemplate = tempActivitiesTemplate.replace(/__KSAP_PRIMARY__/gi, popoverContent.activities[i].primary);

            var planned = this.isPlanned(popoverContent.activities[i].activityId);
            tempActivitiesTemplate = tempActivitiesTemplate.replace(/__KSAP_STATUSCLASS__/gi, (planned ? "schedulePopover__itemDelete" : "schedulePopover__itemAdd"));

            popoverHtml.find("table tbody").append(tempActivitiesTemplate);
        }

        return popoverHtml.show().wrap("<div/>").parent().html();
    },

    openPopover : function (calEvent, event) {
        stopEvent(event);
        this.closePopover();
        var popupElement = (event.currentTarget) ? jQuery(event.currentTarget) : jQuery(event.srcElement);
        var popupHtml = "";
        if (jQuery.isArray(calEvent)) {
            for (var i = 0; i < calEvent.length; i++) {
                popupHtml += this.getPopoverHtml(calEvent[i].popoverContent);
            }
        } else {
            popupHtml = this.getPopoverHtml(calEvent.popoverContent);
            this.highlightActiveEvents(calEvent);
        }
        var popupOptions = {
            innerHtml: popupHtml + '<img src="' + getConfigParam("ksapImageLocation") + 'icons/close.png" class="popover__close"/>',
            position: "top",
            align: "center",
            tail: {align: "center"}
        };
        if (typeof calEvent.start !== "undefined") {
            switch (calEvent.start.getDay()) {
                case 1:
                    popupOptions.align = popupOptions.tail.align = "left";
                    break;
                case 5:
                    popupOptions.align = popupOptions.tail.align = (this.widget.fullCalendar('option', 'weekends')) ? "center" : "right";
                    break;
                case 6:
                    popupOptions.align = popupOptions.tail.align = "right";
                    break;
            }
        } else {
            popupOptions.position = "bottom";
            var tba = popupElement.parents(".scheduleBuilder__tba").find(".scheduleBuilder__tbaItem").filter(function() {
                return jQuery(this).css('display') != 'none';
            });
            var index;
            for (var i = 0; i < tba.length; i++) {
                if (jQuery(tba[i])[0] == popupElement[0]) { index = i; break; }
            }
            switch (index) {
                case 0:
                    popupOptions.align = popupOptions.tail.align = "left";
                    break;
                case 4:
                    popupOptions.align = popupOptions.tail.align = "right";
                    break;
            }
        }
        var popupSettings = jQuery.extend(popupOptionsDefault, popupOptions);
        if (!popupElement.HasPopOver()) popupElement.CreatePopOver({manageMouseEvents: false});
        popupElement.ShowPopOver(popupSettings, false);
        popupElement.FreezePopOver();

        jQuery("body").on("click", function(event) {
            var target = (event.target) ? jQuery(event.target): jQuery(event.srcElement);
            if (target.parents("#" + popupElement.GetPopOverID()).length === 0 || target.hasClass("popover__close")) {
                KsapSbCalendar.closePopover();
            }
        });
    },

    closePopover : function () {
        this.clearActiveEvents();
        fnCloseAllPopups();
        jQuery("body").off("click");
    },

    highlightActiveEvents : function (calEvent) {
        var courseEvents = this.widget.fullCalendar('clientEvents', function(event) {
            if (typeof event.popoverContent === "undefined") return false;
            return (calEvent.popoverContent.courseId === event.popoverContent.courseId &&
                    calEvent.popoverContent.courseCd === event.popoverContent.courseCd &&
                    calEvent.parentUniqueid === event.parentUniqueid);
        });

        for (var i = 0; i < courseEvents.length; i++) {
            jQuery("#possible_schedule_event" + courseEvents[i]._id + ".fc-event").addClass("scheduleCalendar--highlight");
        }
    },

    clearActiveEvents : function () {
        for (var i = 0; i < this.widget.fullCalendar('clientEvents').length; i++) {
            jQuery("#possible_schedule_event" + this.widget.fullCalendar('clientEvents')[i]._id + ".fc-event").removeClass("scheduleCalendar--highlight");
        }
    },

    isPlanned : function (activityId) {
        return (typeof this.plannedActivities[activityId] != "undefined");
    },

    addPlannedActivity : function(data) {
        if (!KsapSbCalendar.isPlanned(data.ActivityOfferingId)) {
            this.plannedActivities[data.ActivityOfferingId] = data.planItemId;
            jQuery("#action-" + data.RegistrationCode).removeClass("schedulePopover__itemAdd").addClass("schedulePopover__itemDelete").blur();
        }

        if (data.ItemsToUpdate) {
            var item = jQuery("#action-" + data.ItemsToUpdate).data();
            if (!KsapSbCalendar.isPlanned(item.activityId)) {
                this.plannedActivities[item.activityId] = data.PrimaryPlanItemId;
                jQuery("#action-" + data.ItemsToUpdate).removeClass("schedulePopover__itemAdd").addClass("schedulePopover__itemDelete");
            }
        }
    },

    removePlannedActivity : function(data) {
        if (KsapSbCalendar.isPlanned(data.ActivityOfferingId)) {
            delete this.plannedActivities[data.ActivityOfferingId];
            jQuery("#action-" + data.RegistrationCode).removeClass("schedulePopover__itemDelete").addClass("schedulePopover__itemAdd").blur();
        }

        if (data.ItemsToUpdate) {
            var items = data.ItemsToUpdate.split(',');
            jQuery.each(items, function (index, value) {
                var item = jQuery("#action-" + items[index]).data();
                if (KsapSbCalendar.isPlanned(item.activityId)) {
                    delete KsapSbCalendar.plannedActivities[item.activityId];
                    jQuery("#action-" + items[index]).removeClass("schedulePopover__itemDelete").addClass("schedulePopover__itemAdd");
                }
            });
        }
    },

    togglePlanSection : function(data, event) {
        var planned = this.plannedActivities.hasOwnProperty(data.activityId);

        var additionalFormData = {
            viewId: "PlannedCourse-FormView",
            methodToCall: (planned ? "removeItem" : "addUpdatePlanItem"),
            courseId: data.courseId,
            code: data.courseCode,
            sectionCode: data.sectionCode,
            registrationCode: data.registrationCode,
            atpId: readUrlParam('termId'),
            primary: data.primary
        };

        if (planned) additionalFormData.planItemId = this.plannedActivities[data.activityId];
        submitHiddenForm('plan', additionalFormData, false, event);
    }
};
