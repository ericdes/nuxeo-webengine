<@extends src="base.ftl">
<@block name="header"><h1><a href="${appPath}">Error</a></h1></@block>
<@block name="content">

<h1>404 - Resource Not Found</h1>

<p>
The page you have requested doesn't exists.
Click <a href="${This.path}/create/${This.nextSegment}">here</a> to create
a new Wiki Page named <i>${This.nextSegment}</i>.
</p>

</@block>
</@extends>
