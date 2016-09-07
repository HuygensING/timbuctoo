package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.rdf.Collection;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ArchetypeTripleProcessorTest {

  public static final String VRE_NAME = "vreName";
  public static final String OBJECT_URI = "http://example.com/object";
  private Collection collection;
  private Collection archetypeCollection;
  private Triple triple;
  private ArchetypeTripleProcessor instance;
  private Database database;
  private Entity entity1;
  private Entity entity2;
  private Collection previousArchetype;

  @Before
  public void setup() {
    collection = mock(Collection.class);
    previousArchetype = mock(Collection.class);
    given(collection.getArchetype()).willReturn(Optional.of(previousArchetype));
    archetypeCollection = mock(Collection.class);
    Node subjectNode = mock(Node.class);
    Node objectNode = mock(Node.class);
    given(objectNode.getURI()).willReturn(OBJECT_URI);
    triple = Triple.create(subjectNode, mock(Node.class), objectNode);
    database = mock(Database.class);
    given(database.findOrCreateCollection(VRE_NAME, subjectNode)).willReturn(collection);
    given(database.findArchetypeCollection(objectNode.getLocalName())).willReturn(Optional.of(archetypeCollection));
    entity1 = mock(Entity.class);
    entity2 = mock(Entity.class);
    Set<Entity> entitiesOfCollection = Sets.newHashSet(entity1, entity2);
    given(database.findEntitiesByCollection(collection)).willReturn(entitiesOfCollection);

    instance = new ArchetypeTripleProcessor(database);
  }

  @Test
  public void processSetsTheCollectionArchetype() {
    instance.process(VRE_NAME, true, triple);

    verify(collection).setArchetype(archetypeCollection, OBJECT_URI);
  }

  @Test
  public void processAddsTheEntitiesOfTheCollectionToTheNewArchetype() {
    instance.process(VRE_NAME, true, triple);

    verify(entity1).addToCollection(archetypeCollection);
    verify(entity2).addToCollection(archetypeCollection);
  }

  @Test
  public void processRemovesThePreviousArchetypeOfTheCollectionFromTheEntities() {
    instance.process(VRE_NAME, true, triple);

    verify(entity1).removeFromCollection(previousArchetype);
    verify(entity2).removeFromCollection(previousArchetype);
  }

}
