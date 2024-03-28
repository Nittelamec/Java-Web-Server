package fr.epita.assistants.presentation.rest;


import fr.epita.assistants.presentation.rest.request.ReverseRequest;
import fr.epita.assistants.presentation.rest.response.HelloResponse;
import fr.epita.assistants.presentation.rest.response.ReverseResponse;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class Endpoints {
    @GET
    @Path("hello/{name}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response helloResponse(@PathParam(value = "name") String name) {
        if (name == null || name.isEmpty())
            return Response.status(Response.Status.BAD_REQUEST).build();
        return Response.ok(new HelloResponse("hello " + name)).build();
    }

    @POST
    @Path("reverse")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response reverseResponse(ReverseRequest request) {
        if (request == null || request.getContent() == null || request.getContent().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        String content = request.getContent();
        String reverse = String.valueOf(new StringBuilder(content).reverse());
        return Response.ok(new ReverseResponse(content, reverse)).build();
    }
}