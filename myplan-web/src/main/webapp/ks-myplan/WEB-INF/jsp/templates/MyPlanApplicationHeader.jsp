<%@ page import="org.kuali.student.myplan.utils.UserSessionHelper" %>
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
        <div id="applicationLogo">
            My<strong>Plan</strong> <span>One-stop Academic Planner</span>
        </div>
        <div id="applicationUser">

            <% if (!UserSessionHelper.isAdviser()) { %>
            <div class="name"><a href="plan?methodToCall=startAcademicPlannerForm&viewId=StudentAcademicPlanner-FormView&pageId=student_academic_planner_page">
                Welcome, ${UserSession.person.firstName}</a></div> <% } %>
            <% if (UserSessionHelper.isAdviser()) { %>
            <span>Welcome, ${UserSession.person.firstName}</span><% } %>
        </div>
    </div>

    <div id="applicationNavigation">
        <ul>
            <li><a href="lookup?methodToCall=search&viewId=PlannedCourses-LookupView">Plan</a></li>
            <li><a href="course?methodToCall=start&viewId=CourseSearch-FormView">Find Courses</a></li>
            <li><a href="audit?methodToCall=audit&viewId=DegreeAudit-FormView">Audit Degree</a></li>
        </ul>
    </div>

    <% if (UserSessionHelper.isAdviser()) { %>
    <div id="adviser_banner" class="fl-container-945 myplan-section " style="background: #ccc; height:25px;">
          <span id="adviser_banner_name">
              <center>
                  <font size="2">You are viewing <b><%= UserSessionHelper.getStudentName() %>.</b>'s MyPlan: functionalities are limited except <u><b><a href="comment?methodToCall=startCommentForm&amp;viewId=Message-FormView&amp;pageId=message_dialog_response_page"><font color="black">leaving
              a message</font></a></b></u> to <b><%= UserSessionHelper.getStudentName() %>. </b><u><a href="#"><font color="gray">Learn more about Adviser View</font>
              </a></u></font>
              </center>
          </span>
    </div>
    <% } %>
</div>