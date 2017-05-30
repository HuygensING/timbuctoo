package nl.knaw.huygens.timbuctoo.bulkupload.loaders;

import nl.knaw.huygens.timbuctoo.bulkupload.loaders.access.MdbLoader;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.csv.CsvLoader;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.dataperfect.DataPerfectLoader;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.excel.allsheetloader.AllSheetLoader;

import java.util.Map;

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
