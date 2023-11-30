package nl.knaw.huygens.timbuctoo.datastores.implementations.bdb;

import nl.knaw.huygens.timbuctoo.hamcrest.CompositeMatcher;
import nl.knaw.huygens.timbuctoo.hamcrest.PropertyEqualityMatcher;
import nl.knaw.huygens.timbuctoo.datastores.schemastore.dto.ExplicitField;

import java.util.Set;

public class ExplicitFieldMatcher extends CompositeMatcher<ExplicitField> {
  private ExplicitFieldMatcher() {
  }

  public static ExplicitFieldMatcher explicitField() {
    return new ExplicitFieldMatcher();
  }

  public ExplicitFieldMatcher withUri(String uri) {
    this.addMatcher(new PropertyEqualityMatcher<>("uri", uri) {
      @Override
      protected String getItemValue(ExplicitField item) {
        return item.getUri();
      }
    });

    return this;
  }

  public ExplicitFieldMatcher withIsList(boolean isList) {
    this.addMatcher(new PropertyEqualityMatcher<>("isList", isList) {
      @Override
      protected Boolean getItemValue(ExplicitField item) {
        return item.isList();
      }
    });

    return this;
  }

  public ExplicitFieldMatcher withValues(Set<String> values) {
    this.addMatcher(new PropertyEqualityMatcher<>("values", values) {
      @Override
      protected Set<String> getItemValue(ExplicitField item) {
        return item.getValues();
      }
    });

    return this;
  }

  public ExplicitFieldMatcher withReferences(Set<String> references) {
    this.addMatcher(new PropertyEqualityMatcher<>("references", references) {
      @Override
      protected Set<String> getItemValue(ExplicitField item) {
        return item.getReferences();
      }
    });

    return this;
  }
}
