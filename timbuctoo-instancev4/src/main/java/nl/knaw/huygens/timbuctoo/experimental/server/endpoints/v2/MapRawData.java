package nl.knaw.huygens.timbuctoo.experimental.server.endpoints.v2;

import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.rdf.ImportPreparer;
import nl.knaw.huygens.timbuctoo.rdf.TripleImporter;
import nl.knaw.huygens.timbuctoo.rml.ExampleMapping;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/v2.1/mapRawData")
public class MapRawData {

  private final GraphWrapper graphWrapper;

  public MapRawData(GraphWrapper graphWrapper) {
    this.graphWrapper = graphWrapper;
  }

  @GET
  @Produces("text/plain")
  public Response mapRawData(@QueryParam("vre") String vreName) {
    final TripleImporter tripleImporter = new TripleImporter(graphWrapper, vreName);
    final ImportPreparer importPreparer = new ImportPreparer(graphWrapper);
    try (Transaction tx = graphWrapper.getGraph().tx()) {
      final GraphTraversal<Vertex, Vertex> vre = graphWrapper.getGraph().traversal().V()
        .hasLabel(Vre.DATABASE_LABEL)
        .has(Vre.VRE_NAME_PROPERTY_NAME, vreName);
      if (vre.hasNext()) {
        Vertex result = vre.next();
        result.vertices(Direction.BOTH, Vre.HAS_COLLECTION_RELATION_NAME).forEachRemaining(coll -> {
          coll.vertices(Direction.BOTH, Collection.HAS_ENTITY_NODE_RELATION_NAME).forEachRemaining(entityNode -> {
            entityNode.vertices(Direction.BOTH, Collection.HAS_ENTITY_RELATION_NAME).forEachRemaining(vertex -> {
              vertex.remove();
            });
            entityNode.remove();
          });
          coll.remove();
        });
        tx.commit();
      }
      importPreparer.setupVre(vreName);
      importPreparer.setUpAdminVre();

      ExampleMapping.executeEmExampleMapping(graphWrapper, vreName).forEach(tripleImporter::importTriple);
      tx.commit();
    }

    return Response.ok("").build();
  }

  //@GET
  //@Produces("text/plain")
  //public Response mapRawData2(@QueryParam("vre") String vre) {
  //  return Response.ok(
  //    ExampleMapping.executeEmExampleMapping(graphWrapper, vre).map(Triple::toString).collect(Collectors
  // .joining("\n"))
  //  ).build();
  //}
  //
}
