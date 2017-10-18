package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.DatabaseWriteException;
import org.junit.Test;

import java.io.IOException;

public class BdbTypeNameStoreTest {

  @Test
  public void test() throws IOException {
    BdbTypeNameStore store = new BdbTypeNameStore(
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
      },
      "http://example.org"
    );
    store.addPrefix("_", "http://example.com/underscore#");
    store.addPrefix("foo", "http://example.com/foo#");

    String graphQlname = store.makeGraphQlname("http://example.com/underscore#test");
    if (!graphQlname.equals("__test")) {
      throw new RuntimeException(graphQlname + " != prefix__test");
    }
  }

}
