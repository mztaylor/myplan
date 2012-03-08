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
Array.prototype.inArrayRegEx = function(v) {
    for ( var i = 0, length = this.length; i < length; i++ ) {
       if ( typeof this[i] === 'string' && this[i].indexOf(v) > -1 ) {
            return i;
       }
    }
    return -1;
};
Object.size = function(obj) {
    var size = 0, key;
    for (key in obj) {
        if (obj.hasOwnProperty(key)) size++;
    }
    return size;
};

function buildFacets() {
    if ( jq.cookie('course_search_facets') ) {
    	applyHistory();
    }
    calculateFacets(null);
	jq(".myplan-facets-panel a.item.all").each(function() {
		var styleClasses = jq(this).attr("class").split(" ");
        var styleIndex = styleClasses.inArrayRegEx("facet");
        var txtColumn = styleClasses[styleIndex].toString();
		if ( typeof objFacets === "undefined" || Object.size(objFacets[txtColumn]) <= 1 ) {
            jq(this).removeClass('checked').parent("span.all").css('display', 'none');
            jq(this).parents(".myplan-facets-group").find("a.item").addClass('static');
        }  else {
            jq(this).parent("span.all").css('display', 'block');
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

function facetFilter(txtColumn, filterText, e) {
    if (!e) var e = window.event;
    e.preventDefault();
    var obj = e.target;
    if ( ( !jq(obj).is('.disabled') && !jq(obj).is('.static') ) || obj === null ){
        oTable = jq("#course_search_results_datatable").dataTable();
	    var colIndex = oTable.fnGetColumnIndex(txtColumn);
        var filterText = filterText.replace('&','&amp;');
        var txtFacet = filterText.substring(1, filterText.length-1);
        if ( filterText === 'All' ) {
            arrFacets[colIndex] = [];
            jq(obj).parents('.myplan-facets-group').find("a.item").not('.all').each(function() {
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
                if ( arrFacets[colIndex].length === 0 ) jq(obj).parents('.myplan-facets-group').find('a.all').addClass('checked');
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
	var oTableFiltered = jq("#course_search_results_datatable").dataTable().fnGetFilteredData();
	jq(".myplan-facets-panel .myplan-facets-group a.item").not(".all").each(function() {
		var styleClasses = jq(this).attr("class").split(" ");
        var styleIndex = styleClasses.inArrayRegEx("facet");
        var txtColumn = styleClasses[styleIndex].toString();
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
			jq(this).parents('.myplan-facets-group').find('a.all').removeClass('checked');
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

jq(window).load(function(){
    if ( jq("#course_search_results_datatable").length > 0 ) {
        jq("#course_search_results_panel_div").fadeIn('fast');
        buildFacets();
    }
    if ( jq("#course_search_results_empty_div").length > 0 ) {
        jq(".myplan-facets-panel .myplan-facets-group").each(function() {
            jq(this).find("a[id$='_toggle'] img").click();
            jq(this).find("div[id^='facet_'][id$='_group']").hide();
        });
        jq("#course_search_results_panel_div").fadeIn('fast');
    }
});