package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.DatabaseWriteException;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataProvider;
import nl.knaw.huygens.timbuctoo.v5.dataset.EntityProcessor;
import nl.knaw.huygens.timbuctoo.v5.dataset.RdfProcessor;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.RdfProcessingFailedException;
import org.junit.Test;

import java.io.IOException;

public class BdbTypeNameStoreTest {

  @Test
  public void test() throws IOException {
    BdbTypeNameStore store = new BdbTypeNameStore(
      new DataProvider() {
        @Override
        public void subscribeToRdf(RdfProcessor processor) {
          try {
            processor.setPrefix("_", "http://example.com/underscore#");
            processor.setPrefix("foo", "http://example.com/foo#");
          } catch (RdfProcessingFailedException e) {
            throw new RuntimeException(e);
          }
        }

        @Override
        public void subscribeToEntities(EntityProcessor processor) {

        }
      },
      new DataStorage() {
        String value;
        @Override
        public String getValue() {
          return value;
        }

        @Override
        public void setValue(String newValue) throws DatabaseWriteException {
          value = newValue;
        }

        @Override
        public void close() throws Exception {
        }
      }
    );
    String graphQlname = store.makeGraphQlname("http://example.com/underscore#test");
    if (!graphQlname.equals("__test")) {
      throw new RuntimeException(graphQlname + " != prefix__test");
    }
  }

}
