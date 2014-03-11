package nl.knaw.huygens.timbuctoo.index;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.model.ExplicitlyAnnotatedModel;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.vre.Scope;

import org.junit.Test;

public class IndexNameCreatorTest {
  @Test
  public void testGetIndexNameFor() {

    // mock
    Scope scopeMock = mock(Scope.class);
    TypeRegistry registryMock = mock(TypeRegistry.class);

    IndexNameCreator instance = new IndexNameCreator(registryMock);
    Class<? extends DomainEntity> type = ExplicitlyAnnotatedModel.class;

    // when
    when(scopeMock.getId()).thenReturn("scopeName");
    when(registryMock.getINameForType(type)).thenReturn("typeName");

    // action
    String indexName = instance.getIndexNameFor(scopeMock, type);

    // verify
    verify(scopeMock).getId();
    verify(registryMock).getINameForType(type);

    assertThat(indexName, equalTo("scopeName.typeName"));
  }
}
