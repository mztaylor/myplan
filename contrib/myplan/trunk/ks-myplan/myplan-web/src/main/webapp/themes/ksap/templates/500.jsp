<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html>
<head>
    <title>Error 500 - Internal Server Error :: UW MyPlan</title>
    <link href="/${ConfigProperties.app.code}/themes/ksap/templates/css/reset.css" rel="stylesheet" type="text/css">
    <link href="/${ConfigProperties.app.code}/themes/ksap/templates/css/error.css" rel="stylesheet" type="text/css">
    <script type="text/javascript" src="/${ConfigProperties.app.code}/plugins/jquery/jquery-1.8.3.js"></script>
    <script type="text/javascript" src="//use.typekit.net/jpx3dug.js"></script>
    <script type="text/javascript">
        jQuery(document).ready(function () {
            try{Typekit.load();}catch(e){}
        });
    </script>
    <script type="text/javascript" src="/${ConfigProperties.app.code}/themes/ksap/scripts/ksap.initialize.js"></script>
    <script type="text/javascript" async="" src="https://ssl.google-analytics.com/ga.js"></script>
</head>
<body>
<div id="container">
    <h1>UH-OH...</h1>

    <div id="content">
        <h2>500 Internal Server Error</h2>

        <p>It looks like our server is playing dead.</p>

        <p>Don&#39;t worry-- we&#39;re working on the problem and will have it up and running again soon.</p>
    </div>
    <h3>Try these links to the MyPlan site:</h3>
    <ul id="links">
        <li class="home"><a href="plan?methodToCall=start&viewId=PlannedCourses-FormView">Plan</a></li>
        <li><a href="course?methodToCall=start&viewId=CourseSearch-FormView">Find Courses</a></li>
        <li><a href="audit?methodToCall=audit&viewId=DegreeAudit-FormView">Audit Degree</a></li>
    </ul>
    <div id="more">Completely lost? Visit our <a href="http://depts.washington.edu/myplan/help-site/">help page</a> or
        <a href="https://depts.washington.edu/myplan/contact-the-myplan-team/feedback/">let us know</a>.
    </div>
</div>
</body>

</html>