<@extends src="base.ftl">
<@block name="header"><h1><a href="${appPath}">Error</a></h1></@block>
<@block name="content">

<h1>401 - Unauthorizated</h1>

<p>
You don't have privileges to access this page
</p>
<p>
<br/>
</p>
<#include "include/login.ftl">

</@block>
</@extends>

