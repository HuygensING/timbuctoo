package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.CollectionBuilder;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.VreBuilder;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class ImportPreparer {
  private final GraphWrapper graphWrapper;

  public ImportPreparer(GraphWrapper graphWrapper) {
    this.graphWrapper = graphWrapper;
  }

  public void setUpAdminVre() {
    GraphTraversalSource traversalSource = graphWrapper.getGraph().traversal();
    final GraphTraversal<Vertex, Vertex> adminVreT = traversalSource.V()
                                                                    .has(T.label, LabelP.of(Vre.DATABASE_LABEL))
                                                                    .has(Vre.VRE_NAME_PROPERTY_NAME, "Admin");

    if (adminVreT.hasNext()) {
      final Vertex adminVreVertex = adminVreT.next();
      if (!traversalSource.V(adminVreVertex.id())
                          .out(Vre.HAS_COLLECTION_RELATION_NAME)
                          .where(__.has(Collection.COLLECTION_NAME_PROPERTY_NAME, "concepts"))
                          .hasNext()) {
        final Vertex conceptsVertex = graphWrapper.getGraph().addVertex(Collection.DATABASE_LABEL);
        final Vertex conceptsEntityNode = graphWrapper.getGraph().addVertex(Collection.COLLECTION_ENTITIES_LABEL);
        conceptsVertex.property(Collection.COLLECTION_NAME_PROPERTY_NAME, "concepts");
        conceptsVertex.property(Collection.ENTITY_TYPE_NAME_PROPERTY_NAME, "concept");
        conceptsVertex.property(Collection.COLLECTION_LABEL_PROPERTY_NAME, "concepts");
        conceptsVertex.property(Collection.IS_RELATION_COLLECTION_PROPERTY_NAME, false);
        conceptsVertex.property(Collection.COLLECTION_IS_UNKNOWN_PROPERTY_NAME, false);
        adminVreVertex.addEdge(Vre.HAS_COLLECTION_RELATION_NAME, conceptsVertex);
        conceptsVertex.addEdge(Collection.HAS_ENTITY_NODE_RELATION_NAME, conceptsEntityNode);
        graphWrapper.getGraph().tx().commit();
      }
    } else {
      Vre vre = VreBuilder.vre("Admin", "").withCollection("concepts").build();
      vre.save(graphWrapper.getGraph());
    }
  }

  public void setupVre(String vreName) {
    Vre vre = VreBuilder.vre(vreName, vreName)
                        .withCollection(vreName + "relations", CollectionBuilder::isRelationCollection)
                        .build();
    vre.save(graphWrapper.getGraph());
  }
}
