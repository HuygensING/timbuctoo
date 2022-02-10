package nl.knaw.huygens.timbuctoo.v5.datastores.rssource;

import javax.ws.rs.core.MediaType;

public class ChangesQuadGenerator extends DataSetQuadGenerator {
  public String delRelation(String subject, String predicate, String object, String graph) {
    return "-" + super.onRelation(subject, predicate, object, graph);
  }

  public String delValue(String subject, String predicate, String value, String valueType, String graph) {
    return "-" + super.onValue(subject, predicate, value, valueType, graph);
  }

  public String delLanguageTaggedString(String subject, String predicate, String value, String language, String graph) {
    return "-" + super.onLanguageTaggedString(subject, predicate, value, language, graph);
  }

  @Override
  public MediaType getMediaType() {
    return new MediaType("application", "vnd.timbuctoo-rdf.nquads_unified_diff");
  }

  @Override
  public String onRelation(String subject, String predicate, String object, String graph) {
    return "+" + super.onRelation(subject, predicate, object, graph);
  }

  @Override
  public String onValue(String subject, String predicate, String value, String valueType, String graph) {
    return "+" + super.onValue(subject, predicate, value, valueType, graph);
  }

  @Override
  public String onLanguageTaggedString(String subject, String predicate, String value, String language, String graph) {
    return "+" + super.onLanguageTaggedString(subject, predicate, value, language, graph);
  }
}
