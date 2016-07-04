package nl.knaw.huygens.timbuctoo.model.vre.vres;

import nl.knaw.huygens.timbuctoo.model.vre.Vre;

import java.util.List;
import java.util.Map;

class CodeConfiguredVres extends ConfiguredVres {
  CodeConfiguredVres(List<Vre> vres, Map<String, Map<String, String>> keywordTypes) {
    super(vres, keywordTypes);
  }
}
