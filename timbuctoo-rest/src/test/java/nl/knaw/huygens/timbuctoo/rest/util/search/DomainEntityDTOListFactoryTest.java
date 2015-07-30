package nl.knaw.huygens.timbuctoo.rest.util.search;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.DomainEntityDTO;
import nl.knaw.huygens.timbuctoo.model.mapping.DomainEntityFieldNameMapFactory;
import nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMap;
import org.junit.Before;
import org.junit.Test;
import test.rest.model.projecta.ProjectADomainEntity;

import java.util.List;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.model.mapping.DomainEntityFieldNameMapFactory.Representation.CLIENT;
import static nl.knaw.huygens.timbuctoo.model.mapping.DomainEntityFieldNameMapFactory.Representation.INDEX;
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
  private DomainEntityFieldNameMapFactory fieldNameMapFactory;
  private FieldNameMap fieldNameMap;
  private DomainEntityDTOFactory domainEntityDTOFactory;
  private DomainEntityDTOListFactory instance;

  @Before
  public void setup(){
    domainEntityDTOFactory = mock(DomainEntityDTOFactory.class);
    setupFieldNameMap();
    instance = new DomainEntityDTOListFactory(fieldNameMapFactory, domainEntityDTOFactory);
  }

  private void setupFieldNameMap() {
    fieldNameMapFactory = mock(DomainEntityFieldNameMapFactory.class);
    fieldNameMap = mock(FieldNameMap.class);
    when(fieldNameMapFactory.create(INDEX, CLIENT, TYPE)).thenReturn(fieldNameMap);
  }


  @Test
  public void createForCreatesAListWithADTOForEachItemInTheRawDataList() {
    // setup
    // use filled maps to match
    Map<String, Object> dataRow1 = createDataRow(ID_1);
    Map<String, Object> dataRow2 = createDataRow(ID_2);
    List<Map<String, Object>> rawData = Lists.newArrayList(dataRow1, dataRow2);

    DomainEntityDTO dto1 = createDTOForDataRow(dataRow1, fieldNameMap);
    DomainEntityDTO dto2 = createDTOForDataRow(dataRow2, fieldNameMap);

    // action
    List<DomainEntityDTO> dtos = instance.createFor(TYPE, rawData);


    // verify
    assertThat(dtos, containsInAnyOrder(dto1, dto2));
    verify(fieldNameMapFactory).create(INDEX, CLIENT, TYPE);
  }


  private DomainEntityDTO createDTOForDataRow(Map<String, Object> dataRow, FieldNameMap fieldNameMap) {
    DomainEntityDTO dto = mock(DomainEntityDTO.class);
    when(domainEntityDTOFactory.create(fieldNameMap, dataRow)).thenReturn(dto);
    return dto;
  }

  private Map<String, Object> createDataRow(String id) {
    Map<String, Object> dataRow = Maps.newHashMap();
    dataRow.put(ID_FIELD, id);
    return dataRow;
  }

}
