package nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto;

import com.google.common.collect.Lists;
import org.hamcrest.collection.IsMapContaining;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ExplicitFieldTest {
  @Test
  public void convertToPredicateTransformsExplicitFieldValuesToPredicate() throws Exception {
    List<String> values = new ArrayList<>();
    values.add("value1");
    values.add("value2");
    values.add("value3");

    ExplicitField title = new ExplicitField("title", "", "", false,
      values, null, new ExplicitType("String", null));

    Predicate predicate = title.convertToPredicate();

    assertThat(predicate.getName(),is("title"));
    assertThat(predicate.getValueTypes(), IsMapContaining.hasEntry("value1", 0L));
    assertThat(predicate.getValueTypes(), IsMapContaining.hasEntry("value2", 0L));
    assertThat(predicate.getValueTypes(), IsMapContaining.hasEntry("value3", 0L));
  }

  @Test
  public void convertToPredicateTransformsExplicitFieldReferencesToPredicate() throws Exception {
    List<String> references = new ArrayList<>();
    references.add("reference1");
    references.add("reference2");
    references.add("reference3");

    ExplicitField title = new ExplicitField("title", "", "", false,
      null, references, new ExplicitType("String", null));

    Predicate predicate = title.convertToPredicate();

    assertThat(predicate.getName(),is("title"));
    assertThat(predicate.getReferenceTypes(), IsMapContaining.hasEntry("reference1", 0L));
    assertThat(predicate.getReferenceTypes(), IsMapContaining.hasEntry("reference2", 0L));
    assertThat(predicate.getReferenceTypes(), IsMapContaining.hasEntry("reference3", 0L));
  }
}