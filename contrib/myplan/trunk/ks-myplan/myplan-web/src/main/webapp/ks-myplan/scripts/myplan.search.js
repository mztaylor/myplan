var oTable;
var oFacets = new Object();

var oProjectedTermOrder = {
    "AU":1,
    "WI":2,
    "SP":3,
    "SU":4
};

var oScheduledTermOrder = {
    "WI":1,
    "SP":2,
    "SU":3,
    "AU":4
};


Object.size = function(obj) {
    var size = 0, key;
    for (key in obj) {
        if (obj.hasOwnProperty(key)) size++;
    }
    return size;
};

function numeric(a, b){
    if ( parseInt(a) > parseInt(b) ) return 1;
    else if ( parseInt(a) < parseInt(b) ) return -1;
    else return 0;
}

function alpha(a, b){
    //  Unknown is always last.
    if (a == 'Unknown' || a == 'None') return 1;
    if (b == 'Unknown' || b == 'None') return -1;
    if ( a > b ) return 1;
    else if ( a < b ) return -1;
    else return 0;
}

function terms(a, b){
    //  Unknown is always last.
    if (a == 'Unknown') return 1;
    if (b == 'Unknown') return -1;

    //  If the facet items that end with a year are scheduled terms and should precede terms.
    var bYearA = a.match(/.*\d{2}$/gi);
    var bYearB = b.match(/.*\d{2}$/gi);

    //  Two scheduled terms.
    if (bYearA && bYearB) {
        var sTermA = a.replace(/\d{2}/gi, "").replace(" ","").toUpperCase();
        var sTermB = b.replace(/\d{2}/gi, "").replace(" ","").toUpperCase();
        var iYearA = parseInt(a.replace(/\D*/gi, ""));
        var iYearB = parseInt(b.replace(/\D*/gi, ""));

        if (iYearA != iYearB) {
            if(iYearA < iYearB) return -1;
            return 1;
        } else {
            if(oScheduledTermOrder[sTermA] < oScheduledTermOrder[sTermB]) return -1;
            else if(oScheduledTermOrder[sTermA] > oScheduledTermOrder[sTermB]) return 1;
            else return 0;
        }
    }

    if (bYearA && !bYearB) return -1;
    if (!bYearA && bYearB) return 1;

    //  Two terms.
    if (!bYearA && !bYearB) {
        var sTermA = a.replace("Projected ", "").toUpperCase();
        var sTermB = b.replace("Projected ", "").toUpperCase();
        if(oProjectedTermOrder[sTermA] < oProjectedTermOrder[sTermB]) return -1;
        else if(oProjectedTermOrder[sTermA] > oProjectedTermOrder[sTermB]) return 1;
        else return 0;
    }
}

jQuery.fn.iterateSorted = function(sorter, print) {
    var keys = [];
    jq.each(this[0], function(key) {
        keys.push(key);
    });
    keys.sort(sorter);
    for (var i = 0; i < keys.length; i++) {
        print(keys[i]);
    }
};

(function($) {
    $.fn.dataTableExt.oApi.fnGetColumnData = function(oSettings, iColumn, bUnique, bFiltered, bIgnoreEmpty) {
        // check for column id
        if (typeof iColumn == "undefined") return new Array();
        // by default only use unique data
        if (typeof bUnique == "undefined") bUnique = true;
        // by default only look at filtered data
        if (typeof bFiltered == "undefined") bFiltered = true;
        // by default ignore empty values
        if (typeof bIgnoreEmpty == "undefined") bIgnoreEmpty = true;
        // list of rows which we're going to loop through
        var aiRows;

        // use only filtered rows
        if (bFiltered) aiRows = oSettings.aiDisplay;
        else aiRows = oSettings.aiDisplayMaster;
        for (var key in oFacets[iColumn]) {
            if (oFacets[iColumn].hasOwnProperty( String(key) )) oFacets[iColumn][ String(key) ].count = 0;
        }
        for (var i = 0, c = aiRows.length; i < c; i++) {
            iRow = aiRows[i];
            var aData = this.fnGetData(iRow);
            var aTemp = aData[iColumn].replace(/(\[|\]|;)/gi,"").split(",").sort();
            if (!oFacets[iColumn]) oFacets[iColumn] = {};
            for (var n = 0; n < aTemp.length; n++) {
                var sTemp = jq.trim( aTemp[n] );
                if (!oFacets[iColumn][sTemp]) oFacets[iColumn][sTemp] = {count: 0};
                if (!oFacets[iColumn][sTemp].checked) oFacets[iColumn][sTemp].checked = false;
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

function searchForCourses(id, parentId) {
    var results = jq("#" + parentId); // course_search_results_panel
    results.fadeOut("fast");
    var sQuery = jq("input[name='searchQuery']").val();
    var sTerm = jq("select[name='searchTerm'] option:selected").val();
    var aCampus = new Array();
    jq.each( jq("input[name='campusSelect']:checked"), function() {
        aCampus.push( jq(this).val() );
    });
    oFacets = new Object();
    oTable = jq("#" + id).dataTable({
        aLengthMenu: [20,50,100],
        aaSorting : [],
        aoColumns: [
            {'sTitle':'Code', 'bSortable':true, 'bSearchable':false, 'sClass':'fl-text-bold myplan-text-nowrap', 'sWidth':'72px', 'sType': 'string'},
            {'sTitle':'Course Name', 'bSortable':true, 'bSearchable':false, 'sWidth':'171px', 'sType': 'string'},
            {'sTitle':'Credit', 'bSortable':false, 'bSearchable':false, 'sWidth':'34px'},
            {'sTitle':'Quarter Offered', 'bSortable':false, 'bSearchable':false, 'sClass':'myplan-data-list', 'sWidth':'119px'},
            {'sTitle':'Gen Edu Req', 'bSortable':false, 'bSearchable':false, 'sWidth':'74px'},
            {'sTitle':'Bookmark', 'bSortable':false, 'bSearchable':false, 'sClass':'fl-text-align-center myplan-course-search-results-status', 'sWidth':'72px'},
            {'bVisible':false},
            {'bVisible':false},
            {'bVisible':false},
            {'bVisible':false},
            {'bVisible':false}
        ],
        bAutoWidth: false,
        bDeferRender: true,
        bDestroy: true,
        bJQueryUI: true,
        bScrollCollapse: true,
        bSortClasses: false,
        bStateSave: false,
        iCookieDuration: 600,
        iDisplayLength: 20,
        fnDrawCallback: function() {
            if ( Math.ceil((this.fnSettings().fnRecordsDisplay()) / this.fnSettings()._iDisplayLength ) > 1)  {
                jq(".dataTables_paginate .ui-button").not(".first, .last").show();
            } else {
                jq(".dataTables_paginate .ui-button").hide();
            }
            if ( this.fnSettings()._iDisplayStart != 0 && jq("#" + parentId).height() > jq(window).height() ) {
                var targetOffset = jq("#" + parentId).offset().top;
                jq('html,body').animate({scrollTop: targetOffset}, 250);
            }
        },
        fnInitComplete: function(oSettings, json) {
            oTable.fnDraw();
            results.fadeIn("fast");
            results.find("table#" + id).width(578);
            jq.publish("GENERATE_FACETS");
        },
        oLanguage: {
            "sInfo":"Showing _START_-_END_ of _TOTAL_ results",
            "sLengthMenu":"Show _MENU_",
            "sEmptyTable":'<div class="myplan-course-search-empty"><p class="fl-font-size-130">We couldn&#39;t find anything matching your search.</p><p>A few suggestions:</p><ul><li>Check your spelling</li><li>Try a more general search (Any quarter, ENGL 1xx)</li><li>Use at least 2 characters</li></ul></div>',
            "sZeroRecords":"0 results found",
            "sInfoEmpty": "0 results found",
            "sInfoFiltered":""
        },
        sAjaxSource: '/student/myplan/course/search?queryText='+sQuery+'&termParam='+sTerm+'&campusParam='+aCampus,
        sCookiePrefix: "myplan_",
        sDom: "ilrtSp",
        sPaginationType: "full_numbers"
    });
}

function fnGenerateFacetGroup(iColumn, obj, sorter) {
    if (oTable.fnSettings().aoColumns[iColumn].bSearchable) {
        oTable.fnGetColumnData(iColumn);
        fnCreateFacetList(oFacets[iColumn], iColumn, obj, sorter);
    }
}

function fnCreateFacetList(oData, i, obj, sorter) {
    var jFacets = obj.find(".uif-disclosureContent .uif-boxLayout");
    jFacets.html("");
    if(Object.size(oData) > 1) {
        jFacets.append( jq('<div class="all"><ul /></div>') );
        var jAll = jq('<li />').attr("title", "All").addClass("all checked").html('<a href="#">All</a>').click(function(e) {
            fnFacetFilter('All', i, e);
        });
        jFacets.find(".all ul").append(jAll);
    }
    jFacets.append( jq('<div class="facets"><ul /></div>') );
    jq(oData).iterateSorted(sorter, function(key) {
        var jItem = jq('<li />').attr("title", key).html('<a href="#">' + key + '</a><span>(' + oData[key].count + ')</span>').click(function(e) {
            fnFacetFilter(key, i, e);
        });
        if(Object.size(oData) == 1) jItem.addClass("static");
        jFacets.find(".facets ul").append(jItem);
    });
}

function fnUpdateFacetList(n, i, obj) {
    if (i != n && oTable.fnSettings().aoColumns[i].bSearchable) {
        oTable.fnGetColumnData(i);
    }
    // Update the style (checked/not checked) on facet links and the count view
    obj.find("li").not(".all").each(function() {
        if (oFacets[i][jq(this).find("a").text()].checked) {
            jq(this).addClass("checked");
        } else {
            jq(this).removeClass("checked");
        }
        jq(this).find("span").text("(" + oFacets[i][jq(this).find("a").text()].count + ")");
    });
    // Update the style on the 'All' facet option (checked if none in the group are selected, not checked if any are selected)
    var bAll = true;
    for (var key in oFacets[i]) {
        if (oFacets[i].hasOwnProperty(key)) {
            if (oFacets[i][key].checked === true) {
                bAll = false;
            }
        }
    }
    if (bAll) {
        obj.find("ul li.all").addClass("checked");
    } else {
        obj.find("ul li.all").removeClass("checked");
    }
}

function fnFacetFilter(sFilter, i, e) {
    stopEvent(e);
    var target = (e.currentTarget) ? e.currentTarget : e.srcElement;
    if ( !jq(target).is('.disabled') && !jq(target).is('.static') ) {
        //var oCookie = eval('(' + unescape(jq.cookie('myplan_course_search_results_course')) + ')');
        if (sFilter === 'All') {
            // Set all facets within column to checked false
            for (var key in oFacets[i]) {
                if (oFacets[i].hasOwnProperty(key)) {
                    oFacets[i][key].checked = false;
                }
            }
            // Clear filter
            oTable.fnFilter('', i, true, false);
            //oCookie.aaSearchCols[i][0] = '';
            jq.publish("UPDATE_FACETS", [-1]);
        } else {
            // Update checked status of facet
            oFacets[i][sFilter].checked = !oFacets[i][sFilter].checked;
            // Build filter regex query
            var aSelections = [];
            for (var key in oFacets[i]) {
                if (oFacets[i].hasOwnProperty(key)) {
                    if (oFacets[i][key].checked === true) {
                        aSelections.push(";"+key+";");
                    }
                }
            }
            // Filter results of facet selection
            oTable.fnFilter(aSelections.join("|"), i, true, false);
            //oCookie.aaSearchCols[i][0] = aSelections.join("|");
            jq.publish("UPDATE_FACETS", [i]);
        }
    }
}