package nl.knaw.huygens.timbuctoo.experimental.bulkupload;

import nl.knaw.huygens.timbuctoo.experimental.bulkupload.loaders.styleawarexlsxloader.StyleAwareXlsxLoader;
import nl.knaw.huygens.timbuctoo.experimental.bulkupload.parsingstatemachine.Importer;
import nl.knaw.huygens.timbuctoo.experimental.bulkupload.savers.TinkerpopSaver;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Transaction;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BulkUploadService {

  private final Vres vres;
  private final GraphWrapper graphwrapper;
  private final Authorizer authorizer;

  public BulkUploadService(Vres vres, GraphWrapper graphwrapper/*, Authorizer authorizer*/) {
    this.vres = vres;
    this.graphwrapper = graphwrapper;
    this.authorizer = null;//authorizer;
  }

  //FIXME: add authorizer on admin
  //FIXME: allow linking to existing vertices (e.g. geboorteplaats in emmigrantunits)

  public String saveToDb(String vreName, InputStream wb/*, String userId*/)
    throws AuthorizationUnavailableException, AuthorizationException, InvalidExcelFileException {
    //
    //for (Collection collection : vre.getCollections().values()) {
    //  if (!authorizer.authorizationFor(collection, userId).isAllowedToWrite()) {
    //    throw new AuthorizationException(
    //      "You cannot use bulkupload because you are not allowed to edit " + collection.getCollectionName()
    //    );
    //  }
    //}

    Vre vre = vres.getVre(vreName);

    final Map<String, RelationDescription> descriptions = graphwrapper.getGraph().traversal()
      .V().has(T.label, LabelP.of("relationtype"))
      .toList()
      .stream()
      .map(RelationDescription::bothWays)
      .collect(
        HashMap::new,
        HashMap::putAll,
        HashMap::putAll
      );
    //FIXME: allow the excel sheet to specify more relationDescriptions

    dropAllVreVertices(vre);

    try (TinkerpopSaver saver = new TinkerpopSaver(graphwrapper, vre, descriptions, 50_000)) {
      StyleAwareXlsxLoader loader = new StyleAwareXlsxLoader();
      return loader.loadData(wb, new Importer(saver));
    }
  }

  private void dropAllVreVertices(Vre vre) {
    final Set<String> keys = vre.getCollections().keySet();
    final String[] entityTypeNames = keys.toArray(new String[keys.size()]);
    P<String> labels = LabelP.of(entityTypeNames[0]);
    for (int i = 1; i < entityTypeNames.length; i++) {
      labels = labels.or(LabelP.of(entityTypeNames[i]));
    }
    try (Transaction tx = graphwrapper.getGraph().tx()) {
      graphwrapper.getGraph().traversal().V().has(T.label, labels).drop().toList();
      tx.commit();
    }
  }
}
