package nl.knaw.huygens.timbuctoo.crud;

import nl.knaw.huygens.timbuctoo.model.vre.vres.VresBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JsonCrudServiceTest {

  @Test
  public void createThrowsAnInvalidCollectionExceptionWhenTheCollectionIsUnknown() throws Exception {
    Assertions.assertThrows(InvalidCollectionException.class, () -> {
      JsonCrudService instance = createInstanceWithoutKnownCollections();

      instance.create("unknown_collection", null, null);
    });
  }

  @Test
  public void deleteThrowsAnInvalidCollectionExceptionWhenTheCollectionIsUnknown() throws Exception {
    Assertions.assertThrows(InvalidCollectionException.class, () -> {
      JsonCrudService instance = createInstanceWithoutKnownCollections();

      instance.delete("unknown_collection", null, null);
    });
  }

  @Test
  public void getThrowsAnInvalidCollectionExceptionWhenTheCollectionIsUnknown() throws Exception {
    Assertions.assertThrows(InvalidCollectionException.class, () -> {
      JsonCrudService instance = createInstanceWithoutKnownCollections();

      instance.get("unknown_collection", null);
    });
  }

  @Test
  public void replaceThrowsAnInvalidCollectionExceptionWhenTheCollectionIsUnknown() throws Exception {
    Assertions.assertThrows(InvalidCollectionException.class, () -> {
      JsonCrudService instance = createInstanceWithoutKnownCollections();

      instance.replace("unknown_collection", null, null, null);
    });
  }

  private JsonCrudService createInstanceWithoutKnownCollections() {
    return new JsonCrudService(new VresBuilder().build(), null, null, null);
  }
}
