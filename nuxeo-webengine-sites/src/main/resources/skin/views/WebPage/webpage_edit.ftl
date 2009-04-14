<link rel="stylesheet" type="text/css" href="${skinPath}/script/markitup/skins/markitup/style.css" />
<link rel="stylesheet" type="text/css" href="${skinPath}/script/markitup/sets/wiki/style.css" />
<!-- end markitup -->

<form method="POST" action="${This.path}/modifyWebPage" accept-charset="utf-8">
  <table class="modifyWebPage">
    <tbody>
      <tr>
        <td width="30%"></td>
        <td width="70%"></td>
      </tr>
      <tr>
        <td>${Context.getMessage("label.page.title")}</td>
        <td><input type="text" name="title" value="${Document.title}"/></td>
      </tr>
      <tr>
        <td>${Context.getMessage("label.page.description")}</td>
        <td><textarea name="description">${Document.dublincore.description}</textarea></td>
      </tr>
      <tr>
        <td>${Context.getMessage("label.page.content")}</td>
        <td>
          <#if (Document.webpage.isRichtext == true)> 
            <textarea name="richtextEditor" style="width: 300px;height: 400px" cols="60" rows="20" id="richtextEditor">${Document.webpage.content}</textarea>
          <#else>
            <textarea name="wikitextEditor" cols="60" rows="20" id="wiki_editor" >${Document.webpage.content}</textarea>
          </#if> 
        </td>
      </tr>
      <tr>
        <td>${Context.getMessage("label.page.push")}</td>
        <td>
          <input type="radio" id="pushToMenuYes" name="pushToMenu" value="true" />${Context.getMessage("label.page.push.yes")}
          <input type="radio" id="pushToMenuNo" name="pushToMenu" value="false" />${Context.getMessage("label.page.push.no")}
        </td>
      </tr>
      <tr>
        <td colspan="2"><input type="submit" value="${Context.getMessage("label.page.save")}"/></td>
      </tr>
    </tbody>
  </table>  
</form>

<script>
function launchEditor() {
  $('#wiki_editor').markItUp(myWikiSettings);
}

$('#richtextEditor').ready(function() {
  if(document.tmce == null) {
    document.tmce = new tinymce.Editor('richtextEditor',{theme : "advanced",
      plugins : "safari,spellchecker,pagebreak,style,layer,table,save,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,preview,media",
      // Theme options
      theme_advanced_buttons1 : "save,newdocument,|,bold,italic,underline,strikethrough,|,justifyleft,justifycenter,justifyright,justifyfull,|,styleselect,formatselect,fontselect,fontsizeselect",
      theme_advanced_buttons2 : "cut,copy,paste,pastetext,pasteword,|,search,replace,|,bullist,numlist,|,outdent,indent,blockquote,|,undo,redo,|,link,unlink,anchor,image,cleanup,help,code,|,insertdate,inserttime,preview,|,forecolor,backcolor",
      theme_advanced_buttons3 : "tablecontrols,|,hr,removeformat,visualaid,|,sub,sup,|,charmap,emotions,iespell,media,advhr,|,print,|,ltr,rtl,|,fullscreen",
      theme_advanced_buttons4 : "insertlayer,moveforward,movebackward,absolute,|,styleprops,spellchecker,|,cite,abbr,acronym,del,ins,attribs,|,visualchars,nonbreaking,template,blockquote,pagebreak,|,insertfile,insertimage",
      theme_advanced_toolbar_location : "top",
      theme_advanced_toolbar_align : "left",
      theme_advanced_statusbar_location : "bottom",
      theme_advanced_resizing : true});
    document.tmce.render();
  }
});

$('#wiki_editor').ready(function() {
  setTimeout(launchEditor, 10);
});

if ('${Document.webpage.pushtomenu}' == 'true') {
  document.getElementById("pushToMenuYes").checked = true;
} else {
  document.getElementById("pushToMenuNo").checked = true;
}
</script>

