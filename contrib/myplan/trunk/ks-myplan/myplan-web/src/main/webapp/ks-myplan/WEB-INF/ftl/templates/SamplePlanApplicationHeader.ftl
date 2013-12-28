<#macro ksap_app_header element>
<div class="appHeader">

    <div class="appHeader__logo">SamplePlan</div>

    <div class="appHeader__user" data-adviser="true">

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

</div>

    <img class="appHeader__patch" src="../themes/ksap/images/appheader_icon_logo.png"/>

</div>

    <div class="appnavigation">
        <ul>
            <#if RequestParameters.viewId??>
            <#--Plan Page Link-->
                <#if RequestParameters.viewId=="SamplePlan-LookupView">
                    <li class="appnavigation__item--home"><a
                            href="lookup?methodToCall=search&viewId=SamplePlan-LookupView"
                            class="appnavigation__item--active">Home</a></li>
                <#else>
                    <li class="appnavigation__item--home"><a
                            href="lookup?methodToCall=search&viewId=SamplePlan-LookupView">Home</a></li>
                </#if>
            </#if>
        </ul>
    </div>

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