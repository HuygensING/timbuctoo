package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.crud.ChangeListener;
import nl.knaw.huygens.timbuctoo.crud.EdgeManipulator;
import nl.knaw.huygens.timbuctoo.crud.EntityFetcher;
import nl.knaw.huygens.timbuctoo.database.dto.EntityRelation;
import nl.knaw.huygens.timbuctoo.database.dto.ImmutableEntityRelation;
import nl.knaw.huygens.timbuctoo.database.dto.ImmutableRelationCreateDescription;
import nl.knaw.huygens.timbuctoo.database.dto.RelationCreateDescription;
import nl.knaw.huygens.timbuctoo.database.dto.RelationType;
import nl.knaw.huygens.timbuctoo.database.exceptions.ObjectSuddenlyDisappearedException;
import nl.knaw.huygens.timbuctoo.database.exceptions.RelationNotPossibleException;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.codahale.metrics.MetricRegistry.name;
import static nl.knaw.huygens.timbuctoo.logging.Logmarkers.databaseInvariant;
import static nl.knaw.huygens.timbuctoo.model.properties.converters.Converters.arrayToEncodedArray;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static nl.knaw.huygens.timbuctoo.util.StreamIterator.stream;
import static org.slf4j.LoggerFactory.getLogger;

public class DataAccess {

  private static final Logger LOG = getLogger(DataAccess.class);
  private final GraphWrapper graphwrapper;
  private final EntityFetcher entityFetcher;
  private final Authorizer authorizer;
  private final ChangeListener listener;

  public DataAccess(GraphWrapper graphwrapper, EntityFetcher entityFetcher, Authorizer authorizer,
                    ChangeListener listener) {
    this.graphwrapper = graphwrapper;
    this.entityFetcher = entityFetcher;
    this.authorizer = authorizer;
    this.listener = listener;
  }

  public DataAccessMethods start() {
    return new DataAccessMethods(graphwrapper, authorizer);
  }


  public static class DataAccessMethods implements AutoCloseable {
    private final Transaction transaction;
    private final Authorizer authorizer;
    private final GraphTraversalSource traversal;
    private Optional<Boolean> isSuccess = Optional.empty();

    private DataAccessMethods(GraphWrapper graphWrapper, Authorizer authorizer) {
      this.transaction = graphWrapper.getGraph().tx();
      this.authorizer = authorizer;
      if (!transaction.isOpen()) {
        transaction.open();
      }
      traversal = graphWrapper.getGraph().traversal();
    }

    public void success() {
      isSuccess = Optional.of(true);
    }

    public void rollback() {
      isSuccess = Optional.of(false);
    }

    @Override
    public void close() {
      if (isSuccess.isPresent()) {
        if (isSuccess.get()) {
          transaction.commit();
        } else {
          transaction.rollback();
        }
        transaction.close();
      } else {
        transaction.rollback();
        transaction.close();
        LOG.error("Transaction was not closed, rolling back. Please add an explicit rollback so that we know this " +
          "was not a missing success()");
      }
    }

    public RelationCreateDescription getCurrentRelationIfExists(UUID sourceId, UUID typeId, UUID targetId,
                                                                Collection coll) throws RelationNotPossibleException,
        IOException {
      List<RelationType> descs = getRelationDescription(traversal, typeId);

      if (descs.size() == 0) {
        throw new RelationNotPossibleException("Relation type " + typeId + " does not exist");
      }

      Vertex sourceV = getEntityByFullIteration(traversal, sourceId)
        .orElseThrow(() -> new RelationNotPossibleException("source is not present"));
      Vertex targetV = getEntityByFullIteration(traversal, targetId)
        .orElseThrow(() -> new RelationNotPossibleException("target is not present"));
      //check if the relation already exists
      final Optional<EntityRelation> existingEdge = stream(sourceV.edges(Direction.BOTH))
        .filter(e ->
          (e.inVertex().id().equals(targetV.id()) || e.outVertex().id().equals(targetV.id())) &&
            getRequiredProp(e, "typeId", "").equals(typeId.toString())
        )
        //sort by rev (ascending)
        .sorted((o1, o2) -> getRequiredProp(o1, "rev", -1).compareTo(getRequiredProp(o2, "rev", -1)))
        //get last element, i.e. with the highest rev, i.e. the most recent
        .reduce((o1, o2) -> o2)
        .map(edge -> makeEntityRelation(edge, coll));

      Collection sourceCollection = getCollection(coll.getVre(), sourceV)
        .orElseThrow(() -> new RelationNotPossibleException("Source vertex is not part of the VRE of " +
          coll.getCollectionName()));
      Collection targetCollection = getCollection(coll.getVre(), targetV)
        .orElseThrow(() -> new RelationNotPossibleException("Target vertex is not part of the VRE of " +
          coll.getCollectionName()));

      for (RelationType desc : descs) {
        if (desc.isValid(sourceCollection, targetCollection)) {
          return ImmutableRelationCreateDescription.builder()
                                                   .existingRelation(existingEdge)
                                                   .collection(coll)
                                                   .sourceId(getRequiredProp(sourceV, "tim_id", ""))
                                                   .targetId(getRequiredProp(targetV, "tim_id", ""))
                                                   .description(desc)
                                                   .build();
        }
      }
      throw new RelationNotPossibleException("You can't have a " + descs.get(0).getName() + " from " +
        sourceCollection.getEntityTypeName() + " to " +
        targetCollection.getEntityTypeName() + " or vice versa");
    }

    public void updateRelation(EntityRelation existingEdge, String userId, boolean accepted, long time) throws
        AuthorizationUnavailableException, AuthorizationException {

      checkIfAllowedToWrite(authorizer, userId, existingEdge.getCollection());
      final Edge origEdge = getExpectedEdge(traversal, existingEdge.getTimId().toString());
      final Edge newEdge = EdgeManipulator.duplicateEdge(origEdge);
      newEdge.property(existingEdge.getCollection().getEntityTypeName() + "_accepted", accepted);
      newEdge.property("rev", existingEdge.getRevision() + 1);
      setModified(newEdge, userId, time);
    }

    public EntityRelation createRelation(RelationCreateDescription relationMetadata, String userId,
                                         boolean accepted, long time) throws
        AuthorizationException, AuthorizationUnavailableException {

      checkIfAllowedToWrite(authorizer, userId, relationMetadata.getCollection());

      UUID id = UUID.randomUUID();
      Edge edge = getExpectedVertex(traversal, relationMetadata.getSourceId()).addEdge(
        relationMetadata.getDescription().getDbName(),
        getExpectedVertex(traversal, relationMetadata.getTargetId()),
        relationMetadata.getCollection().getEntityTypeName() + "_accepted", accepted,
        "types", jsnA(
          jsn(relationMetadata.getCollection().getEntityTypeName()),
          jsn(relationMetadata.getCollection().getAbstractType())
        ).toString(),
        "typeId", relationMetadata.getDescription().getTimId(),
        "tim_id", id.toString(),
        "isLatest", true,
        "rev", 1
      );
      setCreated(edge, userId, time);
      return makeEntityRelation(edge, relationMetadata.getCollection());

    }

    /*******************************************************************************************************************
     * Support methods:
     */
    private Optional<Collection> getCollection(Vre vre, Element sourceV) {
      String ownType = vre.getOwnType(getEntityTypes(sourceV));
      if (ownType == null) {
        return Optional.empty();
      }
      return Optional.of(vre.getCollectionForTypeName(ownType));
    }

    private void setCreated(Element element, String userId, long time) {
      final String value = jsnO(
        "timeStamp", jsn(time),
        "userId", jsn(userId)
      ).toString();
      element.property("created", value);
      element.property("modified", value);
    }

    private void setModified(Element element, String userId, long time) {
      final String value = jsnO(
        "timeStamp", jsn(time),
        "userId", jsn(userId)
      ).toString();
      element.property("modified", value);
    }

    private static UUID asUuid(String input, Element source) {
      try {
        return UUID.fromString(input);
      } catch (IllegalArgumentException e) {
        LOG.error(databaseInvariant, "wrongly formatted UUID as tim_id: " + input + " on " +
          source.id());
        return UUID.fromString("00000000-0000-0000-0000-000000000000");
      }
    }

    private static EntityRelation makeEntityRelation(Edge edge, Collection collection) {
      final String acceptedPropName = collection.getEntityTypeName() + "_accepted";

      return ImmutableEntityRelation.builder()
                                    .isAccepted(getRequiredProp(edge, acceptedPropName, false))
                                    .timId(asUuid(getRequiredProp(edge, "tim_id", ""), edge))
                                    .revision(getRequiredProp(edge, "rev", -1))
                                    .collection(collection)
                                    .build();
    }

    @SuppressWarnings("unchecked")
    public static <V> V getRequiredProp(final Element element, final String key, V valueOnException) {
      try {
        Iterator<? extends Property<Object>> revProp = element.properties(key);
        if (revProp.hasNext()) {
          Object value = revProp.next().value();
          return (V) valueOnException.getClass().cast(value);
        } else {
          LOG.error(databaseInvariant, "Value is missing for property " + key + " on element with id " + element.id());
          return valueOnException;
        }
      } catch (RuntimeException e) {
        LOG.error(databaseInvariant, "Something went wrong while getting the property " + key + " from the element " +
          "with id " + (element != null ? element.id() : "<NULL>") + ": " + e.getMessage());
        return valueOnException;
      }
    }

    private static Optional<Vertex> getEntityByFullIteration(GraphTraversalSource traversal, UUID id) {
      return getFirst(traversal
        .V()
        .has("tim_id", id.toString())
        .not(__.has("deleted", true))
        .has("isLatest", true));
    }

    private static <T> Optional<T> getFirst(Traversal<?, T> traversal) {
      if (traversal.hasNext()) {
        return Optional.of(traversal.next());
      } else {
        return Optional.empty();
      }
    }

    private static Vertex getExpectedVertex(GraphTraversalSource traversal, String timId) {
      GraphTraversal<Vertex, Vertex> vertex = traversal.V().has("tim_id", timId);
      if (vertex.hasNext()) {
        return vertex.next();
      } else {
        throw new ObjectSuddenlyDisappearedException("The code assumes that the vertex with id " + timId + " is " +
          "available, but it isn't!");
      }
    }

    private static Edge getExpectedEdge(GraphTraversalSource traversal, String timId) {
      GraphTraversal<Edge, Edge> edge = traversal.E().has("tim_id", timId);
      if (edge.hasNext()) {
        return edge.next();
      } else {
        throw new ObjectSuddenlyDisappearedException("The code assumes that the edge with id " + timId + " is " +
          "available, but it isn't!");
      }
    }

    public static String[] getEntityTypes(Element element) {
      try {
        String typesProp = getRequiredProp(element, "types", "");
        if (typesProp.equals("[]")) {
          LOG.error(databaseInvariant, "Entitytypes not presen on vertex with ID " + element.id());
          return new String[0];
        } else {
          return arrayToEncodedArray.tinkerpopToJava(typesProp, String[].class);
        }
      } catch (IOException e) {
        LOG.error(databaseInvariant, "Could not parse entitytypes property on vertex with ID " + element.id());
        return new String[0];
      }
    }

    private static List<RelationType> getRelationDescription(GraphTraversalSource traversal, UUID typeId) {
      final ArrayList<RelationType> result = new ArrayList<>();
      getFirst(traversal
        .V()
        //.has(T.label, LabelP.of("relationtype"))
        .has("tim_id", typeId.toString())
      )
        .map(RelationType::bothWays)
        .ifPresent(descs ->
          descs.values().forEach(result::add));
      return result;
    }

    private static void checkIfAllowedToWrite(Authorizer authorizer, String userId, Collection collection) throws
        AuthorizationException, AuthorizationUnavailableException {
      if (!authorizer.authorizationFor(collection, userId).isAllowedToWrite()) {
        throw AuthorizationException.notAllowedToCreate(collection.getCollectionName());
      }
    }

  }
}
