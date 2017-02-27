package nl.knaw.huygens.timbuctoo.server.endpoints.v2.domain;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.jersey.params.UUIDParam;
import nl.knaw.huygens.timbuctoo.core.NotFoundException;
import nl.knaw.huygens.timbuctoo.core.TransactionEnforcer;
import nl.knaw.huygens.timbuctoo.core.dto.ReadEntity;
import nl.knaw.huygens.timbuctoo.core.dto.RelationRef;
import nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.core.dto.property.TimProperty;
import nl.knaw.huygens.timbuctoo.crud.InvalidCollectionException;
import nl.knaw.huygens.timbuctoo.rdf.LinkTriple;
import nl.knaw.huygens.timbuctoo.rdf.LiteralTriple;
import nl.knaw.huygens.timbuctoo.rdf.conversion.TriplePropertyConverter;
import nl.knaw.huygens.timbuctoo.server.UriHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static nl.knaw.huygens.timbuctoo.core.TransactionStateAndResult.commitAndReturn;

@Path("/v2.1/domain/{collection}/{id}")
@Produces("application/n-triples")
public class SingleEntityNTriple {
  public static final Logger LOG = LoggerFactory.getLogger(SingleEntityNTriple.class);
  public static final String SAME_AS_PRED = "http://www.w3.org/2002/07/owl#sameAs";
  public static final String BASE_RDF_URI = "http://timbuctoo.huygens.knaw.nl/";
  private final TransactionEnforcer transactionEnforcer;
  private final UriHelper uriHelper;

  public SingleEntityNTriple(TransactionEnforcer transactionEnforcer, UriHelper uriHelper) {
    this.transactionEnforcer = transactionEnforcer;
    this.uriHelper = uriHelper;
  }

  public static URI makeUrl(String collectionName, UUID id) {
    return UriBuilder.fromResource(SingleEntityNTriple.class)
                     .buildFromMap(ImmutableMap.of(
                       "collection", collectionName,
                       "id", id
                     ));
  }

  @GET
  public Response get(@PathParam("collection") String collectionName,
                      @PathParam("id") UUIDParam id,
                      @QueryParam("rev") Integer rev
  ) {
    return transactionEnforcer.executeAndReturn(timbuctooActions -> {
      try {
        Collection collection = timbuctooActions.getCollectionMetadata(collectionName);
        ReadEntity entity = timbuctooActions.getEntity(collection, id.get(), rev);
        URI rdfUri = entity.getRdfUri();
        String rdfString = rdfUri == null ?
          uriHelper.fromResourceUri(makeUrl(collectionName, id.get())).toString() :
          rdfUri.toString();
        StringBuilder sb = new StringBuilder();
        addRdfProp(rdfString, sb, "id", entity.getId());
        entity.getRdfAlternatives().forEach(alt -> addRdfProp(rdfString, sb, SAME_AS_PRED, alt));
        TriplePropertyConverter converter = new TriplePropertyConverter(collection, rdfString);
        for (TimProperty<?> timProperty : entity.getProperties()) {
          try {
            timProperty.convert(converter).getRight().forEach(triple -> sb.append(triple.getStringValue()));
          } catch (IOException e) {
            LOG.error(
              "Could not convert property with name '{}' and value '{}'", timProperty.getName(), timProperty.getValue()
            );
          }
        }
        entity.getRelations().forEach(rel -> sb.append(
          new LinkTriple(rdfString, getRelationRdfUri(rel), getEntityRdfUri(rel)).getStringValue()
        ));


        return commitAndReturn(Response.ok(sb.toString()).build());
      } catch (InvalidCollectionException e) {
        return commitAndReturn(Response.status(BAD_REQUEST).entity(e.getMessage()).build());
      } catch (NotFoundException e) {
        return commitAndReturn(Response.status(NOT_FOUND).build());
      }
    });
  }

  private String getRelationRdfUri(RelationRef rel) {
    return StringUtils.isBlank(rel.getRelationRdfUri()) ?
      String.format("%srelationtypes/%s", BASE_RDF_URI, rel.getRelationType()) :
      rel.getEntityRdfUri();
  }

  private String getEntityRdfUri(RelationRef rel) {
    return StringUtils.isBlank(rel.getEntityRdfUri()) ?
      uriHelper.fromResourceUri(makeUrl(rel.getCollectionName(), UUID.fromString(rel.getEntityId()))).toString() :
      rel.getEntityRdfUri();
  }

  private void addRdfProp(String rdfString, StringBuilder sb, String propName, Object propValue) {
    sb.append(
      new LiteralTriple(rdfString, String.format("http://timbuctoo.huygens.knaw.nl/%s", propName), propValue)
        .getStringValue());
  }
}
