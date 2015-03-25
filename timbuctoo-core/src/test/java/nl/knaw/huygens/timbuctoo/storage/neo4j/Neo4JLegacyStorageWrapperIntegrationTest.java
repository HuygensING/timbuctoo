package nl.knaw.huygens.timbuctoo.storage.neo4j;

import nl.knaw.huygens.timbuctoo.storage.DBIntegrationTestHelper;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIntegrationTest;

import org.junit.Ignore;
import org.junit.Test;

public class Neo4JLegacyStorageWrapperIntegrationTest extends StorageIntegrationTest {

  @Override
  protected DBIntegrationTestHelper createDBIntegrationTestHelper() {
    return new Neo4JDBIntegrationTestHelper();
  }

  @Test
  @Override
  public void addDomainEntityAddsADomainEntityAndItsPrimitiveVersieToTheDatabase() throws Exception {
    // TODO Auto-generated method stub
    super.addDomainEntityAddsADomainEntityAndItsPrimitiveVersieToTheDatabase();
  }

  @Test
  @Override
  public void addRelationAddsARelationAndItsPrimitiveVersieToTheDatabase() throws Exception {
    // TODO Auto-generated method stub
    super.addRelationAddsARelationAndItsPrimitiveVersieToTheDatabase();
  }

  @Test
  @Override
  public void addSystemEntityAddsASystemEntityToTheStorageAndReturnsItsId() throws Exception {
    // TODO Auto-generated method stub
    super.addSystemEntityAddsASystemEntityToTheStorageAndReturnsItsId();
  }

  @Test
  @Ignore
  @Override
  public void declineRelationsOfEntitySetsAcceptedToFalseForTheVariation() throws Exception {
    // TODO Auto-generated method stub
    super.declineRelationsOfEntitySetsAcceptedToFalseForTheVariation();
  }

  @Test
  @Override
  public void deleteDomainEntityRemovesTheEntityFromTheDatabase() throws Exception {
    // TODO Auto-generated method stub
    super.deleteDomainEntityRemovesTheEntityFromTheDatabase();
  }

  @Test
  @Ignore
  @Override
  public void deleteNonPersistentDomainEntityRemovesTheCompleteDomainEntity() throws Exception {
    // TODO Auto-generated method stub
    super.deleteNonPersistentDomainEntityRemovesTheCompleteDomainEntity();
  }

  @Test
  @Ignore
  @Override
  public void deleteRelationsOfEntityRemovesAllTheRelationsConnectedToTheEntity() throws Exception {
    // TODO Auto-generated method stub
    super.deleteRelationsOfEntityRemovesAllTheRelationsConnectedToTheEntity();
  }

  @Test
  @Override
  public void deleteSystemEntityRemovesAnEntityFromTheDatabase() throws StorageException {
    // TODO Auto-generated method stub
    super.deleteSystemEntityRemovesAnEntityFromTheDatabase();
  }

  @Test
  @Ignore
  @Override
  public void deleteVariationRemovesTheVariationFromTheEntity() throws Exception {
    // TODO Auto-generated method stub
    super.deleteVariationRemovesTheVariationFromTheEntity();
  }

  @Test
  @Ignore
  @Override
  public void getAllVariationsReturnsAllTheVariationsOfADomainEntity() throws Exception {
    // TODO Auto-generated method stub
    super.getAllVariationsReturnsAllTheVariationsOfADomainEntity();
  }

  @Test
  @Ignore
  @Override
  public void getDomainEntitiesReturnsAllDomainEntitiesOfTheRequestedType() throws Exception {
    // TODO Auto-generated method stub
    super.getDomainEntitiesReturnsAllDomainEntitiesOfTheRequestedType();
  }

  @Test
  @Ignore
  @Override
  public void getSystemEntitiesReturnsAllTheSystemEntitiesOfACertainType() throws Exception {
    // TODO Auto-generated method stub
    super.getSystemEntitiesReturnsAllTheSystemEntitiesOfACertainType();
  }

  @Test
  @Override
  public void setPIDGivesTheDomainEntityAPidAndCreatesAVersion() throws Exception {
    // TODO Auto-generated method stub
    super.setPIDGivesTheDomainEntityAPidAndCreatesAVersion();
  }

  @Test
  @Override
  public void updateDomainIncreasesTheRevisionNumberAndChangesTheDomainEntityButDoesNotCreateANewVersion() throws Exception {
    // TODO Auto-generated method stub
    super.updateDomainIncreasesTheRevisionNumberAndChangesTheDomainEntityButDoesNotCreateANewVersion();
  }

  @Test
  @Override
  public void updateSystemEntityChangesTheExistingSystemEntity() throws Exception {
    // TODO Auto-generated method stub
    super.updateSystemEntityChangesTheExistingSystemEntity();
  }

  @Test
  @Override
  public void updateRelationUpdatesTheValuesOfTheRelationAndIncreasesTheRevButDoesNotCreateANewRevision() throws Exception {
    // TODO Auto-generated method stub
    super.updateRelationUpdatesTheValuesOfTheRelationAndIncreasesTheRevButDoesNotCreateANewRevision();
  }

  @Test
  @Override
  public void setPIDForRelationCreatesANewRevisionAndFillsThePID() throws Exception {
    // TODO Auto-generated method stub
    super.setPIDForRelationCreatesANewRevisionAndFillsThePID();
  }

  @Test
  @Override
  public void closeClosesTheDatabaseconnection() {
    super.closeClosesTheDatabaseconnection();
  }

}
