package nuxeo;

import java.io.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import org.nuxeo.ecm.core.rest.*;
import org.nuxeo.ecm.webengine.model.*;
import org.nuxeo.ecm.webengine.model.impl.*;
import org.nuxeo.ecm.webengine.exceptions.*;
import org.nuxeo.ecm.webengine.*;

@WebModule(name="default")
@Path("/")
@Produces(["text/html", "*/*"])
public class Main extends DefaultModule {

  @Path("admin")
  public Object getAdmin() {
    return ctx.newObject("Admin");
  }

  @Path("repository")
  public Object getRepository() {
    return new DocumentRoot(ctx, "/default-domain");
  }

  @GET
  public Object getIndex() {
    return new Template(ctx).fileName("index.ftl")
  }

  @GET
  @Path("help")
  public Object getHelp() {
    return new Template(ctx).fileName("help/help.ftl")
  }

  @GET
  @Path("about")
  public Object getAbout() {
    return new Template(ctx).fileName("help/about.ftl")
  }

  // the login service used to redirect to a wanted page after login / logout
  // which is done in the authentication filter
  @GET @POST
  @Path("login")
  public Response login() {
    return login("/");
  }
  @GET @POST
  @Path("login/{target:.*}")
  public Response login(@PathParam("target") String target) {
    if (target != null) {
      return redirect(target);
    } else {
      return Response.ok().noContent().build();
    }
  }

  // handle errors
  public Object getErrorView(WebApplicationException e) {
    if (e instanceof WebSecurityException) {
      return Response.status(401).entity(new Template(ctx, getFile("error/error_401.ftl"))).build();
    } else if (e instanceof WebResourceNotFoundException) {
      return Response.status(404).entity(new Template(ctx, getFile("error/error_404.ftl"))).build();
    } else {
      return super.getErrorView(e);
    }
  }
  
}

