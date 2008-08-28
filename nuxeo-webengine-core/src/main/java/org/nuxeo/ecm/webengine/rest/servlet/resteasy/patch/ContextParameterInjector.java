package org.nuxeo.ecm.webengine.rest.servlet.resteasy.patch;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWorkers;

import org.nuxeo.ecm.webengine.rest.WebContext2;
import org.nuxeo.ecm.webengine.servlet.WebConst;
import org.resteasy.ValueInjector;
import org.resteasy.specimpl.MessageBodyWorkersImpl;
import org.resteasy.specimpl.RequestImpl;
import org.resteasy.spi.HttpRequest;
import org.resteasy.spi.HttpResponse;
import org.resteasy.spi.ResteasyProviderFactory;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ContextParameterInjector implements ValueInjector
{
   private Class type;
   private ResteasyProviderFactory factory;

   public ContextParameterInjector(Class type, ResteasyProviderFactory factory)
   {
      this.type = type;
      this.factory = factory;
   }

   public Object inject(HttpRequest request, HttpResponse response)
   {
      if (type.equals(HttpHeaders.class)) return request.getHttpHeaders();
      if (type.equals(UriInfo.class)) return request.getUri();
      if (type.equals(Request.class)) return new RequestImpl(request.getHttpHeaders(), request.getHttpMethod());
      if (type.equals(WebContext2.class)) return request;
      if (type.equals(HttpRequest.class)) return request;
      if (type.equals(MessageBodyWorkers.class)) return new MessageBodyWorkersImpl();
      else return factory.getContextData(type);
   }

   private class GenericDelegatingProxy implements InvocationHandler
   {
      public Object invoke(Object o, Method method, Object[] objects) throws Throwable
      {
         try
         {
            Object delegate = factory.getContextData(type);
            if (delegate == null)
               throw new RuntimeException("Unable to inject contextual data of type: " + type.getName());
            return method.invoke(delegate, objects);
         }
         catch (IllegalAccessException e)
         {
            throw new RuntimeException(e);
         }
         catch (IllegalArgumentException e)
         {
            throw new RuntimeException(e);
         }
         catch (InvocationTargetException e)
         {
            throw e.getCause();
         }
      }
   }

   public Object inject()
   {
      if (type.equals(MessageBodyWorkers.class)) return new MessageBodyWorkersImpl();

      if (!type.isInterface()) throw new RuntimeException("Illegal to inject a non-interface type into a singleton");

      Class[] intfs = {type};


      return Proxy.newProxyInstance(type.getClassLoader(), intfs, new GenericDelegatingProxy());
   }
}
