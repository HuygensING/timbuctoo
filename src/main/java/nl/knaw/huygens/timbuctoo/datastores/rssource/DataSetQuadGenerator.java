package nl.knaw.huygens.timbuctoo.datastores.rssource;

import com.google.common.base.Charsets;

import javax.ws.rs.core.MediaType;
import java.nio.charset.Charset;

public class DataSetQuadGenerator {
  private String escapeCharacters(String value) {
    return value
      .replace("\\", "\\\\")
      .replace("\n", "\\n")
      .replace("\r", "\\r")
      .replace("\"", "\\\"");
  }

  public MediaType getMediaType() {
    return new MediaType("application", "n-quads");
  }

  public Charset getCharset() {
    return Charsets.UTF_8;
  }

  public String onRelation(String subject, String predicate, String object, String graph) {
    return "<" + subject + "> <" + predicate + "> <" + object + "> " +
        (graph != null && !graph.isBlank() ? "<" + graph + "> " : "") + ".\n";
  }

  public String onValue(String subject, String predicate, String value, String valueType, String graph) {
    value = escapeCharacters(value);
    return "<" + subject + "> <" + predicate + "> \"" + value + "\"^^<" + valueType + "> " +
        (graph != null && !graph.isBlank() ? "<" + graph + "> " : "") + ".\n";
  }

  public String onLanguageTaggedString(String subject, String predicate, String value, String language, String graph) {
    value = escapeCharacters(value);
    return "<" + subject + "> <" + predicate + "> \"" + value + "\"@" + language + " " +
        (graph != null && !graph.isBlank() ? "<" + graph + "> " : "") + ".\n";
  }
}
