package nl.knaw.huygens.repository.tools.importer.database;

import java.io.File;
import java.io.IOException;
import java.util.List;

import nl.knaw.huygens.repository.model.Document;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;

public class GenericJsonFileWriter extends GenericDataHandler {

  @Override
  protected <T extends Document> void save(Class<T> type, List<T> objects) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    //Make sure the type is added to the json.
    mapper.enableDefaultTyping(DefaultTyping.OBJECT_AND_NON_CONCRETE, As.PROPERTY);

    File file = new File("testdata/" + type.getSimpleName() + ".json");
    System.out.println("file: " + file.getAbsolutePath());

    mapper.writeValue(file, objects);
  }
}
