package nl.knaw.huygens.timbuctoo.bulkupload.loaders;

import nl.knaw.huygens.timbuctoo.bulkupload.loaders.access.MdbLoader;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.csv.CsvLoader;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.dataperfect.DataPerfectLoader;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.excel.allsheetloader.AllSheetLoader;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public class LoaderFactory {
  public static Loader createFor(LoaderConfig loaderConfig) {
    switch (loaderConfig.type) {
      case "csv":
        return new CsvLoader(loaderConfig.extraConfig);
      case "xlsx":
        return new AllSheetLoader();
      case "dataperfect":
        return new DataPerfectLoader();
      case "mdb":
        return new MdbLoader();
      default:
        throw new IllegalStateException("Loader unknown for type: " + loaderConfig.type);
    }
  }

  public enum LoaderConfigType {
    CSV("csv", new MediaType("text", "csv")),
    XLSX("xlsx", new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
    DATAPERFECT("dataperfect", MediaType.APPLICATION_OCTET_STREAM_TYPE),
    MDB("mdb", new MediaType("application", "octet-stream"));

    private final String typeName;
    private final MediaType mediaType;

    LoaderConfigType(String typeName, MediaType mediaType) {

      this.typeName = typeName;
      this.mediaType = mediaType;
    }

    public static Optional<LoaderConfigType> fromString(String typeName) {
      return Arrays.stream(LoaderConfigType.values())
                   .filter(type -> type.typeName.equals(typeName))
                   .findFirst();
    }

    public String getTypeString() {
      return this.typeName;
    }

    public MediaType getMediaType() {
      return mediaType;
    }
  }

  public static class LoaderConfig {
    private String type;
    private Map<String, String> extraConfig;

    public LoaderConfig(String type, Map<String, String> extraConfig) {
      this.type = type;
      this.extraConfig = extraConfig;
    }

    public static LoaderConfig configFor(String type) {
      return new LoaderConfig(type, null);
    }

    public static LoaderConfig csvConfig(Map<String, String> extraConfig) {
      return new LoaderConfig("csv", extraConfig);
    }
  }
}
