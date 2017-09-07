package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import com.fasterxml.jackson.core.JsonProcessingException;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.v5.dataset.RdfProcessor;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import org.eclipse.rdf4j.rio.RDFHandler;

public class InitialStoreUpdater implements RdfProcessor {
  private final BdbTripleStore tripleStore;
  private final BdbTypeNameStore typeNameStore;
  private int currentversion = -1;

  public InitialStoreUpdater(BdbTripleStore tripleStore, BdbTypeNameStore typeNameStore) {
    this.tripleStore = tripleStore;
    this.typeNameStore = typeNameStore;
  }

  @Override
  public void setPrefix(String prefix, String iri) throws RdfProcessingFailedException {
    typeNameStore.addPrefix(prefix, iri);

  }

  @Override
  public void addRelation(String subject, String predicate, String object, String graph)
    throws RdfProcessingFailedException {
  }

  @Override
  public void addValue(String subject, String predicate, String value, String dataType, String graph)
    throws RdfProcessingFailedException {
  }

  @Override
  public void addLanguageTaggedString(String subject, String predicate, String value, String language, String graph)
    throws RdfProcessingFailedException {
  }

  @Override
  public void delRelation(String subject, String predicate, String object, String graph)
    throws RdfProcessingFailedException {
  }

  @Override
  public void delValue(String subject, String predicate, String value, String valueType, String graph)
    throws RdfProcessingFailedException {
  }

  @Override
  public void delLanguageTaggedString(String subject, String predicate, String value, String language, String graph)
    throws RdfProcessingFailedException {
  }

  @Override
  public void start(int index) throws RdfProcessingFailedException {
    currentversion = index;
  }

  @Override
  public int getCurrentVersion() {
    return currentversion;
  }

  @Override
  public void commit() throws RdfProcessingFailedException {
    try {
      typeNameStore.commit();
    } catch (JsonProcessingException | DatabaseWriteException e) {
      throw new RdfProcessingFailedException(e);
    }
  }
}
