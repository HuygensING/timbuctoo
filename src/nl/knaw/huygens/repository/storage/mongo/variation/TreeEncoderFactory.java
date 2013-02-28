package nl.knaw.huygens.repository.storage.mongo.variation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DBEncoder;
import com.mongodb.DBEncoderFactory;
import com.mongodb.DefaultDBEncoder;

import org.mongojack.internal.stream.JacksonDBEncoder;

public class TreeEncoderFactory implements DBEncoderFactory {

  private ObjectMapper objectMapper;

  public TreeEncoderFactory(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public DBEncoder create() {
    return new JacksonDBEncoder(objectMapper, DefaultDBEncoder.FACTORY.create());
  }

  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

}
