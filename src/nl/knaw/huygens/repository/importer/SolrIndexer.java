package nl.knaw.huygens.repository.importer;

import nl.knaw.huygens.repository.index.DocumentIndexer;
import nl.knaw.huygens.repository.index.IndexFactory;
import nl.knaw.huygens.repository.index.LocalSolrServer;
import nl.knaw.huygens.repository.index.ModelIterator;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.pubsub.Hub;
import nl.knaw.huygens.repository.storage.Storage;
import nl.knaw.huygens.repository.storage.StorageConfiguration;
import nl.knaw.huygens.repository.storage.StorageFactory;
import nl.knaw.huygens.repository.storage.StorageIterator;
import nl.knaw.huygens.repository.util.Configuration;
import nl.knaw.huygens.repository.util.Paths;
import nl.knaw.huygens.repository.util.RepositoryException;

import org.apache.commons.configuration.ConfigurationException;

public class SolrIndexer {
  private static Storage storage;
  private static IndexFactory indices;

  private static Configuration conf;
  static {
    try {
      conf = new Configuration("config.xml");
    } catch (ConfigurationException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) {
    storage = StorageFactory.getInstance(new StorageConfiguration(conf));

    String solrPath = Paths.pathInUserHome(conf.getSetting("paths.solr", "solr"));
    String[] doctypes = conf.getSetting("indexeddoctypes", "").split(",");
    LocalSolrServer solrServer = new LocalSolrServer(solrPath, doctypes);
    indices = new IndexFactory(new ModelIterator(), solrServer, new Hub());

    int rv = 0;
    for (String doctype : doctypes) {
      Class<? extends Document> cls = Document.getSubclassByString(doctype);
      if (cls == null) {
        System.err.println("Error: couldn't find class for configured doctype " + doctype + "! Are you sure your models are complete?");
      } else {
        try {
          indexAllDocuments(cls);
        } catch (Exception ex) {
          ex.printStackTrace();
          rv = 1;
          break;
        }
      }
    }
    System.exit(rv);
  }

  private static <T extends Document> void indexAllDocuments(Class<T> cls) throws Exception {
    DocumentIndexer<T> indexer = indices.getIndexForType(cls);
    StorageIterator<T> list;
    try {
      list = storage.getAllByType(cls);
    } catch (Exception ex) {
      ex.printStackTrace();
      return;
    }
    int count = list.size(), i = 0;
    System.out.printf("Start indexing %d documents:\n", count);
    try {
      while (list.hasNext()) {
        T mainDoc = list.next();
        mainDoc.fetchAll(storage);
        System.out.print((i % 100 == 99) ? ".\n" : ".");
        i++;
        if (mainDoc.isDeleted()) {
          continue;
        }
        //System.out.println("id: " + mainDoc.getId());
        try {
          indexer.add(mainDoc);
        } catch (RepositoryException e) {
          System.out.println("\nError while indexing publication " + mainDoc.getId());
          throw new Exception(e);
        }
      }
    } finally {
      list.close();
    }
    try {
      indexer.flush();
    } catch (Exception ex) {
      System.err.println("Error committing changes:" + ex.toString());
      ex.printStackTrace();
    }
    System.out.println("\nIndexing done!");
  }
}
