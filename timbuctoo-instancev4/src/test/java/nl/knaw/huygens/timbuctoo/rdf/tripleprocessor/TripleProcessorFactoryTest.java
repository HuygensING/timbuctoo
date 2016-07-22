package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.TripleHelper;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class TripleProcessorFactoryTest {

  public static final String ARCHETYPE = "object";
  private static final String SUBCLASS_OF_TRIPLE =
    "<http://www.example.com/subject> <http://www.w3.org/2000/01/rdf-schema#subClassOf> " +
      "<http://www.example.com/" + ARCHETYPE + "> .";
  private Database database;
  private TripleProcessorFactory instance;

  @Before
  public void setUp() throws Exception {
    database = mock(Database.class);
    instance = new TripleProcessorFactory(database);
  }

  @Test
  public void getTripleProcessorReturnsAddToArchetypeTripleProcessorWhenThePredicateIsASubClassOfAndTheObjectIsKnown() {
    given(database.isKnownArchetype(ARCHETYPE)).willReturn(true);

    TripleProcessor tripleProcessor = instance.getTripleProcessor(TripleHelper.createSingleTriple(SUBCLASS_OF_TRIPLE));

    assertThat(tripleProcessor, is(instanceOf(AddToArchetypeTripleProcessor.class)));
  }

  @Test
  public void getTripleProcessorReturnsAddToArchetypeTripleProcessorWhenThePredicateIsASubClassOf() {
    given(database.isKnownArchetype(ARCHETYPE)).willReturn(false);

    TripleProcessor tripleProcessor = instance.getTripleProcessor(TripleHelper.createSingleTriple(SUBCLASS_OF_TRIPLE));

    assertThat(tripleProcessor, is(instanceOf(AddRelationTripleProcessor.class)));
  }

}
