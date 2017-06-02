package nl.knaw.huygens.timbuctoo.bulkupload.savers;

import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.ImportPropertyDescriptions;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfSerializer;
import nl.knaw.huygens.timbuctoo.v5.util.TimbuctooRdfIdHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.INTEGER;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.OF_COLLECTION;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDFS_LABEL;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.STRING;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIMBUCTOO_ORDER;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_PROP_DESC;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_PROP_ID;

public class RdfSaver implements Saver<String> {

  private static final Logger LOG = LoggerFactory.getLogger(RdfSaver.class);
  private final String dataSetId;
  private final String dataSetUri;
  private final String fileName;
  private final RdfSerializer saver;
  private int curEntity;
  private int curCollection;

  public RdfSaver(String dataSetId, String fileName, RdfSerializer saver) {
    this.dataSetId = dataSetId;
    this.dataSetUri = TimbuctooRdfIdHelper.dataSet(dataSetId);
    this.fileName = fileName;
    this.saver = saver;
    this.curEntity = 0;
    this.curCollection = 0;
  }

  @Override
  public String addEntity(String collection, Map<String, ?> currentProperties) {
    String subject = TimbuctooRdfIdHelper.rawEntity(dataSetId, fileName, ++curEntity);

    try {
      saver.onRelation(subject, RDF_TYPE, collection, dataSetUri);
    } catch (LogStorageFailedException e) {
      LOG.error("Could not save entity");
    }

    for (Map.Entry<String, ?> property : currentProperties.entrySet()) {
      try {
        String propName = TimbuctooRdfIdHelper.propertyDescription(dataSetId, fileName, property.getKey());
        saver.onValue(subject, propName, "" + property.getValue(), STRING, dataSetUri);
      } catch (LogStorageFailedException e) {
        LOG.error("Could not add property '{}' with value '{}'", property.getKey(), property.getValue());
      }
    }

    return subject;
  }

  @Override
  public String addCollection(String collectionName) {
    String subject = TimbuctooRdfIdHelper.rawCollection(dataSetId, fileName, ++curCollection);

    try {
      saver.onValue(subject, RDFS_LABEL, collectionName, STRING, dataSetUri);
    } catch (LogStorageFailedException e) {
      LOG.error("Could not add label '{}' to collection '{}'", collectionName, subject);
    }

    try {
      saver.onValue(subject, TIMBUCTOO_ORDER, "" + curCollection, INTEGER, dataSetUri);
    } catch (LogStorageFailedException e) {
      LOG.error(
        "Could not add proprerty '{}' with value '{}' to collection '{}'", TIMBUCTOO_ORDER, curCollection, subject
      );
    }

    return subject;
  }

  @Override
  public void addPropertyDescriptions(String collection, ImportPropertyDescriptions importPropertyDescriptions) {
    importPropertyDescriptions.forEach(prop -> {
      // TODO create uri for property name
      String propertyUri = TimbuctooRdfIdHelper.propertyDescription(dataSetId, fileName, prop.getPropertyName());
      try {
          saver.onRelation(propertyUri, RDF_TYPE, TIM_PROP_DESC, dataSetUri);
          saver.onValue(propertyUri, TIM_PROP_ID, "" + prop.getId(), INTEGER, dataSetUri);
          saver.onValue(propertyUri, TIMBUCTOO_ORDER, "" + prop.getOrder(), INTEGER, dataSetUri);
          saver.onValue(propertyUri, RDFS_LABEL, prop.getPropertyName(), STRING, dataSetUri);
        } catch (LogStorageFailedException e) {
          LOG.error("Could add property description for '{}'", propertyUri);
        }

        try {
          saver.onRelation(propertyUri, OF_COLLECTION, collection, dataSetUri);
        } catch (LogStorageFailedException e) {
          LOG.error("Could not add property description '{}' to collection '{}'", propertyUri, collection);
        }

      }
    );
  }
}
