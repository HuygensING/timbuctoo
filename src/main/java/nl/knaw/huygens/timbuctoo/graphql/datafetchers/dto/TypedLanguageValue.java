package nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto;

import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import org.immutables.value.Value;

@Value.Immutable
public interface TypedLanguageValue extends TypedValue {
  String getLanguage();

  static TypedLanguageValue create(String value, String type, String language, DataSet dataSet) {
    return ImmutableTypedLanguageValue.builder()
      .value(value)
      .type(type)
      .language(language)
      .dataSet(dataSet)
      .build();
  }
}
