package nl.knaw.huygens.timbuctoo.graph;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Document;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.util.RelationBuilder;
import nl.knaw.huygens.timbuctoo.model.util.RelationTypeBuilder;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import org.junit.Test;
import test.model.projecta.ProjectADocument;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GraphBuilderTest {

  public static final String REPLACES_TYPE_ID = "replacesRel";
  public static final String REPLACES_TYPE_NAME = "replacesType";
  public static final String TRANSLATION_TYPE_ID = "translationRel";
  public static final String TRANSLATION_TYPE_NAME = "translatesType";
  public static final String CRITIQUE_TYPE_ID = "critiqueRel";
  public static final String CRITIQUES_TYPE_NAME = "critiquesType";
  public static final String START_DOC_ID = "startDoc";
  public static final String PREV_VERSION_DOC_ID = "prevVersion";
  public static final String TRANSLATION_DOC_ID = "translation";
  public static final String CRITIQUE_DOC_ID = "critique";
  public static final String REPLACES_INSTANCE_ID = "replaces";
  public static final String TRANSLATES_INSTANCE_ID = "translates";
  public static final String CRITIQUES_INSTANCE_ID = "critiques";
  public static final Class<Document> BASE_TYPE = Document.class;
  public static final String BASE_TYPE_NAME = TypeNames.getInternalName(BASE_TYPE);
  public static final Class<ProjectADocument> TYPE = ProjectADocument.class;

  private class TestFixture {
    public GraphBuilder builder;
    public ProjectADocument startDoc;
    private Repository repository;
  }

  private TestFixture initializeRepository() throws Exception {
    //Our domain:
    //Documents can have previousVersions
    RelationType replacesRel = createRelationType(REPLACES_TYPE_ID, REPLACES_TYPE_NAME);
    //... translations
    RelationType translationRel = createRelationType(TRANSLATION_TYPE_ID, TRANSLATION_TYPE_NAME);
    //... and critiques
    RelationType critiqueRel = createRelationType(CRITIQUE_TYPE_ID, CRITIQUES_TYPE_NAME);
    //There exists a few documents
    ProjectADocument startDoc = new ProjectADocument(START_DOC_ID);
    ProjectADocument prevVersion = new ProjectADocument(PREV_VERSION_DOC_ID);
    ProjectADocument translation = new ProjectADocument(TRANSLATION_DOC_ID);
    ProjectADocument critique = new ProjectADocument(CRITIQUE_DOC_ID);
    //The startDoc has 1 relation to all the other docs
    List<Relation> relations = Lists.newArrayList(
      RelationBuilder.newInstance(Relation.class)
        .withId(REPLACES_INSTANCE_ID)
        .withRelationType(replacesRel)
        .withSourceType(BASE_TYPE_NAME).withSourceId(START_DOC_ID)
        .withTargetType(BASE_TYPE_NAME).withTargetId(PREV_VERSION_DOC_ID)
        .build(),
      RelationBuilder.newInstance(Relation.class)
        .withId(TRANSLATES_INSTANCE_ID)
        .withRelationType(translationRel)
        .withSourceType(BASE_TYPE_NAME).withSourceId(TRANSLATION_DOC_ID)
        .withTargetType(BASE_TYPE_NAME).withTargetId(START_DOC_ID)
        .build(),
      RelationBuilder.newInstance(Relation.class)
        .withId(CRITIQUES_INSTANCE_ID)
        .withRelationType(critiqueRel)
        .withSourceType(BASE_TYPE_NAME).withSourceId(CRITIQUE_DOC_ID)
        .withTargetType(BASE_TYPE_NAME).withTargetId(START_DOC_ID)
        .build()
    );

    //Access to the domain

    //We need a type registry
    TypeRegistry registry = TypeRegistry.getInstance();
    registry.init(TYPE.getPackage().getName() + " " + BASE_TYPE.getPackage().getName());

    //and a repository that returns it
    Repository repo = mock(Repository.class);
    when(repo.getTypeRegistry()).thenReturn(registry);

    //when the code asks for an entity the repo should return it
    when(repo.getEntityOrDefaultVariation(any(), eq(START_DOC_ID))).thenReturn(startDoc);
    when(repo.getEntityOrDefaultVariation(any(), eq(PREV_VERSION_DOC_ID))).thenReturn(prevVersion);
    when(repo.getEntityOrDefaultVariation(any(), eq(TRANSLATION_DOC_ID))).thenReturn(translation);
    when(repo.getEntityOrDefaultVariation(any(), eq(CRITIQUE_DOC_ID))).thenReturn(critique);

    //when the code asks the repo for the relations the repo return the above list for startDoc and an empty list otherwise
    when(repo.getRelationsByEntityId(eq(START_DOC_ID), anyInt())).thenReturn(relations);
//    Mockito.when(repo.getRelationsByEntityId(anyString(), anyInt())).thenReturn(Lists.newArrayList());

    //when the code asks for the type given a relation id we manually make the repo return the right one
    when(repo.getRelationTypeById(REPLACES_TYPE_ID, true)).thenReturn(replacesRel);
    when(repo.getRelationTypeById(TRANSLATION_TYPE_ID, true)).thenReturn(translationRel);
    when(repo.getRelationTypeById(CRITIQUE_TYPE_ID, true)).thenReturn(critiqueRel);

    TestFixture result = new TestFixture();
    result.startDoc = startDoc;

    VRE vre = mock(VRE.class);
    // Map the TYPE as the scoped type of BASE_TYPE
    doReturn(TYPE).when(vre).mapTypeName(BASE_TYPE_NAME, true);

    result.builder = new GraphBuilder(repo, vre);
    result.repository = repo;
    return result;
  }

  private RelationType createRelationType(String id, String name) {
    return RelationTypeBuilder.newInstance()
      .withId(id)
      .withSourceType(BASE_TYPE) // the source and target type are always base types like person, document
      .withTargetType(BASE_TYPE)
      .withRegularName(name)
      .build();
  }

  @Test
  public void addEntityRetrievesTheRelatedEntitiesAsVariantInScopeOfTheVRE() throws Exception {
    //setup
    TestFixture fixture = initializeRepository();
    GraphBuilder b = fixture.builder;
    ProjectADocument startDoc = fixture.startDoc;

    //action
    b.addEntity(startDoc, 1, null);

    // verify
    verify(fixture.repository, times(3)).getEntityOrDefaultVariation(argThat(equalTo(TYPE)), anyString());
    verify(fixture.repository, never()).getEntityOrDefaultVariation(argThat(equalTo(BASE_TYPE)), anyString());
  }


  @Test
  public void aCallWithoutTypesShouldReturnAllTypes() throws Exception {
    //setup
    TestFixture fixture = initializeRepository();
    GraphBuilder b = fixture.builder;
    ProjectADocument startDoc = fixture.startDoc;

    //action
    b.addEntity(startDoc, 1, null);

    //verify
    assertThat(b.getGraph().nodeCount(), is(4));
  }

  @Test
  public void aCallWithEmptyTypeListShouldReturnAllTypes() throws Exception {
    //setup
    TestFixture fixture = initializeRepository();
    GraphBuilder b = fixture.builder;
    ProjectADocument startDoc = fixture.startDoc;

    //action
    b.addEntity(startDoc, 1, Lists.newArrayList());

    //verify
    assertThat(b.getGraph().nodeCount(), is(4));
  }

  @Test
  public void aCallWithOneTypeInTheListShouldReturnOnlyThoseTypes() throws Exception {
    //setup
    TestFixture fixture = initializeRepository();
    GraphBuilder b = fixture.builder;
    ProjectADocument startDoc = fixture.startDoc;

    //action
    b.addEntity(startDoc, 1, Lists.newArrayList(REPLACES_TYPE_NAME));

    //verify
    assertThat(b.getGraph().nodeCount(), is(2));
  }

  @Test
  public void aCallWithMultipleTypesInTheListShouldReturnOnlyThoseTypes() throws Exception {
    //setup
    TestFixture fixture = initializeRepository();
    GraphBuilder b = fixture.builder;
    ProjectADocument startDoc = fixture.startDoc;

    //action
    b.addEntity(startDoc, 1, Lists.newArrayList(REPLACES_TYPE_NAME, TRANSLATION_TYPE_NAME));

    //verify
    assertThat(b.getGraph().nodeCount(), is(3));
  }
}
