<head>
  <title><@block name="title">WebEngine Webworkspace</@block></title>
  <meta http-equiv="Content-Type" content="text/html;charset=UTF-8">

  <!-- stylesheets -->    
  <link rel="stylesheet" href="${skinPath}/css/webengine.css" type="text/css" media="screen" charset="utf-8">
  <link rel="stylesheet" href="${skinPath}/css/wiki.css" type="text/css" media="screen" charset="utf-8">

</head>

<body>
<div id="wrap">
  <div id="header">
    <div class="webEngineRoot"><a href="${Root.path}"><img src="${skinPath}/image/dots.png" width="16" height="16" alt=""/></a></div>
    <h1><a href="${Root.path}">WebEngine Webworkspaces 
      <#if Document??>
        - ${Root.document.title}
      </#if>
      </a>
      </h1>
  </div>

  <div id="main-wrapper">
    <div id="main">
      <div class="main-content">
        <div id="content">
        
        
<hr>
Logo : <img src="${This.path}/logo" alt="logo">
<hr>

Welcome Animation/Image</br>
<img src="${This.path}/welcomeMedia" alt="Welcome Media">

<hr>
Welcome Text:</br>
${welcomeText}

<hr>
      <@block name="toolbox">

<#include "includes/tree.ftl"/>
<@navigator/>

      </@block>



        </div>
      </div>
    </div>

  </div>

  <div id="footer">
     <@block name="footer">
     <p>&copy; 2000-2008 <a href="http://www.nuxeo.com/en/">Nuxeo</a>.</p>
     </@block>
  </div>
</div>
</body>
</html>

