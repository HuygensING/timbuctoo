package nl.knaw.huygens.timbuctoo.bulkupload.savers;

import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.ImportPropertyDescriptions;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfSerializer;
import nl.knaw.huygens.timbuctoo.v5.util.TimbuctooRdfIdHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.INTEGER;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.OF_COLLECTION;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDFS_LABEL;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.STRING;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIMBUCTOO_ORDER;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_COLLECTION;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_HAS_ROW;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_PROP_DESC;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_PROP_ID;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_PROP_NAME;
import static nl.knaw.huygens.timbuctoo.v5.util.TimbuctooRdfIdHelper.propertyDescription;

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
      saver.onRelation(subject, TIM_HAS_ROW, collection, dataSetUri);
    } catch (LogStorageFailedException e) {
      LOG.error("Could not save entity");
    }

    for (Map.Entry<String, ?> property : currentProperties.entrySet()) {
      try {
        String propName = propertyDescription(dataSetId, fileName, property.getKey());
        saver.onValue(subject, propName, "" + property.getValue(), STRING, dataSetUri);
      } catch (LogStorageFailedException e) {
        LOG.error("Could not add property '{}' with value '{}'", property.getKey(), property.getValue());
      }
    }

    String timIdPropname = propertyDescription(dataSetId, fileName, "tim_id");
    try {
      saver.onValue(subject, timIdPropname, UUID.randomUUID().toString(), STRING, dataSetUri);
    } catch (LogStorageFailedException e) {
      LOG.error("Could not add tim_id property.");
    }


    return subject;
  }

  @Override
  public String addCollection(String collectionName) {
    String subject = TimbuctooRdfIdHelper.rawCollection(dataSetId, fileName, ++curCollection);

    try {
      saver.onRelation(subject, RDF_TYPE, TIM_COLLECTION, dataSetUri);
      saver.onValue(subject, RDFS_LABEL, collectionName, STRING, dataSetUri);
    } catch (LogStorageFailedException e) {
      LOG.error("Could not add label '{}' to collection '{}'", collectionName, subject);
      //FIXME: should break the processing
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
      String propertyName = prop.getPropertyName();
      Integer id = prop.getId();
      int order = prop.getOrder();
      addPropertyDescription(collection, propertyName, id, order);
    });
    addPropertyDescription(collection, "tim_id", -1, -1);
  }

  public void addPropertyDescription(String collection, String propertyName, Integer id, int order) {
    String propertyUri = propertyDescription(dataSetId, fileName, propertyName);
    try {
      saver.onRelation(propertyUri, RDF_TYPE, TIM_PROP_DESC, dataSetUri);
      //FIXME: add collection hasProperty propdesc
      saver.onValue(propertyUri, TIM_PROP_ID, "" + id, INTEGER, dataSetUri);
      saver.onValue(propertyUri, TIMBUCTOO_ORDER, "" + order, INTEGER, dataSetUri);
      saver.onValue(propertyUri, RDFS_LABEL, propertyName, STRING, dataSetUri);
      saver.onValue(propertyUri, TIM_PROP_NAME, propertyName, STRING, dataSetUri);
    } catch (LogStorageFailedException e) {
      LOG.error("Could add property description for '{}'", propertyUri);
    }

    try {
      saver.onRelation(propertyUri, OF_COLLECTION, collection, dataSetUri);
    } catch (LogStorageFailedException e) {
      LOG.error("Could not add property description '{}' to collection '{}'", propertyUri, collection);
    }
  }
}