<#macro allWebpages>

<div class="allWebPagesBlock"> 
  
  <div class="allWebPagesResume">    
    <#list webPages as webPage>  
      <div class="documentInfo"> 
        <a href="${This.path}/${webPage.path}"> ${webPage.name}</a>
       </div>
       <div style="clear:both;"></div>
      </div>
    </#list>
  </div>
</div>

</#macro>
        
        
        
        
