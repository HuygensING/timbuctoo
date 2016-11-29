package nl.knaw.huygens.timbuctoo.server.databasemigration;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static nl.knaw.huygens.timbuctoo.rdf.Database.RDF_SYNONYM_PROP;
import static nl.knaw.huygens.timbuctoo.rdf.Database.RDF_URI_PROP;

public class RelationTypeRdfUriMigration implements DatabaseMigration {
  private static final Logger LOG = LoggerFactory.getLogger(RelationTypeRdfUriMigration.class);
  private static final String TIMBUCTOO_NAMESPACE = "http://timbuctoo.huygens.knaw.nl/";

  @Override
  public void beforeMigration(GraphWrapper graphManager) {

  }

  @Override
  public void execute(GraphWrapper graphWrapper) throws IOException {
    final Graph graph = graphWrapper.getGraph();
    final Transaction transaction = graph.tx();

    if (!transaction.isOpen()) {
      transaction.open();
    }

    final String regularNameProp = "relationtype_regularName";
    final String inverseNameProp = "relationtype_inverseName";

    graph.traversal().V().has(regularNameProp).forEachRemaining(vertex -> {
      final String regularName = vertex.property(regularNameProp).isPresent() ?
        vertex.<String>property(regularNameProp).value() : "";

      final String inverseName = vertex.property(inverseNameProp).isPresent() ?
        vertex.<String>property(inverseNameProp).value() : "";

      final String rdfUri = TIMBUCTOO_NAMESPACE + regularName;
      final String[] rdfAlternatives = new String[]{ TIMBUCTOO_NAMESPACE + inverseName };

      LOG.info("setting rdfUri: \"{}\" and rdfAlternatives [{}] for relationType", rdfUri, rdfAlternatives);
      vertex.property(RDF_URI_PROP, rdfUri);
      vertex.property(RDF_SYNONYM_PROP, rdfAlternatives);

    });

    transaction.commit();
    transaction.close();
  }
}
