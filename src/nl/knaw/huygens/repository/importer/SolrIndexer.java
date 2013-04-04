package nl.knaw.huygens.repository.importer;

import java.util.List;

import nl.knaw.huygens.repository.Configuration;
import nl.knaw.huygens.repository.BasicInjectionModule;
import nl.knaw.huygens.repository.index.DocumentIndexer;
import nl.knaw.huygens.repository.index.IndexFactory;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.util.DocumentTypeRegister;
import nl.knaw.huygens.repository.storage.Storage;
import nl.knaw.huygens.repository.storage.StorageIterator;
import nl.knaw.huygens.repository.util.RepositoryException;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class SolrIndexer {

  public static class SolrIndexerRunner {
    private final Storage storage;
    private final IndexFactory indices;
    private final Configuration conf;
    private final DocumentTypeRegister docTypeRegistry;

    @Inject
    public SolrIndexerRunner(Configuration conf, IndexFactory indices, Storage storage, DocumentTypeRegister docTypeRegistry) {
      this.conf = conf;
      this.indices = indices;
      this.storage = storage;
      this.docTypeRegistry = docTypeRegistry;
    }

    public int run() {
      String[] doctypes = conf.getSetting("indexeddoctypes", "").split(",");

      int rv = 0;
      for (String doctype : doctypes) {
        Class<? extends Document> cls = docTypeRegistry.getClassFromTypeString(doctype);
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
      return rv;
    }

    private <T extends Document> void indexAllDocuments(Class<T> type) throws Exception {
      DocumentIndexer<T> indexer = indices.getIndexForType(type);
      StorageIterator<T> list;
      try {
        // FIXME: this should just fetch a list of IDs that we can use later.
        list = storage.getAllByType(type);
      } catch (Exception ex) {
        ex.printStackTrace();
        return;
      }
      int count = list.size(), i = 0;
      System.out.printf("Start indexing %d documents:\n", count);
      try {
        while (list.hasNext()) {
          T mainDoc = list.next();
          List<T> allVariations = storage.getAllVariations(type, mainDoc.getId());
          System.out.print((i % 100 == 99) ? ".\n" : ".");
          i++;
          if (mainDoc.isDeleted()) {
            continue;
          }
          //System.out.println("id: " + mainDoc.getId());
          try {
            indexer.add(allVariations);
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

  public static void main(String[] args) {
    Injector injector = Guice.createInjector(new BasicInjectionModule("config.xml"));
    SolrIndexerRunner runner = injector.getInstance(SolrIndexerRunner.class);
    System.exit(runner.run());
  }
}
