package test.model.derivedrelationtest;

import com.google.common.collect.ImmutableList;
import nl.knaw.huygens.timbuctoo.model.DerivedRelationDescription;
import nl.knaw.huygens.timbuctoo.model.Language;

import java.util.List;

public class DRTLanguage extends Language {
  public static final String DERIVED_RELATION = "hasLangPerson";
  private static final DerivedRelationDescription PERSON_HAS_LANG = new DerivedRelationDescription(DERIVED_RELATION, "isLanguageOf", "hasPerson");
  private static final List<DerivedRelationDescription> DERIVED_RELATION_TYPES = ImmutableList.of(PERSON_HAS_LANG);

  @Override
  public List<DerivedRelationDescription> getDerivedRelationTypes() {
    return DERIVED_RELATION_TYPES;
  }

}
