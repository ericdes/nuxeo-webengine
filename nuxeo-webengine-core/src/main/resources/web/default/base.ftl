<html>
<body>
<h1>${This.document.title}</h1>

<p>
<@block name="message"/>
</p>

<@block name="content"/>

<hr>
engine : ${env.engine} ${env.version}

</body>
</html>
