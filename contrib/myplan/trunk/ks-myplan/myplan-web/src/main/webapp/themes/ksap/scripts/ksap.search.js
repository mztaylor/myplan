var oTable;
// Object for saving facet data and selections
var oFacets = new Object();

var oProjectedTermOrder = {
    "AU": 1,
    "WI": 2,
    "SP": 3,
    "SU": 4
};

var oScheduledTermOrder = {
    "WI": 1,
    "SP": 2,
    "SU": 3,
    "AU": 4
};

var oGenEduNames = {
    "VLPA": "Visual, Literary and Performing Arts",
    "I&S": "Individuals and Societies",
    "NW": "Natural World",
    "QSR": "Quantitative, Symbolic or Formal Reasoning",
    "C": "English Composition",
    "W": "Writing",
    "DIV": "Diversity"
};

Object.size = function (obj) {
    var size = 0, key;
    for (key in obj) {
        if (obj.hasOwnProperty(key)) size++;
    }
    return size;
};

// Formatter for meeting day facet key - number to day of the week
function meetingDay(x) {
    var days = {
        "0": "Sunday",
        "1": "Monday",
        "2": "Tuesday",
        "3": "Wednesday",
        "4": "Thursday",
        "5": "Friday",
        "6": "Saturday",
        "tba": "TBA"
    };
    return days[x];
}

// Formatter for meeting time facet key - combo of 24 hour time (start and end) to 12 hour time showing time range
function meetingTime(x) {
    if (x.length === 8) {
        var range = [x.substring(0, 4), x.substring(4, x.length)];
        for (var i = 0; i < range.length; i++) {
            var hours24 = parseInt(range[i].substring(0, 2), 10);
            var hours = ((hours24 + 11) % 12) + 1;
            var amPm = hours24 > 11 ? 'p' : 'a';
            var minutes = range[i].substring(2);
            range[i] = hours + ':' + minutes + amPm;
        }
        return range.join(" - ");
    } else {
        return x.toUpperCase();
    }
}

// Facet sorter for numeric keys
function numeric(a, b) {
    if (parseInt(a) > parseInt(b)) return 1;
    else if (parseInt(a) < parseInt(b)) return -1;
    else return 0;
}

// Facet sorter for alpha keys
function alpha(a, b) {
    //  Unknown/None/TBA is always last in the facet group
    if (a == 'Unknown' || a == 'None' || a == 'tba') return 1;
    if (b == 'Unknown' || b == 'None' || a == 'tba') return -1;
    if (a > b) return 1;
    else if (a < b) return -1;
    else return 0;
}

// Facet sorter for terms
function terms(a, b) {
    //  Unknown/None is always last.
    if (a == 'Unknown' || a == 'None') return 1;
    if (b == 'Unknown' || b == 'None') return -1;

    //  If the facet items that end with a year are scheduled terms and should precede terms.
    var bYearA = a.match(/.*\d{2}$/gi);
    var bYearB = b.match(/.*\d{2}$/gi);

    //  Two scheduled terms.
    if (bYearA && bYearB) {
        var sTermA = a.replace(/\d{2}/gi, "").replace(" ", "").toUpperCase();
        var sTermB = b.replace(/\d{2}/gi, "").replace(" ", "").toUpperCase();
        var iYearA = parseInt(a.replace(/\D*/gi, ""));
        var iYearB = parseInt(b.replace(/\D*/gi, ""));

        if (iYearA != iYearB) {
            if (iYearA < iYearB) return -1;
            return 1;
        } else {
            if (oScheduledTermOrder[sTermA] < oScheduledTermOrder[sTermB]) return -1;
            else if (oScheduledTermOrder[sTermA] > oScheduledTermOrder[sTermB]) return 1;
            else return 0;
        }
    }

    if (bYearA && !bYearB) return -1;
    if (!bYearA && bYearB) return 1;

    //  Two terms.
    if (!bYearA && !bYearB) {
        var sTermA = a.replace("Projected ", "").toUpperCase();
        var sTermB = b.replace("Projected ", "").toUpperCase();
        if (oProjectedTermOrder[sTermA] < oProjectedTermOrder[sTermB]) return -1;
        else if (oProjectedTermOrder[sTermA] > oProjectedTermOrder[sTermB]) return 1;
        else return 0;
    }
}

jQuery.fn.iterateSorted = function (sorter, print) {
    var keys = [];
    jQuery.each(this[0], function (key) {
        keys.push(key);
    });
    keys.sort(sorter);
    for (var i = 0; i < keys.length; i++) {
        print(keys[i]);
    }
};

(function ($) {
    $.fn.dataTableExt.oApi.fnGetColumnIndex = function ( oSettings, sCol ) {
        if (typeof sCol == "undefined") return null;
        var cols = oSettings.aoColumns;
        for (var x = 0; x < cols.length; x++) {
            if (cols[x].sTitle.toLowerCase() == sCol.toLowerCase()) {
                return x;
            }
        }
        return -1;
    }

    $.fn.dataTableExt.oApi.fnGetColumnData = function (oSettings, iColumn, bUnique, bFiltered, bIgnoreEmpty) {
        // check for column id
        if (typeof iColumn == "undefined") return new Array();
        // by default only use unique data
        if (typeof bUnique == "undefined") bUnique = true;
        // by default only look at filtered data
        if (typeof bFiltered == "undefined") bFiltered = true;
        // by default ignore empty values
        if (typeof bIgnoreEmpty == "undefined") bIgnoreEmpty = true;

        // list of rows to loop through
        var aiRows;

        // use only filtered rows
        if (bFiltered) aiRows = oSettings.aiDisplay;
        else aiRows = oSettings.aiDisplayMaster;
        // reset facet counts to 0
        for (var key in oFacets[iColumn]) {
            if (oFacets[iColumn].hasOwnProperty(String(key))) oFacets[iColumn][String(key)].count = 0;
        }
        for (var i = 0; i < aiRows.length; i++) {
            iRow = aiRows[i];
            var aData = this.fnGetData(iRow);
            if (aData === null || aData[iColumn] === null) continue;
            var aTemp = aData[iColumn].replace(/(\[|\]|;)/gi, "").split(",").sort();
            if (!oFacets[iColumn]) oFacets[iColumn] = {};
            for (var n = 0; n < aTemp.length; n++) {
                var sTemp = jQuery.trim(aTemp[n]);
                if (!oFacets[iColumn][sTemp]) oFacets[iColumn][sTemp] = {count: 0};
                if (!oFacets[iColumn][sTemp].checked) {
                    oFacets[iColumn][sTemp].checked = false;
                    if (readUrlHash(iColumn)) {
                        if (jQuery.inArray(sTemp, readUrlHash(iColumn).split("|")) != -1) {
                            oFacets[iColumn][sTemp].checked = true;
                        }
                    }
                }
                if (bIgnoreEmpty === true && aTemp[n].length === 0) {
                    continue;
                } else if (bUnique === true && oFacets[iColumn].hasOwnProperty(sTemp)) {
                    ++oFacets[iColumn][sTemp].count;
                    continue;
                }
            }
        }
    }
}(jQuery));

function getSearchParams(form) {
    var formData = {};
    form.find("[data-save-value=true]:input").each(function(){
        var type = this.type || this.tagName.toLowerCase();
        if (type !== 'checkbox' && !formData.hasOwnProperty(String(this.name))) {
            formData[String(this.name)] = this.value;
        } else if (type === 'checkbox' && !formData.hasOwnProperty(String(this.name))) {
            if (this.checked) formData[String(this.name)] = this.value;
        } else if (type === 'checkbox' && formData.hasOwnProperty(String(this.name))) {
            if (this.checked) formData[String(this.name)] = formData[String(this.name)] + "," + this.value;
        }
    });
    return formData;
}

function searchForCourses(id, parentId) {
    var formData = getSearchParams(jQuery("#kualiForm"));
    jQuery(".courseSearch__field, .courseSearch__input, .courseSearch__submit").addClass("disabled").prop("disabled", true);
    var results = jQuery("#" + parentId + " > .uif-horizontalBoxLayout"); // course_search_results_panel
    results.fadeOut("fast");
    jQuery(".courseResults__facet").data("skip-processing", false);
    if (formData.searchTerm === 'any') jQuery("#facet_meeting_times, #facet_meeting_days").data("skip-processing", true);
    var iDisplayLength = 20;
    if (readUrlHash("show")) iDisplayLength = parseFloat(readUrlHash("show"));
    var iDisplayStart = 0;
    if (readUrlHash("start")) iDisplayStart = parseFloat(readUrlHash("start"));
    oFacets = new Object();
    oTable = jQuery("#" + id).dataTable({
        aLengthMenu: [20, 50, 100],
        aaSorting: [],
        aoColumns: [
            {'sTitle': 'Code', 'bSortable': true, 'bSearchable': false, 'sClass': 'courseResults__tableHeader--sortable', 'sWidth': '73px', 'sType': 'string'},
            {'sTitle': 'Course Name', 'bSortable': true, 'bSearchable': false, 'sClass': 'courseResults__tableHeader--sortable', 'sWidth': '188px'},
            {'sTitle': 'Credits', 'bSortable': false, 'bSearchable': false, 'sWidth': '36px'},
            {'sTitle': 'Quarter Offered', 'bSortable': false, 'bSearchable': false, 'sClass': 'termBadge', 'sWidth': '80px'},
            {'sTitle': 'Gen Edu Req', 'bSortable': false, 'bSearchable': false, 'sWidth': '69px'},
            {'sTitle': '', 'bSortable': false, 'bSearchable': false, 'sClass':'courseResults__item--right', 'sWidth': '62px'},
            {'sTitle': 'facet_quarter', 'bVisible': false},
            {'sTitle': 'facet_genedureq', 'bVisible': false},
            {'sTitle': 'facet_credits', 'bVisible': false},
            {'sTitle': 'facet_level', 'bVisible': false},
            {'sTitle': 'facet_curriculum', 'bVisible': false},
            {'sTitle': 'facet_meeting_days', 'bVisible': false},
            {'sTitle': 'facet_meeting_times', 'bVisible': false},
            {'sTitle': 'facet_meeting_day_time', 'bVisible': false, 'bSearchable': false}
        ],
        bAutoWidth: false,
        bDeferRender: true,
        bDestroy: true,
        bJQueryUI: true,
        bScrollCollapse: true,
        bSortClasses: false,
        iDisplayLength: iDisplayLength,
        iDisplayStart: iDisplayStart,
        fnDrawCallback: function (oSettings) {
            if (Math.ceil((oSettings.fnRecordsDisplay()) / oSettings._iDisplayLength) > 1) {
                jQuery(".dataTables_paginate .ui-button").not(".first, .last").show();
            } else {
                jQuery(".dataTables_paginate .ui-button").hide();
            }
            fnSaveState(id, oSettings);
        },
        fnInitComplete: function (oSettings, json) {
            //oTable.fnDraw(); //Clears the iDisplayStart from being initially set.
            for (var key in formData) {
                if (formData.hasOwnProperty(key) && !readUrlHash(key)) {
                    setUrlHash(key, formData[key]);
                }
            }
            results.fadeIn("fast");
            jQuery("#" + parentId).bind('page', function () {
                if (jQuery("#" + parentId).height() > jQuery(window).height()) {
                    var targetOffset = jQuery("#" + parentId).offset().top;
                    jQuery('html,body').animate({scrollTop: targetOffset}, 200);
                }
            });
            jQuery(".courseResults__facet .uif-disclosureContent .uif-verticalBoxLayout").each(function () {
                jQuery(this).empty();
            });
            if (oSettings.fnRecordsDisplay() > 0) {
                jQuery.event.trigger("GENERATE_FACETS");
            }
        },
        fnServerData: function (sSource, aoData, fnCallback) {
            jQuery.ajax({
                dataType: 'json',
                type: "GET",
                url: sSource,
                data: aoData,
                success: fnCallback,
                beforeSend: function () {
                    jQuery("#" + parentId).block({
                        centerX: true,
                        centerY: false,
                        message: '<p><img src="' + getConfigParam("ksapImageLocation") + 'loader/ajax_large.gif" alt="Please wait while we are digging up courses..." /></p><p>Please wait while we are digging up courses...</p>',
                        fadeIn: 0,
                        fadeOut: 0,
                        overlayCSS: {
                            backgroundColor: '#fff',
                            opacity: 0.9,
                            cursor: 'wait',
                            border: 'none'
                        },
                        css: {
                            top: '20px',
                            color: '#c09853',
                            backgroundColor: '#fcf8e3',
                            border: 'solid 1px #fbeed5',
                            borderRadius: '15px',
                            '-webkit-border-radius': '15px',
                            '-moz-border-radius': '15px',
                            width: '230px',
                            textAlign: 'center',
                            padding: '20px'
                        }
                    });
                },
                complete: function () {
                    jQuery("#" + parentId).unblock();
                    jQuery(".courseSearch__field, .courseSearch__input, .courseSearch__submit").removeClass("disabled").prop("disabled", false);
                }
            });
        },
        oLanguage: {
            "sEmptyTable": '<div class="courseResults__empty"><h4>We couldn&#39;t find anything matching your search.</h4><p>A few suggestions:</p><ul><li>Check your spelling</li><li>Try a more general search (Any quarter, ENGL 1xx)</li><li>Use at least 3 characters</li></ul></div>',
            "sInfo": "Showing _START_-_END_ of _TOTAL_ results",
            "sInfoEmpty": "0 results found",
            "sInfoFiltered": "",
            "sLengthMenu": "Show _MENU_",
            "sZeroRecords": "0 results found"
        },
        sAjaxSource: '/student/myplan/course/search?' + jQuery.param(formData).replace(/\+/g, "%20") + '&formKey=' + jQuery("#formKey").val() + '&time=' + new Date().getTime(),
        sDom: "ilrtSp",
        sPaginationType: "full_numbers"
    });
}

function fnGenerateFacetGroup(columnTitle, obj, sorter, formatter) {
    var iColumn = oTable.fnGetColumnIndex(columnTitle);
    if (typeof obj.data("skip-processing") === "undefined" || obj.data("skip-processing") === false) {
        var oTableSettings = oTable.fnSettings();
        if (typeof oTableSettings.aoColumns[iColumn] !== "undefined" && oTableSettings.aoColumns[iColumn].bSearchable) {
            oTable.fnGetColumnData(iColumn, true, false);
            fnCreateFacetList(iColumn, obj, oFacets[iColumn], sorter, formatter);
        }
        if (nonEmpty(obj.find(".uif-footer"))) {
            obj.find(".uif-footer").hide();
        }
    } else {
        if (nonEmpty(obj.find(".uif-footer"))) {
            obj.find(".uif-footer").show();
        }
    }
}

function fnCreateFacetList(i, obj, oData, sorter, formatter) {
    if (typeof oData === "undefined") return false;
    var aSelections = [];
    var oTableSettings = oTable.fnSettings();
    var jFacets = obj.find(".uif-disclosureContent .uif-verticalBoxLayout");
    jFacets.empty();
    if (Object.size(oData) > 1) {
        jFacets.append(jQuery('<div class="courseResults__facetAll"><ul /></div>'));
        var allClass = 'courseResults__facet--checked';
        for (var key in oData) {
            if (oData.hasOwnProperty(key)) {
                if (oData[key].checked === true) {
                    allClass = '';
                    aSelections.push(";" + key + ";");
                }
            }
        }
        var jAll = jQuery('<li />').attr("title", "All").addClass(allClass).html('<a href="#">All</a>').click(function (e) {
            fnFacetFilter('All', i, e,
                oTable.fnGetColumnIndex(obj.data("related-column")),
                oTable.fnGetColumnIndex(obj.data("merged-column")),
                obj.data("save-facet"),
                obj.data("save-facet-property"),
                obj.data("save-facet-key")
            );
        });
        jFacets.find(".courseResults__facetAll ul").append(jAll);
    }
    jFacets.append(jQuery('<div class="courseResults__facetList"><ul /></div>'));
    jQuery(oData).iterateSorted(sorter, function (key) {
        var jItem = jQuery('<li />').data("key", key);
        var jLink = jQuery('<a />').attr("href", "#");
        if (key in oGenEduNames) {
            jLink.attr("title", oGenEduNames[key]);
        }
        if (formatter) {
            jLink.text(formatter(key));
        } else {
            jLink.text(key);
        }
        jItem.append(jLink);
        if (!obj.data("skip-count")) jItem.append(' <span>(' + oData[key].count + ')</span>')
        jItem.click(function (e) {
            fnFacetFilter(key, i, e,
                oTable.fnGetColumnIndex(obj.data("related-column")),
                oTable.fnGetColumnIndex(obj.data("merged-column")),
                obj.data("save-facet"),
                obj.data("save-facet-property"),
                obj.data("save-facet-key")
            );
        });
        if (oData[key].checked) jItem.addClass("courseResults__facet--checked");
        if (Object.size(oData) == 1) jItem.addClass("courseResults__facet--static");
        jFacets.find(".courseResults__facetList ul").append(jItem);
    });
    if (aSelections.length > 0) {
        oTable.fnFilter(aSelections.join("|"), i, true, false);
        if (readUrlHash("start")) {
            oTableSettings._iDisplayStart = parseFloat(readUrlHash("start"));
            oTable.fnDraw(false);
        }
    }
}

function fnUpdateFacetList(columnTitle, obj, n) {
    var i = oTable.fnGetColumnIndex(columnTitle);
    if (typeof oTable.fnSettings().aoColumns[i] !== "undefined") {
        if (i != n && oTable.fnSettings().aoColumns[i].bSearchable) {
            oTable.fnGetColumnData(i);
        }
        // Update the style (checked/not checked) on facet links and the count view
        obj.find(".courseResults__facetList li").each(function () {
            if (oFacets[i][jQuery(this).data("key")].checked) {
                jQuery(this).addClass("courseResults__facet--checked");
            } else {
                jQuery(this).removeClass("courseResults__facet--checked");
            }
            if (!obj.data("skip-count")) jQuery(this).find("span").text("(" + oFacets[i][jQuery(this).find("a").text()].count + ")");
        });
        // Update the style on the 'All' facet option ('checked' if none in the group are selected, 'not checked' if any are selected)
        if (isFiltered(oFacets[i])) {
            obj.find(".courseResults__facetAll li").removeClass("courseResults__facet--checked");
        } else {
            obj.find(".courseResults__facetAll li").addClass("courseResults__facet--checked");
        }
    }
}

function fnFacetFilter(sFilter, i, e, iRelated, iMerged, bSaveFacet, sProperty, sKey) {
    stopEvent(e);
    var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    var facetData = {};
    if (!jQuery(target).is('.courseResults__facet--disabled') && !jQuery(target).is('.courseResults__facet--static')) {
        if (sFilter === 'All') {
            // Set all facets within column to checked false
            for (var key in oFacets[i]) {
                if (oFacets[i].hasOwnProperty(key)) {
                    oFacets[i][key].checked = false;
                }
            }
            // Clear filter
            oTable.fnFilter('', i, true, false);
            setUrlHash(i, '');
            if (bSaveFacet) {
                facetData[sKey] = '';
                setSessionFacets(sProperty, facetData);
            }
            // If facet group is related, clear merged column filter as well
            if (iRelated) {
                oTable.fnFilter('', iMerged, true, false);
                setUrlHash(iMerged, '');
            }
            jQuery.event.trigger("UPDATE_FACETS", -1);
        } else {
            // Update checked status of facet
            oFacets[i][sFilter].checked = !oFacets[i][sFilter].checked;
            // Build array of selected items in facet group
            var aSelections = getSelections(oFacets[i]);
            // Filter results of facet selection
            oTable.fnFilter(aSelections.map(function(value){
                return ";" + value + ";"
            }).join("|"), i, true, false);
            setUrlHash(i, aSelections.join("|"));
            if (bSaveFacet) {
                facetData[sKey] = aSelections.join(",");
                setSessionFacets(sProperty, facetData);
            }
            if (iRelated && isFiltered(oFacets[iRelated])) {
                if (i < iRelated) {
                    aSelections = permutate(aSelections, getSelections(oFacets[iRelated]));
                } else {
                    aSelections = permutate(getSelections(oFacets[iRelated]), aSelections);
                }
                oTable.fnFilter(aSelections.map(function(value){
                    return ";" + value + ";"
                }).join("|"), iMerged, true, false);
                setUrlHash(iMerged, aSelections.join("|"));
            }
            jQuery.event.trigger("UPDATE_FACETS", i);
        }
    }
}

function getSelections(oFacetGroup) {
    var aSelections = [];
    for (var key in oFacetGroup) {
        if (oFacetGroup.hasOwnProperty(key)) {
            if (oFacetGroup[key].checked === true) {
                aSelections.push(key);
            }
        }
    }
    return aSelections;
}

function permutate() {
    var r = [], arg = arguments, max = arg.length-1;
    function helper(arr, i) {
        for (var j=0, l=arg[i].length; j<l; j++) {
            var a = arr.slice(0);
            a.push(arg[i][j]);
            if (i==max) {
                r.push(a.join(''));
            } else
                helper(a, i+1);
        }
    }
    helper([], 0);
    return r;
}

function isFiltered(oFacetGroup) {
    var filtered = false;
    for (var key in oFacetGroup) {
        if (oFacetGroup.hasOwnProperty(String(key))) {
            if (oFacetGroup[key].checked === true) {
                filtered = true;
                break;
            }
        }
    }
    return filtered;
}

function fnSaveState(id, oSettings) {
    jQuery("#" + id + "_length select").change(function () {
        setUrlHash("show", oSettings._iDisplayLength);
    });
    jQuery("#" + id + "_paginate a.ui-button").click(function (e) {
        setUrlHash("start", oSettings._iDisplayStart);
    });
}

function setSessionFacets(property, data) {
    var submitData = {
        "formKey": jQuery("#formKey").val()
    };
    submitData[property] = JSON.stringify(data);

    jQuery.ajax({
        url: "/student/myplan/course/updateFacets",
        data: submitData,
        dataType: "json"
    });
}