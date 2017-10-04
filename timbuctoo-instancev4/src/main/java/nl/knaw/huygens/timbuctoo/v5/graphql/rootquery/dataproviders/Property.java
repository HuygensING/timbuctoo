package nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders;

import org.immutables.value.Value;

@Value.Immutable
public interface Property {
  String getName();

  long getDensity();

  boolean getIsList();

  StringList getReferenceTypes();

  default StringList getReferenceTypes(int count, String cursor) {
    return getReferenceTypes();
  }

  StringList getValueTypes();

  default StringList getValueTypes(int count, String cursor) {
    return getValueTypes();
  }

}
