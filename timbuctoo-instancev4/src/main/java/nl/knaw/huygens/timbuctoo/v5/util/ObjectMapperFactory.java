package nl.knaw.huygens.timbuctoo.v5.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

public class ObjectMapperFactory {

  private final ObjectMapper indentedJava8Mapper = new ObjectMapper()
      .configure(SerializationFeature.INDENT_OUTPUT, true)
      .registerModule(new Jdk8Module());

  public ObjectMapper getIndentedJava8Mapper() {
    return indentedJava8Mapper;
  }

}
