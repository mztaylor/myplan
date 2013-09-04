<%@ page import="org.kuali.student.myplan.plan.util.AtpHelper" %>
<%--

    Copyright 2005-2012 The Kuali Foundation

    Licensed under the Educational Community License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.opensource.org/licenses/ecl2.php

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>
<div id="applicationHeader">
    <div id="applicationHeading">
        <div id="applicationLogo">MyPlan</div>
        <div id="applicationUser">
            <% if (!AtpHelper.getUserSessionHelper().isAdviser()) { %>
            <div class="identity">
                Welcome, <a class="name"
                            onclick="var retrieveData = {action:'plan', viewId:'StudentAcademicPlanner-FormView', methodToCall:'startPlanAccessForm', pageId:'student_academic_planner_page'}; var popupStyle = {width:'16px', height:'16px'}; var popupOptions = {tail:{align:'right'}, position:'bottom', align:'right', close:true}; openPopup('student_academic_planner_page', retrieveData, 'plan', popupStyle, popupOptions, window.event);">${UserSession.person.firstName}</a>
            </div>
            <% } %>
            <% if (AtpHelper.getUserSessionHelper().isAdviser()) { %>
            <div class="identity">
                Welcome, <span class="name">${UserSession.person.firstName}</span>
            </div>
            <% } %>
        </div>
        <img id="myplanUwPatch" src="../ks-myplan/images/myplan_w_patch_purple.png"/>

        <div id="applicationNavigation">
            <ul>
                <li class="active home"><a href="plan?methodToCall=start&viewId=PlannedCourses-FormView">Plan</a></li>
                <li><a href="course?methodToCall=start&viewId=CourseSearch-FormView">Find Courses</a></li>
                <li><a href="audit?methodToCall=audit&viewId=DegreeAudit-FormView">Audit Degree</a></li>
            </ul>
        </div>
    </div>


    <% if (AtpHelper.getUserSessionHelper().isAdviser()) { %>
    <div class="adviser-banner">
        You are viewing <strong><%= AtpHelper.getUserSessionHelper().getStudentName() %>.</strong>'s MyPlan: functionalities are
        limited except
        <a href="comment?methodToCall=startCommentForm&amp;viewId=Message-FormView&amp;pageId=message_dialog_response_page">leaving
            a message</a> to <%= AtpHelper.getUserSessionHelper().getStudentName() %>. <a href="#">Learn more about Adviser View</a>
    </div>
    <% } %>
</div>