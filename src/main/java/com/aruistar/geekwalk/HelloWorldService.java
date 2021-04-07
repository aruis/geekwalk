package com.aruistar.geekwalk;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/hello")
public class HelloWorldService {

    @GET
    @Path("/")
    public Response index() {
        return this.doGet(null);
    }

    @GET
    @Path("/{name:.*}")
    public Response doGet(@PathParam("name") String name) {
        if (name == null || name.isEmpty()) {
            name = "World";
        }
        return Response.status(200).entity("Hello " + name).build();
    }
}
