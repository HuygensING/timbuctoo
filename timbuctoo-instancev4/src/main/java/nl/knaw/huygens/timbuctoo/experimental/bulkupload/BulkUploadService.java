package nl.knaw.huygens.timbuctoo.experimental.bulkupload;

import nl.knaw.huygens.timbuctoo.experimental.bulkupload.loaders.styleawarexlsxloader.StyleAwareXlsxLoader;
import nl.knaw.huygens.timbuctoo.experimental.bulkupload.parsingstatemachine.Importer;
import nl.knaw.huygens.timbuctoo.experimental.bulkupload.savers.TinkerpopSaver;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.Authorizer;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.io.InputStream;

public class BulkUploadService {

  private final TinkerpopGraphManager graphwrapper;
  private final Authorizer authorizer;

  public BulkUploadService(TinkerpopGraphManager graphwrapper/*, Authorizer authorizer*/) {
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

    Vertex vre = initVre(vreName);

    try (TinkerpopSaver saver = new TinkerpopSaver(graphwrapper, vre, 50_000)) {
      StyleAwareXlsxLoader loader = new StyleAwareXlsxLoader();
      return loader.loadData(wb, new Importer(saver));
    }
  }

  private Vertex initVre(String vreName) {
    try (Transaction tx = graphwrapper.getGraph().tx()) {
      graphwrapper.getVreRootNode().vertices(Direction.OUT, vreName).forEachRemaining(vre -> {
        vre.vertices(Direction.BOTH, "hasCollection").forEachRemaining(coll -> {
          coll.vertices(Direction.BOTH, "hasEntity").forEachRemaining(vertex -> {
            vertex.remove();
          });
          coll.remove();
        });
        vre.remove();
      });
      tx.commit();
    }
    Vertex vre = graphwrapper.getGraph().addVertex("name", vreName); //FIXME, make namespaced (username/VRE)
    graphwrapper.getVreRootNode().addEdge(vreName, vre);
    return vre;
  }
}
