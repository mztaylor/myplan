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
 <div id="appheader_div">
        <div id="appheading_div">
            <div id="applogo_div">
                My<strong>Plan</strong> <span>One-stop Academic Planner</span>
            </div>
            <div id="appuser_div">
                <div class="name">Welcome, ${UserSession.person.firstName}</div>
                <div class="links">
                    <ul>
                        <li><a href="#">Log out</a></li>
                    </ul>
                </div>
            </div>
        </div>

        <div id="appnav_div">
            <ul>
                <li><a href="lookup?methodToCall=search&viewId=PlannedCourses-LookupView">Home</a></li>
                <li><a href="course?methodToCall=start&viewId=CourseSearch-FormView">Find a Course</a></li>
                <li><a href="#">Explore Programs</a></li>
                <li><a href="audit?methodToCall=audit&viewId=DegreeAudit-FormView">Audit Degree</a></li>
            </ul>
        </div>
  </div>