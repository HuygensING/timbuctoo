package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import nl.knaw.huygens.timbuctoo.rdf.Relation;
import nl.knaw.huygens.timbuctoo.rdf.RelationType;
import nl.knaw.huygens.timbuctoo.rdf.TripleHelper;
import org.apache.jena.graph.Triple;
import org.junit.Test;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AddRelationTripleProcessorTest {
  private static final String ABADAN_URI = "http://tl.dbpedia.org/resource/Abadan,_Iran";
  private static final String IRAN_URI = "http://tl.dbpedia.org/resource/Iran";
  private static final String IS_PART_OF_URI = "http://tl.dbpedia.org/ontology/isPartOf";

  private static final String ABADAN_IS_PART_OF_IRAN_TRIPLE =
    "<" + ABADAN_URI + "> " +
      "<" + IS_PART_OF_URI + "> " +
      "<" + IRAN_URI + "> .";

  @Test
  public void processCreatesANewRelationBetweenSubjectAndObjectOfTheTriple() {
    final Database database = mock(Database.class);
    final Entity subjectEntity = mock(Entity.class);
    final Entity objectEntity = mock(Entity.class);
    final Relation relation = mock(Relation.class);
    final RelationType relationType = mock(RelationType.class);
    final AddRelationTripleProcessor instance = new AddRelationTripleProcessor(database);
    final Triple triple = TripleHelper.createTripleIterator(ABADAN_IS_PART_OF_IRAN_TRIPLE).next();
    final String vreName = "vreName";
    given(database.findOrCreateEntity(vreName, triple.getSubject())).willReturn(subjectEntity);
    given(database.findOrCreateEntity(vreName, triple.getObject())).willReturn(objectEntity);
    given(database.findOrCreateRelationType(triple.getPredicate())).willReturn(relationType);
    given(subjectEntity.addRelation(any(), any())).willReturn(relation);

    instance.process(vreName, triple);

    verify(subjectEntity).addRelation(relationType, objectEntity);
    verify(relation).setCommonVreProperties(vreName);

  }

}
