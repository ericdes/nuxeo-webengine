<!-- markit up -->
<script src="${skinPath}/script/markitup/jquery.markitup.pack.js"></script>
<script src="${skinPath}/script/markitup/sets/wiki/set.js"></script>
<!-- end markitup -->

<form method="POST" action="${This.path}/@put" accept-charset="utf-8">
  <h1><input type="text" name="dc:title" value="${Document.dublincore.title}"/></h1>
  <textarea name="wp:content" cols="75" rows="40" id="wiki_editor" class="entryEdit">${Document.wikiPage.content}</textarea>
  <p class="entryEditOptions">
    Version increment:
    <input type="radio" name="versioning" value="major" checked> Major
    &nbsp;&nbsp;
    <input type="radio" name="versioning" value="minor"/> Minor
  </p>
  <p class="buttonsGadget">
    <input type="submit" class="button"/>
  </p>
</form>

<script>
function launchEditor() {
  $('#wiki_editor').markItUp(myWikiSettings);
}

$('#wiki_editor').ready(function() {
  setTimeout(launchEditor, 10);
});
</script>