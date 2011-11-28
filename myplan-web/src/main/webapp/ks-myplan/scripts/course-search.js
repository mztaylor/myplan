var arrFacets = [];
var arrFacetSearch = [];

jq.fn.dataTableExt.oApi.fnGetColumnIndex = function ( oSettings, sCol ) {
    var cols = oSettings.aoColumns;
    for ( var x=0, xLen=cols.length ; x<xLen ; x++ ) {
        if ( cols[x].sTitle.toLowerCase() == sCol.toLowerCase() ) {
            return x;
        }
    }
    return -1;
}

jq(document).ready(function() {
    jq("#course_search_fields_span span input[type='text']").blur();

    jq('#course_search_result_facets_div a.item').each(function() {
    	if (jq(this).text() != 'All') {
    		if (!arrFacetSearch[jq(this).attr('rel')]) arrFacetSearch[jq(this).attr('rel')] = [];
    		arrFacetSearch[jq(this).attr('rel')].push(jq(this).text());
    	}
	});
} );

function facetFilter(colName, filterText, obj) {
	oTable = jq('#course_search_results_datatable').dataTable();
	var colIndex = oTable.fnGetColumnIndex(colName);

    if (filterText === 'All') {
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

function filterDataTable(colIndex, filterText) {
    oTable = jq('#course_search_results_datatable').dataTable();
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
}