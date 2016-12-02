package nl.knaw.huygens.timbuctoo.server.databasemigration;

import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.database.TransactionEnforcer;
import nl.knaw.huygens.timbuctoo.database.TransactionState;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.model.properties.ReadableProperty;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.TinkerPopGraphManager;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.database.dto.RelationType.relationType;
import static nl.knaw.huygens.timbuctoo.model.properties.PropertyTypes.localProperty;

public class PrepareForBiaImportMigration implements DatabaseMigration {
  private static final Logger LOG = LoggerFactory.getLogger(PrepareForBiaImportMigration.class);
  private final Vres mappings;
  private TransactionEnforcer transactionEnforcer;


  public PrepareForBiaImportMigration(Vres vres, TransactionEnforcer transactionEnforcer) {
    this.mappings = vres;
    this.transactionEnforcer = transactionEnforcer;
  }

  @Override
  public void execute(TinkerPopGraphManager graphWrapper) throws IOException {
    final Graph graph = graphWrapper.getGraph();


    LOG.info("Rebuilding Admin VRE from current mappings, adding 'concepts' collection");
    addConceptsCollectionToAdminVre(graph);

    LOG.info("Adding BIA specific relation types");
    addRelationTypes();
  }

  private void addRelationTypes() {
    transactionEnforcer.execute(db -> {
      db.saveRelationTypes(
        // TODO+FIXME, these BIA relationTypes should be made VRE specific and editable before mapping
        // person to person relations
        relationType("concept", "hasFirstPerson", "person", "isFirstPersonInRelation",
          false, false, false, UUID.randomUUID()),
        relationType("concept", "hasSecondPerson", "person", "isSecondPersonInRelation",
          false, false, false, UUID.randomUUID()),
        relationType("concept", "hasPersonToPersonRelationType", "concept", "isPersonToPersonRelationTypeOf",
          false, false, false, UUID.randomUUID()),

        // states of persons
        relationType("concept", "hasStateType", "concept", "isStateTypeOf", false, false, false, UUID.randomUUID()),
        relationType("concept", "isStateOfPerson", "person", "hasPersonState",
          false, false, false, UUID.randomUUID()),
        relationType("concept", "isStateLinkedToInstitute", "collective", "isInstituteLinkedToState",
          false, false, false, UUID.randomUUID()),
        relationType("concept", "isStateLinkedToLocation", "location", "isLocationLinkedToState",
          false, false, false, UUID.randomUUID()),

        // data lines for persons
        relationType("concept", "hasDataLineType", "concept", "isDataLineTypeOf",
          false, false, false, UUID.randomUUID()),
        relationType("concept", "isDataLineForPerson", "person", "hasDataLine",
          false, false, false, UUID.randomUUID()),

        // scientist_bios for persons
        relationType("concept", "hasFieldOfInterest", "concept", "isFieldOfInterestOf",
          false, false, false, UUID.randomUUID()),
        relationType("concept", "isScientistBioOf", "person", "hasScientistBio",
          false, false, false, UUID.randomUUID())
      );
      return TransactionState.commit();
    });
  }

  private void addConceptsCollectionToAdminVre(Graph graph) {
    transactionEnforcer.execute(db -> {

      final Vre adminVre = mappings.getVre("Admin");
      final LinkedHashMap<String, ReadableProperty> conceptProperties = Maps.newLinkedHashMap();
      conceptProperties.put("label", localProperty("label"));

      final Collection conceptsCollection =
        new Collection("concept", "concept", localProperty("label"), conceptProperties, "concepts",
          mappings.getVre("Admin"), "concepts", false, false);

      adminVre.addCollection(conceptsCollection);
      adminVre.save(graph);
      return TransactionState.commit();
    });
  }
}
