<@extends src="base.ftl">
<@block name="header"><h1><a href ="${This.path}">User Administration</a></h1></@block>

<@block name="toolbox">
  <div class="sideblock contextual">
    <h3>Toolbox</h3>
    <div class="sideblock-content">
      <ul>
        <li><a href="${This.activeService.path}/.views/create_user">Create User</a></li>
        <li><a href="${This.activeService.path}/.views/create_group">Create Group</a></li>
      </ul>
    </div>
  </div>
</@block>

<@block name="content">



<div id="entry-actions">
<div class="ui-tabs-panel">

<h1>Search</h1>

<div class="userSearch">
  <h2>Users</h2>
  <form method="GET" action="${Context.URL}" accept-charset="utf-8">
    <input type="text" name="query" value=""/>
    <input type="submit" value="Search"/>
  </form>
</div>

<div class="groupSearch">
  <h2>Groups</h2>
  <form method="GET" action="${Context.URL}" accept-charset="utf-8">
    <input type="text" name="query" value=""/>
    <input type="hidden" name="group" value="true"/>
    <input type="submit" value="Search"/>
  </form>
</div>

<#if users>
<table class="itemListing directories">
<thead>
	<tr>
		<th>User Name</th>
		<th>First Name</th>
		<th>Last Name</th>
	</tr>
</thead>
<tbody>
<#list users as user>
	<tr>
		<td><a href="${This.activeService.path}/user/${user}">${user}<a/></td>
		<td>${user.firstName}</td>
		<td>${user.lastName}</td>
	</tr>
</#list>
</tbody>
</table>
</#if>


<#if groups>
<table class="itemListing directories">
<thead>
  <tr>
    <th>Group Name</th>
  </tr>
</thead>
<tbody>
<#list groups as user>
  <tr>
    <td><a href="${This.activeService.path}/group/${user}">${user}<a/></td>
  </tr>
</#list>
</tbody>
</table>
</#if>


</div>
</div>

</@block>
</@extends>
