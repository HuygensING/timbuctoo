package nl.knaw.huygens.repository.importer.database;

import java.io.File;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.Person;
import nl.knaw.huygens.repository.model.atla.AtlaPerson;
import nl.knaw.huygens.repository.util.Progress;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

public class AtlantischeGidsImporter {

  // Maps from project id to storage id
  private Map<String, String> personIdMap;

  public AtlantischeGidsImporter() {
    personIdMap = Maps.newHashMap();
  }

  public void importData(String path, StorageManager storageManager) throws Exception {
    File file = new File(path + "keywords/keyw_pers.json");
    if (file.canRead()) {
      Progress progress = new Progress();
      ObjectMapper mapper = new ObjectMapper();
      PersonKeywords keywords = mapper.readValue(file, PersonKeywords.class);
      for (Map<String, String> map : keywords.keywords_pers) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
          progress.step();
          AtlaPerson person = new AtlaPerson();
          person.name = entry.getValue();
          storageManager.addDocument(Person.class, person);
          personIdMap.put(entry.getKey(), person.getId());
        }
      }
      progress.done();
    } else {
      System.err.println("Cannot read " + file.getCanonicalPath());
    }
  }

  public static class PersonKeywords {
    public List<Map<String, String>> keywords_pers;
  }

}
