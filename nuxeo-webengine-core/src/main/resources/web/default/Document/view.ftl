<@extends src="base.ftl">
<@block name="content">
<script>
$(document).ready(function(){
  $("#entry-actions > ul").tabs();
  //$("#entry-actions > ul").tabs("select", '#view_content');
});
</script>

<div id="entry-actions">
<ul>
  <#list Context.getActions("TABVIEW")?sort as action>
    <li><a href="${This.urlPath}@@${action.id}"><span>${message('action.' + action.id)}</span></a></li>
  </#list>
</ul>

</div>

</@block>
</@extends>