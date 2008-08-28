package org.nuxeo.ecm.webengine.rest.servlet.resteasy.patch;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.resteasy.Failure;
import org.resteasy.PathParamIndex;
import org.resteasy.ValueInjector;
import org.resteasy.specimpl.ResponseImpl;
import org.resteasy.spi.HttpRequest;
import org.resteasy.spi.HttpResponse;
import org.resteasy.spi.MethodInjector;
import org.resteasy.spi.ResteasyProviderFactory;
import org.resteasy.util.HttpResponseCodes;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class MethodInjectorImpl extends AbstractInjector implements MethodInjector
{
   protected Method method;
   protected ValueInjector[] params;
   protected PathParamIndex index;
   protected ResteasyProviderFactory factory;

   public MethodInjectorImpl(Method method, PathParamIndex index, ResteasyProviderFactory factory)
   {
      this.method = method;
      this.index = index;
      this.factory = factory;
      params = new ValueInjector[method.getParameterTypes().length];
      for (int i = 0; i < method.getParameterTypes().length; i++)
      {
         Class type = method.getParameterTypes()[i];
         Type genericType = method.getGenericParameterTypes()[i];
         Annotation[] annotations = method.getParameterAnnotations()[i];
         params[i] = getParameterExtractor(index, type, genericType, annotations, method, factory);
      }
   }

   public Object[] injectArguments(HttpRequest input, HttpResponse response)
   {
      Object[] args = null;
      if (params != null && params.length > 0)
      {
         args = new Object[params.length];
         int i = 0;
         for (ValueInjector extractor : params)
         {
            args[i++] = extractor.inject(input, response);
         }
      }
      return args;
   }

   public Response invoke(HttpRequest request, HttpResponse httpResponse, Object resource) throws Failure
   {
      Object[] args = null;
      try
      {
         args = injectArguments(request, httpResponse);
      }
      catch (Exception e)
      {
         throw new Failure("Failed processing arguments of " + method.toString(), e, HttpResponseCodes.SC_BAD_REQUEST);
      }
      try
      {
         Object rtn = method.invoke(resource, args);
         if (method.getReturnType().equals(void.class))
         {
            ResponseImpl response = new ResponseImpl();
            if (request.getHttpMethod().toUpperCase().equals("DELETE") || request.getHttpMethod().toUpperCase().equals("POST"))
               response.setStatus(HttpResponseCodes.SC_NO_CONTENT);

         }
         if (method.getReturnType().equals(Response.class))
         {
            return (Response) rtn;
         }

         ResponseImpl response = new ResponseImpl();
         response.setEntity(rtn);
         if (rtn == null && (request.getHttpMethod().toUpperCase().equals("DELETE") || request.getHttpMethod().toUpperCase().equals("POST")))
            response.setStatus(HttpResponseCodes.SC_NO_CONTENT);
         else response.setStatus(HttpResponseCodes.SC_OK);
         return response;
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException("Failed processing of " + method.toString(), e);
      }
      catch (InvocationTargetException e)
      {
         Throwable cause = e.getCause();
         if (cause instanceof WebApplicationException)
         {
            WebApplicationException wae = (WebApplicationException) cause;
            if (wae.getResponse() != null)
            {
               cause.printStackTrace();
               return wae.getResponse();
            }
         }
         ExceptionMapper mapper = factory.createExceptionMapper(cause.getClass());
         if (mapper == null) throw new RuntimeException("Failed processing " + method.toString(), e.getCause());
         return mapper.toResponse(cause);
      }
      catch (IllegalArgumentException e)
      {
         String msg = "Bad arguments passed to " + method.toString() + "  (";
         boolean first = false;
         for (Object arg : args)
         {
            if (!first)
            {
               first = true;
            }
            else
            {
               msg += ",";
            }
            if (arg == null)
            {
               msg += " null";
               continue;
            }
            msg += " " + arg;
         }
         throw new RuntimeException(msg, e);
      }
   }

}
