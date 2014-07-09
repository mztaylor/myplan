<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html>
<head>
    <title>Scheduled Maintenance</title>
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
    <h1 style="font-size: 300%">We'll Be Right Back</h1>

    <div id="content">
        <h2>We are performing scheduled maintenance</h2>

        <p>We expect to be back in a couple hours.</p>

        <p>Thank you for your patience.</p>

        <p>Try going outside to play.</p>
    </div>
</div>
</body>

</html>