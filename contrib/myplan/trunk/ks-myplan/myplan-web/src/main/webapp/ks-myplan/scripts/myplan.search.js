var oTable;
oFacets = new Object();

Array.prototype.alphanumSort = function(caseInsensitive) {
  for (var z = 0, t; t = this[z]; z++) {
    this[z] = [];
    var x = 0, y = -1, n = 0, i, j;

    while (i = (j = t.charAt(x++)).charCodeAt(0)) {
      var m = (i == 46 || (i >=48 && i <= 57));
      if (m !== n) {
        this[z][++y] = "";
        n = m;
      }
      this[z][y] += j;
    }
  }

  this.sort(function(a, b) {
    for (var x = 0, aa, bb; (aa = a[x]) && (bb = b[x]); x++) {
      if (caseInsensitive) {
        aa = aa.toLowerCase();
        bb = bb.toLowerCase();
      }
      if (aa !== bb) {
        var c = Number(aa), d = Number(bb);
        if (c == aa && d == bb) {
          return c - d;
        } else return (aa > bb) ? 1 : -1;
      }
    }
    return a.length - b.length;
  });

  for (var z = 0; z < this.length; z++)
    this[z] = this[z].join("");
};

Object.size = function(obj) {
    var size = 0, key;
    for (key in obj) {
        if (obj.hasOwnProperty(key)) size++;
    }
    return size;
};

jQuery.fn.iterateSorted = function(print) {
    var keys = [];
    jq.each(this[0], function(key) {
        keys.push(key);
    });
    keys.alphanumSort(true);
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
            {'sTitle':'Code', 'bSortable':true, 'bSearchable':false, 'sClass':'fl-text-bold myplan-text-nowrap', 'sWidth':'69px', 'sSortDataType': 'dom-text'},
            {'sTitle':'Course Name', 'bSortable':true, 'bSearchable':false, 'sWidth':'171px'},
            {'sTitle':'Credit', 'bSortable':false, 'bSearchable':false, 'sWidth':'34px'},
            {'sTitle':'Quarter Offered', 'bSortable':false, 'bSearchable':false, 'sClass':'myplan-data-list', 'sWidth':'122px'},
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
        bScrollCollapse: true,
        bSortClasses: false,
        bStateSave: true,
        iCookieDuration: 600,
        iDisplayLength: 20,
        fnDrawCallback: function() {
            if (Math.ceil((this.fnSettings().fnRecordsDisplay()) / this.fnSettings()._iDisplayLength) > 1)  {
                jq(".dataTables_paginate span").not(".first, .last").show();
            } else {
                jq(".dataTables_paginate span").hide();
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

function fnGenerateFacetGroup(iColumn, obj) {
    if (oTable.fnSettings().aoColumns[iColumn].bSearchable) {
        oTable.fnGetColumnData(iColumn);
        fnCreateFacetList(oFacets[iColumn], iColumn, obj);
    }
}

function fnCreateFacetList(oData, i, obj) {
    var jFacets = obj.find(".uif-disclosureContent .uif-boxLayout");
    if(Object.size(oData) > 1) {
        jFacets.append( jq('<div class="all"><ul /></div>') );
        var jAll = jq('<li />').attr("title", "All").addClass("all checked").html('<a href="#">All</a>').click(function(e) {
            fnFacetFilter('All', i, e);
        });
        jFacets.find(".all ul").append(jAll);
    }
    jFacets.append( jq('<div class="facets"><ul /></div>') );
    jq(oData).iterateSorted(function(key) {
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