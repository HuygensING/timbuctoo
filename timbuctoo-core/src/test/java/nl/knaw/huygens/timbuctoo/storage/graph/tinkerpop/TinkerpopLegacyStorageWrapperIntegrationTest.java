package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import nl.knaw.huygens.timbuctoo.storage.DBIntegrationTestHelper;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIntegrationTest;

import org.junit.Ignore;
import org.junit.Test;

public class TinkerpopLegacyStorageWrapperIntegrationTest extends StorageIntegrationTest {

  @Override
  protected DBIntegrationTestHelper createDBIntegrationTestHelper() {
    return new TinkerpopDBIntegrationTestHelper();
  }

  @Test
  @Override
  public void addDomainEntityAddsADomainEntityAndItsPrimitiveVersieToTheDatabase() throws Exception {
    super.addDomainEntityAddsADomainEntityAndItsPrimitiveVersieToTheDatabase();
  }

  @Ignore
  @Test
  @Override
  public void addRelationAddsARelationAndItsPrimitiveVersionToTheDatabase() throws Exception {
    super.addRelationAddsARelationAndItsPrimitiveVersionToTheDatabase();
  }

  @Test
  @Override
  public void addSystemEntityAddsASystemEntityToTheStorageAndReturnsItsId() throws Exception {
    super.addSystemEntityAddsASystemEntityToTheStorageAndReturnsItsId();
  }

  @Ignore
  @Test
  @Override
  public void closeClosesTheDatabaseconnection() {
    super.closeClosesTheDatabaseconnection();
  }

  @Ignore
  @Test
  @Override
  public void countDomainEntityOnlyCountsTheLatestVersions() throws Exception {
    super.countDomainEntityOnlyCountsTheLatestVersions();
  }

  @Ignore
  @Test
  @Override
  public void countDomainEntityReturnsTheCountOfThePrimitive() throws Exception {
    super.countDomainEntityReturnsTheCountOfThePrimitive();
  }

  @Ignore
  @Test
  @Override
  public void countRelationsOnlyCountsTheLatest() throws Exception {
    super.countRelationsOnlyCountsTheLatest();
  }

  @Ignore
  @Test
  @Override
  public void countSystemEntityReturnsAllTheNumberOfEntitiseOfACertainType() throws Exception {
    super.countSystemEntityReturnsAllTheNumberOfEntitiseOfACertainType();
  }

  @Ignore
  @Test
  @Override
  public void declineRelationsOfEntitySetsAcceptedToFalseForTheVariation() throws Exception {
    super.declineRelationsOfEntitySetsAcceptedToFalseForTheVariation();
  }

  @Ignore
  @Test
  @Override
  public void deleteDomainEntityRemovesTheEntityFromTheDatabase() throws Exception {
    super.deleteDomainEntityRemovesTheEntityFromTheDatabase();
  }

  @Ignore
  @Test
  @Override
  public void deleteNonPersistentDomainEntityRemovesTheCompleteDomainEntity() throws Exception {
    super.deleteNonPersistentDomainEntityRemovesTheCompleteDomainEntity();
  }

  @Ignore
  @Test
  @Override
  public void deleteRelationsOfEntityRemovesAllTheRelationsConnectedToTheEntity() throws Exception {
    super.deleteRelationsOfEntityRemovesAllTheRelationsConnectedToTheEntity();
  }

  @Ignore
  @Test
  @Override
  public void deleteSystemEntityRemovesAnEntityFromTheDatabase() throws StorageException {
    super.deleteSystemEntityRemovesAnEntityFromTheDatabase();
  }

  @Ignore
  @Test
  @Override
  public void deleteVariationRemovesTheVariationFromTheEntity() throws Exception {
    super.deleteVariationRemovesTheVariationFromTheEntity();
  }

  @Ignore
  @Test
  @Override
  public void entityExistsForDomainEntityShowsIfTheEntityExistsInTheDatabase() throws Exception {
    super.entityExistsForDomainEntityShowsIfTheEntityExistsInTheDatabase();
  }

  @Ignore
  @Test
  @Override
  public void entityExistsForRelationShowsIfTheEntityExistsInTheDatabase() throws Exception {
    super.entityExistsForRelationShowsIfTheEntityExistsInTheDatabase();
  }

  @Ignore
  @Test
  @Override
  public void entityExistsForSystemEntityShowsIfTheEntityExistsInTheDatabase() throws Exception {
    super.entityExistsForSystemEntityShowsIfTheEntityExistsInTheDatabase();
  }

  @Ignore
  @Test
  @Override
  public void findItemByPropertyForDomainEntityReturnsTheFirstDomainEntityFound() throws StorageException {
    super.findItemByPropertyForDomainEntityReturnsTheFirstDomainEntityFound();
  }

  @Ignore
  @Test
  @Override
  public void findItemByPropertyForRelationReturnsTheFirstRelationFound() throws Exception {
    super.findItemByPropertyForRelationReturnsTheFirstRelationFound();
  }

  @Ignore
  @Test
  @Override
  public void findItemByPropertyForSystemEntityReturnsTheFirstFoundInTheDatabase() throws Exception {
    super.findItemByPropertyForSystemEntityReturnsTheFirstFoundInTheDatabase();
  }

  @Ignore
  @Test
  @Override
  public void findRelationSearchesARelationByClassSourceIdTargetIdAndTypeId() throws Exception {
    super.findRelationSearchesARelationByClassSourceIdTargetIdAndTypeId();
  }

  @Ignore
  @Test
  @Override
  public void getAllIdsWithoutPIDForDomainEntityReturnsTheNonPersistentDomainEntities() throws Exception {
    super.getAllIdsWithoutPIDForDomainEntityReturnsTheNonPersistentDomainEntities();
  }

  @Ignore
  @Test
  @Override
  public void getAllIdsWithoutPIDForRelationReturnsTheIdsOfNonPersistentDomainEntities() throws Exception {
    super.getAllIdsWithoutPIDForRelationReturnsTheIdsOfNonPersistentDomainEntities();
  }

  @Ignore
  @Test
  @Override
  public void getAllVariationsReturnsAllTheVariationsOfADomainEntity() throws Exception {
    super.getAllVariationsReturnsAllTheVariationsOfADomainEntity();
  }

  @Ignore
  @Test
  @Override
  public void getDomainEntitiesReturnsAllDomainEntitiesOfTheRequestedType() throws Exception {
    super.getDomainEntitiesReturnsAllDomainEntitiesOfTheRequestedType();
  }

  @Ignore
  @Test
  @Override
  public void getEntityOrDefaultVariationReturnsTheEntityAndItsValuesIfItExistsElseItReturnsTheEntityWithTheValuesOfTheDefaultVariation() throws Exception {
    super.getEntityOrDefaultVariationReturnsTheEntityAndItsValuesIfItExistsElseItReturnsTheEntityWithTheValuesOfTheDefaultVariation();
  }

  @Ignore
  @Test
  @Override
  public void getRelationIdsReturnsAListOfRelationIdsThatBelongToTheEntityIds() throws Exception {
    super.getRelationIdsReturnsAListOfRelationIdsThatBelongToTheEntityIds();
  }

  @Ignore
  @Test
  @Override
  public void getRelationsByEntityIdReturnsAllTheIncomingAndOutgoingRelationsOfAnEntity() throws Exception {
    super.getRelationsByEntityIdReturnsAllTheIncomingAndOutgoingRelationsOfAnEntity();
  }

  @Ignore
  @Test
  @Override
  public void getSystemEntitiesReturnsAllTheSystemEntitiesOfACertainType() throws Exception {
    super.getSystemEntitiesReturnsAllTheSystemEntitiesOfACertainType();
  }

  @Ignore
  @Test
  @Override
  public void setPIDForRelationCreatesANewRevisionAndFillsThePID() throws Exception {
    super.setPIDForRelationCreatesANewRevisionAndFillsThePID();
  }

  @Ignore
  @Test
  @Override
  public void setPIDGivesTheDomainEntityAPidAndCreatesAVersion() throws Exception {
    super.setPIDGivesTheDomainEntityAPidAndCreatesAVersion();
  }

  @Test
  @Override
  public void updateDomainEntityIncreasesTheRevisionNumberAndChangesTheDomainEntityButDoesNotCreateANewVersion() throws Exception {
    super.updateDomainEntityIncreasesTheRevisionNumberAndChangesTheDomainEntityButDoesNotCreateANewVersion();
  }

  @Ignore
  @Test
  @Override
  public void updateDomainEntityWithADifferentTypeAddsTheNewFields() throws Exception {
    super.updateDomainEntityWithADifferentTypeAddsTheNewFields();
  }

  @Ignore
  @Test
  @Override
  public void updateRelationUpdatesTheValuesOfTheRelationAndIncreasesTheRevButDoesNotCreateANewRevision() throws Exception {
    super.updateRelationUpdatesTheValuesOfTheRelationAndIncreasesTheRevButDoesNotCreateANewRevision();
  }

  @Test
  @Override
  public void updateSystemEntityChangesTheExistingSystemEntity() throws Exception {
    super.updateSystemEntityChangesTheExistingSystemEntity();
  }

}
