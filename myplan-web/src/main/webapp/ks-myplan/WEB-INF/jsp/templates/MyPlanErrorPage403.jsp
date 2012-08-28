<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html>
<head>
    <title>Error 403 - Page Forbidden</title>
    <link href="/student/krad/css/global/fss-reset.css" rel="stylesheet" type="text/css">
    <link href="/student/ks-myplan/css/error.css" rel="stylesheet" type="text/css">
</head>
<body>
<div id="container">
    <h1>UH-OH...</h1>
    <div id="content">
        <h2>Error 403 - Page Forbidden</h2>
        <p>It's looking like you may have taken a wrong turn.</p>
        <p>Check the URL and try again.</p>
    </div>
    <h3>Try MyPlan site that might help you:</h3>
    <ul id="links">
        <li><a href="/student/myplan/lookup?methodToCall=search&viewId=PlannedCourses-LookupView">Plan</a></li>
        <li><a href="/student/myplan/course?methodToCall=start&viewId=CourseSearch-FormView">Find Courses</a></li>
        <li><a href="/student/myplan/audit?methodToCall=audit&viewId=DegreeAudit-FormView">Audit Degree</a></li>
    </ul>
    <div id="more">Completely Lost? Go to <a href="http://depts.washington.edu/myplan/help-site/">Help</a> section or <a href="https://depts.washington.edu/myplan/contact-the-myplan-team/feedback/">let us know</a>.</div>
</div>
</body>

</html>