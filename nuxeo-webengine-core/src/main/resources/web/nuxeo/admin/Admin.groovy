package nuxeo.admin;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import org.nuxeo.ecm.webengine.model.*;
import org.nuxeo.ecm.webengine.model.impl.*;
import org.nuxeo.ecm.webengine.*;

@WebObject(type="Admin", facets=["domain"], guard="user=Administrator")
@Produces(["text/html", "*/*"])
public class Admin extends DefaultObject {

  @Path("users")  
  public Object getUserManagement() {
    return newService("users");
  }

  @GET
  public Object getIndex() {
    return getTemplate("index.ftl");
  }

}

