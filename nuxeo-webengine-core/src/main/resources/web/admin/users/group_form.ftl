<@extends src="base.ftl">
<@block name="header"><h1><a href ="${appPath}/users"><#if group>Group Details<#else>Group creation</#if></a></h1></@block>

<@block name="toolbox">
  <div class="sideblock contextual">
    <h3>Toolbox</h3>
    <div class="sideblock-content">
      <ul>
        <li><a href="${appPath}/users/create_user">Create User</a></li>
        <li><a href="${appPath}/users/create_group">Create Group</a></li>
        <li><a href="${appPath}/users/delete_user">Delete Group</a></li>
      </ul>
    </div>
  </div>
</@block>


<@block name="content">
<#if group>
<h1>${group.name}</h1>
</#if>
<br/>
<form method="POST" action="${appPath}/users/save_group" accept-charset="utf-8">
        <#if group>
          <input type="hidden" name="groupName" value="${group.name}"/>
          <#list usersGroup as user>
            <li><a href="${appPath}/users/user/${user}">${user}</a>
          </#list>
        <#else>
<table>
    <tbody>
        <tr>
            <td>Name</td> 
            <td><input type="text" name="groupName" value=""/></td>
        </tr>
        <tr>
          <td colspan="2"><input type="submit" value="Save"/></td>
        </tr>
    </tbody>
</table>
</#if>
</form>

</@block>
</@extends>
