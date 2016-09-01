package nl.knaw.huygens.timbuctoo.server.databasemigration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.crud.changelistener.AddLabelChangeListener;
import nl.knaw.huygens.timbuctoo.crud.changelistener.CollectionHasEntityRelationChangeListener;
import nl.knaw.huygens.timbuctoo.crud.changelistener.FulltextIndexChangeListener;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.search.description.indexes.IndexDescriptionFactory;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jVertex;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypesOrDefault;

public class NorwegianNynorskToNorwegianDatabaseMigration extends AbstractVertexMigration {
  public static final Logger LOG = LoggerFactory.getLogger(NorwegianNynorskToNorwegianDatabaseMigration.class);

  private Vertex norwegianVertex;

  @Override
  public void beforeMigration(GraphWrapper graphManager) {
    final TinkerpopGraphManager tinkerpopGraphManager = (TinkerpopGraphManager) graphManager;
    final AddLabelChangeListener addLabelChangeListener = new AddLabelChangeListener();
    final CollectionHasEntityRelationChangeListener collectionHasEntityRelationChangeListener =
      new CollectionHasEntityRelationChangeListener(graphManager);
    final FulltextIndexChangeListener fulltextIndexChangeListener =
      new FulltextIndexChangeListener(tinkerpopGraphManager.getGraphDatabase(), new IndexDescriptionFactory());


    norwegianVertex = graphManager
      .getLatestState().V().has(T.label, LabelP.of("language")).has("language_name", "Norwegian").next();

    final List<String> types = new ArrayList<>(Arrays.asList(getEntityTypesOrDefault(norwegianVertex)));

    // Add wwlanguage to types
    types.add("wwlanguage");


    // Set the types and variation properties
    try {
      norwegianVertex.property("types", new ObjectMapper().writeValueAsString(types));
      norwegianVertex.property("variations", new ObjectMapper().writeValueAsString(types));
    } catch (JsonProcessingException e) {
      LOG.error("Norwegian migration failed: ", e.getMessage());
      throw new RuntimeException(e);
    }

    // Set the wwlanguage properties
    norwegianVertex.property("wwlanguage_name", "Norwegian");
    norwegianVertex.property("wwlanguage_core", true);

    // Invoke the change listeners
    addLabelChangeListener.onUpdate(Optional.empty(), norwegianVertex);
    collectionHasEntityRelationChangeListener.onUpdate(Optional.empty(), norwegianVertex);
    fulltextIndexChangeListener.onUpdate(Optional.empty(), norwegianVertex);


    // Verify succesful migration with theese log messages
    LOG.info("Copied 'Norwegian' with tim_id '{}' to WomenWriters.", norwegianVertex.property("tim_id").value());
    LOG.info("types: {}", norwegianVertex.property("types").value());
    LOG.info("variations: {}", norwegianVertex.property("variations").value());
    LOG.info("labels: {}", ((Neo4jVertex) norwegianVertex).labels());
    LOG.info("wwlanguage_name: {}", norwegianVertex.property("wwlanguage_name").value());
    LOG.info("wwlanguage_core: {}", norwegianVertex.property("wwlanguage_core").value());
    LOG.info("Edge to wwlanguages collection exists: {}", graphManager
      .getGraph().traversal().V(norwegianVertex.id())
      .in(Collection.HAS_ENTITY_RELATION_NAME)
      .in(Collection.HAS_ENTITY_NODE_RELATION_NAME)
      .has(Collection.COLLECTION_NAME_PROPERTY_NAME, "wwlanguages")
      .next().property(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME).value()
    );
  }

  @Override
  public void applyToVertex(Vertex vertex) throws IOException {
    final List<String> types = new ArrayList<>(Arrays.asList(getEntityTypesOrDefault(vertex)));
    final Iterator<Edge> hasWorkLanguage = vertex.edges(Direction.OUT, "hasWorkLanguage");


    // Loop through all the languages if the vertex is a wwdocument
    if (getIsLatest(vertex) && types.contains("wwdocument") && hasWorkLanguage.hasNext()) {
      hasWorkLanguage.forEachRemaining((edge) -> {
        final Boolean isAccepted = (Boolean) edge.property("wwrelation_accepted").isPresent() ?
          (Boolean) edge.property("wwrelation_accepted").value() : false;

        if (isAccepted && getIsLatest(edge)) {
          final Vertex currentLanguageVertex = edge.inVertex();
          final String currentLanguageName = currentLanguageVertex.property("wwlanguage_name").isPresent() ?
            (String) currentLanguageVertex.property("wwlanguage_name").value() : null;

          if (currentLanguageName != null && currentLanguageName.equals("Norwegian Nynorsk")) {
            addRelationToNorwegian(vertex, edge);
            edge.remove();
          }
        }
      });
    }
  }

  private boolean getIsLatest(Element element) {
    return element.property("isLatest").isPresent() ?
      (Boolean) element.property("isLatest").value() : false;
  }

  private void addRelationToNorwegian(Vertex vertex, Edge edgeToNynorsk) {
    final Edge newRelation = vertex.addEdge("hasWorkLanguage", norwegianVertex);
    edgeToNynorsk.properties().forEachRemaining(prop -> newRelation.property(prop.key(), prop.value()));
  }
}
