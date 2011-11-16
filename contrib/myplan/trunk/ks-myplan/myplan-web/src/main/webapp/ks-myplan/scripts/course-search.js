var arrFacets = [];

jq(document).ready(function() {
    jq("#course_search_fields_span span input[type='text']").blur();
} );

function facetFilter(colIndex, filterText, obj) {
    if ( !arrFacets[colIndex] ) {
    	arrFacets[colIndex] = [];
    }
    var key = jq.inArray(String(filterText), arrFacets[colIndex]);
    if ( key === -1 ) {
    	arrFacets[colIndex].push(String(filterText));
    } else {
    	arrFacets[colIndex].splice(key, 1);
    }
    jq(obj).toggleClass('checked');
    filterDataTable(arrFacets, colIndex);
}

function facetAll(colIndex) {
    arrFacets[colIndex] = [];
    filterDataTable(arrFacets, colIndex);
}

function filterDataTable(arrFacets, colIndex) {
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