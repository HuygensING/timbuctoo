package nl.knaw.huygens.timbuctoo.rest.util.search;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.DomainEntityDTO;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.mapping.DomainEntityFieldNameMapFactory;
import nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMap;
import org.junit.Test;
import test.rest.model.projecta.ProjectADomainEntity;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DomainEntityDTOListFactoryTest {

  public static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;
  public static final String ID_1 = "id1";
  public static final String ID_2 = "id2";
  public static final String DISPLAY_NAME = "displayName";

  @Test
  public void createForCreatesAListWithADTOForEachItemInTheRawDataList() {
    // setup
    Map<String, Object> dataRow1 = createDataRow(ID_1);
    Map<String, Object> dataRow2 = createDataRow(ID_2);
    List<Map<String, Object>> rawData = Lists.newArrayList(dataRow1, dataRow2);


    DomainEntityFieldNameMapFactory fieldMapFactory = mock(DomainEntityFieldNameMapFactory.class);
    FieldNameMap fieldNameMap = mock(FieldNameMap.class);
    when(fieldMapFactory.create(TYPE)).thenReturn(fieldNameMap);

    DomainEntityRemapper domainEntityRemapper = mock(DomainEntityRemapper.class);
    when(domainEntityRemapper.remap(fieldNameMap, rawData)).thenReturn()
    DomainEntityDTOFactory instance = new DomainEntityDTOFactory();



    // action
    List<DomainEntityDTO> dtos = instance.createFor(TYPE, rawData);


    // verify
    verify(mock())
  }

  private Map<String, Object> createDataRow(String id) {
    Map<String, Object> dataRow = Maps.newHashMap();
    dataRow.put(Entity.INDEX_FIELD_ID, id);
    dataRow.put(Entity.INDEX_FIELD_IDENTIFICATION_NAME, DISPLAY_NAME);
    return dataRow;
  }


}
