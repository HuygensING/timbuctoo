package nl.knaw.huygens.timbuctoo.rest.util.search;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.facetedsearch.model.parameters.SortParameter;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.DomainEntityDTO;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.RelationDTO;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMap;
import nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMapFactory;
import nl.knaw.huygens.timbuctoo.model.mapping.MappingException;
import nl.knaw.huygens.timbuctoo.vre.NotInScopeException;
import nl.knaw.huygens.timbuctoo.vre.SearchException;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import test.model.projecta.ProjectAPerson;
import test.rest.model.BaseDomainEntity;
import test.rest.model.projecta.ProjectADomainEntity;
import test.rest.model.projecta.ProjectARelation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.model.Entity.INDEX_FIELD_IDENTIFICATION_NAME;
import static nl.knaw.huygens.timbuctoo.model.Relation.INDEX_FIELD_SOURCE_TYPE;
import static nl.knaw.huygens.timbuctoo.model.Relation.INDEX_FIELD_TARGET_TYPE;
import static nl.knaw.huygens.timbuctoo.model.Relation.SOURCE_ID_FACET_NAME;
import static nl.knaw.huygens.timbuctoo.model.Relation.TARGET_ID_FACET_NAME;
import static nl.knaw.huygens.timbuctoo.model.Relation.TYPE_ID_FACET_NAME;
import static nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMapFactory.Representation.CLIENT;
import static nl.knaw.huygens.timbuctoo.model.mapping.FieldNameMapFactory.Representation.INDEX;
import static nl.knaw.huygens.timbuctoo.rest.util.search.RelationDTOMatcher.likeRelationDTO;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RelationDTOFactoryTest {

  private static final Class<? extends DomainEntity> SOURCE_TYPE = ProjectAPerson.class;
  public static final Class<? extends DomainEntity> SOURCE_BASE_TYPE = Person.class;
  public static final String RELATION_SOURCE_NAME = TypeNames.getInternalName(SOURCE_BASE_TYPE);
  private static final Class<? extends DomainEntity> TARGET_TYPE = ProjectADomainEntity.class;
  public static final Class<? extends DomainEntity> TARGET_BASE_TYPE = BaseDomainEntity.class;
  public static final String RELATION_TARGET_NAME = TypeNames.getInternalName(TARGET_BASE_TYPE);
  private static final Map<String, Object> SOURCE_DATA;
  private static final Map<String, Object> TARGET_DATA;
  private static final String SOURCE_ID = "sourceId";
  private static final String TARGET_ID = "targetId";
  private static final Class<ProjectARelation> RELATION_TYPE = ProjectARelation.class;
  private static final String INTERNAL_NAME = TypeNames.getInternalName(RELATION_TYPE);
  private static final String RELATION_ID = "relationId";
  private static final String TYPE_ID = "typeId";
  private static final Class<RelationType> RELATION_TYPE_TYPE = RelationType.class;
  private static final String RELATION_NAME = "regularName";
  public static final String SOURCE_NAME = "sourceName";
  public static final String TARGET_NAME = "targetName";
  public static final HashMap<String, Object> CONVERTED_SOURCE_DATA = Maps.<String, Object>newHashMap();
  public static final HashMap<String, Object> CONVERTED_TARGET_DATA = Maps.<String, Object>newHashMap();
  private static final List<SortParameter> EMPTY_SORT = Lists.newArrayList();

  static {
    SOURCE_DATA = Maps.newHashMap();
    SOURCE_DATA.put(INDEX_FIELD_IDENTIFICATION_NAME, SOURCE_NAME);
    TARGET_DATA = Maps.newHashMap();
    TARGET_DATA.put(INDEX_FIELD_IDENTIFICATION_NAME, TARGET_NAME);
  }

  private Repository repository;
  private TypeRegistry typeRegistry;
  private VRE vre;
  private RelationDTOFactory instance;
  private Map<String, Object> relationData;
  private RelationType relationType;
  public static final FieldNameMap SOURCE_FIELD_MAP = new FieldNameMap(mock(SOURCE_TYPE));
  public static final FieldNameMap TARGET_FIELD_MAP = new FieldNameMap(mock(TARGET_TYPE));
  private DomainEntityDTOFactory domainEntityDTOFactory;
  private DomainEntityDTO sourceDTO;
  private DomainEntityDTO targetDTO;
  private FieldNameMapFactory fieldNameMapFactory;

  @Before
  public void setup() throws Exception {
    relationData = setupRelationData();
    setupFieldNameMapFactory();
    setupDomainEntityDTOFactory();
    setupRepository();
    setupTypeRegistry();
    setupVRE();
    instance = new RelationDTOFactory(repository, typeRegistry, domainEntityDTOFactory, fieldNameMapFactory) ;
  }

  private void setupFieldNameMapFactory() throws nl.knaw.huygens.timbuctoo.model.mapping.MappingException {
    fieldNameMapFactory = mock(FieldNameMapFactory.class);
    when(fieldNameMapFactory.create(INDEX, CLIENT, SOURCE_TYPE)).thenReturn(SOURCE_FIELD_MAP);
    when(fieldNameMapFactory.create(INDEX, CLIENT, TARGET_TYPE)).thenReturn(TARGET_FIELD_MAP);
  }

  private void setupDomainEntityDTOFactory() {
    domainEntityDTOFactory = mock(DomainEntityDTOFactory.class);

    sourceDTO = createDTOWithNameAndData(SOURCE_NAME, CONVERTED_SOURCE_DATA);
    targetDTO = createDTOWithNameAndData(TARGET_NAME, CONVERTED_TARGET_DATA);

    when(domainEntityDTOFactory.create(Matchers.<Class<? extends DomainEntity>>any(), any(FieldNameMap.class), Matchers.<Map<String, Object>>any())).thenReturn(sourceDTO, targetDTO);
  }

  private void setupVRE() throws Exception {
    vre = mock(VRE.class);
    when(vre.getRawDataFor(SOURCE_TYPE, Lists.newArrayList(SOURCE_ID), EMPTY_SORT)).thenReturn(Lists.newArrayList(SOURCE_DATA));
    when(vre.getRawDataFor(TARGET_TYPE, Lists.newArrayList(TARGET_ID), EMPTY_SORT)).thenReturn(Lists.newArrayList(TARGET_DATA));

    doReturn(SOURCE_TYPE).when(vre).mapToScopeType(SOURCE_BASE_TYPE);
    doReturn(TARGET_TYPE).when(vre).mapToScopeType(TARGET_BASE_TYPE);
  }

  private void setupRepository() {
    repository = mock(Repository.class);
    relationType = new RelationType();
    relationType.setRegularName(RELATION_NAME);
    when(repository.getRelationTypeById(TYPE_ID, true)).thenReturn(relationType);
  }

  private void setupTypeRegistry() {
    typeRegistry = mock(TypeRegistry.class);
    doReturn(SOURCE_BASE_TYPE).when(typeRegistry).getDomainEntityType(RELATION_SOURCE_NAME);
    doReturn(TARGET_BASE_TYPE).when(typeRegistry).getDomainEntityType(RELATION_TARGET_NAME);
  }

  private Map<String, Object> setupRelationData() {
    Map<String, Object> relationData = Maps.newHashMap();
    relationData.put(Entity.INDEX_FIELD_ID, Lists.newArrayList(RELATION_ID));
    relationData.put(INDEX_FIELD_SOURCE_TYPE, Lists.newArrayList(RELATION_SOURCE_NAME));
    relationData.put(SOURCE_ID_FACET_NAME, Lists.newArrayList(SOURCE_ID));
    relationData.put(INDEX_FIELD_TARGET_TYPE, Lists.newArrayList(RELATION_TARGET_NAME));
    relationData.put(TARGET_ID_FACET_NAME, Lists.newArrayList(TARGET_ID));
    relationData.put(TYPE_ID_FACET_NAME, Lists.newArrayList(TYPE_ID));
    return relationData;
  }

  @Test
  public void createRetrievesThatSourceTargetAndRelationType() throws Exception {
    // setup
    String path = TypeNames.getExternalName(RELATION_TYPE) + "/" + RELATION_ID;

    // action
    RelationDTO dto = instance.create(vre, RELATION_TYPE, relationData);

    // verify
    verify(domainEntityDTOFactory).create(SOURCE_TYPE, SOURCE_FIELD_MAP, SOURCE_DATA);
    verify(domainEntityDTOFactory).create(TARGET_TYPE, TARGET_FIELD_MAP, TARGET_DATA);

    assertThat(dto, likeRelationDTO() //
      .withInternalType(INTERNAL_NAME) //
      .withId(RELATION_ID) //
      .withPathThatEndsWith(path) //
      .withRelationName(RELATION_NAME) //
      .withSourceName(SOURCE_NAME) //
      .withSourceData(CONVERTED_SOURCE_DATA) //
      .withTargetName(TARGET_NAME) //
      .withTargetData(CONVERTED_TARGET_DATA));
  }



  private DomainEntityDTO createDTOWithNameAndData(String sourceName, Map<String, Object> data) {
    DomainEntityDTO dto = new DomainEntityDTO();
    dto.setDisplayName(sourceName);
    dto.setData(data);

    return dto;
  }


  @Test(expected = SearchException.class)
  public void createThrowsASearchExceptionWhenTheVREThrowsASearchException() throws Exception {
    // setup
    when(vre.getRawDataFor(SOURCE_TYPE, Lists.newArrayList(SOURCE_ID), EMPTY_SORT)).thenThrow(new SearchException(new Exception()));

    // action
    instance.create(vre, RELATION_TYPE, relationData);
  }

  @Test(expected = NotInScopeException.class)
  public void createThrowsANotInScopeExceptionWhenTheVREThrowsNotInScopeException() throws Exception {
    // setup
    when(vre.getRawDataFor(SOURCE_TYPE, Lists.newArrayList(SOURCE_ID), EMPTY_SORT)).thenThrow(NotInScopeException.typeIsNotInScope(SOURCE_TYPE, "vreId"));

    // action
    instance.create(vre, RELATION_TYPE, relationData);
  }

  @Test(expected = MappingException.class)
  public void createThrowsAMappingExceptionWhenTheFieldNameMapFactoryDoes() throws Exception {
    // setup
    when(fieldNameMapFactory.create(INDEX, CLIENT, SOURCE_TYPE)).thenThrow(new MappingException(SOURCE_TYPE, new Exception()));

    // action
    instance.create(vre, RELATION_TYPE, relationData);

  }
}
