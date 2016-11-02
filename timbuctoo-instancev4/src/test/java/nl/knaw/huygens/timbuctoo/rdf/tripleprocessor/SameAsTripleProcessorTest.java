package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;


import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import nl.knaw.huygens.timbuctoo.rdf.TripleHelper;
import org.apache.jena.graph.Triple;
import org.junit.Test;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SameAsTripleProcessorTest {
  private static final String ABADAN_URI = "http://tl.dbpedia.org/resource/Abadan,_Iran";
  private static final String ABADAN_URI_2 = "http://n3.dbpedia.org/resource/Abadan,_Iran";
  private static final String SAME_AS_URI = "http://www.w3.org/2002/07/owl#sameAs";

  private static final String ABADAN_IS_PART_OF_IRAN_TRIPLE =
    "<" + ABADAN_URI + "> " +
      "<" + SAME_AS_URI + "> " +
      "<" + ABADAN_URI_2 + "> .";

  @Test
  public void shouldCopyAndRemoveObjectEntityWhenBothEntitiesExist() {
    final Database database = mock(Database.class);
    final String vreName = "vreName";
    final Entity subjectEntity = mock(Entity.class);
    final Entity objectEntity = mock(Entity.class);
    final Optional<Entity> subjectEntityOptional = Optional.of(subjectEntity);
    final Optional<Entity> objectEntityOptional = Optional.of(objectEntity);

    final Triple triple = TripleHelper.createTripleIterator(ABADAN_IS_PART_OF_IRAN_TRIPLE).next();

    final SameAsTripleProcessor instance = new SameAsTripleProcessor(database);

    given(database.findEntity(vreName, triple.getSubject())).willReturn(subjectEntityOptional);
    given(database.findEntity(vreName, triple.getObject())).willReturn(objectEntityOptional);

    instance.process(vreName, true, triple);

    verify(database).mergeObjectIntoSubjectEntity(vreName, subjectEntity, objectEntity);
  }
}
