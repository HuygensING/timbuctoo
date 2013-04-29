package nl.knaw.huygens.repository.importer;

import java.util.List;

import nl.knaw.huygens.repository.config.BasicInjectionModule;
import nl.knaw.huygens.repository.config.Configuration;
import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.index.DocumentIndexer;
import nl.knaw.huygens.repository.index.IndexerFactory;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.storage.Storage;
import nl.knaw.huygens.repository.storage.StorageIterator;
import nl.knaw.huygens.repository.util.Progress;
import nl.knaw.huygens.repository.util.RepositoryException;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class SolrIndexer {

  public static void main(String[] args) {
    Injector injector = Guice.createInjector(new BasicInjectionModule("config.xml"));
    SolrIndexerRunner runner = injector.getInstance(SolrIndexerRunner.class);
    System.exit(runner.run());
  }

  public static class SolrIndexerRunner {
    private final Configuration config;
    private final IndexerFactory indices;
    private final Storage storage;
    private final DocTypeRegistry docTypeRegistry;

    @Inject
    public SolrIndexerRunner(Configuration config, IndexerFactory indices, Storage storage, DocTypeRegistry docTypeRegistry) {
      this.config = config;
      this.indices = indices;
      this.storage = storage;
      this.docTypeRegistry = docTypeRegistry;
    }

    public int run() {
      int rv = 0;
      for (String doctype : config.getSettings("indexeddoctypes")) {
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
      System.out.printf("%n=== Indexing documents of type '%s'%n", type.getSimpleName());

      DocumentIndexer<T> indexer = indices.getIndexForType(type);
      StorageIterator<T> list;
      try {
        // FIXME: this should just fetch a list of IDs that we can use later.
        list = storage.getAllByType(type);
      } catch (Exception ex) {
        ex.printStackTrace();
        return;
      }

      Progress progress = new Progress();
      try {
        while (list.hasNext()) {
          progress.step();
          T mainDoc = list.next();
          List<T> allVariations = storage.getAllVariations(type, mainDoc.getId());
          if (mainDoc.isDeleted()) {
            continue;
          }
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
      progress.done();
      System.out.println("\nIndexing done!");
    }
  }

}
