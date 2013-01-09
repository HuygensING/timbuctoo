package nl.knaw.huygens.repository.storage.mongo;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.internal.object.BsonObjectGenerator;

import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.storage.generic.JsonViews;

public class MongoUtils {
  private static ObjectWriter dbWriter;
  static {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(Include.NON_DEFAULT);
    dbWriter = mapper.writerWithView(JsonViews.DBView.class);
  }


  public static String getVersioningCollectionName(Class<?> cls) {
    return getCollectionName(cls) + "-versions";
  }

  public static String getCollectionName(Class<?> cls) {
    return cls.getSimpleName().toLowerCase();
  }

  public static DBObject getObjectForDoc(Object doc) throws IOException {
    BsonObjectGenerator generator = new BsonObjectGenerator();
    dbWriter.writeValue(generator, doc);
    return generator.getDBObject();
  }
  

  public static <T extends Document> JacksonDBCollection<T, String> getCollection(DB db, Class<T> cls) {
    DBCollection col = db.getCollection(getCollectionName(cls));
    return JacksonDBCollection.wrap(col, cls, String.class, JsonViews.DBView.class);
  }

  public static <T extends Document> JacksonDBCollection<MongoChanges, String> getVersioningCollection(DB db, Class<T> cls) {
    DBCollection col = db.getCollection(getVersioningCollectionName(cls));
    return JacksonDBCollection.wrap(col, MongoChanges.class, String.class, JsonViews.DBView.class);
  }
}
