var arrFacets = [], arrHistory = [];
var objFacets = {}, objCookie = {};
var oTable;

jq.fn.dataTableExt.oApi.fnGetColumnIndex = function ( oSettings, sCol ) {
    var cols = oSettings.aoColumns;
    for ( var x=0, xLen=cols.length ; x<xLen ; x++ ) {
        if ( cols[x].sTitle.toLowerCase() == sCol.toLowerCase() ) return x;
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
    if ( jq.cookie('course_search_facets') ) {
    	applyHistory();
    }
    calculateFacets(null);
	jq("#course_search_result_facets_div a.item.all").each(function() {
		var txtColumn = jq(this).attr('class').split(' ',1)[0].toString();
		if ( typeof objFacets[txtColumn] === 'undefined' || objFacets[txtColumn].length <= 1 ) {
            jq(this).removeClass('checked').css('display', 'none');
            jq(this).parents(".facets").find("a.item").addClass('static');
        }  else {
            jq(this).css('display', 'block');
        }
	});
}

function applyHistory() {
	arrHistory = eval('(' + jq.cookie('course_search_facets') + ')');
	objCookie = eval('(' + unescape(jq.cookie('SpryMedia_DataTables_course_search_results_datatable_course')) + ')');
	oTable = jq("#course_search_results_datatable").dataTable();
	for ( var i = 0; i < arrHistory.length; i++ ) {
		var txtColumn = arrHistory[i][0];
		var filterText = arrHistory[i][1];
		var txtFacet = filterText.substring(1, filterText.length-1);
		var colIndex = oTable.fnGetColumnIndex(txtColumn);
		if ( !arrFacets[colIndex] ) arrFacets[colIndex] = [];
		if ( !objFacets[txtColumn] ) objFacets[txtColumn] = {};
		if ( !objFacets[txtColumn][txtFacet] ) objFacets[txtColumn][txtFacet] = {"checked":false};
		if ( filterText === 'All' ) {
			arrFacets[colIndex] = [];
		} else {
			var key = jq.inArray(String(filterText), arrFacets[colIndex]);
			if ( key === -1 ) {
				arrFacets[colIndex].push(String(filterText));
				objFacets[txtColumn][txtFacet]["checked"] = true;
			} else {
				arrFacets[colIndex].splice(key, 1);
				objFacets[txtColumn][txtFacet]["checked"] = false;
			}
		}
		oTable.fnFilter( '', colIndex, true, false );
		if ( arrFacets[colIndex] && arrFacets[colIndex].length > 0 ) {
			oTable.fnFilter( arrFacets[colIndex].join('|'), colIndex, true, false );
		}
	}
	var oSettings = oTable.fnSettings();
    oSettings._iDisplayStart = objCookie["iStart"];
    oTable.fnDraw(false);
}

function facetFilter(txtColumn, filterText, obj) {
    if ( ( !jq(obj).is('.disabled') && !jq(obj).is('.static') ) || obj === null ){
        oTable = jq("#course_search_results_datatable").dataTable();
	    var colIndex = oTable.fnGetColumnIndex(txtColumn);
        var filterText = filterText.replace('&','&amp;');
        var txtFacet = filterText.substring(1, filterText.length-1);
        if ( filterText === 'All' ) {
            arrFacets[colIndex] = [];
            jq(obj).parents('.facets').find("div[id$='_group'] a.item").not('.all').each(function() {
                objFacets[txtColumn][jq(this).text().replace("&", "&amp;").split(" (",1).toString()]["checked"] = false;
            });
            jq(obj).addClass('checked');
            for ( var i = 0; i < arrHistory.length; i++ ) {
				if ( arrHistory[i][0] === txtColumn ) {
					arrHistory.splice(i, 1);
					i--;
				}
			}
        } else {
            if ( !arrFacets[colIndex] ) arrFacets[colIndex] = [];
            var key = jq.inArray(String(filterText), arrFacets[colIndex]);
            if ( key === -1 ) {
                arrFacets[colIndex].push(String(filterText));
                objFacets[txtColumn][txtFacet]["checked"] = true;
                var temp = [txtColumn, filterText];
				arrHistory.push(temp);
            } else {
                arrFacets[colIndex].splice(key, 1);
                objFacets[txtColumn][txtFacet]["checked"] = false;
                if ( arrFacets[colIndex].length === 0 ) jq(obj).parents('.facets').find('a.all').addClass('checked');
                for ( var i = 0; i < arrHistory.length; i++ ) {
                	if ( arrHistory[i][0] === txtColumn && arrHistory[i][1] ===  filterText ) arrHistory.splice(i, 1);
                }
            }
        }
		jq.cookie( 'course_search_facets', JSON.stringify(arrHistory), { expires: null, path: '/' } );
		oTable.fnFilter( '', colIndex, true, false );
		if ( arrFacets[colIndex] && arrFacets[colIndex].length > 0 ) {
			oTable.fnFilter( arrFacets[colIndex].join('|'), colIndex, true, false );
		}
		if ( filterText === "All" ) {
			calculateFacets(null);
		} else {
			calculateFacets(colIndex);
		}
	}
}

function calculateFacets(colSelected) {
	oTable = jq("#course_search_results_datatable").dataTable();
	oTableFiltered = jq("#course_search_results_datatable").dataTable().fnGetFilteredData();
	jq("#course_search_result_facets_div .facets a.item").not(".all").each(function() {
		var txtColumn = jq(this).attr('class').split(' ',1)[0].toString();
		var txtFacet = jq(this).text().replace("&", "&amp;").split(" (",1).toString();
		var colIndex = oTable.fnGetColumnIndex(txtColumn);
		if ( colIndex != colSelected ) {
			var count = 0;
			for ( var i = 0; i < oTableFiltered.length; i++ ) {
				if ( oTableFiltered[i][colIndex].search(';'+txtFacet+';') >= 0 ) ++count;
			}
		} else {
			var count = objFacets[txtColumn][txtFacet]["count"];
		}
		if ( !objFacets[txtColumn] ) objFacets[txtColumn] = {};
		if ( !objFacets[txtColumn][txtFacet] ) objFacets[txtColumn][txtFacet] = {"checked":false};
		objFacets[txtColumn][txtFacet]["count"] = count;
		if ( objFacets[txtColumn][txtFacet]["checked"] ) {
			jq(this).addClass('checked');
			jq(this).parents('.facets').find('a.all').removeClass('checked');
		} else {
			jq(this).removeClass('checked');
			// if none in the facet group are checked, add checked class to 'all' -----------------
		}
		if ( count != 0 ) {
			jq(this).html(txtFacet + ' <span>(' + count + ')</span>').removeClass('disabled');
		} else {
			jq(this).html(txtFacet + ' <span>(0)</span>').addClass('disabled');
		}
	});
}

jq(document).ready(function() {
    jq("input[type='text']").blur();
    jq("button.search-submit").click(function(){
    	jq.cookie( 'course_search_facets', null, { expires: -1, path: '/' } );
    });
    jq(".search-text input[type='text']").bind('keypress', function(e) {
	    if ( e.keyCode === 13 ) jq("button.search-submit").click();
	});
});

jq(window).load(function(){
    if ( jq("#course_search_results_datatable").length > 0 ) {
        oTable = jq("#course_search_results_datatable").dataTable();
        jq("#course_search_results_panel_div").fadeIn('fast');
        buildFacets();
    }
    if ( jq("#course_search_no_results_found_div").length > 0 ) {
        jq("#course_search_result_facets_div .facets").each(function() {
            jq(this).find("span.all").hide();
            jq(this).find("a[id$='_toggle'] img").click();
        });
        jq("#course_search_no_results_found_div").fadeIn('fast');
    }
});


