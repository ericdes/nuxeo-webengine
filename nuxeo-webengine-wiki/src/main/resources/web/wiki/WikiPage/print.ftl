<#import "common/util.ftl" as base/>

<html>
  <head>
    <title>${Root.document.title} :: ${This.document.title} :: print preview</title>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
    <link rel="stylesheet" href="/nuxeo/site/files/resources/css/print.css" type="text/css" media="print" charset="utf-8">
    <link rel="stylesheet" href="/nuxeo/site/files/resources/css/print_version.css" type="text/css" media="screen" charset="utf-8">
  </head>
  <body>


<div class="printButton">
  <form>
    <input type="button" value=" Print! " onclick="window.print();return false;" />
  </form>
</div>

<div class="closeWindow">
<form>
    <input type="button" value=" Close this window " onclick="self.close()" />
  </form>
</div>

<hr/>

<div class="wikiName">From <span>${Root.document.title}</span></div>

<hr/>

<div id="entry-print">
<h1>${Document.title}</h1>

<div id="entry-content">
    <@wiki>${Document.wikiPage.content}</@wiki>
</div>

<div id="entry-attachments">
  <#include "includes/attached_files.ftl">
</div>
  
</div>

<hr/>

<div class="byline">Last modified on ${Document.modified?datetime} by ${Document.author}</div>

<hr/>

<div class="printButton">
  <form>
    <input type="button" value=" Print! " onclick="window.print();return false;" />
  </form>
</div>

<div class="closeWindow">
<form>
    <input type="button" value=" Close this window " onclick="self.close()" />
  </form>
</div>


  </body>
</html>