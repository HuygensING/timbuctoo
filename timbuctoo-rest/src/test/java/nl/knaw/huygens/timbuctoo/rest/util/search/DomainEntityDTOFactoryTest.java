package nl.knaw.huygens.timbuctoo.rest.util.search;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.DomainEntityDTO;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMap;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import test.rest.model.projecta.ProjectADomainEntity;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DomainEntityDTOFactoryTest {

  public static final String ID_VALUE = "idValue";
  public static final String DISPLAY_NAME = "displayName";
  public static final Class<ProjectADomainEntity> TYPE = ProjectADomainEntity.class;
  public static final String EXTERNAL_NAME = TypeNames.getExternalName(TYPE);
  public static final String INTERNAL_NAME = TypeNames.getInternalName(TYPE);
  public Map<String, String> remappedData;
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

    Map<String, String>expectedMap = Maps.newHashMap();
    expectedMap.put(Entity.INDEX_FIELD_ID, ID_VALUE);
    expectedMap.put(Entity.INDEX_FIELD_IDENTIFICATION_NAME, DISPLAY_NAME);

    when(fieldNameMap.remap(expectedMap)).thenReturn(remappedData);
  }

  private void setupData() {
    data = Maps.newHashMap();
    data.put(Entity.INDEX_FIELD_ID, Lists.newArrayList(ID_VALUE));
    data.put(Entity.INDEX_FIELD_IDENTIFICATION_NAME, Lists.newArrayList(DISPLAY_NAME));
  }

  @Test
  public void createAddsTheIdAndDisplayNameAndTypeAndPathToTheDTORetrievedFromTheDataMap() {
    // action
    DomainEntityDTO domainEntityDTO = instance.create(TYPE, fieldNameMap, data);

    // verify
    assertThat(domainEntityDTO.getDisplayName(), is(DISPLAY_NAME));
    assertThat(domainEntityDTO.getId(), is(ID_VALUE));
    assertThat(domainEntityDTO.getType(), is(INTERNAL_NAME));
    assertThat(domainEntityDTO.getPath(), containsString(EXTERNAL_NAME));
    assertThat(domainEntityDTO.getPath(), containsString(ID_VALUE));
  }


  @Test
  public void createAddsTheByFieldNameMapTranslatedDataToTheDomainEntityDTO() {
    // action
    DomainEntityDTO domainEntityDTO = instance.create(TYPE, fieldNameMap, data);

    // verify
    hasKeyWithValue((Map<String, Object>) domainEntityDTO.getData(), Entity.ID_PROPERTY_NAME, ID_VALUE);
  }

  private <T, U> void hasKeyWithValue(Map<T, U> map, T expectedKey, U expectedValue) {
    MatcherAssert.assertThat(map.keySet(), hasItem(expectedKey));
    MatcherAssert.assertThat(map.get(expectedKey), is(expectedValue));
  }


}
