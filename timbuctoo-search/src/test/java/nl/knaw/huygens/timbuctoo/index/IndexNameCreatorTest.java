package nl.knaw.huygens.timbuctoo.index;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.index.model.ExplicitlyAnnotatedModel;
import nl.knaw.huygens.timbuctoo.index.model.SubModel;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import org.junit.Test;

public class IndexNameCreatorTest {
  @Test
  public void testGetIndexNameFor() {

    // mock
    VRE vreMock = mock(VRE.class);

    IndexNameCreator instance = new IndexNameCreator();
    Class<? extends DomainEntity> type = ExplicitlyAnnotatedModel.class;

    // when
    when(vreMock.getScopeId()).thenReturn("scopeName");

    // action
    String indexName = instance.getIndexNameFor(vreMock, type);

    // verify
    assertThat(indexName, equalTo("scopeName.explicitlyannotatedmodel"));
  }

  @Test
  public void testGetIndexNameForSubType() {

    // mock
    VRE vreMock = mock(VRE.class);

    IndexNameCreator instance = new IndexNameCreator();
    Class<? extends DomainEntity> type = SubModel.class;

    // when
    when(vreMock.getScopeId()).thenReturn("scopeName");

    // action
    String indexName = instance.getIndexNameFor(vreMock, type);

    // verify
    assertThat(indexName, equalTo("scopeName.explicitlyannotatedmodel"));
  }
}
