package nl.knaw.huygens.timbuctoo.v5.dataset;

import java.util.List;

public interface PredicateHandler {
  void onRelation(String uri, List<String> types);

  void onValue(String value, String dataType);

  void onLanguageTaggedString(String value, String language);
}
