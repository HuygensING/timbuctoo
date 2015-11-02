package nl.knaw.huygens.timbuctoo.graph;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.util.RelationBuilder;
import nl.knaw.huygens.timbuctoo.model.util.RelationTypeBuilder;
import org.junit.Test;
import org.mockito.Mockito;
import test.model.projecta.ProjectADocument;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;

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

  private class TestFixture {
    public GraphBuilder builder;
    public ProjectADocument startDoc;
  }

  private TestFixture initializeRepository() throws Exception {
    //Our domain:
    //Documents can have previousVersions
    RelationType replacesRel = RelationTypeBuilder.newInstance()
      .withId(REPLACES_TYPE_ID)
      .withSourceType(ProjectADocument.class)
      .withTargetType(ProjectADocument.class)
      .withRegularName(REPLACES_TYPE_NAME)
      .build();
    //... translations
    RelationType translationRel = RelationTypeBuilder.newInstance()
      .withId(TRANSLATION_TYPE_ID)
      .withSourceType(ProjectADocument.class)
      .withTargetType(ProjectADocument.class)
      .withRegularName(TRANSLATION_TYPE_NAME)
      .build();
    //... and critiques
    RelationType critiqueRel = RelationTypeBuilder.newInstance()
      .withId(CRITIQUE_TYPE_ID)
      .withSourceType(ProjectADocument.class)
      .withTargetType(ProjectADocument.class)
      .withRegularName(CRITIQUES_TYPE_NAME)
      .build();
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
        .withSource(startDoc)
        .withTarget(prevVersion)
        .build(),
      RelationBuilder.newInstance(Relation.class)
        .withId(TRANSLATES_INSTANCE_ID)
        .withRelationType(translationRel)
        .withSource(translation)
        .withTarget(startDoc)
        .build(),
      RelationBuilder.newInstance(Relation.class)
        .withId(CRITIQUES_INSTANCE_ID)
        .withRelationType(critiqueRel)
        .withSource(critique)
        .withTarget(startDoc)
        .build()
    );

    //Access to the domain

    //We need a type registry
    TypeRegistry registry = TypeRegistry.getInstance();
    registry.init(ProjectADocument.class.getPackage().getName());

    //and a repository that returns it
    Repository repo = mock(Repository.class);
    Mockito.when(repo.getTypeRegistry()).thenReturn(registry);

    //when the code asks for an entity the repo should return it
    Mockito.when(repo.getEntityOrDefaultVariation(any(), eq(START_DOC_ID))).thenReturn(startDoc);
    Mockito.when(repo.getEntityOrDefaultVariation(any(), eq(PREV_VERSION_DOC_ID))).thenReturn(prevVersion);
    Mockito.when(repo.getEntityOrDefaultVariation(any(), eq(TRANSLATION_DOC_ID))).thenReturn(translation);
    Mockito.when(repo.getEntityOrDefaultVariation(any(), eq(CRITIQUE_DOC_ID))).thenReturn(critique);

    //when the code asks the repo for the relations the repo return the above list for startDoc and an empty list otherwise
    Mockito.when(repo.getRelationsByEntityId(eq(START_DOC_ID), anyInt())).thenReturn(relations);
//    Mockito.when(repo.getRelationsByEntityId(anyString(), anyInt())).thenReturn(Lists.newArrayList());

    //when the code asks for the type given a relation id we manually make the repo return the right one
    Mockito.when(repo.getRelationTypeById(REPLACES_TYPE_ID, true)).thenReturn(replacesRel);
    Mockito.when(repo.getRelationTypeById(TRANSLATION_TYPE_ID, true)).thenReturn(translationRel);
    Mockito.when(repo.getRelationTypeById(CRITIQUE_TYPE_ID, true)).thenReturn(critiqueRel);

    TestFixture result = new TestFixture();
    result.startDoc = startDoc;
    result.builder = new GraphBuilder(repo);
    return result;
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
