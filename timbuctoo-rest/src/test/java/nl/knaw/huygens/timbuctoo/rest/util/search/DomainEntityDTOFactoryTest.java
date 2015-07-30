package nl.knaw.huygens.timbuctoo.rest.util.search;

import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.DomainEntityDTO;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMap;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class DomainEntityDTOFactoryTest {

  public static final Object ID_VALUE = "idValue";
  public static final Object DISPLAY_NAME = "displayName";
  public Map<String, Object> remappedData;
  private Map<String, Object> data;
  private DomainEntityDTOFactory instance;
  private FieldNameMap fieldNameMap;

  @Before
  public void setup() {
    setupData();

    setupFieldNameMap();

    instance = new DomainEntityDTOFactory();
  }

  private void setupFieldNameMap() {
    fieldNameMap = mock(FieldNameMap.class);
    remappedData = Maps.newHashMap();
    remappedData.put(Entity.ID_PROPERTY_NAME, ID_VALUE);
    Mockito.when(fieldNameMap.remap(data)).thenReturn(remappedData);
  }

  private void setupData() {
    data = Maps.newHashMap();
    data.put(Entity.INDEX_FIELD_ID, ID_VALUE);
    data.put(Entity.INDEX_FIELD_IDENTIFICATION_NAME, DISPLAY_NAME);
  }

  @Test
  public void createAddsTheIdAndDisplayNameToTheDTORetrievedFromTheDataMap() {
    // action
    DomainEntityDTO domainEntityDTO = instance.create(fieldNameMap, data);

    // verify
    assertThat(domainEntityDTO.getDisplayName(), is(DISPLAY_NAME));
    assertThat(domainEntityDTO.getId(), is(ID_VALUE));
  }


  @Test
  public void createAddsTheByFieldNameMapTranslatedDataToTheDomainEntityDTO() {
    // action
    DomainEntityDTO domainEntityDTO = instance.create(fieldNameMap, data);

    // verify
    hasKeyWithValue((Map<String, Object>)domainEntityDTO.getData(), Entity.ID_PROPERTY_NAME, ID_VALUE);
  }

  private <T,U> void hasKeyWithValue(Map<T,U> map, T expectedKey, U expectedValue) {
    MatcherAssert.assertThat(map.keySet(), hasItem(expectedKey));
    MatcherAssert.assertThat(map.get(expectedKey), is(expectedValue));
  }



}
