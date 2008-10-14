
<#if This.isRoot()>
<h2>${Document.title}</h2>
<#else>
<h2><a href="${This.previous.path}"><img src="/nuxeo/site/files/resources/image/up_nav.gif" alt="Up" border="0"/></a> ${Document.title}</h2>
</#if>
<div><b>Name:</b> ${Document.name}</div>
<div><b>ID:</b> ${Document.id}</div>
<div><b>Type:</b> ${Document.type}</div>
<blockquote>${Document.description}</blockquote>

<#assign file = Document["file:content"]/>
<#if file>
<div id="mainContentBox">
  Attachment: <a href="${This.path}/.file?property=file:content">${file.filename}</a>
</div>
</#if>

</div>

