//Script called when the object is not found
//Set a 404 error code and render the unknown.ftl template

import javax.servlet.http.HttpServletResponse

response = req.getResponse()
//response.setStatus(HttpServletResponse.SC_NO_CONTENT)
//response.setStatus(404, 'Page not found!')
//response.sendError(HttpServletResponse.SC_NOT_FOUND, 'Yeah')
response.setCharacterEncoding('utf-8')

response.setHeader('Error', 'Yes')


req.render('/default/unknown.ftl')