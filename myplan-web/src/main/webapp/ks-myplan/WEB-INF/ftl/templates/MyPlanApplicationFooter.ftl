<#macro myplan_app_footer group>
<div id="applicationFooter">
    <div id="applicationFooterSection">
        <div class="links">
            <ul>
                <li><a href="javascript:openHelpWindow('http://depts.washington.edu/myplan/help-site/');">Help</a></li>
                <li><a href="http://depts.washington.edu/myplan/feedback/" target="_blank">Feedback</a></li>
                <li><a href="http://myuw.washington.edu/" target="_blank">MyUW</a></li>
                <li><a href="http://www.washington.edu/" target="_blank">UW Home</a></li>
            </ul>
            <ul class="smaller">
                <li>&copy; ${.now?string("yyyy")} University of Washington</li>
                <li><a href="http://www.washington.edu/online/terms" target="_blank">Terms &amp; Conditions</a></li>
                <li><a href="http://www.washington.edu/online/privacy" target="_blank">Privacy</a></li>
            </ul>
        </div>

        <#assign hostName= "${Request.request.serverName}">
        <#if hostName?contains(".")>
            <#list hostName?split(".") as x>
                <#assign hostName="${x}">
                <#if "${x_index}" = "0"><#break></#if>
            </#list>
        </#if>

        <div class="version smaller" title="${hostName}">Version: ${ConfigProperties['myplan.version']}</div>
    </div>
</div>
</#macro>