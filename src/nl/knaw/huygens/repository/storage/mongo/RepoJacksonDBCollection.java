package nl.knaw.huygens.repository.storage.mongo;

import java.util.Map;

import org.mongojack.JacksonDBCollection;
import org.mongojack.internal.MongoJacksonMapperModule;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DBCollection;


public class RepoJacksonDBCollection<T, K> extends JacksonDBCollection<T, K> {
  private static final ObjectMapper DEFAULT_OBJECT_MAPPER = MongoJacksonMapperModule.configure(new ObjectMapper());
  
  protected RepoJacksonDBCollection(DBCollection dbCollection, JavaType type, JavaType keyType, ObjectMapper objectMapper, Class<?> view, Map<Feature, Boolean> features) {
    super(dbCollection, type, keyType, objectMapper, view, features);
  }
  
  public static <T> RepoJacksonDBCollection<T, String> wrap(DBCollection dbCollection, JavaType type, Class<?> view) {
    return new RepoJacksonDBCollection<T, String>(dbCollection, type, DEFAULT_OBJECT_MAPPER.constructType(String.class), DEFAULT_OBJECT_MAPPER, view, null);
  }
}
