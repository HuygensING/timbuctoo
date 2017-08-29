package nl.knaw.huygens.timbuctoo.v5.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.HTTP_TIMBUCTOO_COLLECTIONS;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.HTTP_TIMBUCTOO_PROPS;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RAW_ROW;

public class TimbuctooRdfIdHelper {
  private final String baseUri;

  public TimbuctooRdfIdHelper(String baseUri) {
    this.baseUri = baseUri;
  }

  public static String rawEntity(String ownerId, String dataSet, String fileName, int entityId) {
    return RAW_ROW + "" + encode(ownerId) + "/" + encode(dataSet) + "/" + encode(fileName) + "/" + entityId;
  }

  public static String rawFile(String ownerId, String dataSet, String fileName) {
    return RAW_ROW + "" + encode(ownerId) + "/" + encode(dataSet) + "/" + encode(fileName) + "/";
  }

  public static String rawCollection(String ownerId, String dataSetId, String fileName, int collectionId) {
    return HTTP_TIMBUCTOO_COLLECTIONS + encode(ownerId) + "/" + encode(dataSetId) + "/" + encode(fileName) + "/" +
      collectionId;
  }

  public static String propertyDescription(String ownerId, String dataSetId, String fileName, String propertyName) {
    return HTTP_TIMBUCTOO_PROPS + encode(ownerId) + "/" + encode(dataSetId) + "/" + encode(fileName) + "/" +
      encode(propertyName);
  }

  public static String dataSet(String ownerId, String dataSetId) {
    return RdfConstants.HTTP_TIMBUCTOO_DATA_SETS + encode(ownerId) + "/" + encode(dataSetId);
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
