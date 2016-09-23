package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.rdf.Collection;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import org.apache.jena.graph.Triple;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.rdf.TripleHelper.createSingleTriple;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CollectionMembershipTripleProcessorTest {
  private static final String ABADAN_URI = "http://tl.dbpedia.org/resource/Abadan,_Iran";
  private static final String IRAN_URI = "http://tl.dbpedia.org/resource/Iran";
  private static final String IS_PART_OF_URI = "http://tl.dbpedia.org/ontology/isPartOf";

  private static final String ABADAN_IS_PART_OF_IRAN_TRIPLE =
    "<" + ABADAN_URI + "> " +
      "<" + IS_PART_OF_URI + "> " +
      "<" + IRAN_URI + "> .";

  @Test
  public void processAddsTheEntityToTheRequestedCollectionAndRemovesItFromTheDefaultCollection() {
    Database database = mock(Database.class);
    Triple triple = createSingleTriple(ABADAN_IS_PART_OF_IRAN_TRIPLE);
    Collection collectionFromTriple = mock(Collection.class);
    Collection archetypeCollection = mock(Collection.class);
    when(collectionFromTriple.getArchetype()).thenReturn(Optional.of(archetypeCollection));
    when(database.findOrCreateCollection("vreName", triple.getObject())).thenReturn(collectionFromTriple);
    Collection defaultCollection = mock(Collection.class);
    when(database.getDefaultCollection("vreName")).thenReturn(defaultCollection);
    Entity entity = mock(Entity.class);
    when(database.findOrCreateEntity("vreName", triple.getSubject())).thenReturn(entity);
    CollectionMembershipTripleProcessor instance = new CollectionMembershipTripleProcessor(database);

    instance.process("vreName", true, triple);

    InOrder inOrder = inOrder(entity);
    inOrder.verify(entity).moveToCollection(defaultCollection, collectionFromTriple);
  }

}
