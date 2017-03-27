package nl.knaw.huygens.timbuctoo.rdf.tripleprocessor;

import nl.knaw.huygens.timbuctoo.core.RdfImportErrorReporter;
import nl.knaw.huygens.timbuctoo.core.RdfImportSession;
import nl.knaw.huygens.timbuctoo.rdf.Collection;
import nl.knaw.huygens.timbuctoo.rdf.Database;
import nl.knaw.huygens.timbuctoo.rdf.Entity;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CollectionMembershipTripleProcessorTest {
  private static final String SUBJECT_URI = "http://tl.dbpedia.org/resource/Abadan,_Iran";
  private static final String PREDICATE_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
  private static final String OBJECT_NAME = "location";
  private static final String OBJECT_URI = "http://example.com/" + OBJECT_NAME;

  @Test
  public void processMovesTheEntityToTheKnownCollectionIfItIsAnAssertion() {
    Database database = mock(Database.class);
    Collection collectionFromTriple = mock(Collection.class);
    Collection archetypeCollection = mock(Collection.class);
    when(collectionFromTriple.getArchetype()).thenReturn(Optional.of(archetypeCollection));
    when(database.findOrCreateCollection(anyString(), anyString(), anyString())).thenReturn(collectionFromTriple);
    Collection defaultCollection = mock(Collection.class);
    when(database.getDefaultCollection("vreName")).thenReturn(defaultCollection);
    Entity entity = mock(Entity.class);
    when(database.findOrCreateEntity("vreName", SUBJECT_URI)).thenReturn(entity);
    CollectionMembershipTripleProcessor instance = new CollectionMembershipTripleProcessor(database, mock(
      RdfImportSession.class));

    instance.process("vreName", SUBJECT_URI, PREDICATE_URI, OBJECT_URI, true);

    verify(entity).moveToNewCollection(defaultCollection, collectionFromTriple);
  }

  @Test
  public void processLogsAnExceptionWhenTheEntityAlreadyIsPartOfACollectionWhenAsserted() {
    Database database = mock(Database.class);
    Collection collectionFromTriple = mock(Collection.class);
    Collection archetypeCollection = mock(Collection.class);
    when(collectionFromTriple.getArchetype()).thenReturn(Optional.of(archetypeCollection));
    when(database.findOrCreateCollection(anyString(), anyString(), anyString())).thenReturn(collectionFromTriple);
    Collection defaultCollection = mock(Collection.class);
    when(database.getDefaultCollection("vreName")).thenReturn(defaultCollection);
    Entity entity = mock(Entity.class);
    when(entity.isInKnownCollection()).thenReturn(true);
    when(database.findOrCreateEntity("vreName", SUBJECT_URI)).thenReturn(entity);
    RdfImportSession rdfImportSession = mock(RdfImportSession.class);
    RdfImportErrorReporter errorReporter = mock(RdfImportErrorReporter.class);
    when(rdfImportSession.getErrorReporter()).thenReturn(errorReporter);
    CollectionMembershipTripleProcessor instance = new CollectionMembershipTripleProcessor(database, rdfImportSession);

    instance.process("vreName", SUBJECT_URI, PREDICATE_URI, OBJECT_URI, true);

    verify(errorReporter).multipleRdfTypes(SUBJECT_URI, OBJECT_URI);
  }

  @Test
  public void processRemovesTheEntityToTheRequestedCollectionIfItIsARetraction() {
    Database database = mock(Database.class);
    Collection collectionFromTriple = mock(Collection.class);
    Collection archetypeCollection = mock(Collection.class);
    when(collectionFromTriple.getArchetype()).thenReturn(Optional.of(archetypeCollection));
    when(database.findOrCreateCollection(anyString(), anyString(), anyString())).thenReturn(collectionFromTriple);
    Collection defaultCollection = mock(Collection.class);
    when(database.getDefaultCollection("vreName")).thenReturn(defaultCollection);
    Entity entity = mock(Entity.class);
    when(database.findOrCreateEntity("vreName", SUBJECT_URI)).thenReturn(entity);
    CollectionMembershipTripleProcessor instance = new CollectionMembershipTripleProcessor(database, mock(
      RdfImportSession.class));

    instance.process("vreName", SUBJECT_URI, PREDICATE_URI, OBJECT_URI, false);

    InOrder inOrder = inOrder(entity, database);
    inOrder.verify(database).findOrCreateCollection("vreName", OBJECT_URI, OBJECT_NAME);
    inOrder.verify(entity).removeFromCollection(collectionFromTriple);
  }

}
