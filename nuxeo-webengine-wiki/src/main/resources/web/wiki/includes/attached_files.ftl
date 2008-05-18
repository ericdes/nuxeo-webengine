<div id="attached_files">
    <h4>Attached files:</h4>
    <#list Document.files.files as file>
      <#if (file_index == 0)><ul></#if>
      <@compress single_line=true>
      <li><a href="${This.urlPath}@@getfile?property=files:files/item[${file_index}]/file">${file.filename}</a>
        <#if (canWrite)>
        - <a href="${This.urlPath}@@deletefile?property=files:files/item[${file_index}]">Remove</a>
        </#if>
      </li>
      </@compress>
      <#if (!file_has_next)></ul></#if>
      <br/>
    </#list>

<#if base.canWrite>
    <br/>
    <form id="add_file" action="${This.urlPath}@@addfile" accept-charset="utf-8" method="POST" enctype="multipart/form-data">
        <label for="file_to_add">Add a new file</label>
        <input type="file" name="files:files" value="" id="file_to_add">
        <input type="submit" name="attach_file" value="Attach" id="attach_file">
    </form>
</#if>

</div>
