package nl.knaw.huygens.timbuctoo.v5.bulkupload;

import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.ImportPropertyDescriptions;
import nl.knaw.huygens.timbuctoo.bulkupload.savers.Saver;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.PromotedDataSet;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.INTEGER;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.OF_COLLECTION;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.PROV_DERIVED_FROM;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDFS_LABEL;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.STRING;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIMBUCTOO_ORDER;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_COLLECTION;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_HAS_ROW;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_MIMETYPE;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_PROP_DESC;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_PROP_ID;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_PROP_NAME;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_TABULAR_FILE;

public class RawUploadRdfSaver implements Saver<String> {

  private static final Logger LOG = LoggerFactory.getLogger(RawUploadRdfSaver.class);
  private final String dataSetUri;
  private final RdfSerializer saver;
  private int curEntity;
  private int curCollection;
  private final String fileUri;

  public RawUploadRdfSaver(PromotedDataSet dataSet, String fileName, MediaType mimeType,
                           RdfSerializer saver)
    throws LogStorageFailedException {
    this.saver = saver;
    this.curEntity = 0;
    this.curCollection = 0;

    this.dataSetUri = dataSet.getBaseUri();

    String prefix = dataSet.getBaseUri();
    if (!prefix.endsWith("/") && !prefix.endsWith("#") && !prefix.endsWith("?")) {
      //it might have some parts

      //?foo
      //?foo=bar
      //?boo&foo
      //?boo&foo=bar
      //#foo
      //#foo=bar
      //#boo&foo
      //#boo&foo=bar
      if (prefix.contains("#") || prefix.contains("?")) {
        if (!prefix.endsWith("&")) {
          prefix += "&";
        }
      } else {
        prefix += "/";
      }
    }
    fileUri = prefix + "rawData/" + encode(fileName) + "/";
    saver.onRelation(fileUri, RDF_TYPE, TIM_TABULAR_FILE, this.dataSetUri);
    saver.onRelation(this.dataSetUri, PROV_DERIVED_FROM, fileUri, this.dataSetUri);
    saver.onValue(fileUri, TIM_MIMETYPE, mimeType.toString(), STRING, this.dataSetUri);
  }

  @Override
  public String addEntity(String collection, Map<String, String> currentProperties) {
    String subject = rawEntity(++curEntity);

    try {
      saver.onRelation(subject, RDF_TYPE, collection, dataSetUri);
      saver.onRelation(subject, TIM_HAS_ROW, collection, dataSetUri);
    } catch (LogStorageFailedException e) {
      LOG.error("Could not save entity");
    }

    for (Map.Entry<String, ?> property : currentProperties.entrySet()) {
      try {
        String propName = propertyDescription(property.getKey());
        saver.onValue(subject, propName, "" + property.getValue(), STRING, dataSetUri);
      } catch (LogStorageFailedException e) {
        LOG.error("Could not add property '{}' with value '{}'", property.getKey(), property.getValue());
      }
    }

    String timIdPropname = propertyDescription("tim_id");
    try {
      saver.onValue(subject, timIdPropname, UUID.randomUUID().toString(), STRING, dataSetUri);
    } catch (LogStorageFailedException e) {
      LOG.error("Could not add tim_id property.");
    }


    return subject;
  }

  @Override
  public String addCollection(String collectionName) {
    String subject = rawCollection(++curCollection);

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
    String propertyUri = propertyDescription(propertyName);
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

  public String rawEntity(int entityId) {
    return fileUri + "entities/" + entityId;
  }

  public String rawCollection(int collectionId) {
    return fileUri + "collections/" + collectionId;
  }

  public String propertyDescription(String propertyName) {
    return fileUri + "props/" + encode(propertyName);
  }

  private static String encode(String input) {
    try {
      return URLEncoder.encode(input, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      //will never happen
      throw new RuntimeException(e);
    }
  }
}
