package nl.knaw.huygens.timbuctoo.rest.util.search;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.DomainEntityDTO;
import nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMapFactory;
import nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMap;
import nl.knaw.huygens.timbuctoo.model.mapping.MappingException;
import org.junit.Before;
import org.junit.Test;
import test.rest.model.projecta.ProjectADomainEntity;

import java.util.List;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMapFactory.Representation.CLIENT;
import static nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMapFactory.Representation.INDEX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DomainEntityDTOListFactoryTest {

  public static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;
  public static final String ID_1 = "id1";
  public static final String ID_FIELD = "id";
  public static final String ID_2 = "id2";
  private FieldNameMapFactory fieldNameMapFactory;
  private FieldNameMap fieldNameMap;
  private DomainEntityDTOFactory domainEntityDTOFactory;
  private DomainEntityDTOListFactory instance;
  // use filled maps for matching method calls
  public static final Map<String, Object> DATA_ROW_1 = createDataRow(ID_1);
  public static final Map<String, Object> DATA_ROW_2 = createDataRow(ID_2);
  public static final List<Map<String, Object>> RAW_DATA = Lists.newArrayList(DATA_ROW_1, DATA_ROW_2);

  @Before
  public void setup() throws Exception {
    domainEntityDTOFactory = mock(DomainEntityDTOFactory.class);
    setupFieldNameMap();
    instance = new DomainEntityDTOListFactory(fieldNameMapFactory, domainEntityDTOFactory);
  }

  private void setupFieldNameMap() throws Exception{
    fieldNameMapFactory = mock(FieldNameMapFactory.class);
    fieldNameMap = mock(FieldNameMap.class);
    when(fieldNameMapFactory.create(INDEX, CLIENT, TYPE)).thenReturn(fieldNameMap);
  }


  @Test
  public void createForCreatesAListWithADTOForEachItemInTheRawDataList() throws Exception {
    // setup
    DomainEntityDTO dto1 = createDTOForDataRow(DATA_ROW_1, fieldNameMap);
    DomainEntityDTO dto2 = createDTOForDataRow(DATA_ROW_2, fieldNameMap);

    // action
    List<DomainEntityDTO> dtos = instance.createFor(TYPE, RAW_DATA);


    // verify
    assertThat(dtos, containsInAnyOrder(dto1, dto2));
    verify(fieldNameMapFactory).create(INDEX, CLIENT, TYPE);
  }

  @Test(expected = SearchResultCreationException.class)
  public void createForThrowsASearchResultCreationExceptionWhenTheFieldMapFactory() throws Exception {
    // setup
    when(fieldNameMapFactory.create(INDEX, CLIENT, TYPE)).thenThrow(new MappingException(TYPE, new Exception()));

    // action
    instance.createFor(TYPE, RAW_DATA);
  }


  private DomainEntityDTO createDTOForDataRow(Map<String, Object> dataRow, FieldNameMap fieldNameMap) {
    DomainEntityDTO dto = mock(DomainEntityDTO.class);
    when(domainEntityDTOFactory.create(fieldNameMap, dataRow)).thenReturn(dto);
    return dto;
  }

  private static Map<String, Object> createDataRow(String id) {
    Map<String, Object> dataRow = Maps.newHashMap();
    dataRow.put(ID_FIELD, id);
    return dataRow;
  }

}
