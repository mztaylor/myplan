var account;

switch(document.location.hostname.split('.')[0]) {
    case 'uwksdev01':
        account = '1';
        break;
    case 'uwkseval':
        account = '2';
        break;
    case 'uwkstrn01':
        account = '3';
        break;
    case 'uwstudent':
        account = '4';
        break;
}

var _gaq = _gaq || [];
_gaq.push(['_setAccount', 'UA-33432259-' + account]);
_gaq.push(['_trackPageview']);

(function () {
    var ga = document.createElement('script');
    ga.type = 'text/javascript';
    ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0];
    s.parentNode.insertBefore(ga, s);
})();