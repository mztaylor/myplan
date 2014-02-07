<%@ page import="org.kuali.rice.core.api.config.property.ConfigContext,java.util.*" %>
<!DOCTYPE html>
<html>
<head>
    <%String url = ConfigContext.getCurrentContextConfig().getProperty("application.url");%>
    <META http-equiv="refresh" content="2;URL=<%=url%>">
    <title>Session Expired</title>
    <link href="/student/themes/ksap/templates/css/reset.css" rel="stylesheet" type="text/css">
    <link href="/student/themes/ksap/templates/css/error.css" rel="stylesheet" type="text/css">
    <script type="text/javascript" src="/student/plugins/jquery/jquery-1.8.3.js"></script>
    <script type="text/javascript" src="//use.typekit.net/jpx3dug.js"></script>
    <script type="text/javascript">
        jQuery(document).ready(function () {
            try{Typekit.load();}catch(e){}
        });
    </script>
    <script type="text/javascript" src="/student/themes/ksap/scripts/ksap.initialize.js"></script>
    <script type="text/javascript" async="" src="https://ssl.google-analytics.com/ga.js"></script>
</head>
<body>
<div id="container">
    <h1>UH-OH...</h1>

    <div id="content">
        <h2>YOUR SESSION EXPIRED.</h2>

        <p>It appears that your session is expired Please Wait while we <br/> redirect to Login Page</p>
    </div>
    <div id="more">Completely lost? Visit our <a href="http://depts.washington.edu/myplan/help-site/">help page</a>
        or <a href="https://depts.washington.edu/myplan/contact-the-myplan-team/feedback/">let us know</a>.
    </div>
</div>
</body>

</html>