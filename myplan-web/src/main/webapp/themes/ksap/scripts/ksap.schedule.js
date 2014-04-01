var KsapScheduleBuild = {
    plannedActivities: (jQuery("#schedule_build_learningPlanId").data("planned-activities") ? jQuery("#schedule_build_learningPlanId").data("planned-activities") : {}),

    registeredSchedule: (jQuery("#schedule_build_learningPlanId").data("registered-schedule") ? jQuery("#schedule_build_learningPlanId").data("registered-schedule") : {}),

    addReservedSchedule: function () {
        var form = jQuery("#popupForm");
        form.ajaxSubmit({
            data: ksapAdditionalFormData({
                methodToCall: "createReservedTime"
            }),
            dataType: 'json',
            success: function (response, textStatus, jqXHR) {
                var container = jQuery("div.scheduleReserved__container");
                var template = jQuery("#sb-reserved-item-template").wrap("<div/>").parent().html();
                template = template.replace(/id="sb-reserved-item-template"/gi, "");
                template = template.replace(/__KSAP_ID__/gi, response.id);
                template = template.replace(/__KSAP_TERM_ID__/gi, response.termId);
                template = template.replace(/__KSAP_DAYSTIMES__/gi, response.daysTimes);
                var item = jQuery(template).attr("id", "reserved-item-" + response.id).attr("data-source", JSON.stringify(response)).show();
                container.append(item);
                fnCloseAllPopups();
                KsapScheduleBuild.toggleAddReservedAction(container.parents(".scheduleReserved"));
                jQuery.event.trigger("REFRESH_POSSIBLE_SCHEDULES");
            },
            error: function (jqXHR, textStatus, errorThrown) {
                if (textStatus == "parsererror")
                    textStatus = "Parse Error in response";
                showGrowl(errorThrown, jqXHR.status + " " + textStatus);
                fnCloseAllPopups();
            }
        });
    },

    editReservedSchedule: function (id) {
        var form = jQuery("#popupForm");
        form.ajaxSubmit({
            data: ksapAdditionalFormData({
                id: id,
                methodToCall: "updateReservedTime"
            }),
            dataType: 'json',
            success: function (response, textStatus, jqXHR) {
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
            error: function (jqXHR, textStatus, errorThrown) {
                if (textStatus == "parsererror")
                    textStatus = "Parse Error in response";
                showGrowl(errorThrown, jqXHR.status + " " + textStatus);
                fnCloseAllPopups();
            }
        });
    },

    removeReservedSchedule: function (id, event) {
        var form = jQuery("#kualiForm");
        form.ajaxSubmit({
            data: ksapAdditionalFormData({
                methodToCall: "removeReservedTime",
                uniqueId: id
            }),
            dataType: 'json',
            success: function (response, textStatus, jqXHR) {
                var target = (event.currentTarget) ? jQuery(event.currentTarget) : jQuery(event.srcElement);
                target.parents(".scheduleReserved__item").remove();
                KsapScheduleBuild.toggleAddReservedAction(jQuery(".scheduleReserved"));
                jQuery.event.trigger("REFRESH_POSSIBLE_SCHEDULES");
            },
            error: function (jqXHR, textStatus, errorThrown) {
                if (textStatus == "parsererror")
                    textStatus = "JSON Parse Error";
                showGrowl(errorThrown, jqXHR.status + " " + textStatus);
            }
        });
    },

    toggleAddReservedAction: function (group) {
        var limit = group.data("limit");
        var count = group.find(".scheduleReserved__container .scheduleReserved__item").length;
        var action = group.find(".uif-footer");
        if (count >= limit) {
            action.hide();
        } else {
            action.show();
        }
    },

    getPopoverHtml: function (popoverContent, id, templateId) {
        var popoverTemplate = jQuery("#" + templateId).clone().wrap('<div/>').parent().html();
        popoverTemplate = popoverTemplate.replace(/sb-popover-content-template/gi, id);
        var regex = new RegExp(' id="u[0-9]*"', "gi");
        popoverTemplate = popoverTemplate.replace(regex, "");
        popoverTemplate = popoverTemplate.replace(/__KSAP_COURSECD__/gi, popoverContent.courseCd);
        popoverTemplate = popoverTemplate.replace(/__KSAP_COURSEID__/gi, popoverContent.courseId);
        popoverTemplate = popoverTemplate.replace(/__KSAP_COURSETITLE__/gi, popoverContent.courseTitle);

        var cellClasses = [
            "sectionCd",
            "meetingDay",
            "meetingTime",
            "meetingLocation",
            "registrationCode",
            "termCd",
            "instituteCd",
            "enrollRestriction",
            "enrollStatus",
            "enrollState",
            "activityAction"
        ];
        var popoverHtml = jQuery(popoverTemplate);
        var activitiesTemplate = popoverHtml.find("td").each(function(index){
            jQuery(this).addClass(cellClasses[index]);
        }).parents("tbody").html();
        if (popoverContent.errorType) {
            popoverHtml.find(".uif-validationMessages").addClass("alert alert-" + popoverContent.errorType).html(popoverContent.errorMessage).show();
        }
        popoverHtml.find("table tbody").empty();

        for (var i = 0; i < popoverContent.activities.length; i++) {
            var tempActivitiesTemplate = activitiesTemplate;
            tempActivitiesTemplate = tempActivitiesTemplate.replace(/__KSAP_SECTIONCD__/gi, popoverContent.activities[i].sectionCd);
            var tempMeetingDay = "", tempMeetingTime = "", tempMeetingLocation = "";
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
            tempActivitiesTemplate = tempActivitiesTemplate.replace(/__KSAP_SUMMERTERM__/gi, "");// Summer A or B label
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

    openPopover: function (calEvent, event) {
        stopEvent(event);
        this.closePopover();
        var popupElement = (event.currentTarget) ? jQuery(event.currentTarget) : jQuery(event.srcElement);
        var popupHtml = "";
        if (jQuery.isArray(calEvent)) {
            for (var i = 0; i < calEvent.length; i++) {
                popupHtml += this.getPopoverHtml(calEvent[i].popoverContent, "u-" + calEvent[i].popoverContent.courseId, "sb-popover-content-template");
            }
        } else {
            popupHtml = this.getPopoverHtml(calEvent.popoverContent, "u-" + calEvent.popoverContent.courseId, "sb-popover-content-template");
            KsapSbCalendar.highlightActiveEvents(calEvent);
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
                    popupOptions.align = popupOptions.tail.align = (KsapSbCalendar.widget.fullCalendar('option', 'weekends')) ? "center" : "right";
                    break;
                case 6:
                    popupOptions.align = popupOptions.tail.align = "right";
                    break;
            }
        } else {
            popupOptions.position = "bottom";
            var tba = popupElement.parents(".scheduleBuilder__tba").find(".scheduleBuilder__tbaItem").filter(function () {
                return jQuery(this).css('display') != 'none';
            });
            var index;
            for (var i = 0; i < tba.length; i++) {
                if (jQuery(tba[i])[0] == popupElement[0]) {
                    index = i;
                    break;
                }
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

        jQuery("body").on("click", function (event) {
            var target = (event.target) ? jQuery(event.target) : jQuery(event.srcElement);
            if (target.parents("#" + popupElement.GetPopOverID()).length === 0 || target.hasClass("popover__close")) {
                KsapScheduleBuild.closePopover();
            }
        });
    },

    closePopover: function () {
        KsapSbCalendar.clearActiveEvents();
        fnCloseAllPopups();
        jQuery("body").off("click");
    },

    isPlanned: function (activityId) {
        return (typeof this.plannedActivities[activityId] != "undefined");
    },

    addPlannedActivity: function (data) {
        if (!KsapScheduleBuild.isPlanned(data.ActivityOfferingId)) {
            this.plannedActivities[data.ActivityOfferingId] = data.planItemId;
            jQuery("#action-" + data.RegistrationCode).removeClass("schedulePopover__itemAdd").addClass("schedulePopover__itemDelete").blur();
        }

        if (data.ItemsToUpdate) {
            var item = jQuery("#action-" + data.ItemsToUpdate).data();
            if (!KsapScheduleBuild.isPlanned(item.activityId)) {
                this.plannedActivities[item.activityId] = data.PrimaryPlanItemId;
                jQuery("#action-" + data.ItemsToUpdate).removeClass("schedulePopover__itemAdd").addClass("schedulePopover__itemDelete");
            }
        }
    },

    removePlannedActivity: function (data) {
        if (KsapScheduleBuild.isPlanned(data.ActivityOfferingId)) {
            delete this.plannedActivities[data.ActivityOfferingId];
            jQuery("#action-" + data.RegistrationCode).removeClass("schedulePopover__itemDelete").addClass("schedulePopover__itemAdd").blur();
        }

        if (data.ItemsToUpdate) {
            var items = data.ItemsToUpdate.split(',');
            jQuery.each(items, function (index, value) {
                var item = jQuery("#action-" + items[index]).data();
                if (KsapScheduleBuild.isPlanned(item.activityId)) {
                    delete KsapSbCalendar.plannedActivities[item.activityId];
                    jQuery("#action-" + items[index]).removeClass("schedulePopover__itemDelete").addClass("schedulePopover__itemAdd");
                }
            });
        }
    },

    togglePlanSection: function (data, event) {
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

    viewScheduleDetails: function (calEvents, index, id, event) {
        stopEvent(event);
        var cache = [];
        var lightboxHtml = "";
        for (var i = 0; i < calEvents.length; i++) {
            var course = calEvents[i].popoverContent.courseId + "-" + calEvents[i].popoverContent.courseCd.replace(/ /g,'');
            if (cache.indexOf(course) === -1) {
                lightboxHtml += this.getPopoverHtml(calEvents[i].popoverContent, "u-" + calEvents[i].popoverContent.courseId, "sb-additional-details-template");
                cache.push(course);
            }
        }
        var lightboxTemplate = jQuery("#sb-additional-details-group").clone().wrap("<div/>").parent().html();
        lightboxTemplate = lightboxTemplate.replace(/sb-additional-details-group/gi, "u-" + id);
        lightboxTemplate = lightboxTemplate.replace(/__KSAP_PINNED_INDEX__/gi, index);
        lightboxTemplate = lightboxTemplate.replace(/__KSAP_PINNED_ID__/gi, id);
        var lightboxContent = jQuery(lightboxTemplate);
        lightboxContent.find(".uif-verticalBoxLayout").html(lightboxHtml);

        jQuery.fancybox({
            content: lightboxContent.show().wrap("<div/>").parent().html()
        });
    },

    confirmRemovedSavedSchedule: function (id, inLightBox, event) {
        stopEvent(event);
        var confirmHtml = jQuery("#sb_confirm_remove_pinned").clone().show().wrap("<div/>").parent().html();
        confirmHtml = confirmHtml.replace(/sb_confirm_remove_pinned/gi, "u-" + id + "-remove");
        confirmHtml = confirmHtml.replace(/__KSAP_PINNED_ID__/gi, id);
        confirmHtml = confirmHtml.replace(/__KSAP_IN_LIGHTBOX__/gi, inLightBox);
        if (inLightBox) {
            jQuery("#u-" + id).hide().parent(".fancybox-inner").append(confirmHtml);
        } else {
            openDialog(confirmHtml, null, true);
        }
    },

    cancel: function (id, inLightBox) {
        if (inLightBox) {
            jQuery("#u-" + id + "-remove").remove();
            jQuery("#u-" + id).show();
        } else {
            fnCloseAllPopups();
        }
    }


};