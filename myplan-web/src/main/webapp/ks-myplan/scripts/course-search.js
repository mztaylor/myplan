/* Course Search */
jq(document).ready(function() {
    jq("#course_search_fields_span span input[type='text']").blur();
} );


function facetFilter(colIndex, filterText ) {

     oTable = jq('#course_search_results_datatable').dataTable();

    /* Filter immediately */
    oTable.fnFilter(filterText, colIndex );
}

