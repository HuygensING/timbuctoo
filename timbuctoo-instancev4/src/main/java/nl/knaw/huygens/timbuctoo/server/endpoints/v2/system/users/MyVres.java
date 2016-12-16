package nl.knaw.huygens.timbuctoo.server.endpoints.v2.system.users;

import com.fasterxml.jackson.databind.node.ObjectNode;
import nl.knaw.huygens.timbuctoo.core.TransactionEnforcer;
import nl.knaw.huygens.timbuctoo.core.TransactionStateAndResult;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.VreMetadata;
import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.security.LoggedInUserStore;
import nl.knaw.huygens.timbuctoo.security.User;
import nl.knaw.huygens.timbuctoo.server.UriHelper;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.VreImage;
import nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload.BulkUploadVre;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toMap;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;

@Path("/v2.1/system/users/me/vres")
public class MyVres {
  private final LoggedInUserStore loggedInUserStore;
  private final Authorizer authorizer;
  private final BulkUploadVre bulkUploadVre;
  private final TransactionEnforcer transactionEnforcer;
  private final UriHelper uriHelper;

  public MyVres(LoggedInUserStore loggedInUserStore, Authorizer authorizer, BulkUploadVre bulkUploadVre,
                TransactionEnforcer transactionEnforcer, UriHelper uriHelper) {
    this.loggedInUserStore = loggedInUserStore;
    this.authorizer = authorizer;
    this.bulkUploadVre = bulkUploadVre;
    this.transactionEnforcer = transactionEnforcer;
    this.uriHelper = uriHelper;
  }

  @GET
  @Produces(APPLICATION_JSON)
  public Response get(@HeaderParam("Authorization") String authorizationHeader) {
    Optional<User> user = loggedInUserStore.userFor(authorizationHeader);

    if (!user.isPresent()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    return transactionEnforcer.executeAndReturn(timbuctooActions -> {
      final Map<String, Map<String, ObjectNode>> result = timbuctooActions
        .loadVres().getVres().values().stream()
        .map(vre -> {
          boolean isAllowedToWrite;
          try {
            isAllowedToWrite = authorizer
              .authorizationFor(vre.getVreName(),
                user.get().getId())
              .isAllowedToWrite();
          } catch (AuthorizationUnavailableException e) {
            isAllowedToWrite = false;
          }
          boolean isPublished = vre.getPublishState().equals(Vre.PublishState.AVAILABLE);
          return new VreJson(vre.getVreName(), isAllowedToWrite,
            isPublished, vre.getPublishState(), vre.getMetadata());
        })
        .filter(x -> x.isMine() || x.isPublished())
        .collect(groupingBy(
          x -> x.isMine() ? "mine" : "public",
          mapping(x -> {
            if (x.isMine()) {
              return jsnO(
                "name", jsn(x.getVreName()),
                "label", jsn(x.getLabel()),
                "published", jsn(x.isPublished),
                "vreMetadata", x.getMetadata(),
                "publishState", jsn(x.getPublishState().toString()),
                "rmlUri", jsn(
                  bulkUploadVre.createUri(x.getVreName())
                               .toASCIIString())
              );
            } else {
              return jsnO(
                "name", jsn(x.getVreName()),
                "vreMetadata", x.getMetadata(),
                "label", jsn(x.getLabel())
              );
            }
          }, toMap(x -> x.get("name").asText(), x -> x))));

      return TransactionStateAndResult.commitAndReturn(Response.ok(result).build());
    });
  }

  private class VreJson {
    private final boolean isMine;
    private final boolean isPublished;
    private final Vre.PublishState publishState;
    private final String vreName;
    private final VreMetadata metadata;

    public VreJson(String vreName, boolean isMine, boolean isPublished, Vre.PublishState publishState,
                   VreMetadata metadata) {
      this.vreName = vreName;
      this.isMine = isMine;
      this.isPublished = isPublished;
      this.publishState = publishState;
      this.metadata = metadata;
    }

    public String getVreName() {
      return vreName;
    }

    public boolean isMine() {
      return isMine;
    }

    public boolean isPublished() {
      return isPublished;
    }

    private Vre.PublishState getPublishState() {
      return publishState;
    }

    private String getLabel() {
      return metadata.getLabel();
    }

    private ObjectNode getMetadata() {
      final URI imageUri = createImageUri(vreName, metadata);
      return jsnO(
        "colorCode", jsn(metadata.getColorCode()),
        "provenance", jsn(metadata.getProvenance()),
        "description", jsn(metadata.getDescription()),
        "image", jsn(imageUri == null ? null : imageUri.toString()),
        "uploadedFilename", jsn(metadata.getUploadedFilename())
      );
    }

    private URI createImageUri(String vreName, VreMetadata metadata) {
      if (metadata.getImageRev() == null) {
        return null;
      }
      return uriHelper.fromResourceUri(UriBuilder.fromResource(VreImage.class)
                                                 .resolveTemplate("vreName", vreName)
                                                 .resolveTemplate("rev", metadata.getImageRev())
                                                 .build());
    }
  }
}
