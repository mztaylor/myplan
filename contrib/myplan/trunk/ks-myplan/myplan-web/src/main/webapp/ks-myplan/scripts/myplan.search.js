oFacets = new Object();

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
        if (bFiltered == true) aiRows = oSettings.aiDisplay;
        else aiRows = oSettings.aiDisplayMaster;
        for (var key in oFacets[iColumn]) {
            if (oFacets[iColumn].hasOwnProperty(key)) oFacets[iColumn][key].count = 0;
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
    var oTable = jq("#" + id).dataTable({
        bDestroy: true,
        bAutoWidth: false,
        bDeferRender: true,
        bSortClasses: false,
        bScrollCollapse: true,
        oLanguage: {
            "sInfo":"Showing _START_-_END_ of _TOTAL_ results",
            "sLengthMenu":"Show _MENU_",
            "sEmptyTable":'<div class="myplan-course-search-empty"><p class="fl-font-size-130">We couldn&#39;t find anything matching your search.</p><p>A few suggestions:</p><ul><li>Check your spelling</li><li>Try a more general search (Any quarter, ENGL 1xx)</li><li>Use at least 2 characters</li></ul></div>',
            "sZeroRecords":"0 results found",
            "sInfoEmpty": "0 results found",
            "sInfoFiltered":""
        },
        iDisplayLength: 20,
        aLengthMenu: [20,50,100],
        sDom: "ilrtSp",
        sPaginationType: "full_numbers",
        aaSorting : [],
        aoColumns: [
            {'sTitle':'Code', 'bSortable':true, 'bSearchable':false, 'sClass':'fl-text-bold myplan-text-nowrap', 'sWidth':'69px'},
            {'sTitle':'Course Name', 'bSortable':true, 'bSearchable':false, 'sWidth':'171px'},
            {'sTitle':'Credit', 'bSortable':false, 'bSearchable':false, 'sWidth':'34px'},
            {'sTitle':'Quarter Offered', 'bSortable':false, 'bSearchable':false, 'sClass':'myplan-data-list', 'sWidth':'122px'},
            {'sTitle':'Gen Edu Req', 'bSortable':false, 'bSearchable':false, 'sWidth':'74px'},
            {'sTitle':'Bookmark', 'bSortable':false, 'bSearchable':false, 'sClass':'fl-text-align-center', 'sWidth':'72px'},
            {'sTitle':'facetQuarter', 'bVisible':false},
            {'sTitle':'facetGenEduReq', 'bVisible':false},
            {'sTitle':'facetCredits', 'bVisible':false},
            {'sTitle':'facetLevel', 'bVisible':false},
            {'sTitle':'facetCurriculum', 'bVisible':false}
        ],
        sAjaxSource: '/student/search/course/'+sQuery+'/'+sTerm+'/'+aCampus,
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
            jq.each(oTable.fnSettings().aoColumns, function(i) {
                if (oTable.fnSettings().aoColumns[i].bSearchable) {
                    oTable.fnGetColumnData(i);
                    jq(".myplan-facets-group." + oTable.fnSettings().aoColumns[i].sTitle + " .uif-disclosureContent .uif-boxLayout").html(fnCreateFacets(oFacets[i]));
                }
            });
            console.log(oFacets);
        }
    });
}

function fnCreateFacets(oData) {
    var jFacetSet = jq('<ul />').append('<li class="all"><a href="#">All</a></li>');
    for (var key in oData) {
        if (oData.hasOwnProperty(key)) {
            var jItem = jq('<li />');
            var jLink = jq('<a href="#">' + key + '</a>');
            if (oData[key].checked) jLink.addClass("checked");
            var jCount = jq('<span>(' + oData[key].count + ')</span>');
            jFacetSet.append(jItem.append(jLink).append(jCount));
        }
    }
    return jFacetSet.prop('outerHTML');
}