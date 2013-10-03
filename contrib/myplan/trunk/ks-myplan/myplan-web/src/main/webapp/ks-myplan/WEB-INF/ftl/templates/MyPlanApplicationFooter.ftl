<#macro ksap_app_footer group>
<div class="appfooter">
    <div class="appfooter__links">
            <ul>
                <li><a href="http://depts.washington.edu/myplan/help-site/" target="_blank">Help</a></li>
                <li><a href="http://depts.washington.edu/myplan/feedback/" target="_blank">Feedback</a></li>
                <li><a href="http://myuw.washington.edu/" target="_blank">MyUW</a></li>
                <li><a href="http://www.washington.edu/" target="_blank">UW Home</a></li>
            </ul>
        <ul>
                <li>&copy; ${.now?string("yyyy")} University of Washington</li>
                <li><a href="http://www.washington.edu/online/terms" target="_blank">Terms &amp; Conditions</a></li>
                <li><a href="http://www.washington.edu/online/privacy" target="_blank">Privacy</a></li>
            </ul>
        </div>

    <div class="appfooter__version">Version: ${ConfigProperties['myplan.version']}</div>
    <div class="appfooter__host">${Request.hostName}</div>
</div>
</#macro>