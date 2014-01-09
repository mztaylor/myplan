<#macro ksap_app_header element>
<div class="appHeader">
    <div class="appHeader__logo">MyPlan</div>

        <#if UserSession.objectMap["kuali.uw.authz.adviser"]??>
            <#if UserSession.objectMap["kuali.uw.authz.adviser"]?string("true","false")=="true">
        <div class="appHeader__user" data-adviser="true">
            </#if>
        <#else>
    <div class="appHeader__user" data-adviser="false">
        </#if>

        <#if UserSession.objectMap["kuali.uw.authz.adviser"]??>
            <#if UserSession.objectMap["kuali.uw.authz.adviser"]?string("true","false")=="true">
            <div class="appHeader__identity">
                    Welcome, <span
                    class="appHeader__person appHeader__person--adviser">${UserSession.person.firstName?cap_first} ${UserSession.person.lastName?substring(0,1)?capitalize}
                    .</span>
                </div>
            <div class="appHeader__logout">
                    <a href="http://depts.washington.edu/myplan/help-site/" target="_blank">Help</a>
                    |
                    <a href="/${ConfigProperties['app.code']}/logout.do">Log out</a>
                </div>
            </#if>
        <#else>
        <div class="appHeader__identity">
            Welcome, <a class="appHeader__person"
                        onclick="var retrieveData = {action:'plan', viewId:'StudentAcademicPlanner-FormView', methodToCall:'startPlanAccessForm', pageId:'student_academic_planner_page'}; var popupStyle = {width:'16px', height:'16px'}; var popupOptions = {tail:{align:'right'}, position:'bottom', align:'right', close:true}; openPopup('student_academic_planner_page', retrieveData, 'plan', popupStyle, popupOptions, ((event) ? event : window.event));">${UserSession.person.firstName?cap_first} ${UserSession.person.lastName?substring(0,1)?capitalize}
                .</a>
            </div>
        </#if>
    </div>

    <img class="appHeader__patch" src="../themes/ksap/images/appheader_icon_logo.png"/>

</div>

    <div class="appnavigation">
            <ul>
                <#if RequestParameters.viewId??>
                <#--Plan Page Link-->
                    <#if RequestParameters.viewId=="PlannedCourses-FormView" || RequestParameters.viewId=="SingleTerm-InquiryView">
                    <li class="appnavigation__item--home"><a
                            href="plan?methodToCall=start&viewId=PlannedCourses-FormView"
                            class="appnavigation__item--active">Plan</a></li>
                    <#else>
                    <li class="appnavigation__item--home"><a
                            href="plan?methodToCall=start&viewId=PlannedCourses-FormView">Plan</a></li>
                    </#if>

                <#--Find a Course Page Link-->
                    <#if RequestParameters.viewId=="CourseSearch-FormView">
                    <li><a href="course?methodToCall=start&viewId=CourseSearch-FormView"
                           class="appnavigation__item--active">Find
                            Courses</a></li>
                    <#else>
                        <li><a href="course?methodToCall=start&viewId=CourseSearch-FormView">Find Courses</a></li>
                    </#if>

                <#--DegreeAudit Page Link-->
                    <#if RequestParameters.viewId=="DegreeAudit-FormView" || RequestParameters.viewId=="PlanAudit-FormView">
                    <li><a href="audit?methodToCall=audit&viewId=PlanAudit-FormView"
                           class="appnavigation__item--active">Audit</a></li>
                    <#else>
                        <li><a href="audit?methodToCall=audit&viewId=PlanAudit-FormView">Audit</a></li>
                    </#if>
                <#else >
                <li class="appnavigation__item--home"><a
                            href="plan?methodToCall=start&viewId=PlannedCourses-FormView&currentPage=planPage">Plan</a>
                    </li>
                    <li><a href="course?methodToCall=start&viewId=CourseSearch-FormView&currentPage=coursePage">Find
                        Courses</a></li>
                    <li><a href="audit?methodToCall=audit&viewId=PlanAudit-FormView&currentPage=auditPage">Audit</a>
                    </li>
                </#if>
            </ul>
        </div>

        <#if UserSession.objectMap["kuali.uw.authz.adviser"]??>
            <#if UserSession.objectMap["kuali.uw.authz.adviser"]?string("true","false")=="true">
            <div class="alert alert-info">
            <#--<img src="/student/ks-myplan/images/pixel.gif" alt="" class="icon"/>-->

            <#--<div class="message">-->
                        You&#39;re viewing <strong>${UserSession.objectMap["kuali.uw.authn.studentName"]}
                        &#39;s <#if UserSession.objectMap["kuali.uw.authn.studentNumber"]??>
                            (${UserSession.objectMap["kuali.uw.authn.studentNumber"]})</#if></strong>
                        MyPlan.
                        Some features are restricted in Adviser View. <a
                            href="javascript:openHelpWindow('https://depts.washington.edu/myplan/?page_id=985#view');">Learn
                        more.</a>
            <#--</div>-->
                    </div>
            </#if>
        </#if>
        <#if Request.messageBannerText??>
        <div class="alert alert-info">
            <strong>[ ANNOUNCEMENT ]</strong> ${Request.messageBannerText}
        <#--<div class="message">-->
        <#--<span class="myplan-text-gray">[ ANNOUNCEMENT ]</span>-->
        <#--<span>${Request.messageBannerText}</span>-->
        <#--</div>-->
                </div>
        </#if>
</#macro>