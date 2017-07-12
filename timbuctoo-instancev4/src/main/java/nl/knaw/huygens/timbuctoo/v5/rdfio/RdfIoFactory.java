package nl.knaw.huygens.timbuctoo.v5.rdfio;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.OutputStream;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface RdfIoFactory {
  RdfParser makeRdfParser();

  RdfSerializer makeRdfSerializer(OutputStream output);
}
