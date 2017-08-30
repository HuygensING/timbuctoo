package nl.knaw.huygens.timbuctoo.bulkupload.loaders;

import nl.knaw.huygens.timbuctoo.bulkupload.loaders.access.MdbLoader;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.csv.CsvLoader;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.dataperfect.DataPerfectLoader;
import nl.knaw.huygens.timbuctoo.bulkupload.loaders.excel.allsheetloader.AllSheetLoader;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import javax.ws.rs.core.MediaType;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class LoaderFactory {
  public static Optional<Loader> createFor(MediaType mediaType, FormDataMultiPart formData) {
    switch (mediaType.toString()) {
      case "text/csv":
        Map<String, String> extraConfig = formData.getFields().entrySet().stream()
          .filter(entry -> entry.getValue().size() > 0 && entry.getValue().get(0) != null)
          .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get(0).getValue()));

        return Optional.of(new CsvLoader(extraConfig));
      case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
        return Optional.of(new AllSheetLoader());
      case "application/x.dataperfect":
        return Optional.of(new DataPerfectLoader());
      case "application/x-msaccess":
        return Optional.of(new MdbLoader());
      default:
        return Optional.empty();
    }
  }
}
