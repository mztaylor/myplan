

function readUrlHash(key) {
    if (window.location.href.split("#")[1]) {
        var aHash = window.location.href.split("#")[1].replace('#', '').split('&');
        var oHash = {};
        jQuery.each(aHash, function (index, value) {
            oHash[value.split('=')[0]] = value.split('=')[1];
        });
        if (oHash[key]) {
            if (decodeURIComponent(oHash[key]) == "true" || decodeURIComponent(oHash[key]) == "false") {
                return (decodeURIComponent(oHash[key]) == "true");
            } else {
                return decodeURIComponent(oHash[key]);
            }
        } else {
            return false;
        }
    } else {
        return false;
    }
}

function setUrlHash(key, value) {
    var aHash = [];
    if (window.location.href.split("#")[1]) {
        aHash = window.location.href.split("#")[1].replace('#', '').split('&');
    }
    var oHash = {};
    if (aHash.length > 0) {
        jQuery.each(aHash, function (index, value) {
            oHash[decodeURIComponent(value.split('=')[0])] = decodeURIComponent(value.split('=')[1]);
        });
        var oTemp = {};
        oTemp[key] = value;
        jQuery.extend(oHash, oTemp);
    } else {
        oHash[key] = value;
    }
    aHash = [];
    for (var key in oHash) {
        if (key !== "" && oHash[key] !== "") aHash.push(encodeURIComponent(key) + "=" + encodeURIComponent(oHash[key]));
    }
    window.location.replace("#" + aHash.join("&"));
}

function readUrlParam(key) {
    var aParams = window.location.search.replace('?', '').split('&');
    var oParams = {};
    jQuery.each(aParams, function (index, value) {
        oParams[value.split('=')[0]] = value.split('=')[1];
    });
    if (oParams[key]) {
        return decodeURIComponent(oParams[key]);
    } else {
        return false;
    }
}

function setUrlParam(key, value) {
    var aParams = [];
    if (window.location.search) {
        aParams = window.location.search.replace('?', '').split('&');
    }
    var oParams = {};
    if (aParams.length > 0) {
        jQuery.each(aParams, function (index, value) {
            oParams[value.split('=')[0]] = value.split('=')[1];
        });
        var oTemp = {};
        oTemp[key] = value;
        jQuery.extend(oParams, oTemp);
    } else {
        oParams[key] = value;
    }
    aParams = [];
    for (var key in oParams) {
        if (key != "" && oParams[key] != "") aParams.push(encodeURIComponent(key) + "=" + encodeURIComponent(oParams[key]));
    }
    window.location.replace(window.location.protocol + "//" + window.location.host + window.location.pathname + "?" + aParams.join("&"));
}

/* This is for DOM changes to refresh the view on back to keep the view updated */
if (readUrlHash("modified")) {
    var url = window.location.href;
    var newParams = window.location.search.substring(1);
    if (readUrlParam('viewId') == "DegreeAudit-FormView" || readUrlParam('viewId') == "PlanAudit-FormView") newParams = removeUrlParams(window.location.search, ["formKey", "cacheKey", "planAudit.auditId", "degreeAudit.auditId"]);
    var aHash = window.location.href.split("#")[1].replace("#", "").split("&");
    aHash.splice(aHash.indexOf("modified=true"), 1);
    window.location.assign(window.location.protocol + "//" + window.location.host + window.location.pathname + "?" + newParams + ((aHash.length > 0) ? "#" + aHash.join("&") : ""));
}

var _gaq = _gaq || [];

jQuery(document).ready(function () {
    var profile;
    var subdomain = document.location.hostname.split('.')[0];

    if (subdomain.indexOf("uwksdev") != -1) {
        profile = "1";
    }
    else if (subdomain.indexOf("uwkseval") != -1) {
        profile = "2";
    }
    else if (subdomain.indexOf("uwkstrn") != -1) {
        profile = "3";
    }
    else if (subdomain.indexOf("uwksprod") != -1 || subdomain.indexOf("uwstudent") != -1) {
        if (!jQuery("#applicationUser").data("adviser")) {
            profile = "4";
        } // Students analytics profile
        else {
            profile = "5";
        } // Advisers analytics profile
    }

    _gaq.push(['_setAccount', 'UA-33432259-' + profile]);
    _gaq.push(['_setDomainName', 'washington.edu']);
    _gaq.push(['_trackPageview']);

    (function () {
        var ga = document.createElement('script');
        ga.type = 'text/javascript';
        ga.async = true;
        ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
        var s = document.getElementsByTagName('script')[0];
        s.parentNode.insertBefore(ga, s);
    })();

    jQuery("head").append('<!--[if ie 9]><style type="text/css" media="screen"> \
        .btn, .btn:hover,\
        .btn[disabled="true"], .btn[disabled="true"]:hover,\
        .btn[disabled="disabled"], .btn[disabled="disabled"]:hover,\
        .planYear__term .planYear__termHeader, .planYear__term .planYear__termHeader:hover,\
        .planTerm__header.planTerm__header--past, .planTerm__header.planTerm__header--current, .planTerm__header.planTerm__header--future\
        {filter:none !important;}</style><![endif]-->\
    ');
});

function sessionExpired() {
    window.location = '/student/myplan/sessionExpired';
}

function stopEvent(e) {
    if (!e) var e = window.event;
    if (e.stopPropagation) {
        e.preventDefault();
        e.stopPropagation();
    } else {
        e.returnValue = false;
        e.cancelBubble = true;
    }
    return false;
}

function openDocument(url) {
    var newUrl;
    if (url.substring(0, 4) == "http") {
        newUrl = url;
    } else {
        newUrl = window.location.protocol + "//" + window.location.host + window.location.pathname.substring(0, window.location.pathname.lastIndexOf("/")) + "/" + url;
    }
    if (newUrl == window.location.href) {
        window.location.reload(true);
    } else {
        window.location.assign(newUrl);
    }
}

function removeUrlParams(url, removeList) {
    var tempArray = url.substring(1).split("&");
    var tempObject = {};
    jQuery.each(tempArray, function (index, value) {
        tempObject[value.split('=')[0]] = value.split('=')[1];
    });
    jQuery.each(removeList, function (index, value) {
        delete tempObject[value];
    });
    tempArray = [];
    for (var key in tempObject) {
        if (key !== "" && tempObject[key] !== "") tempArray.push(encodeURIComponent(key) + "=" + encodeURIComponent(tempObject[key]));
    }

    return tempArray.join("&");
}