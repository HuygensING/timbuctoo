package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.rdf.Collection;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.junit.Test;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AddToArchetypeTripleProcessorTest {

  public static final String VRE_NAME = "vreName";

  @Test
  public void processSetsTheCollectionArchetype() {
    Collection collection = mock(Collection.class);
    Collection archetypeCollection = mock(Collection.class);
    Node subjectNode = mock(Node.class);
    Node objectNode = mock(Node.class);
    String objectUri = "http://example.com/object";
    given(objectNode.getURI()).willReturn(objectUri);
    Triple triple = Triple.create(subjectNode, mock(Node.class), objectNode);
    Database database = mock(Database.class);
    given(database.findOrCreateCollection(VRE_NAME, subjectNode)).willReturn(collection);
    given(database.findOrCreateCollection(VRE_NAME, objectNode)).willReturn(archetypeCollection);
    AddToArchetypeTripleProcessor instance = new AddToArchetypeTripleProcessor(database);

    instance.process(triple, VRE_NAME);

    verify(collection).setArchetype(archetypeCollection, objectUri);
  }

}
