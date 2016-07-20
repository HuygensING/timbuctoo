package nl.knaw.huygens.timbuctoo.rdf;

import com.google.common.base.Stopwatch;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
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
  private Vres vres;

  public RdfImporter(GraphWrapper graphWrapper, String vreName, Vres vres) {
    this(graphWrapper, vreName, new TripleImporter(graphWrapper, vreName), new ImportPreparer(graphWrapper));
    this.vres = vres;
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
    LOG.info("Starting import...");

    model.getGraph().find(Triple.ANY).forEachRemaining(tripleImporter::importTriple);
    graphWrapper.getGraph().tx().commit();
    if (vres != null) {
      vres.reload();
    }
    LOG.info("Import took {}", stopwatch.stop());
  }

}
