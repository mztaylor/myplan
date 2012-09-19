<%@ page import="org.kuali.rice.core.api.config.property.ConfigContext,java.util.*" %>
<!DOCTYPE html>
<html>
<head>
    <%String url = ConfigContext.getCurrentContextConfig().getProperty("application.url");%>
    <META http-equiv="refresh" content="2;URL=<%=url%>" >
    <title>Session Expired</title>
    <link href="/student/krad/css/global/fss-reset.css" rel="stylesheet" type="text/css">
    <link href="/student/ks-myplan/css/error.css" rel="stylesheet" type="text/css">
</head>
<body>
<div id="container">
    <h1>UH-OH...</h1>

    <div id="content">
        <h2>YOUR SESSION EXPIRED.</h2>

        <p>It appears that your session is expired Please Wait while we redirect to Login Page</p>
    </div>
    <div id="more">Still can't find it? Visit our <a href="http://depts.washington.edu/myplan/help-site/">help page</a>
        or <a href="https://depts.washington.edu/myplan/contact-the-myplan-team/feedback/">let us know</a>.
    </div>
</div>
</body>

</html>