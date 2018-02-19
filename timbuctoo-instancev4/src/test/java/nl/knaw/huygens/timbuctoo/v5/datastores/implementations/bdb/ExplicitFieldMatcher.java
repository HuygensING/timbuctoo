package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.ExplicitField;

import java.util.List;

public class ExplicitFieldMatcher extends CompositeMatcher<ExplicitField> {
  private ExplicitFieldMatcher() {
  }

  public static ExplicitFieldMatcher explicitField() {
    return new ExplicitFieldMatcher();
  }


  public ExplicitFieldMatcher withUri(String uri) {
    this.addMatcher(new PropertyEqualityMatcher<ExplicitField, String>("uri", uri) {
      @Override
      protected String getItemValue(ExplicitField item) {
        return item.getUri();
      }
    });

    return this;
  }

  public ExplicitFieldMatcher withIsList(boolean isList) {
    this.addMatcher(new PropertyEqualityMatcher<ExplicitField, Boolean>("isList", isList) {
      @Override
      protected Boolean getItemValue(ExplicitField item) {
        return item.isList();
      }
    });

    return this;
  }

  public ExplicitFieldMatcher withValues(List<String> values) {
    this.addMatcher(new PropertyEqualityMatcher<ExplicitField, List<String>>("values", values) {
      @Override
      protected List<String> getItemValue(ExplicitField item) {
        return item.getValues();
      }
    });

    return this;
  }

  public ExplicitFieldMatcher withReferences(List<String> references) {
    this.addMatcher(new PropertyEqualityMatcher<ExplicitField, List<String>>("references", references) {
      @Override
      protected List<String> getItemValue(ExplicitField item) {
        return item.getReferences();
      }
    });

    return this;
  }
}
