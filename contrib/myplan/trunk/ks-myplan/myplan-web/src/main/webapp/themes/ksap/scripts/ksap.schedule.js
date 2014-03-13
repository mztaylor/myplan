var KsapSbCalendarActions = {
    initialize : function (element) {
        this.appendReservedSchedules(jQuery('.scheduleReserved__container').find('.scheduleReserved__item'));
        this.cssClasses = this.data("class-selections").split(",");
        this.plannedActivities = (this.data("planned-activities") ? this.data("planned-activities") : {});
        element.click();
    },

    isPlanned : function (activityId) {
        return (typeof this.plannedActivities[activityId] != "undefined");
    },

    addPlannedActivity : function(data) {
        console.log(data);

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
        console.log(data);

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
    },

    getCssClass : function (index) {
        if (index == null) index = 0;
        return this.cssClasses[index];
    },

    addCssClass : function (cssClass) {
        this.cssClasses.splice(0, 0, cssClass);
    },

    removeCssClass : function (cssClass) {
        this.cssClasses.splice(this.cssClasses.indexOf(cssClass), 1);
    },

    getNumAvailable : function () {
        return this.cssClasses.length;
    },

    showTba : function (id, cssClass) {
        jQuery("#possible-tba-" + id).addClass(cssClass).show();
        this.toggleTbaSection();
    },

    hideTba : function (id, cssClass) {
        jQuery("#possible-tba-" + id).removeClass(cssClass).hide();
        this.toggleTbaSection();
    },

    toggleTbaSection : function () {
        var numTba = jQuery(".schedulePossible__tba .schedulePossible__tbaItem").filter(function() {
            return jQuery(this).css('display') != 'none';
        }).length;

        if (numTba > 0) {
            jQuery(".schedulePossible__tba").show();
        } else {
            jQuery(".schedulePossible__tba").hide();
        }
    },

    addSchedule : function (source, cssClass) {
        if (KsapSbCalendar.getNumAvailable() === 0) return false;
        source.className = ["schedulePossible__event", cssClass];
        this.fullCalendar('addEventSource', source);
        return source;
    },

    removeSchedule : function (source) {
        this.fullCalendar('removeEventSource', source);
        return source;
    },

    togglePossibleSchedule : function (id, scheduleNumber, target, hasTba) {
        var source = target.data("events");

        var isSelected = this.fullCalendar('clientEvents', function(event) {
            return (event.uniqueid === id);
        }).length > 0;

        if (isSelected) {
            // Possible Schedule is active in calendar
            var cssClass = (target.attr("class").match(/schedulePossible\-\-([a-z]*)\b/g) || []).join(" ");
            this.removeSchedule(source);
            target.removeClass(cssClass).find(".schedulePossible__save").hide();
            this.addCssClass(cssClass);
            if (hasTba) this.hideTba(id);

        } else {
            // Possible Schedule is NOT active in calendar
            for (var i = 0; i < source.events.length; i++) {
                source.events[i].title = scheduleNumber.toString();
                source.events[i].start = source.events[i].start + scheduleNumber;
            }
            var cssClass = this.getCssClass();
            this.addSchedule(source, cssClass);
            target.addClass(cssClass).find(".schedulePossible__save").show();
            this.removeCssClass(cssClass);
            if (hasTba) this.showTba(id);

        }
    },

    toggleSavedSchedule : function (id, methodToCall, event) {
        event.stopPropagation();
        jQuery("#kualiForm").ajaxSubmit({
            data : {
                methodToCall: methodToCall,
                uniqueId: id
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
                template = template.replace(/__KSAP_DAYSTIMES__/gi, response.daysTimes);
                var item = jQuery(template).attr("id", "reserved-item-" + response.id).attr("data-events", JSON.stringify(response)).show();
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
                var item = jQuery(template).attr("id", "reserved-item-" + response.id).attr("data-events", JSON.stringify(response)).show();
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
                methodToCall: "remove",
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
            var source = jQuery.extend(
                jQuery(selection[i]).data("events"),
                {"className": ["scheduleReserved__event"]}
            );
            this.fullCalendar('addEventSource', source);
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
        var activitiesTemplate = popoverHtml.find("table tbody").html();
        popoverHtml.find("table tbody").empty();

        for (var i = 0; i < popoverContent.activities.length; i++) {
            var tempActivitiesTemplate = activitiesTemplate;
            tempActivitiesTemplate = tempActivitiesTemplate.replace(/__KSAP_SECTIONCD__/gi, popoverContent.activities[i].sectionCd);
            tempActivitiesTemplate = tempActivitiesTemplate.replace(/__KSAP_ENROLLMENT__/gi, popoverContent.activities[i].enrollStatus);
            tempActivitiesTemplate = tempActivitiesTemplate.replace(/__KSAP_ACTIVITYID__/gi, popoverContent.activities[i].activityId);
            tempActivitiesTemplate = tempActivitiesTemplate.replace(/__KSAP_REGISTRATIONCODE__/gi, popoverContent.activities[i].registrationCode);
            tempActivitiesTemplate = tempActivitiesTemplate.replace(/__KSAP_PRIMARY__/gi, popoverContent.activities[i].primary);
            var tempMeetingDay = "", tempMeetingTime = "";
            for (var n = 0; n < popoverContent.activities[i].meetings.length; n++) {
                if (n != 0) {
                    tempMeetingDay += "<br />";
                    tempMeetingTime += "<br />";
                }
                tempMeetingDay += popoverContent.activities[i].meetings[n].meetingDay;
                tempMeetingTime += popoverContent.activities[i].meetings[n].meetingTime;
            }
            tempActivitiesTemplate = tempActivitiesTemplate.replace(/__KSAP_MEETINGDAY__/gi, tempMeetingDay);
            tempActivitiesTemplate = tempActivitiesTemplate.replace(/__KSAP_MEETINGTIME__/gi, tempMeetingTime);

            var planned = this.plannedActivities.hasOwnProperty(popoverContent.activities[i].activityId);
            tempActivitiesTemplate = tempActivitiesTemplate.replace(/__KSAP_STATUSCLASS__/gi, (planned ? "schedulePopover__itemDelete" : "schedulePopover__itemAdd"));

            popoverHtml.find("table tbody").append(tempActivitiesTemplate);
        }

        return popoverHtml.show().wrap("<div/>").parent().html();
    },

    openPopover : function (calEvent, event) {
        stopEvent(event);
        this.closePopover();
        var popupElement = (event.currentTarget) ? jQuery(event.currentTarget) : jQuery(event.srcElement);
        var popupOptions = {
            innerHtml: this.getPopoverHtml(calEvent.popoverContent) + '<img src="' + getConfigParam("ksapImageLocation") + 'icons/close.png" class="popover__close"/>',
            position: "top",
            align: "center",
            tail: {align: "center"}
        };
        switch (calEvent.start.getDay()) {
            case 1:
                popupOptions.align = popupOptions.tail.align = "left";
                break;
            case 5:
                popupOptions.align = popupOptions.tail.align = (this.fullCalendar('option', 'weekends')) ?  "center" : "right";
                break;
            case 6:
                popupOptions.align = popupOptions.tail.align = "right";
                break;
        }
        var popupSettings = jQuery.extend(popupOptionsDefault, popupOptions);
        if (!popupElement.HasPopOver()) popupElement.CreatePopOver({manageMouseEvents: false});
        popupElement.ShowPopOver(popupSettings, false);
        popupElement.FreezePopOver();

        this.highlightActiveEvents(calEvent);

        jQuery("body").on("click", function(event) {
            var target = (event.target) ? jQuery(event.target): jQuery(event.srcElement);
            if (target.parents("#" + popupElement.GetPopOverID()).length === 0 || target.hasClass("popover__close")) {
                KsapSbCalendar.closePopover();
            }
        });
    },

    highlightActiveEvents : function (calEvent) {
        var courseEvents = this.fullCalendar('clientEvents', function(event) {
            if (typeof event.popoverContent === "undefined") return false;
            return (calEvent.popoverContent.courseId === event.popoverContent.courseId &&
                calEvent.popoverContent.courseCd === event.popoverContent.courseCd &&
                calEvent.uniqueid === event.uniqueid);
        });

        for (var i = 0; i < courseEvents.length; i++) {
            jQuery("#possible_schedule_event" + courseEvents[i]._id + ".fc-event").addClass("schedulePossible--highlight");
        }
    },

    clearActiveEvents : function () {
        for (var i = 0; i < this.fullCalendar('clientEvents').length; i++) {
            jQuery("#possible_schedule_event" + this.fullCalendar('clientEvents')[i]._id + ".fc-event").removeClass("schedulePossible--highlight");
        }
    },

    closePopover : function () {
        this.clearActiveEvents();
        fnCloseAllPopups();
        jQuery("body").off("click");
    }
};
