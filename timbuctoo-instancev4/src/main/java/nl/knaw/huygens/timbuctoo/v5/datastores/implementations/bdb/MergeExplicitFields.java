package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.ExplicitField;

import java.util.List;

public class MergeExplicitFields {
  public ExplicitField mergeExplicitFields(ExplicitField explicitField1, ExplicitField explicitField2) {
    ExplicitField mergedExplicitField = explicitField1;

    if (!explicitField1.getUri().equals(explicitField2.getUri())) {
      throw new IllegalArgumentException("Explicit field URIs do not match.");
    }

    if (explicitField1.isList() || explicitField2.isList()) {
      mergedExplicitField.setList(true);
    } else {
      mergedExplicitField.setList(false);
    }

    List<String> values = explicitField1.getValues();

    if (explicitField2.getValues() != null) {
      for (String value : explicitField2.getValues()) {
        if (!values.contains(value)) {
          values.add(value);
        }
      }
    }

    mergedExplicitField.setValues(values);

    List<String> references = explicitField1.getReferences();

    if (explicitField2.getReferences() != null) {
      for (String reference : explicitField2.getReferences()) {
        if (!references.contains(reference)) {
          references.add(reference);
        }
      }
    }

    mergedExplicitField.setReferences(references);

    return mergedExplicitField;
  }
}
