package nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb;

import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.ExplicitField;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MergeExplicitFieldsTest {
  @Test(expected = IllegalArgumentException.class)
  public void mergeThrowsExceptionIfUrisDontMatch() throws Exception {
    ExplicitField explicitField1 = new ExplicitField("test:test", false, null, null);
    ExplicitField explicitField2 = new ExplicitField("test:test2", false, null, null);

    ExplicitField mergedExplicitField = explicitField1.mergeWith(explicitField2);
  }

  @Test
  public void mergeMaintainsIsListProperty() throws Exception {
    ExplicitField explicitField1 = new ExplicitField("test:test", false, null, null);
    ExplicitField explicitField2 = new ExplicitField("test:test", true, null, null);

    ExplicitField mergedField = explicitField1.mergeWith(explicitField2);

    assertThat(mergedField.isList(), is(true));
  }

  @Test
  public void mergeCombinesValuesList() throws Exception {
    ExplicitField explicitField1 = new ExplicitField("test:test", false, Sets.newHashSet("String"), null);
    ExplicitField explicitField2 = new ExplicitField("test:test", false, Sets.newHashSet("Integer"), null);

    ExplicitField mergedExplicitField = explicitField1.mergeWith(explicitField2);

    assertThat(mergedExplicitField.getValues(), is(IsCollectionContaining.hasItems("String", "Integer")));
  }

  @Test
  public void mergeCombinesReferencesList() throws Exception {
    ExplicitField explicitField1 = new ExplicitField("test:test", false, null, Sets.newHashSet("Integer", "String"));
    ExplicitField explicitField2 = new ExplicitField("test:test", false, null, Sets.newHashSet("String"));

    ExplicitField mergedExplicitField = explicitField1.mergeWith(explicitField2);

    assertThat(mergedExplicitField.getReferences(), is(IsCollectionContaining.hasItems("Integer", "String")));
  }
}
