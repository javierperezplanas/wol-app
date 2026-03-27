package org.javi.wol.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.javi.wol.service.WolService; // Importa tu servicio

@Path("/api/wol") // <--- ESTA es la ruta que te falta en el proyecto
@Produces(MediaType.APPLICATION_JSON)
public class WolResource {

    @Inject
    WolService wolService; // Inyectamos el "motor" que ya tienes escrito

    @GET
    @Path("/status")
    public Response status() {
        WolService.PingResult ping = wolService.checkStatus();
        return Response.ok(ping).build();
    }

    @POST
    @Path("/wake/{mac}")
    public Response wake(@PathParam("mac") String mac) {
        try {
            wolService.sendMagicPacket(mac); // Llamamos al método del servicio
            return Response.ok("{\"status\":\"OK\"}").build();
        } catch (Exception e) {
            return Response.status(500).entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }
}