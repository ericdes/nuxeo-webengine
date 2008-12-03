<@extends src="base.ftl">
<@block name="title">Welcome to Nuxeo WebEngine!</@block>

<@block name="header"><h1><a href="${appPath}">Nuxeo WebEngine</a></h1></@block>
<@block name="content">
Hello <strong>${Context.principal.name}</strong>! This is the root of your web site.
</p>

<div id="mainContentBox">
Welcome to Nuxeo WebEngine. Here are the available applications :
<ul>
<#--
<#list Engine.moduleRegistry.modules as module>
  <#if module.name != "base" >
  
  <li><a href="${basePath}${module.moduleBinding.path}">${module.moduleTitle}</a></li>
  </#if>
</#list>
-->

<#list Engine.moduleRegistry.lazyModuleKeys as path>
  <#assign name=Engine.moduleRegistry.getLazyModuleName(path)>
  <#if name != "base" >  
    <li><a href="${basePath}${path}">${name}</a></li>
  </#if>
</#list>


</ul>
</div>

<div class="tip">
Your web root is <pre>${env.installDir}</pre>
</div>

</@block>

</@extends>
