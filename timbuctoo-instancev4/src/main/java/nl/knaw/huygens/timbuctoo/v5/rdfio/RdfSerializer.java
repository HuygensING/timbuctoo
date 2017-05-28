package nl.knaw.huygens.timbuctoo.v5.rdfio;

import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;

import javax.ws.rs.core.MediaType;
import java.nio.charset.Charset;

public interface RdfSerializer extends AutoCloseable {

  MediaType getMediaType();

  Charset getCharset();

  void onPrefix(String prefix, String iri) throws LogStorageFailedException;

  void onRelation(String subject, String predicate, String object, String graph) throws LogStorageFailedException;

  void onValue(String subject, String predicate, String value, String valueType, String graph)
      throws LogStorageFailedException;

  void onLanguageTaggedString(String subject, String predicate, String value, String language, String graph)
      throws LogStorageFailedException;

  void close() throws LogStorageFailedException;
}
