package nl.knaw.huygens.timbuctoo.bulkupload.savers;

import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.ImportProperty;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.ImportPropertyDescription;
import nl.knaw.huygens.timbuctoo.bulkupload.parsingstatemachine.ImportPropertyDescriptions;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.QuadHandler;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.exceptions.LogProcessingFailedException;
import org.slf4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.BULK_GRAPH;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.HTTP_TIMBUCTOO_COLLECTIONS;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.HTTP_TIMBUCTOO_PROPS;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.OF_COLLECTION;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.ORDER_PROP;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RAW_ROW;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDFS_LABEL;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.STRING;
import static org.apache.xmlbeans.XmlErrorCodes.INTEGER;
import static org.slf4j.LoggerFactory.getLogger;

public class RdfSaver implements Saver<String> {
  private final QuadHandler quadHandler;
  protected long curLine;
  protected long curEntity;
  protected long curCollection;
  private static final Logger LOG = getLogger(RdfSaver.class);

  public RdfSaver(QuadHandler quadHandler) {
    this.quadHandler = quadHandler;
    curLine = 0;
    curEntity = 0;
    curCollection = 0;
  }

  private String encode(String input) {
    try {
      return URLEncoder.encode(input, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      //will never happen
      throw new RuntimeException(e);
    }
  }

  private String makeEntityUri() {
    return RAW_ROW + curEntity++;
  }

  private String makeCollectionUri() {
    return HTTP_TIMBUCTOO_COLLECTIONS + curCollection++;
  }

  @Override
  public String addEntity(String collection, List<ImportProperty> currentProperties) {
    String subject = makeEntityUri();
    try {
      quadHandler.onRelation(curLine++, subject, RDF_TYPE, collection, BULK_GRAPH);
      for (ImportProperty prop : currentProperties) {
        String propName = prop.getName();
        quadHandler.onLiteral(
          curLine++,
          subject,
          makePropUri(propName),
          prop.getValue(),
          STRING,
          BULK_GRAPH
        );
      }

    } catch (LogProcessingFailedException e) {
      LOG.error("Import failed", e);
    }
    return subject;
  }

  private String makePropUri(String propName) {
    return HTTP_TIMBUCTOO_PROPS + encode(propName);
  }

  @Override
  public String addCollection(String collectionName) {
    String collectionUri = makeCollectionUri();
    try {
      quadHandler.onLiteral(
        curLine++,
        collectionUri,
        RDFS_LABEL,
        collectionName,
        STRING,
        BULK_GRAPH
      );
      quadHandler.onLiteral(
        curLine++,
        collectionUri,
        "http://timbuctoo.com/things/order",
        curCollection + "",
        INTEGER,
        BULK_GRAPH
      );
    } catch (LogProcessingFailedException e) {
      LOG.error("Import failed", e);
    }
    return collectionUri;
  }

  @Override
  public void addPropertyDescriptions(String collection, ImportPropertyDescriptions importPropertyDescriptions) {
    for (ImportPropertyDescription importPropertyDescription : importPropertyDescriptions) {
      try {
        String propUri = makePropUri(importPropertyDescription.getPropertyName());
        quadHandler.onRelation(
          curLine++,
          propUri,
          RDF_TYPE,
          "http://timbuctoo.com/things/propertyDescription/",
          BULK_GRAPH
        );
        quadHandler.onRelation(
          curLine++,
          propUri,
          OF_COLLECTION,
          collection,
          BULK_GRAPH
        );
        quadHandler.onLiteral(
          curLine++,
          propUri,
          "http://timbuctoo.com/things/propertyId",
          importPropertyDescription.getId() + "",
          INTEGER,
          BULK_GRAPH
        );
        quadHandler.onLiteral(
          curLine++,
          propUri,
          ORDER_PROP,
          importPropertyDescription.getOrder() + "",
          INTEGER,
          BULK_GRAPH
        );
        quadHandler.onLiteral(
          curLine++,
          propUri,
          RDFS_LABEL,
          importPropertyDescription.getPropertyName(),
          STRING,
          BULK_GRAPH
        );
      } catch (LogProcessingFailedException e) {
        LOG.error("Import failed", e);
      }
    }
  }
}
