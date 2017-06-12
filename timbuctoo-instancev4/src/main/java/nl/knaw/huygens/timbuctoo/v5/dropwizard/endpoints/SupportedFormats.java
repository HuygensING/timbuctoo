package nl.knaw.huygens.timbuctoo.v5.dropwizard.endpoints;

import nl.knaw.huygens.timbuctoo.v5.dropwizard.SupportedExportFormats;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("v5/supported_formats")
public class SupportedFormats {
  private final SupportedExportFormats supportedExportFormats;

  public SupportedFormats(SupportedExportFormats supportedExportFormats) {

    this.supportedExportFormats = supportedExportFormats;
  }

  @GET
  @Path("export")
  @Produces("application/json")
  public Response getExportFormat() {
    return Response.ok(supportedExportFormats.getSupportedMimeTypes()).build();
  }
}
