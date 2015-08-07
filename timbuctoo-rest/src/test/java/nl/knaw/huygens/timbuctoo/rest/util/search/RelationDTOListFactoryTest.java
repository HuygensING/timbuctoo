package nl.knaw.huygens.timbuctoo.rest.util.search;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.RelationDTO;
import nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMap;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import org.junit.Before;
import org.junit.Test;
import test.rest.model.projecta.ProjectARelation;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
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
  private static final Class<? extends DomainEntity> TYPE = ProjectARelation.class;
  public static final FieldNameMap FIELD_NAME_MAP = mock(FieldNameMap.class);
  private Repository repository;
  private RelationDTOFactory relationDTOFactory;
  private RelationDTOListFactory instance;
  private VRE mock;

  @Before
  public void setup() {
    mock = mock(VRE.class);
    repository = mock(Repository.class);
    relationDTOFactory = mock(RelationDTOFactory.class);
    instance = new RelationDTOListFactory(repository, relationDTOFactory);
  }

  @Test
  public void createCreatesACollectionRelationDTOsForEachOneFoundInTheRawData() {
    // setup
    RelationDTO dto1 = createDTOForDataRow(DATA_ROW_1, FIELD_NAME_MAP);
    RelationDTO dto2 = createDTOForDataRow(DATA_ROW_2, FIELD_NAME_MAP);

    // action
    List<RelationDTO> relationDTOs = instance.create(mock, TYPE, Lists.newArrayList(DATA_ROW_1, DATA_ROW_2));

    // verity
    assertThat(relationDTOs, containsInAnyOrder(dto1, dto2));
    verify(relationDTOFactory).create(TYPE, DATA_ROW_1);
    verify(relationDTOFactory).create(TYPE, DATA_ROW_2);
  }


  private static Map<String, Object> createDataRow(String id) {
    Map<String, Object> dataRow = Maps.newHashMap();
    dataRow.put(ID_FIELD, id);
    return dataRow;
  }

  private RelationDTO createDTOForDataRow(Map<String, Object> dataRow, FieldNameMap fieldNameMap) {
    RelationDTO dto = mock(RelationDTO.class);
    when(relationDTOFactory.create(TYPE, dataRow)).thenReturn(dto);
    return dto;
  }
}
