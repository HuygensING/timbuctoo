package nl.knaw.huygens.repository.tools.importer;

import java.util.List;

import nl.knaw.huygens.repository.config.BasicInjectionModule;
import nl.knaw.huygens.repository.config.Configuration;
import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.index.DocumentIndexer;
import nl.knaw.huygens.repository.index.IndexerFactory;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.DomainDocument;
import nl.knaw.huygens.repository.storage.StorageIterator;
import nl.knaw.huygens.repository.storage.VariationStorage;
import nl.knaw.huygens.repository.util.Progress;
import nl.knaw.huygens.repository.util.RepositoryException;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Currently this class is not used, because every object is indexed at the time it is stored in the database.
 * This class could be the base of a process for (re)indexing all or a part of the data. 
 */
public class SolrIndexer {

  public static void main(String[] args) throws Exception {
    Configuration config = new Configuration("config.xml");
    Injector injector = Guice.createInjector(new BasicInjectionModule(config));
    SolrIndexerRunner runner = injector.getInstance(SolrIndexerRunner.class);
    System.exit(runner.run());
  }

  public static class SolrIndexerRunner {
    private final Configuration config;
    private final IndexerFactory indices;
    private final VariationStorage storage;
    private final DocTypeRegistry docTypeRegistry;

    @Inject
    public SolrIndexerRunner(Configuration config, IndexerFactory indices, VariationStorage storage, DocTypeRegistry docTypeRegistry) {
      this.config = config;
      this.indices = indices;
      this.storage = storage;
      this.docTypeRegistry = docTypeRegistry;
    }

    @SuppressWarnings("unchecked")
    public int run() {
      int rv = 0;
      for (String doctype : config.getSettings("indexeddoctypes")) {
        Class<? extends Document> cls = docTypeRegistry.getTypeForIName(doctype);
        // Only DomainDocuments should be indexed.
        if (DomainDocument.class.isAssignableFrom(cls)) {
          try {
            indexAllDocuments((Class<DomainDocument>) cls);
          } catch (Exception e) {
            e.printStackTrace();
            rv = 1;
            break;
          }
        }
      }
      return rv;
    }

    private <T extends DomainDocument> void indexAllDocuments(Class<T> type) throws Exception {
      System.out.printf("%n=== Indexing documents of type '%s'%n", type.getSimpleName());

      DocumentIndexer<T> indexer = indices.indexerForType(type);
      indexer.removeAll();

      StorageIterator<T> list = storage.getAllByType(type);
      try {
        Progress progress = new Progress();
        while (list.hasNext()) {
          progress.step();
          T doc = list.next();
          List<T> allVariations = storage.getAllVariations(type, doc.getId());
          if (!doc.isDeleted()) {
            try {
              indexer.add(allVariations);
            } catch (RepositoryException e) {
              System.out.println("\nError while indexing publication " + doc.getId());
              throw e;
            }
          }
        }
        progress.done();
      } finally {
        list.close();
      }
      try {
        indexer.flush();
      } catch (Exception e) {
        System.err.println("Error committing changes: " + e.getMessage());
      }
      System.out.printf("%nIndexing done!%n");
    }
  }

}
