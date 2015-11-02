package nl.knaw.huygens.timbuctoo.graph;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.ModelException;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.util.RelationBuilder;
import nl.knaw.huygens.timbuctoo.model.util.RelationTypeBuilder;
import org.junit.Test;
import org.mockito.Mockito;
import test.model.projecta.ProjectADocument;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;

public class GraphBuilderTest {

  private Repository initializeRepository() throws Exception {
    //Our domain:
    //Documents can have previousVersions
    RelationType replacesRel = RelationTypeBuilder.newInstance()
      .withId("replacesRel")
      .withSourceType(ProjectADocument.class)
      .withTargetType(ProjectADocument.class)
      .withRegularName("replacesType")
      .build();
    //... translations
    RelationType translationRel = RelationTypeBuilder.newInstance()
      .withId("translationRel")
      .withSourceType(ProjectADocument.class)
      .withTargetType(ProjectADocument.class)
      .withRegularName("translatesType")
      .build();
    //... and critiques
    RelationType critiqueRel = RelationTypeBuilder.newInstance()
      .withId("critiqueRel")
      .withSourceType(ProjectADocument.class)
      .withTargetType(ProjectADocument.class)
      .withRegularName("critiquesType")
      .build();
    //There exists a few documents
    ProjectADocument startDoc = new ProjectADocument("startDoc");
    ProjectADocument prevVersion = new ProjectADocument("prevVersion");
    ProjectADocument translation = new ProjectADocument("translation");
    ProjectADocument critique = new ProjectADocument("critique");
    //The startDoc has 1 relation to all the other docs
    List<Relation> relations = Lists.newArrayList(
      RelationBuilder.newInstance(Relation.class)
        .withId("replaces")
        .withRelationType(replacesRel)
        .withSource(startDoc)
        .withTarget(prevVersion)
        .build(),
      RelationBuilder.newInstance(Relation.class)
        .withId("translates")
        .withRelationType(translationRel)
        .withSource(translation)
        .withTarget(startDoc)
        .build(),
      RelationBuilder.newInstance(Relation.class)
        .withId("critiques")
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
    Mockito.when(repo.getEntityOrDefaultVariation(any(), eq("startDoc"))).thenReturn(startDoc);
    Mockito.when(repo.getEntityOrDefaultVariation(any(), eq("prevVersion"))).thenReturn(prevVersion);
    Mockito.when(repo.getEntityOrDefaultVariation(any(), eq("translation"))).thenReturn(translation);
    Mockito.when(repo.getEntityOrDefaultVariation(any(), eq("critique"))).thenReturn(critique);

    //when the code asks the repo for the relations the repo return the above list for startDoc and an empty list otherwise
    Mockito.when(repo.getRelationsByEntityId(eq("startDoc"), anyInt())).thenReturn(relations);
//    Mockito.when(repo.getRelationsByEntityId(anyString(), anyInt())).thenReturn(Lists.newArrayList());

    //when the code asks for the type given a relation id we manually make the repo return the right one
    Mockito.when(repo.getRelationTypeById("replacesRel", true)).thenReturn(replacesRel);
    Mockito.when(repo.getRelationTypeById("translationRel", true)).thenReturn(translationRel);
    Mockito.when(repo.getRelationTypeById("critiqueRel", true)).thenReturn(critiqueRel);

    return repo;
  }

  @Test
  public void aCallWithoutTypesShouldReturnAllTypes() throws Exception {
    Repository repo = initializeRepository();
    ProjectADocument startDoc = repo.getEntityOrDefaultVariation(ProjectADocument.class, "startDoc");

    GraphBuilder b = new GraphBuilder(repo);
    b.addEntity(startDoc, 1, null);
    D3Graph g = b.getGraph();
    assertEquals(4, g.nodeCount());
  }

  @Test
  public void aCallWithEmptyTypeListShouldReturnAllTypes() throws Exception {
    Repository repo = initializeRepository();
    ProjectADocument startDoc = repo.getEntityOrDefaultVariation(ProjectADocument.class, "startDoc");

    GraphBuilder b = new GraphBuilder(repo);
    b.addEntity(startDoc, 1, null);
    D3Graph g = b.getGraph();
    assertEquals(4, g.nodeCount());
  }

  @Test
  public void aCallWithTypeListShouldReturnOnlyThoseTypes() throws Exception {
    Repository repo = initializeRepository();
    ProjectADocument startDoc = repo.getEntityOrDefaultVariation(ProjectADocument.class, "startDoc");

    GraphBuilder b = new GraphBuilder(repo);
    b.addEntity(startDoc, 1, Lists.newArrayList("replacesType"));
    D3Graph g = b.getGraph();
    assertEquals(2, g.nodeCount());
  }
}