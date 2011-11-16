var arrFacets = [];

jq(document).ready(function() {
    jq("#course_search_fields_span span input[type='text']").blur();
} );

function facetFilter(colIndex, filterText) {

    if ( !arrFacets[colIndex] ) {
    	arrFacets[colIndex] = [];
    }
    var key = jq.inArray(String(filterText), arrFacets[colIndex]);
    if ( key === -1 ) {
    	arrFacets[colIndex].push(String(filterText));
    } else {
    	arrFacets[colIndex].splice(key, 1);
    }

    filterDataTable(arrFacets);
}

function filterDataTable(arrFacets) {

    oTable = jq('#course_search_results_datatable').dataTable();

	for ( var i = 0; i < arrFacets.length; i++ ) {

        oTable.fnFilter('', i, true, false);

        var queryString;

		if ( arrFacets[i] && arrFacets[i].length > 0 ) {

            for ( var n = 0; n < arrFacets[i].length; n++ ) {

				if ( n === 0 ) {
					queryString = arrFacets[i][n];
				} else {
					queryString = queryString + '|' + arrFacets[i][n];
				}
			}
            oTable.fnFilter(queryString, i, true, false);
		}
	}
}

