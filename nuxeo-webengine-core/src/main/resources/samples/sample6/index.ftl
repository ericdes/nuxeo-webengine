<#-- we are reusing the base template from the base module -->
<@extends src="base.ftl">

<#-- we are redefining only te title block -->
<@block name="title">Sample 6: Web Module Extensibility</@block>

<@block name="content">
Browse <a href="${This.path}/repository">repository</a>
</@block>


</@extends>
