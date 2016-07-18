package nl.knaw.huygens.timbuctoo.rdf;

import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;

public class RdfImporter {
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
    importPreparer.setupVre(vreName);
    importPreparer.setUpAdminVre();

    model.getGraph().find(Triple.ANY).forEachRemaining(
      // TODO: each new triple should be committed
      tripleImporter::importTriple
    );
  }

}
