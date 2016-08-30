package nl.knaw.huygens.timbuctoo.experimental.womenwriters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.crud.EntityFetcher;
import nl.knaw.huygens.timbuctoo.crud.InvalidCollectionException;
import nl.knaw.huygens.timbuctoo.crud.NotFoundException;
import nl.knaw.huygens.timbuctoo.crud.UrlGenerator;
import nl.knaw.huygens.timbuctoo.logging.Logmarkers;
import nl.knaw.huygens.timbuctoo.model.properties.ReadableProperty;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.security.AuthenticationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.User;
import nl.knaw.huygens.timbuctoo.security.UserStore;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.groupingBy;
import static nl.knaw.huygens.timbuctoo.logging.Logmarkers.databaseInvariant;
import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypes;
import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypesOrDefault;
import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getProp;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static nl.knaw.huygens.timbuctoo.util.StreamIterator.stream;
import static nl.knaw.huygens.timbuctoo.util.Tuple.tuple;

public class WomenWritersJsonCrudService {

  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(WomenWritersJsonCrudService.class);

  private final GraphWrapper graphwrapper;
  private final Vres mappings;
  private final UrlGenerator relationUrlFor;
  private final JsonNodeFactory nodeFactory;
  private final UserStore userStore;
  private EntityFetcher entityFetcher;

  public WomenWritersJsonCrudService(GraphWrapper graphwrapper, Vres mappings,
                                     UserStore userStore,
                                     UrlGenerator relationUrlFor,
                                     EntityFetcher entityFetcher) {
    this.graphwrapper = graphwrapper;
    this.mappings = mappings;
    this.relationUrlFor = relationUrlFor;
    this.userStore = userStore;
    this.entityFetcher = entityFetcher;
    nodeFactory = JsonNodeFactory.instance;
  }


  public JsonNode get(String collectionName, UUID id) throws InvalidCollectionException, NotFoundException {
    return get(collectionName, id, null);
  }

  public JsonNode get(String collectionName, UUID id, Integer rev)
    throws InvalidCollectionException, NotFoundException {

    final Collection collection = mappings.getCollection(collectionName)
                                          .orElseThrow(() -> new InvalidCollectionException(collectionName));

    if (collection.isRelationCollection()) {
      return getRelation(id, rev, collection);
    } else {
      return getVertex(id, rev, collection);
    }
  }

  private JsonNode getRelation(UUID id, Integer rev, Collection collection) throws NotFoundException {
    return jsnO("message", jsn("Getting a wwrelation is not yet supported"));
  }

  private JsonNode getVertex(UUID id, Integer rev, Collection collection) throws NotFoundException {
    final Map<String, ReadableProperty> mapping = collection.getReadableProperties();
    final String entityTypeName = collection.getEntityTypeName();
    final GraphTraversalSource traversalSource = graphwrapper.getGraph().traversal();

    final ObjectNode result = JsonNodeFactory.instance.objectNode();

    result.set("@type", nodeFactory.textNode(entityTypeName));
    result.set("_id", nodeFactory.textNode(id.toString()));

    Vertex entityTs = null;
    try {
      entityTs = entityFetcher.getEntity(traversalSource, id, rev,  collection.getCollectionName()).next();
    } catch (NoSuchElementException e) {
      throw new NotFoundException();
    }
    GraphTraversal<Vertex, Vertex> entityT = traversalSource.V(entityTs.id());

    if (!entityT.asAdmin().clone().hasNext()) {
      throw new NotFoundException();
    }

    GraphTraversal[] propertyGetters = mapping
      .entrySet().stream()
      //append error handling and resuling to the traversal
      .map(prop -> prop.getValue().traversal().sideEffect(x ->
        x.get()
         .onSuccess(node -> result.set(prop.getKey(), node))
         .onFailure(e -> {
           if (e.getCause() instanceof IOException) {
             LOG.error(
               databaseInvariant,
               "Property '" + prop.getKey() + "' is not encoded correctly",
               e.getCause()
             );
           } else {
             LOG.error("Something went wrong while reading the property '" + prop.getKey() + "'.", e.getCause());
           }
         })
      ))
      .toArray(GraphTraversal[]::new);

    entityT.asAdmin().clone().union(propertyGetters).forEachRemaining(x -> {
      //Force side effects to happen
    });
    Vertex entity = entityT.asAdmin().clone().next();

    String entityTypesStr = getProp(entity, "types", String.class).orElse("[]");
    if (!entityTypesStr.contains("\"" + collection.getEntityTypeName() + "\"")) {
      throw new NotFoundException();
    }

    Tuple<ObjectNode, Long> relations = getRelations(entity, traversalSource, collection);
    result.set("@relationCount", nodeFactory.numberNode(relations.getRight()));
    result.set("@relations", relations.getLeft());

    result.set(
      "^rev", nodeFactory.numberNode(
        getProp(entity, "rev", Integer.class)
          .orElse(rev == null ? -1 : rev)
      )
    );
    getModification(entity, "modified").ifPresent(val -> result.set("^modified", val));
    getModification(entity, "created").ifPresent(val -> result.set("^created", val));

    result.set("@variationRefs", getVariationRefs(entity, id, entityTypeName));
    result.set("@authorLanguages", jsnA(getLanguagesForAuthor(traversalSource, collection.getVre(), entity).stream()));
    result.set("^deleted", nodeFactory.booleanNode(getProp(entity, "deleted", Boolean.class).orElse(false)));

    result.set("^pid",nodeFactory.textNode(getProp(entity, "pid", String.class).orElse(null)));

    return result;
  }

  private Tuple<ObjectNode, Long> getRelations(Vertex entity, GraphTraversalSource traversalSource,
                                               Collection collection) {
    final ObjectMapper mapper = new ObjectMapper();

    final long[] relationCount = new long[1];

    GraphTraversal<Vertex, ObjectNode> realRelations = getRealRelations(entity, traversalSource, collection);

    Map<String, List<ObjectNode>> relations = stream(realRelations)
      .filter(x -> x != null)
      .peek(x -> relationCount[0]++)
      .collect(groupingBy(jsn -> jsn.get("relationType").asText()));

    return new Tuple<>(mapper.valueToTree(relations), relationCount[0]);
  }


  private GraphTraversal<Vertex, ObjectNode> getRealRelations(Vertex entity, GraphTraversalSource traversalSource,
                                                              Collection collection) {
    final Vre vre = collection.getVre();
    final Vre admin = mappings.getVre("Admin");

    Object[] relationTypes = traversalSource.V().has(T.label, LabelP.of("relationtype")).id().toList().toArray();

    return collection.getVre().getCollections().values().stream()
      .filter(Collection::isRelationCollection)
      .findAny()
      .map(Collection::getEntityTypeName)
      .map(ownRelationType -> traversalSource.V(entity.id())
        .union(
          __.outE()
            .as("edge")
            .label().as("label")
            .select("edge"),
          __.inE()
            .as("edge")
            .label().as("edgeLabel")
            .V(relationTypes)
            .has("relationtype_regularName", __.where(P.eq("edgeLabel")))
            .properties("relationtype_inverseName").value()
            .as("label")
            .select("edge")
        )
        .where(
          //FIXME move to strategy
          __.has("isLatest", true)
            .not(__.has("deleted", true))
            .not(__.hasLabel("VERSION_OF"))
            //The old timbuctoo showed relations from all VRE's. Changing that behaviour caused breakage in the
            //frontend and exposed errors in the database that
            //.has("types", new P<>((val, def) -> val.contains("\"" + ownRelationType + "\""), ""))
            // FIXME: string concatenating methods like this should be delegated to a configuration clas
            .not(__.has(ownRelationType + "_accepted", false))
        )
        .otherV().as("vertex")
        .select("edge", "vertex", "label")
        .map(r -> {
          try {
            Map<String, Object> val = r.get();
            final Edge edge = (Edge) val.get("edge");
            final Vertex vertex = (Vertex) val.get("vertex");
            final String label = (String) val.get("label");

            String targetEntityType = getOwnEntityType(collection, vertex);
            Collection targetCollection = vre.getCollectionForTypeName(targetEntityType);
            if (targetEntityType == null) {
              //this means that the edge is of this VRE, but the Vertex it points to is of another VRE
              //In that case we use the admin vre
              targetEntityType = admin.getOwnType(getEntityTypesOrDefault(vertex));
              targetCollection = admin.getCollectionForTypeName(targetEntityType);
            }

            String displayName =
              getDisplayname(traversalSource, vertex, targetCollection)
                .orElse("<No displayname found>");
            String uuid = getProp(vertex, "tim_id", String.class).orElse("");

            String gender = getProp(vertex, "wwperson_gender", String.class).orElse(null);
            if (gender != null) {
              gender = gender.replaceAll("\"", "");
            }

            List<JsonNode> authors = getAuthorsForPublication(traversalSource, vre, vertex);

            URI relatedEntityUri =
              relationUrlFor.apply(targetCollection.getCollectionName(), UUID.fromString(uuid), null);
            return jsnO(
              tuple("id", jsn(uuid)),
              tuple("path", jsn(relatedEntityUri.toString())),
              tuple("relationType", jsn(label)),
              tuple("type", jsn(targetEntityType)),
              tuple("accepted", jsn(getProp(edge, "accepted", Boolean.class).orElse(true))),
              tuple("relationId",
                getProp(edge, "tim_id", String.class).map(x -> (JsonNode) jsn(x)).orElse(jsn())),
              tuple("rev", jsn(getProp(edge, "rev", Integer.class).orElse(1))),
              tuple("displayName", jsn(displayName)),
              tuple("gender", jsn(gender)),
              tuple("authors", jsnA(authors.stream()))
            );
          } catch (Exception e) {
            LOG.error(databaseInvariant, "Something went wrong while formatting the entity", e);
            return null;
          }
        })
      )
      .orElse(EmptyGraph.instance().traversal().V().map(x->jsnO()));
  }

  private Set<JsonNode> getLanguagesForAuthor(GraphTraversalSource traversalSource, Vre vre, Vertex vertex) {
    Set<JsonNode> languages = new HashSet<>();

    final Iterator<Edge> isCreatorOf = vertex.edges(Direction.IN, "isCreatedBy");

    while (isCreatorOf.hasNext()) {
      final Edge next = isCreatorOf.next();
      final Boolean creatorOfIsAccepted = (Boolean) next.property("wwrelation_accepted").value();
      final Boolean creatorOfIsLatest = (Boolean)  next.property("isLatest").value();
      if (creatorOfIsAccepted && creatorOfIsLatest) {
        final Vertex publication = next.outVertex();
        final Iterator<Edge> hasWorkLanguage = publication.edges(Direction.OUT, "hasWorkLanguage");
        while (hasWorkLanguage.hasNext()) {
          final Edge nextLanguage = hasWorkLanguage.next();
          final Boolean languageIsAccepted = (Boolean) nextLanguage.property("wwrelation_accepted").value();
          final Boolean languageIsLatest = (Boolean) nextLanguage.property("isLatest").value();
          if (languageIsAccepted && languageIsLatest) {
            final Vertex languageVertex = nextLanguage.inVertex();
            final String language = getProp(languageVertex, "wwlanguage_name", String.class).orElse(null);
            if (language != null) {
              languages.add(jsn(language));
            }
          }
        }
      }
    }
    return languages;
  }

  private List<JsonNode> getAuthorsForPublication(GraphTraversalSource traversalSource, Vre vre, Vertex vertex) {
    List<JsonNode> authors = new ArrayList<>();

    final Iterator<Edge> isCreatedBy = vertex.edges(Direction.OUT, "isCreatedBy");

    while (isCreatedBy.hasNext()) {
      final Edge next = isCreatedBy.next();
      final Boolean isAccepted = (Boolean) next.property("wwrelation_accepted").value();
      final Object isLatest = next.property("isLatest").value();
      if (isAccepted && (Boolean) isLatest) {
        final Vertex author = next.inVertex();
        final Collection personCollection = vre.getCollectionForTypeName("wwperson");
        final String authorName = getDisplayname(traversalSource, author, personCollection).orElse(null);
        String authorGender = getProp(author, "wwperson_gender", String.class).orElse(null);
        if (authorGender != null) {
          authorGender = authorGender.replaceAll("\"", "");
        }
        if (authorName != null) {
          authors.add(jsnO(
            tuple("displayName", jsn(authorName)),
            tuple("gender", jsn(authorGender))
          ));
        }
      }
    }
    return authors;
  }

  private Optional<String> getDisplayname(GraphTraversalSource traversalSource, Vertex vertex,
                                          Collection targetCollection) {
    ReadableProperty displayNameProperty = targetCollection.getDisplayName();
    if (displayNameProperty != null) {
      GraphTraversal<Vertex, Try<JsonNode>> displayNameGetter = traversalSource.V(vertex.id()).union(
        targetCollection.getDisplayName().traversal()
      );
      if (displayNameGetter.hasNext()) {
        Try<JsonNode> traversalResult = displayNameGetter.next();
        if (!traversalResult.isSuccess()) {
          LOG.error(databaseInvariant, "Retrieving displayname failed", traversalResult.getCause());
        } else {
          if (traversalResult.get() == null) {
            LOG.error(databaseInvariant, "Displayname was null");
          } else {
            if (!traversalResult.get().isTextual()) {
              LOG.error(databaseInvariant, "Displayname was not a string but " + traversalResult.get().toString());
            } else {
              return Optional.of(traversalResult.get().asText());
            }
          }
        }
      } else {
        LOG.error(databaseInvariant, "Displayname traversal resulted in no results: " + displayNameGetter);
      }
    } else {
      LOG.warn("No displayname configured for " + targetCollection.getEntityTypeName());
    }
    return Optional.empty();
  }

  /* returns the entitytype for the current collection's vre or else the type of the current collection */
  private String getOwnEntityType(Collection collection, Element vertex) throws IOException {
    final Vre vre = collection.getVre();
    return getEntityTypes(vertex)
      .map(x -> x.map(vre::getOwnType))
      .orElse(Try.success(collection.getEntityTypeName()))
      .get(); //throws IOException on failure
  }

  private ArrayNode getVariationRefs(Vertex entity, UUID id, String entityTypeName) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      ArrayNode variationRefs = nodeFactory.arrayNode();
      JsonNode types = mapper.readTree((String) entity.value("types"));
      if (!types.isArray()) {
        throw new IOException("types should be a JSON encoded Array");
      }
      for (int i = 0; i < types.size(); i++) {
        ObjectNode ref = nodeFactory.objectNode();
        ref.set("id", nodeFactory.textNode(id.toString()));
        ref.set("type", nodeFactory.textNode(types.get(i).asText()));
        variationRefs.add(ref);
      }
      return variationRefs;
    } catch (Exception e) {
      //When something goes wrong we log the error and return a functional representation
      LOG.error(databaseInvariant, "Error while generating variation refs", e);
      return jsnA(
        jsnO(
          "id", jsn(id.toString()),
          "type", jsn(entityTypeName)
        )
      );
    }
  }

  private Optional<ObjectNode> getModification(Vertex entity, String propertyName) {
    ObjectMapper mapper = new ObjectMapper();
    return getProp(entity, propertyName, String.class)
      .flatMap(content -> {
        try {
          return Optional.of(mapper.readTree(content));
        } catch (IOException e) {
          return Optional.empty();
        }
      })
      .flatMap(parsed -> parsed instanceof ObjectNode ? Optional.of((ObjectNode) parsed) : Optional.empty())
      .map(modifiedObj -> {
        try {
          userStore.userForId(modifiedObj.get("userId").asText(""))
                   .map(User::getDisplayName)
                   .ifPresent(userName -> modifiedObj.set("username", nodeFactory.textNode(userName)));
        } catch (AuthenticationUnavailableException e) {
          LOG.error(Logmarkers.serviceUnavailable, "could not get user for modifiedObj", e);
          modifiedObj.set("username", nodeFactory.nullNode());
        }
        return modifiedObj;
      });
  }


}
