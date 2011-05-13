package org.jboss.resteasy.test.finegrain.resource;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.test.EmbeddedContainer;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.io.IOException;

import static org.jboss.resteasy.test.TestPortProvider.*;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ComplexPathParamTest
{
   private static Dispatcher dispatcher;

   @Path("/")
   public static class ExtensionResource
   {
      @GET
      @Path("/{1},{2}/{3}/blah{4}-{5}ttt")
      public String get(@PathParam("1") int one, @PathParam("2") int two, @PathParam("3") int three,
                        @PathParam("4") int four, @PathParam("5") int five)
      {
         Assert.assertEquals(one, 1);
         Assert.assertEquals(two, 2);
         Assert.assertEquals(three, 3);
         Assert.assertEquals(four, 4);
         Assert.assertEquals(five, 5);
         return "hello";
      }

   }

   @Path("/tricky")
   public static class TrickyResource
   {
      @GET
      @Path("{hello}")
      public String getHello(@PathParam("hello") int one)
      {
         Assert.assertEquals(one, 1);
         return "hello";
      }

      @GET
      @Path("{1},{2}")
      public String get2Groups(@PathParam("1") int one, @PathParam("2") int two)
      {
         Assert.assertEquals(1, one);
         Assert.assertEquals(2, two);
         return "2Groups";
      }

      @GET
      @Path("h{1}")
      public String getPrefixed(@PathParam("1") int one)
      {
         Assert.assertEquals(1, one);
         return "prefixed";
      }
   }

   @Path("/unlimited")
   public static class UnlimitedResource
   {
      @Path("{1}-{rest:.*}")
      @GET
      public String get(@PathParam("1") int one, @PathParam("rest") String rest)
      {
         Assert.assertEquals(1, one);
         Assert.assertEquals("on/and/on", rest);
         return "ok";
      }
   }

   public static class Sub1
   {
      @GET
      public String get()
      {
         return "sub1";
      }
   }

   public static class Sub2
   {
      @GET
      public String get()
      {
         return "sub2";
      }
   }

   @Path("/repository/workspaces")
   public static class Resteasy145
   {
      @Path("{service: x.*}")
      public Sub1 getService(@PathParam("service") String serviceName)
      {
         return new Sub1();
      }

      @Path("{path:.*}")
      public Sub2 getChild(@PathParam("path") String path)
      {
         return new Sub2();
      }

   }

   @BeforeClass
   public static void before() throws Exception
   {
   }

   @AfterClass
   public static void after() throws Exception
   {
   }


   private void _test(HttpClient client, String path, String body)
   {
      {
         GetMethod method = createGetMethod(path);
         try
         {
            int status = client.executeMethod(method);
            Assert.assertEquals(status, HttpResponseCodes.SC_OK);
            Assert.assertEquals(body, method.getResponseBodyAsString());
         }
         catch (IOException e)
         {
            throw new RuntimeException(e);
         }
      }

   }

   @Test
   public void testIt() throws Exception
   {
      dispatcher = EmbeddedContainer.start().getDispatcher();
      try
      {
         dispatcher.getRegistry().addPerRequestResource(ExtensionResource.class);
         dispatcher.getRegistry().addPerRequestResource(TrickyResource.class);
         dispatcher.getRegistry().addPerRequestResource(UnlimitedResource.class);
         dispatcher.getRegistry().addPerRequestResource(Resteasy145.class);
         HttpClient client = new HttpClient();
         _test(client, "/1,2/3/blah4-5ttt", "hello");
         _test(client, "/tricky/1,2", "2Groups");
         _test(client, "/tricky/h1", "prefixed");
         _test(client, "/tricky/1", "hello");
         _test(client, "/unlimited/1-on/and/on", "ok");
         _test(client, "/repository/workspaces/aaaaaaxvi/wdddd", "sub2");
      }
      finally
      {
         EmbeddedContainer.stop();
      }

   }

}