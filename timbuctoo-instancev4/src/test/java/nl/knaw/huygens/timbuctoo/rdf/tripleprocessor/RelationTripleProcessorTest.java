package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import nl.knaw.huygens.timbuctoo.rdf.Relation;
import nl.knaw.huygens.timbuctoo.rdf.RelationType;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class RelationTripleProcessorTest {
  private static final String PREDICATE_NAME = "isPartOf";
  private static final String VRE_NAME = "vreName";
  private static final String SUBJECT_URI = "http://tl.dbpedia.org/resource/Abadan,_Iran";
  private static final String OBJECT_URI = "http://tl.dbpedia.org/resource/Iran";
  private static final String PREDICATE_URI = "http://tl.dbpedia.org/ontology/" + PREDICATE_NAME;
  private Entity subjectEntity;
  private Entity objectEntity;
  private Relation relation;
  private RelationType relationType;
  private RelationTripleProcessor instance;

  @Before
  public void setup() {
    final Database database = mock(Database.class);
    subjectEntity = mock(Entity.class);
    objectEntity = mock(Entity.class);
    relation = mock(Relation.class);
    relationType = mock(RelationType.class);
    given(database.findOrCreateEntity(VRE_NAME, SUBJECT_URI)).willReturn(subjectEntity);
    given(database.findOrCreateEntity(VRE_NAME, OBJECT_URI)).willReturn(objectEntity);
    given(database.findOrCreateRelationType(PREDICATE_URI, PREDICATE_NAME)).willReturn(relationType);
    given(subjectEntity.addRelation(any(), any())).willReturn(relation);
    instance = new RelationTripleProcessor(database);
  }

  @Test
  public void processCreatesANewRelationBetweenSubjectAndObjectOfTheTriple() {
    instance.process(VRE_NAME, SUBJECT_URI, PREDICATE_URI, OBJECT_URI, true);

    verify(subjectEntity).addRelation(relationType, objectEntity);
    verify(relation).setCommonVreProperties(VRE_NAME);
  }

  @Test
  public void processRemovesTheRelationWhenTheTripleIsRetracted() {
    instance.process(VRE_NAME, SUBJECT_URI, PREDICATE_URI, OBJECT_URI, false);

    verify(subjectEntity).removeRelation(relationType, objectEntity);
  }

}
