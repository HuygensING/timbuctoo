package nl.knaw.huygens.repository.importer.database;

import java.io.File;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.atla.AtlaPerson;
import nl.knaw.huygens.repository.model.atla.AtlaPlace;
import nl.knaw.huygens.repository.util.Progress;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

public class AtlantischeGidsImporter {

  // Maps for converting from project id to storage id
  private Map<String, String> personIdMap;
  private Map<String, String> placeIdMap;

  public AtlantischeGidsImporter() {
    personIdMap = Maps.newHashMap();
    placeIdMap = Maps.newHashMap();
  }

  public void importData(String path, StorageManager storageManager) throws Exception {
    importPersons(storageManager, path + "keywords/keyw_pers.json");
    importPlaces(storageManager, path + "keywords/keyw_geo.json");
  }

  private void importPersons(StorageManager storageManager, String path) throws Exception {
    System.out.printf("%n=== Import documents of type '%s'%n", AtlaPerson.class.getSimpleName());
    File file = new File(path);
    if (file.canRead()) {
      Progress progress = new Progress();
      ObjectMapper mapper = new ObjectMapper();
      PersonKeywords keywords = mapper.readValue(file, PersonKeywords.class);
      for (Map<String, String> map : keywords.keywords_pers) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
          progress.step();
          AtlaPerson person = new AtlaPerson();
          person.name = entry.getValue();
          storageManager.addDocument(AtlaPerson.class, person);
          personIdMap.put(entry.getKey(), person.getId());
        }
      }
      progress.done();
    } else {
      System.err.println("Cannot read " + file.getCanonicalPath());
    }
  }

  private void importPlaces(StorageManager storageManager, String path) throws Exception {
    System.out.printf("%n=== Import documents of type '%s'%n", AtlaPlace.class.getSimpleName());
    File file = new File(path);
    if (file.canRead()) {
      Progress progress = new Progress();
      ObjectMapper mapper = new ObjectMapper();
      PlaceKeywords keywords = mapper.readValue(file, PlaceKeywords.class);
      for (Map<String, String> map : keywords.keywords_geo) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
          progress.step();
          AtlaPlace place = new AtlaPlace();
          place.name = entry.getValue();
          storageManager.addDocument(AtlaPlace.class, place);
          placeIdMap.put(entry.getKey(), place.getId());
        }
      }
      progress.done();
    } else {
      System.err.println("Cannot read " + file.getCanonicalPath());
    }
  }

  // --- Helper classes ------------------------------------------------

  private static class PersonKeywords {
    public List<Map<String, String>> keywords_pers;
  }

  private static class PlaceKeywords {
    public List<Map<String, String>> keywords_geo;
  }

}
