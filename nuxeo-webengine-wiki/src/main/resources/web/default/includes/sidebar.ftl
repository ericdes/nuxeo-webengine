<div class="sideblock general">
     <div class="searchBox">
      <form action="${root.docURL}@@search" method="get" accept-charset="utf-8">
        <input class="complete" type="text" name="q" id="q" autosave="com.mysite" results="5" value="Search">
        <input type="hidden" name="p" value="${root.path}">
      </form>
     </div>  
</div>

<!-- let other templates add more things to the sidebar -->

  <@block name="sidebar"/>

  <!-- See other related document should contributed by the wiki-->
  <!--  JQuery-needed : appears only if wiki page contains seeother -->
  <div class="sideblock contextual">
    <h3>Related documents</h3>
    <div class="sideblock-content">
      <@block name="seeother"/>
    </div>  
  </div>
  <div class="sideblock-content-bottom"></div>

<div class="sideblock general">
    <h3>Last Items</h3>
    <ul>
        <#list root.children?reverse as entry>
            <li><a href="/nuxeo/site/${root.name}/${entry.name}">${entry.title}</a></li>
            <#if entry_index = 3><#break></#if>
        </#list>
    </ul>
</div>



