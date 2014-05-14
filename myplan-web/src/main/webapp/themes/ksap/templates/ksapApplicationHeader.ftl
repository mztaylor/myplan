<#if UserSession.objectMap["kuali.uw.authz.adviser"]?? && UserSession.objectMap["kuali.uw.authz.adviser"]?string("true","false")=="true">
    <#assign isAdviser = true>
<#else>
    <#assign isAdviser = false>
</#if>

<#assign banner>
    <#if Request.messageBannerText??>
        <div class="messageBanner">
            ${Request.messageBannerText}
        </div>
    </#if>
    <#if isAdviser>
        <div>
            You&#39;re viewing <strong>${UserSession.objectMap["kuali.uw.authn.studentName"]}&#39;s</strong>
            <#if UserSession.objectMap["kuali.uw.authn.studentNumber"]??><strong>(${UserSession.objectMap["kuali.uw.authn.studentNumber"]})</strong></#if>
            MyPlan. Some features are restricted in Adviser View. <a href="javascript:openHelpWindow('https://depts.washington.edu/myplan/?page_id=985#view');">Learn more.</a>
        </div>
    </#if>
    <#if UserSession?? && UserSession.backdoorInUse>
        <div class="backdoor">
            Backdoor Id <b>${UserSession.principalName}</b> is in use
        </div>
    </#if>
</#assign>

<#macro ksap_app_header element>
<#if banner?has_content>
<div id="KSAP-Banner-Wrapper" class="alert alert-banner alert-info">
    <div id="KSAP-Banner" class="alert-wrapper">
    ${banner}
    </div>
</div>
</#if>

<div id="KSAP-ApplicationHeader-Wrapper">
    <div id="KSAP-ApplicationHeader">
        <div class="appHeader">
            <div class="appHeader__logo">MyPlan</div>
            <div class="appHeader__user" data-adviser="${isAdviser?string("true","false")}">
                <div class="appHeader__identity">
                    Welcome,
                    <#if isAdviser>
                        <span class="appHeader__person appHeader__person--adviser">${UserSession.person.firstName?cap_first} ${UserSession.person.lastName?substring(0,1)?capitalize}.</span>
                    <#else>
                        <a class="appHeader__person" onclick="var retrieveData = {action:'plan', viewId:'StudentAcademicPlanner-FormView', methodToCall:'startPlanAccessForm', pageId:'student_academic_planner_page'}; var popupStyle = {width:'16px', height:'16px'}; var popupOptions = {tail:{align:'right'}, position:'bottom', align:'right', close:true}; openPopup('student_academic_planner_page', retrieveData, 'plan', popupStyle, popupOptions, ((event) ? event : window.event));">${UserSession.person.firstName?cap_first} ${UserSession.person.lastName?substring(0,1)?capitalize}.</a>
                    </#if>
                </div>
                <#if isAdviser>
                <div class="appHeader__logout">
                    <a href="http://depts.washington.edu/myplan/help-site/" target="_blank">Help</a> | <a href="/${ConfigProperties['app.code']}/logout.do">Log out</a>
                </div>
                </#if>
            </div>
            <img class="appHeader__patch" src="../themes/ksap/images/appheader_icon_logo@2x.png"/>
        </div>

        <div class="appNavigation">
            <ul>
                <#if RequestParameters.viewId??>
                <#--Plan Page Link-->
                    <#if RequestParameters.viewId=="PlannedCourses-FormView" || RequestParameters.viewId=="SingleTerm-InquiryView" || RequestParameters.viewId=="ScheduleBuild-FormView">
                    <li><a class="appNavigation__item--home appNavigation__item--active"
                           href="plan?methodToCall=start&viewId=PlannedCourses-FormView">Plan</a></li>
                    <#else>
                    <li><a class="appNavigation__item--home"
                           href="plan?methodToCall=start&viewId=PlannedCourses-FormView">Plan</a></li>
                    </#if>

                <#--Find a Course Page Link-->
                    <#if RequestParameters.viewId=="CourseSearch-FormView">
                    <li><a class="appNavigation__item--active"
                           href="course?methodToCall=start&viewId=CourseSearch-FormView">Find Courses</a></li>
                    <#else>
                    <li><a href="course?methodToCall=start&viewId=CourseSearch-FormView">Find Courses</a></li>
                    </#if>

                <#--DegreeAudit Page Link-->
                    <#if RequestParameters.viewId=="DegreeAudit-FormView" || RequestParameters.viewId=="PlanAudit-FormView">
                    <li><a class="appNavigation__item--active"
                           href="audit?methodToCall=audit&viewId=PlanAudit-FormView">Audit</a></li>
                    <#else>
                    <li><a href="audit?methodToCall=audit&viewId=PlanAudit-FormView">Audit</a></li>
                    </#if>
                <#else >
                    <li><a class="appNavigation__item--home"
                           href="plan?methodToCall=start&viewId=PlannedCourses-FormView&currentPage=planPage">Plan</a></li>
                    <li><a href="course?methodToCall=start&viewId=CourseSearch-FormView&currentPage=coursePage">Find Courses</a></li>
                    <li><a href="audit?methodToCall=audit&viewId=PlanAudit-FormView&currentPage=auditPage">Audit</a></li>
                </#if>
            </ul>
        </div>
    </div>
</div>
</#macro>