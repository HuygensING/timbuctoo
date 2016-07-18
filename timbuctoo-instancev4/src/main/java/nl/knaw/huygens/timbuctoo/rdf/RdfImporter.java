package nl.knaw.huygens.timbuctoo.rdf;

import com.google.common.base.Stopwatch;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RdfImporter {
  public static final Logger LOG = LoggerFactory.getLogger(RdfImporter.class);
  private final GraphWrapper graphWrapper;
  private final String vreName;
  private final TripleImporter tripleImporter;
  private final ImportPreparer importPreparer;

  public RdfImporter(GraphWrapper graphWrapper, String vreName) {
    this(graphWrapper, vreName, new TripleImporter(graphWrapper, vreName), new ImportPreparer(graphWrapper));
  }

  RdfImporter(GraphWrapper graphWrapper, String vreName, TripleImporter tripleImporter, ImportPreparer importPreparer) {
    this.graphWrapper = graphWrapper;
    this.vreName = vreName;
    this.tripleImporter = tripleImporter;
    this.importPreparer = importPreparer;
  }

  public void importRdf(Model model) {
    final Stopwatch stopwatch = Stopwatch.createStarted();
    importPreparer.setupVre(vreName);
    importPreparer.setUpAdminVre();

    model.getGraph().find(Triple.ANY).forEachRemaining(
      // TODO: each new triple should be committed
      tripleImporter::importTriple
    );
    graphWrapper.getGraph().tx().commit();

    LOG.info("Import took {}", stopwatch.stop());
  }

}
