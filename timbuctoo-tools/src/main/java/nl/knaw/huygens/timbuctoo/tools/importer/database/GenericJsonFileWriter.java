package nl.knaw.huygens.timbuctoo.tools.importer.database;

import java.io.File;
import java.io.IOException;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;

public class GenericJsonFileWriter extends GenericDataHandler {
  private final String testDataDir;
  private final TypeRegistry typeRegistry;

  public GenericJsonFileWriter(String testDataDir, TypeRegistry typeRegistry) {
    super();
    this.testDataDir = testDataDir;
    this.typeRegistry = typeRegistry;
  }

  @Override
  protected <T extends Entity> void save(Class<T> type, List<T> objects) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    //Make sure the type is added to the json.
    mapper.enableDefaultTyping(DefaultTyping.JAVA_LANG_OBJECT, As.PROPERTY);

    File file = new File(testDataDir + typeRegistry.getIName(type) + ".json");
    System.out.println("file: " + file.getAbsolutePath());

    mapper.writeValue(file, objects);
  }

}
