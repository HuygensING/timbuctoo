package nl.knaw.huygens.timbuctoo.database.dto.dataset;

import nl.knaw.huygens.timbuctoo.model.properties.ReadableProperty;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;

import java.util.LinkedHashMap;

import static org.mockito.Mockito.mock;

public class CollectionStubs {

  public static Collection forEntitytypeName(String vrename, String entitytypename) {
    return new Collection(
      vrename + entitytypename,
      "",
      mock(ReadableProperty.class),
      new LinkedHashMap<>(),
      vrename + entitytypename + "s",
      mock(Vre.class),
      vrename + entitytypename,
      false,
      false
    );
  }

}
