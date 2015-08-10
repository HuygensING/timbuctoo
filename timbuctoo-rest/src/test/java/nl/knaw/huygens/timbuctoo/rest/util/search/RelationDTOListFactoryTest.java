package nl.knaw.huygens.timbuctoo.rest.util.search;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.RelationDTO;
import nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMap;
import nl.knaw.huygens.timbuctoo.model.mapping.MappingException;
import nl.knaw.huygens.timbuctoo.vre.NotInScopeException;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import test.rest.model.projecta.ProjectARelation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RelationDTOListFactoryTest {
  public static final String ID_1 = "id1";
  public static final String ID_FIELD = "id";
  public static final String ID_2 = "id2";
  public static final Map<String, Object> DATA_ROW_1 = createDataRow(ID_1);
  public static final Map<String, Object> DATA_ROW_2 = createDataRow(ID_2);
  public static final ArrayList<Map<String, Object>> RAW_DATA = Lists.newArrayList(DATA_ROW_1, DATA_ROW_2);
  private static final Class<? extends DomainEntity> TYPE = ProjectARelation.class;
  public static final FieldNameMap FIELD_NAME_MAP = mock(FieldNameMap.class);
  private Repository repository;
  private RelationDTOFactory relationDTOFactory;
  private RelationDTOListFactory instance;
  private VRE vre;

  @Before
  public void setup() {
    vre = mock(VRE.class);
    repository = mock(Repository.class);
    relationDTOFactory = mock(RelationDTOFactory.class);
    instance = new RelationDTOListFactory(repository, relationDTOFactory);
  }

  @Test
  public void createCreatesACollectionRelationDTOsForEachOneFoundInTheRawData() throws Exception {
    // setup
    RelationDTO dto1 = createDTOForDataRow(DATA_ROW_1, FIELD_NAME_MAP);
    RelationDTO dto2 = createDTOForDataRow(DATA_ROW_2, FIELD_NAME_MAP);

    // action
    List<RelationDTO> relationDTOs = instance.create(vre, TYPE, RAW_DATA);

    // verity
    assertThat(relationDTOs, containsInAnyOrder(dto1, dto2));
    verify(relationDTOFactory).create(vre, TYPE, DATA_ROW_1);
    verify(relationDTOFactory).create(vre, TYPE, DATA_ROW_2);
  }

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void createThrowsASearchResultCreationExceptionWhenTheRelationDTOFactoryThrowsASearchException() throws Exception {
    // setup
    expectedException.expect(SearchResultCreationException.class);
    expectedException.expectCause(any(SearchException.class));
    when(relationDTOFactory.create(vre, TYPE, DATA_ROW_1)).thenThrow(new SearchException(new Exception()));

    // action
    instance.create(vre, TYPE, RAW_DATA);
  }

  @Test
  public void createThrowsASearchResultCreationExceptionWhenTheRelationDTOFactoryThrowsANotInScopeException() throws Exception{
    // setup
    expectedException.expect(SearchResultCreationException.class);
    expectedException.expectCause(any(NotInScopeException.class));
    when(relationDTOFactory.create(vre, TYPE, DATA_ROW_1)).thenThrow(new NotInScopeException(TYPE, ""));

    // action
    instance.create(vre, TYPE, RAW_DATA);
  }

  @Test
  public void createThrowsASearchResultCreationExceptionWhenTheRelationDTOFactoryThrowsAMappingException() throws Exception{
    // setup
    expectedException.expect(SearchResultCreationException.class);
    expectedException.expectCause(any(MappingException.class));
    when(relationDTOFactory.create(vre, TYPE, DATA_ROW_1)).thenThrow(new MappingException(TYPE, new Exception()));

    // action
    instance.create(vre, TYPE, RAW_DATA);
  }

  private static Map<String, Object> createDataRow(String id) {
    Map<String, Object> dataRow = Maps.newHashMap();
    dataRow.put(ID_FIELD, id);
    return dataRow;
  }

  private RelationDTO createDTOForDataRow(Map<String, Object> dataRow, FieldNameMap fieldNameMap) throws Exception {
    RelationDTO dto = mock(RelationDTO.class);
    when(relationDTOFactory.create(vre, TYPE, dataRow)).thenReturn(dto);
    return dto;
  }
}
