<html>

<head>
    <title>${this.title}</title>
<link rel="stylesheet" href="/nuxeo/site/files/resources/css/webengine.css" type="text/css" media="screen" charset="utf-8">
<link rel="stylesheet" href="/nuxeo/site/files/resources/css/wiki.css" type="text/css" media="screen" charset="utf-8">
<script src="/nuxeo/site/files/resources/script/jquery/jquery.js"></script>
  <link rel="stylesheet" href="/nuxeo/site/files/resources/script/jquery/ui/themes/flora/flora.all.css" type="text/css" media="screen" title="Flora (Default)">
  <script type="text/javascript" src="/nuxeo/site/files/resources/script/jquery/ui/ui.base.js"></script>
  <script type="text/javascript" src="/nuxeo/site/files/resources/script/jquery/ui/ui.tabs.js"></script>

<base href="${this.docURL}">

</head>

<body>

<div id="wrap">
    <div id="header">
       <h1><a href="/nuxeo/site/wikione/">${root.title}</a></h1>
       <img alt="Nuxeo Wiki" src="/nuxeo/site/files/resources/image/logo-community.png"/>
        
    </div>

    <div id="main">
        <@block name="content">
        ##This is the content block##
        </@block>
    </div>

    <div id="sidebar">
        <#include "/default/includes/sidebar.ftl"/>
    </div>
    
    <div id="footer">
       <p>Last modified by ${this.author} @ ${this.dublincore.modified?datetime}</p>
       <p>Copyright <a href="">Nuxeo SAS</a> 2000-2015</p>
    </div>
    
</div>

</body>
</html>