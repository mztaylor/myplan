var arrFacets = [], arrFacetSearch = [], arrFacetCount = [];

jq.fn.dataTableExt.oApi.fnGetColumnIndex = function ( oSettings, sCol ) {
    var cols = oSettings.aoColumns;
    for ( var x=0, xLen=cols.length ; x<xLen ; x++ ) {
        if ( cols[x].sTitle.toLowerCase() == sCol.toLowerCase() ) {
            return x;
        }
    }
    return -1;
}
jq.fn.dataTableExt.oApi.fnGetFilteredData = function ( oSettings ) {
    var a = [];
    for ( var i=0, iLen=oSettings.aiDisplay.length ; i<iLen ; i++ ) {
            a.push(oSettings.aoData[ oSettings.aiDisplay[i] ]._aData);
    }
    return a;
}

function buildFacets() {
    var oTable = jq('#course_search_results_datatable').dataTable();
    jq("#course_search_result_facets_div a.item").not(".all").each(function() {
    	// Create multidimensional array filled with facets provided from server, keys are set to datatable column index
        var colIndex = oTable.fnGetColumnIndex(jq(this).attr('class').split(" ",1)[0]); // Get the column number based on first hardcoded class (the column sTitle) on facet
        if (!arrFacetSearch[colIndex]) arrFacetSearch[colIndex] = []; // If key is undefined, create it
        arrFacetSearch[colIndex].push(jq(this).text().trim()); // Insert facet text to array
	});
    jq("#course_search_result_facets_div a.item.all").each(function() {
        var colIndex = oTable.fnGetColumnIndex(jq(this).attr('class').split(" ",1)[0]); // Get the column number based on first hardcoded class (the column sTitle) on facet
        if ( typeof arrFacetSearch[colIndex] === 'undefined' || arrFacetSearch[colIndex].length <= 1 ) {
            jq(this).removeClass('checked').hide();
            jq(this).parents(".facets").find("a.item").addClass('static');
        }
    });
    calculateFacets(null);
}

function facetFilter(colName, filterText, obj) {
    if ( !jq(obj).is('.disabled') && !jq(obj).is('.static') ) {
        oTable = jq("#course_search_results_datatable").dataTable();
	    var colIndex = oTable.fnGetColumnIndex(colName);
        var filterText = filterText.replace('&','&amp;');

        if ( filterText === 'All' ) {
            arrFacets[colIndex] = [];
            jq(obj).parents('.facets').find("div[id$='_group'] a.item").each(function() {
                jq(this).removeClass('checked');
            });
            jq(obj).addClass('checked');
        } else {
            if ( !arrFacets[colIndex] ) {
                arrFacets[colIndex] = [];
            }
            var key = jq.inArray(String(filterText), arrFacets[colIndex]);
            if ( key === -1 ) {
                arrFacets[colIndex].push(String(filterText));
                jq(obj).addClass('checked');
                jq(obj).parents('.facets').find('a.all').removeClass('checked');
            } else {
                arrFacets[colIndex].splice(key, 1);
                jq(obj).removeClass('checked');
                if ( arrFacets[colIndex].length === 0 ) {
                   jq(obj).parents('.facets').find('a.all').addClass('checked');
                }
            }
        }
        filterDataTable(colIndex, filterText);
    }
}

function filterDataTable(colIndex, filterText) {
    oTable = jq("#course_search_results_datatable").dataTable();
    oTable.fnFilter('', colIndex, true, false);
    var queryString;
    if ( arrFacets[colIndex] && arrFacets[colIndex].length > 0 ) {
        for ( var i = 0; i < arrFacets[colIndex].length; i++ ) {
            if ( i === 0 ) {
                queryString = arrFacets[colIndex][i];
            } else {
                queryString = queryString + '|' + arrFacets[colIndex][i];
            }
        }
        oTable.fnFilter(queryString, colIndex, true, false);
    }
    if ( filterText === "All" ) {
    	calculateFacets(null);
    } else {
    	calculateFacets(colIndex);
    }
}

function calculateFacets(colIndex) {
	var oTable = jq("#course_search_results_datatable").dataTable().fnGetFilteredData();
	// Reset facet counts on all columns except the one selected
	for ( x = 0; x < arrFacetCount.length; x++ ) {
		if (x != colIndex) arrFacetCount[x] = [];
	}
    // Loop through the full data set from datatable (rows)
	for ( i = 0; i < oTable.length; i++ ) {
		// Loop through the data from each row (columns)
        for ( n = 0; n < oTable[i].length; n++ ) {
			// Ignore recalculating the facet group selected from
            if (n != colIndex) {
				// Check for a facet group (column number) to search the data set with
                if ( arrFacetSearch[n] ) {
					// Loop through the facet group values (provided from server side)
                    for ( c = 0; c < arrFacetSearch[n].length; c++ ) {
						// Check for an array to store facet group count values, if none, create it
                        if ( !arrFacetCount[n] ) arrFacetCount[n] = [];
                        // Check for array variable with facet text as key, if none, create it and set to inital count value to 0
						if ( !arrFacetCount[n][arrFacetSearch[n][c]] ) arrFacetCount[n][arrFacetSearch[n][c]] = 0;
						// Search for facet text in datatable column values, if found, increment array variable value
                        if ( oTable[i][n].search(';'+arrFacetSearch[n][c].replace("&", "&amp;")+';') >= 0 ) arrFacetCount[n][arrFacetSearch[n][c]]++;

					}
				}
			}
		}
	}

    var oTable = jq("#course_search_results_datatable").dataTable();

	jq("#course_search_result_facets_div a.item").not('.all').each(function() {
        var colIndex = oTable.fnGetColumnIndex(jq(this).attr('class').split(" ",1)[0]);
        var txtFacet = jq(this).text().split(" (",1);
        if ( typeof arrFacetCount[colIndex][txtFacet] != 'undefined' && arrFacetCount[colIndex][txtFacet] != 0 ) {
            jq(this).html(txtFacet + ' <span>(' + arrFacetCount[colIndex][txtFacet] + ')</span>').removeClass('disabled');
        } else {
            jq(this).html(txtFacet + ' <span>(0)</span>').addClass('disabled');
        }
	});
}

jq(document).ready(function() {
    jq("input[type='text']").blur();
    jq(".search-text input[type='text']").bind('keypress', function(e) {
	    if ( e.keyCode === 13 ) jq("button.search-submit").click();
	});
} );

jq(window).load(function(){
    if ( jq("#course_search_results_datatable").length > 0 ) {
        buildFacets();
    }
});