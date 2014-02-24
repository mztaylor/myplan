function uuid() {
	// http://stackoverflow.com/questions/105034/how-to-create-a-guid-uuid-in-javascript
	return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
	    var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
	    return v.toString(16);
	});
}

var KsapScheduleBuild = {};
var KsapSbCalendar;

function createScheduleBuilder(obj) {
    KsapSbCalendar = obj;

    KsapScheduleBuild = {
        triggerEnabled : true,
        pendingBuild : 0,
        possibleSchedules : null,

        initialize : function() {
            this.build(false);
        },

        trigger : function() {
            if (!this.triggerEnabled)
                return;
            if (this.pendingBuild != 0)
                clearTimeout(this.pendingBuild);
            this.pendingBuild = setTimeout("KsapScheduleBuild.build(false)", 1250);
        },

        build : function(more, removeReserved) {
            var sb = this;
            sb.triggerEnabled = false;
            showLoading("Building...", KsapSbCalendar);
            jQuery("#kualiForm").ajaxSubmit({
                data : {
                    "methodToCall" : "build",
                    "more" : more,
                    "removeReserved" : removeReserved
                },
                dataType : 'json',
                success : function(newPossibleSchedules, textStatus, jqXHR) {
                    if (hasWeekends(newPossibleSchedules) != KsapSbCalendar.fullCalendar('option', 'weekends')) {
                        KsapSbCalendar.fullCalendar('destroy');
                        delete KsapSbCalendarOptions.complete;
                        KsapSbCalendarOptions.weekends = hasWeekends(newPossibleSchedules);
                        KsapSbCalendar.fullCalendar(KsapSbCalendarOptions);
                    } else {
                        KsapSbCalendar.fullCalendar('removeEvents');
                    }
                    if (newPossibleSchedules.todo != null)
                        showGrowl(newPossibleSchedules.todo);
                    sb.possibleSchedules = newPossibleSchedules;

                    jQuery(".ksap-sb-possible-options-container").empty();
                    for (var i in sb.possibleSchedules.possible)
                        sb.appendPossibleOption(sb.possibleSchedules.possible[i]);
                    if (sb.possibleSchedules.possible.length == 0)
                        jQuery(".ksap-sb-noschedules").show();
                    else
                        jQuery(".ksap-sb-noschedules").hide();

                    if (sb.possibleSchedules.more)
                        jQuery(".ksap-sb-morelink").show();
                    else
                        jQuery(".ksap-sb-morelink").hide();

                    jQuery(".ksap-sb-saved-schedules-container").empty();
                    for (var i in sb.possibleSchedules.saved)
                        sb.appendSavedOption(sb.possibleSchedules.saved[i]);

                    jQuery(".ksap-sb-reserved-times-container").empty();
                    for (var i=0; i<sb.possibleSchedules.reserved.length; i++) {
                        sb.appendReservedTime(i);
                    }

                    var weekgroups = jQuery(".ksap-sb-week-group-area-container");
                    weekgroups.empty();
                    for (var i=0; i<sb.possibleSchedules.weeks.length; i++) sb.appendWeekGroup(i);

                },
                error : function(jqXHR, textStatus, errorThrown) {
                    if (textStatus == "parsererror")
                        textStatus = "JSON Parse Error";
                    showGrowl(errorThrown, jqXHR.status + " " + textStatus);
                },
                complete : function() {
                    hideLoading(KsapSbCalendar);
                    sb.triggerEnabled = true;
                }
            });
        },

        isSaved : function(uniqueId) {
            if (this.possibleSchedules == null) return false;
            if (this.possibleSchedules.saved != null)
                for (var i in this.possibleSchedules.saved)
                    if (this.possibleSchedules.saved[i].uniqueId == uniqueId)
                        return true;
            return false;
        },

        getSchedule : function(uniqueId) {
            if (this.possibleSchedules == null) return null;
            if (this.possibleSchedules.possible != null)
                for (var i in this.possibleSchedules.possible)
                    if (this.possibleSchedules.possible[i].uniqueId == uniqueId)
                        return this.possibleSchedules.possible[i];
            if (this.possibleSchedules.saved != null)
                for (var i in this.possibleSchedules.saved)
                    if (this.possibleSchedules.saved[i].uniqueId == uniqueId)
                        return this.possibleSchedules.saved[i];
            return null;
        },

        appendPossibleOption : function(schedule) {
            appendScheduleOption(schedule, jQuery(".ksap-sb-possible-options-container"));
        },

        appendSavedOption : function(schedule) {
            appendScheduleOption(schedule, jQuery(".ksap-sb-saved-schedules-container"));
        },

        moveUp : function(uniqueId) {
            moveScheduleOption(uniqueId, false);
        },

        moveDown : function(uniqueId) {
            moveScheduleOption(uniqueId, true);
        },

        trash : function(uniqueId) {
		    trashScheduleOption(uniqueId);
        },

        save : function(uniqueId) {
            saveOrRemoveScheduleOption(uniqueId);
        },

        sendToCart : function(uniqueId, termId, e) {
            ksapSbOpenDialog("sb_cart_add_from_sb", "sb/cart", {"possibleScheduleId" : uniqueId, "termId": termId}, e.target, e);
        },

        getReservedTime : function(index) {
            if (this.possibleSchedules == null ||
                this.possibleSchedules.reserved == null ||
                this.possibleSchedules.reserved.length <= index) return null;
            return this.possibleSchedules.reserved[index];
        },

        appendReservedTime : function(i) {
            var reserved = this.possibleSchedules.reserved[i];
            var container = jQuery(".ksap-sb-reserved-times-container");
            var template = jQuery("#sb_reserved_time_template").html();

            var uid = reserved.uniqueId;
            var row = jQuery("#sb-reserved-time-row-" + uid);
            if (row.length > 0) {
                uid = uuid();
                row.parent().data("altuid", uid);
            }

            template = template.replace(/__KSAP_UID__/gi, uid);
            template = template.replace(/__KSAP_INDEX__/gi, i);
            template = template.replace(/__KSAP_DESCR__/gi, reserved.descr);
            template = template.replace(/__KSAP_START__/gi, reserved.startDate);
            template = template.replace(/__KSAP_UNTIL__/gi, reserved.untilDate);
            template = template.replace(/__KSAP_DAYSTIMES__/gi, reserved.daysTimes);
            template = template.replace(/id=\"(u\d+)/gi, "id=\""+uid+"_$1");
            row = jQuery(template);

            var _cb = row.find(":hidden[name='_selected']");
            var cb = row.find(":checkbox");
            cb.prop("checked", reserved.selected);

            if (uid == reserved.uniqueId) {
                _cb.attr("name", "_reservedTimes["+i+"].selected");
                cb.attr("name", "reservedTimes["+i+"].selected");
            } else {
                _cb.removeAttr("name");
                cb.removeAttr("name");
            }

            container.append(row);
            row.find("input[data-role='script']").removeAttr("script").attr("name", "script");
            evaluateScripts(row);

            row.data("realuid", reserved.uniqueId);
            row.data("altuid", uid);
            toggleReservedTimeSelect(uid);
            row.show();
        },

        trashReservedTime : function(uniqueId) {
            var row = jQuery("#sb-reserved-time-row-" + uniqueId);
            if (row.length == 0) return;
            var i = row.data("index");
            if (i < 0 || i >= this.possibleSchedules.reserved.length) return;
            this.build(false,i);
            row.parent().remove();
        },

        appendWeekGroup : function(i) {
            var week = this.possibleSchedules.weeks[i];
            var container = jQuery(".ksap-sb-week-group-area-container");
            var template = jQuery("#sb_weekgroup_template").html();

            template = template.replace(/__KSAP_TITLE__/gi, week.title);
            template = template.replace(/__KSAP_SUBTITLE__/gi, week.subtitle);
            template = template.replace(/id=\"(u\d+)/gi, "id=\"$1_"+i);
            var row = jQuery(template).children(".uif-boxGroup");
            if (i == 0) {
                KsapSbCalendar.fullCalendar('gotoDate', week.gotoYear, week.gotoMonth-1, week.gotoDate);
            } else {
                row.removeClass("ksap-sb-selected");
                row.addClass("ksap-sb-deselected");
            }

            container.append(row);

            row.data('year', week.gotoYear);
            row.data('month', week.gotoMonth);
            row.data('date', week.gotoDate);
            row.on('click', function(){
                var t = jQuery(this);
                KsapSbCalendar.fullCalendar('gotoDate', t.data('year'), t.data('month')-1, t.data('date'));
                jQuery(".ksap-sb-week-group-area-container .ksap-sb-weekGroup").each(function() {
                    var t = jQuery(this);
                    t.removeClass("ksap-sb-selected");
                    t.addClass("ksap-sb-deselected");
                });
                t.addClass("ksap-sb-selected");
                t.removeClass("ksap-sb-deselected");
            });

            row.show();
        }

    };

    KsapScheduleBuild.initialize();
}

function moreSchedules() {
	KsapScheduleBuild.build(true);
}

///////////////////////////////////////////////
// Possible/Saved schedule options section support

function fixUpDownIcons() {
	jQuery(".ksap-sb-saved-schedules-container, .ksap-sb-possible-options-container")
		.children().each(function(){
			var trow = jQuery(this);
			if (trow.prev().length > 0) {
				trow.find(".ksap-sb-schedule-upicon").show();
				trow.find(".ksap-sb-schedule-noupicon").hide();
			} else {
				trow.find(".ksap-sb-schedule-upicon").hide();
				trow.find(".ksap-sb-schedule-noupicon").show();
			}

			if (trow.next().length > 0) {
				trow.find(".ksap-sb-schedule-downicon").show();
				trow.find(".ksap-sb-schedule-nodownicon").hide();
			} else {
				trow.find(".ksap-sb-schedule-downicon").hide();
				trow.find(".ksap-sb-schedule-nodownicon").show();
			}
		});
}

function appendScheduleOption(schedule, container) {
	var template = jQuery("#sb_schedule_option_template").html();
	template = template.replace(/__KSAP_UID__/gi, schedule.uniqueId);
	template = template.replace(/__KSAP_FORMATTED__/gi, schedule.htmlDescription);
	template = template.replace(/id=\"(u\d+)/gi, "id=\""+schedule.uniqueId+"_$1");
	var row = jQuery(template);
	
	row.attr("id", schedule.uniqueId);
	
	if (schedule.saved) {
		var saveicon = row.find(".ksap-sb-schedule-saveicon");
		saveicon.removeClass("ksap-sb-schedule-saveicon");
		saveicon.addClass("ksap-sb-schedule-savedicon");
		row.children(".ksap-sb-schedule-row").data("saveid", schedule.id);
	}

	var _cb = row.find(":hidden[name='_selected']");
	_cb.attr("name", "_"+schedule.path+".selected");

	var cb = row.find(":checkbox");
	cb.prop("checked", schedule.selected);
	cb.attr("name", schedule.path+".selected");

	container.append(row);
	
	var shuf = row.find(":hidden[name='shuffle']");
	shuf.attr("name", schedule.path+".shuffle");
	shuf.attr("value", row.index());
	
	row.find("input[data-role='script']").removeAttr("script").attr("name", "script");
	evaluateScripts(row);
	
	toggleScheduleOptionSelect(schedule.uniqueId);
	fixUpDownIcons();
	
	row.show();
}

function moveScheduleOption(uniqueId, down) {
	if (KsapScheduleBuild.getSchedule(uniqueId) == null) return;
	var row = jQuery("#" + uniqueId);
	
	if (down) {
		row = row.next();
	}
	if (row.index() == 0) return;
	
	var drow = row.prev();
	row.detach().insertBefore(drow);

	row.find(":hidden[name$='shuffle']").attr("value", row.index());
	drow.find(":hidden[name$='shuffle']").attr("value", drow.index());
	
	fixUpDownIcons();
}

function trashScheduleOption(uniqueId) {
	if (KsapScheduleBuild.isSaved(uniqueId)) {
		removeSavedScheduleOption(uniqueId);
		return;
	}	
	
	var model = KsapScheduleBuild.getSchedule(uniqueId);
	if (model == null) return;
	
	KsapSbCalendar.fullCalendar('removeEventSource', model);

	// remove from view
	var row = jQuery("#" + uniqueId);
	var container = row.parent();
	row.remove();

	// adjust rows
	container.children().each(function(){
		var crow = jQuery(this);
		crow.find(":hidden[name$='shuffle']").attr("value", crow.index());
	});

	var discardInput = jQuery("<input type=\"hidden\" value=\"true\" />");
	discardInput.attr("name", model.path+".discard");
	container.append(discardInput);
}

function removeSavedScheduleOption(uniqueId) {
    jQuery("#kualiForm").ajaxSubmit({
        data : {
            methodToCall : "remove",
            uniqueId : uniqueId
        },
        dataType : 'json',
        success : function(response, textStatus, jqXHR) {
            console.log(response);
            customRetrieveComponent('saved_schedules_summary','saved_schedules_summary','search','lookup',{viewId:'SavedSchedulesSummary-LookupView',termId:jQuery('#schedule_build_termId_control').val(),learningPlanId:jQuery('#schedule_build_learningPlanId_control').val()},null,{css:{right:'0px',top:'0px',width:'16px',height:'16px',lineHeight:'16px',border:'none'}});
        },
        error : function(jqXHR, textStatus, errorThrown) {
            if (textStatus == "parsererror")
                textStatus = "JSON Parse Error";
            showGrowl(errorThrown, jqXHR.status + " " + textStatus);
        }
    });

}

function saveScheduleOption(uniqueId) {
    jQuery("#kualiForm").ajaxSubmit({
        data : {
            methodToCall : "save",
            uniqueId : uniqueId
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

function saveOrRemoveScheduleOption(uniqueId) {
	var model = KsapScheduleBuild.getSchedule(uniqueId);
	if (model == null) return;
	
	if (jQuery("#"+uniqueId+" .ksap-sb-schedule-savedicon").length > 0)
		removeSavedScheduleOption(uniqueId);
	else
		saveScheduleOption(uniqueId);
}

function toggleScheduleOption(uniqueId) {
	var cb = jQuery("#sb-schedule-option-select-" + uniqueId + "_control");
	cb.prop("checked", !cb.is(":checked"));
	toggleScheduleOptionSelect(uniqueId);
}

function toggleScheduleOptionSelect(uniqueId) {
	var row = jQuery("#sb-schedule-option-row-" + uniqueId);
	var cb = jQuery("#sb-schedule-option-select-" + uniqueId + "_control");
	var schedule = KsapScheduleBuild.getSchedule(uniqueId);
	var selected = cb.is(":checked");
	if (selected) {
		row.addClass("ksap-sb-selected");
		row.removeClass("ksap-sb-deselected");
		//row.find(".ksap-sb-schedule-activitydetail").show();
		if (schedule != null) {
			for (var i in schedule.eventClass)
				row.addClass(schedule.eventClass[i]);
			KsapSbCalendar.fullCalendar('addEventSource', schedule);
		}
	} else {
		row.removeClass("ksap-sb-selected");
		row.addClass("ksap-sb-deselected");
		//row.find(".ksap-sb-schedule-activitydetail").hide();
		if (schedule != null) {
			for (var i in schedule.eventClass)
				row.removeClass(schedule.eventClass[i]);
			KsapSbCalendar.fullCalendar('removeEventSource', schedule);
		}
	}
}


///////////////////////////////////////
// Schedule Build dialog box support

function ksapSbOpenDialog(pageId, action, data, target, e) {
	var t = jQuery(target);
	var retrieveData = jQuery.extend(data, {
		action : action, pageId : pageId + "_page"
	});
	var popupOptions = {
		tail : {
			hidden : true
		},
		close : true ,
		sticky : true ,
        position : "right" ,
		themeName : "default"
	};
	jQuery("#popupForm").remove();
	fnClosePopup();
	openPopup(pageId + "_inner", retrieveData, action, {width: "16px", height: "16px"}, popupOptions, e);
	var form = jQuery("#popupForm");
	form.attr("accept-charset", "UTF-8");
}

function ksapSbValidateDialog() {
	clientErrorStorage = new Object();
	var summaryTextExistence = new Object();
	var popupForm = jQuery("#popupForm");
	var validForm = true;

	jQuery.watermark.hideAll();
	pauseTooltipDisplay = true;

    messageSummariesShown = true;
	validForm = popupForm.valid();

	if (!validForm) {
	    validForm = false;

	    //ensure all non-visible controls are visible to the user
	    popupForm.find(".error:not(:visible)").each(function () {
	        cascadeOpen(jQuery(this));
	    });

	    showClientSideErrorNotification();
	    popupForm.find(".uif-pageValidationMessages li.uif-errorMessageItem:first > a").focus();
	}

	jQuery.watermark.showAll();
	pauseTooltipDisplay = false;
	
	return validForm;
}

function ksapSbSubmitDialog(methodToCall, e) {
	
    if (!ksapSbValidateDialog()) {
        clearHiddens();

        return;
    }
	
	var button = jQuery(e.currentTarget);

	button.block({
		message : '<img src="../ks-ap/images/btnLoader.gif"/>',
		css : {
			width : '100%',
			border : 'none',
			backgroundColor : 'transparent'
		},
		overlayCSS : {
			backgroundColor : '#fff',
			opacity : 0.6,
			padding : '0px 1px',
			margin : '0px -1px'
		}
	});

	var form = jQuery("#popupForm");
	form.ajaxSubmit({
		data : ksapAdditionalFormData({
			methodToCall : methodToCall
		}),
		dataType : 'json',
		success : function(newPossibleSchedules, textStatus, jqXHR){
            jQuery('#sb_reserved_time ul').append('<li><div id="possible-schedule-'+newPossibleSchedules.id+'" class="uif-boxSection" data-selected="false"><div class="uif-horizontalFieldGroup uif-boxLayoutVerticalItem clearfix" data-parent="possible-schedule-'+newPossibleSchedules.id+'"><div class="uif-horizontalBoxGroup"><div class="uif-horizontalBoxLayout clearfix"><span class="uif-message uif-boxLayoutHorizontalItem">'+newPossibleSchedules.daysTimes+' </span><a onclick="return false;" class="uif-actionLink uif-boxLayoutHorizontalItem">Edit</a><span class="uif-message uif-boxLayoutHorizontalItem"> | </span><a onclick="return false;" class="uif-actionLink uif-boxLayoutHorizontalItem" data-ajaxsubmit="true" data-onclick="removeReservedScheduleOption(\''+newPossibleSchedules.id+'\');" >Delete</a></div></div></div></div></li>')
            fnClosePopup();
            customRetrieveComponent('possible_schedules_details','possible_schedules_details','search','lookup',{viewId:'PossibleSchedules-LookupView',termId:jQuery('#schedule_build_termId_control').val(),learningPlanId:jQuery('#schedule_build_learningPlanId_control').val()},null,{css:{right:'0px',top:'0px',width:'16px',height:'16px',lineHeight:'16px',border:'none'}});
        },
		error : function(jqXHR, textStatus, errorThrown) {
			if (textStatus == "parsererror")
				textStatus = "Parse Error in response";
			showGrowl(errorThrown, jqXHR.status + " " + textStatus);
			fnClosePopup();
		},
		complete : function() {
			button.unblock();
		}
	});
}

function ksapSbDialogResponse(response, textStatus, jqXHR) {
	fnClosePopup();
	KsapScheduleBuild.trigger();
}

////////////////////////////////////
// Reserved Times section support

function toggleReservedTime(uniqueId) {
	var row = jQuery("#sb-reserved-time-row-" + uniqueId);
	var cb = jQuery("#sb-reserved-time-select-" + uniqueId + "_control");
	cb.prop("checked", !cb.is(":checked"));
	toggleReservedTimeSelect(uniqueId);
}

function toggleReservedTimeSelect(uniqueId) {
	var row = jQuery("#sb-reserved-time-row-" + uniqueId);
	var cb = jQuery("#sb-reserved-time-select-" + uniqueId + "_control");

	var realuid = row.parent().data("realuid");
	if (realuid != uniqueId)
		row = jQuery("#sb-reserved-time-row-" + realuid);
	
	var altuid = row.parent().data("altuid");
	var altrow = jQuery("#sb-reserved-time-row-" + altuid);

	var selected = cb.is(":checked");
	jQuery("#sb-reserved-time-select-" + realuid + "_control").prop("checked", selected);
	jQuery("#sb-reserved-time-select-" + altuid + "_control").prop("checked", selected);
	
	var reserved = KsapScheduleBuild.getReservedTime(row.data("index"));
	if (selected) {
		row.addClass("ksap-sb-selected");
		row.removeClass("ksap-sb-deselected");
		altrow.addClass("ksap-sb-selected");
		altrow.removeClass("ksap-sb-deselected");
		if (reserved != null) {
			for (var i in reserved.eventClass) {
				row.addClass(reserved.eventClass[i]);
				altrow.addClass(reserved.eventClass[i]);
			}
			if (!reserved.visible) {
				reserved.visible = true;
				KsapSbCalendar.fullCalendar('addEventSource', reserved);
			}
		}
	} else {
		row.removeClass("ksap-sb-selected");
		row.addClass("ksap-sb-deselected");
		altrow.removeClass("ksap-sb-selected");
		altrow.addClass("ksap-sb-deselected");
		if (reserved != null) {
			for (var i in reserved.eventClass) {
				row.removeClass(reserved.eventClass[i]);
				altrow.removeClass(reserved.eventClass[i]);
			}
			if (reserved.visible) {
				reserved.visible = false;
				KsapSbCalendar.fullCalendar('removeEventSource', reserved);
			}
		}
	}
	KsapScheduleBuild.trigger();
}

////////////////////////////////////////////////////////
// Classes section course options (top level) support

function getCourseActivityCount(courseUniqueId) {
	return jQuery("#sb_activity_options_" + courseUniqueId
			+ " .ksap-sb-primary-row .ksap-sb-activity-select .uif-checkboxControl").length;
}

function getCourseActivitySelectedCount(courseUniqueId) {
	return jQuery("#sb_activity_options_" + courseUniqueId
			+ " .ksap-sb-primary-row .ksap-sb-activity-select .uif-checkboxControl:checked").length;
}

function updateCourseSelectedCount(courseUniqueId) {
	var sel = getCourseActivitySelectedCount(courseUniqueId);
	var tot = getCourseActivityCount(courseUniqueId);
	if (tot == 0)
		return;
	jQuery("#sb-course-selected-message-" + courseUniqueId + "_span").text(
			sel + " / " + tot + " selected");
	var row = jQuery("#sb-course-option-row-" + courseUniqueId);
	var cb = jQuery("#sb-course-option-select-" + courseUniqueId + "_control");
	if (sel > 0) {
		row.addClass("ksap-sb-selected");
		row.removeClass("ksap-sb-deselected");
		cb.prop("checked", true);
	} else {
		row.removeClass("ksap-sb-selected");
		row.addClass("ksap-sb-deselected");
		cb.prop("checked", false);
	}
}

function toggleCourseOption(uniqueId) {
	var cb = jQuery("#sb-course-option-select-" + uniqueId + "_control");
	if (cb.length == 0) return;
	cb.prop("checked", !cb.is(":checked"));
	toggleCourseOptionSelect(uniqueId);
}

function toggleCourseOptionSelect(uniqueId) {
	var row = jQuery("#sb-course-option-row-" + uniqueId);
	var cb = jQuery("#sb-course-option-select-" + uniqueId + "_control");
	var selected = cb.is(":checked");
	var primaryRows = jQuery("#sb_activity_options_" + uniqueId + " .ksap-sb-primary-row");
			primaryRows.each(
					function() {
						var t = jQuery(this);
						t.find(".uif-checkboxControl").prop("checked",
							selected && t.hasClass("ksap-sb-activity-open"));
						toggleActivitySelect(t.data("primary"), t.data("uniqueid"), uniqueId);
					});
	updateCourseSelectedCount(uniqueId);
	KsapScheduleBuild.trigger();
}

function showHideActivityOptions(uniqueId) {
	var ao = jQuery("#sb_activity_options_" + uniqueId);
	var dicon = jQuery("#ksap-sb-course-disclosure-icon-" + uniqueId);
	if (ao.hasClass("ksap-sb-disclosure-closed")
			&& getCourseActivityCount(uniqueId) > 0) {
		ao.removeClass("ksap-sb-disclosure-closed");
		dicon.addClass("ksap-sb-course-disclosure-icon-open");
		dicon.removeClass("ksap-sb-course-disclosure-icon-closed");
	} else {
		ao.addClass("ksap-sb-disclosure-closed");
		dicon.removeClass("ksap-sb-course-disclosure-icon-open");
		dicon.addClass("ksap-sb-course-disclosure-icon-closed");
	}
}

//////////////////////////////////////////////
// Classes section activity options support

function getPrimaryActivityCount(primaryUniqueId) {
	return jQuery("#sb_secondary_options_" + primaryUniqueId + " .ksap-sb-secondary-row").length;
}

function getPrimaryActivitySelectedCount(primaryUniqueId) {
	var row = jQuery("#sb_secondary_options_" + primaryUniqueId + " .ksap-sb-secondary-row");
	var cb = row.find(".uif-checkboxControl").length;
	if (cb == 0) {
		if (jQuery("#sb-activity-select-" + primaryUniqueId + "_control").is(":checked"))
			return row.length;
		else
			return 0;
	} else
		return row.find(".uif-checkboxControl:checked").length;
}

function updatePrimarySelectedCount(primaryUniqueId) {
	var sel = getPrimaryActivitySelectedCount(primaryUniqueId);
	var tot = getPrimaryActivityCount(primaryUniqueId);
	if (tot == 0)
		return;
	var row = jQuery("#sb-secondary-row-" + primaryUniqueId);
	var arow = jQuery("#sb-activity-row-" + primaryUniqueId);
	var cb = jQuery("#sb-activity-select-" + primaryUniqueId + "_control");
	if (sel > 0) {
		row.addClass("ksap-sb-selected");
		row.removeClass("ksap-sb-deselected");
		arow.addClass("ksap-sb-selected");
		arow.removeClass("ksap-sb-deselected");
		cb.prop("checked", true);
	} else {
		row.removeClass("ksap-sb-selected");
		row.addClass("ksap-sb-deselected");
		arow.removeClass("ksap-sb-selected");
		arow.addClass("ksap-sb-deselected");
		cb.prop("checked", false);
	}
}

function toggleActivity(primary, uniqueId, parentUniqueId) {
	var cb = jQuery("#sb-activity-select-" + uniqueId + "_control");
	cb.prop("checked", !cb.is(":checked"));
	toggleActivitySelect(primary, uniqueId, parentUniqueId);
}

function toggleActivitySelect(primary, uniqueId, parentUniqueId) {
	var row = jQuery("#sb-activity-row-" + uniqueId);
	var cb = jQuery("#sb-activity-select-" + uniqueId + "_control");
	if (cb.length == 0 && !primary) {
		var parrow = jQuery("#sb-activity-row-" + parentUniqueId);
		cb = jQuery("#sb-activity-select-" + parentUniqueId + "_control");
	}
	var selected = cb.is(":checked");
	if (selected) {
		row.addClass("ksap-sb-selected");
		row.removeClass("ksap-sb-deselected");
	} else {
		row.removeClass("ksap-sb-selected");
		row.addClass("ksap-sb-deselected");
	}
	if (primary) {
		updateCourseSelectedCount(parentUniqueId);
		jQuery("#sb_secondary_options_" + uniqueId + " .ksap-sb-secondary-row")
				.each(
						function() {
							var t = jQuery(this);
							t.find(".uif-checkboxControl").prop("checked",
									selected && t.hasClass("ksap-sb-activity-open"));
							toggleActivitySelect(t.data("primary").toString().toLowerCase() == "true", t.data("uniqueid"), uniqueId);
						});
	} else {
		updatePrimarySelectedCount(parentUniqueId);
	}
	KsapScheduleBuild.trigger();
}

function showHideSecondaryOptions(uniqueId) {
	var ao = jQuery("#sb_activity_options_" + uniqueId);
	var dicon = jQuery("#ksap-sb-secondary-disclosure-icon-" + uniqueId);
	if (ao.hasClass("ksap-sb-disclosure-closed")
			&& getPrimaryActivityCount(uniqueId) > 0) {
		ao.removeClass("ksap-sb-disclosure-closed");
		dicon.addClass("ksap-sb-secondary-disclosure-icon-open");
		dicon.removeClass("ksap-sb-secondary-disclosure-icon-closed");
	} else {
		ao.addClass("ksap-sb-disclosure-closed");
		dicon.removeClass("ksap-sb-secondary-disclosure-icon-open");
		dicon.addClass("ksap-sb-secondary-disclosure-icon-closed");
	}
}

function removeReservedScheduleOption(uniqueId) {
    jQuery("#kualiForm").ajaxSubmit({
        data : {
            methodToCall : "remove",
            uniqueId : uniqueId
        },
        dataType : 'json',
        success : function(response, textStatus, jqXHR) {
            jQuery("#possible-schedule-"+uniqueId).remove();
            customRetrieveComponent('possible_schedules_details','possible_schedules_details','search','lookup',{viewId:'PossibleSchedules-LookupView',termId:jQuery('#schedule_build_termId_control').val(),learningPlanId:jQuery('#schedule_build_learningPlanId_control').val()},null,{css:{right:'0px',top:'0px',width:'16px',height:'16px',lineHeight:'16px',border:'none'}});
        },
        error : function(jqXHR, textStatus, errorThrown) {
            if (textStatus == "parsererror")
                textStatus = "JSON Parse Error";
            showGrowl(errorThrown, jqXHR.status + " " + textStatus);
        }
    });

}

