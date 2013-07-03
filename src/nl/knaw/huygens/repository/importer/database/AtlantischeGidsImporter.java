package nl.knaw.huygens.repository.importer.database;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.atlg.ATLGKeyword;
import nl.knaw.huygens.repository.model.atlg.ATLGLegislation;
import nl.knaw.huygens.repository.model.atlg.ATLGPerson;
import nl.knaw.huygens.repository.model.util.PersonName;
import nl.knaw.huygens.repository.model.util.PersonNameComponent.Type;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class AtlantischeGidsImporter {

  public static void main(String[] args) throws Exception {
    new AtlantischeGidsImporter(null, "../AtlantischeGids/work/").importAll();
    System.out.printf("%n.. done%n");
  }

  // -------------------------------------------------------------------

  private static final String[] JSON_EXTENSION = { "json" };

  private final ObjectMapper objectMapper;
  private final StorageManager storageManager;
  private final File inputDir;

  public AtlantischeGidsImporter(StorageManager manager, String inputDirName) {
    System.out.printf("%n.. Importing from %s%n", inputDirName);
    objectMapper = new ObjectMapper();
    storageManager = manager;
    inputDir = new File(inputDirName);
  }

  public void importAll() throws Exception {
    System.out.printf("%n.. 'keyword'%n");
    Map<String, String> keywordIdMap = importKeywords();
    System.out.printf("Number of entries = %d%n", keywordIdMap.size());

    System.out.printf("%n.. 'person'%n");
    Map<String, String> personIdMap = importPersons();
    System.out.printf("Number of entries = %d%n", personIdMap.size());

    System.out.printf("%n.. 'archiefmat' -- pass 1%n");
    Map<String, String> archiefmatIdMap = importArchiefMats();
    System.out.printf("Number of entries = %d%n", archiefmatIdMap.size());

    System.out.printf("%n.. 'creator' -- pass 1%n");
    importCreators();

    System.out.printf("%n.. 'wetgeving' -- pass 1%n");
    Map<String, String> wetgevingIdMap = importWetgevings();
    System.out.printf("Number of entries = %d%n", wetgevingIdMap.size());
  }

  // -------------------------------------------------------------------

  private static final String KEYWORD_DIR = "keywords";
  private static final String KEYWORD_FILE = "keywords.json";

  private Map<String, String> importKeywords() throws Exception {
    Map<String, String> ids = Maps.newHashMap();
    File file = new File(new File(inputDir, KEYWORD_DIR), KEYWORD_FILE);
    for (Keyword keyword : objectMapper.readValue(file, Keyword[].class)) {
      // System.out.println(keyword);
      String id = keyword._id;
      if (ids.containsKey(id)) {
        System.err.printf("## [%s] Duplicate id %s%n", KEYWORD_FILE, id);
      } else if (storageManager == null) {
        ids.put(id, id);
      } else {
        ATLGKeyword atlgKeyword = convert(keyword);
        storageManager.addDocument(ATLGKeyword.class, atlgKeyword);
        ids.put(id, atlgKeyword.getId());
      }
    }
    return ids;
  }

  private ATLGKeyword convert(Keyword input) {
    ATLGKeyword keyword = new ATLGKeyword();

    String type = input.type;
    keyword.setType(type);

    if (type == null) {
      System.err.printf("Missing type");
    } else if (type.equals("subject")) {
      keyword.setValue(input.onderwerp);
    } else if (type.equals("geography")) {
      keyword.setValue(input.onderwerp);
    } else {
      System.err.printf("Missing type");
      keyword.setValue("?");
    }

    if (input.label != null) {
      keyword.setLabel(input.label);
    }

    return keyword;
  }

  // -------------------------------------------------------------------

  private static final String PERSON_DIR = "keywords";
  private static final String PERSON_FILE = "persons.json";

  private Map<String, String> importPersons() throws Exception {
    Map<String, String> ids = Maps.newHashMap();
    File file = new File(new File(inputDir, PERSON_DIR), PERSON_FILE);
    for (Person person : objectMapper.readValue(file, Person[].class)) {
      String id = person._id;
      if (ids.containsKey(id)) {
        System.err.printf("## [%s] Duplicate id %s%n", PERSON_FILE, id);
      } else if (storageManager == null) {
        ids.put(id, id);
      } else {
        ATLGPerson atlgPerson = convert(person);
        storageManager.addDocument(ATLGPerson.class, atlgPerson);
        ids.put(id, atlgPerson.getId());
      }
    }
    return ids;
  }

  private ATLGPerson convert(Person input) {
    ATLGPerson person = new ATLGPerson();

    PersonName name = new PersonName();
    if (input.voorl != null) {
      name.addNameComponent(Type.FORENAME, input.voorl);
    }
    if (input.tussenv != null) {
      name.addNameComponent(Type.NAME_LINK, input.tussenv);
    }
    if (input.achternaam != null) {
      name.addNameComponent(Type.SURNAME, input.achternaam);
    }
    if (input.toevoeging != null) {
      name.addNameComponent(Type.ADD_NAME, input.toevoeging);
    }
    person.setName(name);

    if (input.label != null) {
      String value = StringUtils.join(input.label, "; ");
      person.setLabel(value);
    }

    if (input.verwijzing != null) {
      String value = StringUtils.join(input.verwijzing, "; ");
      person.setReference(value);
    }

    return person;
  }

  // -------------------------------------------------------------------

  private static final String ARCHIEFMAT_DIR = "archiefmat";

  public Map<String, String> importArchiefMats() throws Exception {
    Map<String, String> ids = Maps.newHashMap();
    File directory = new File(inputDir, ARCHIEFMAT_DIR);
    for (File file : FileUtils.listFiles(directory, JSON_EXTENSION, true)) {
      for (ArchiefMatEntry entry : objectMapper.readValue(file, ArchiefMatEntry[].class)) {
        ArchiefMat object = entry.archiefmat;
        String id = object._id;
        if (ids.containsKey(id)) {
          System.err.printf("## [%s] Duplicate id %s%n", file.getName(), id);
        } else if (storageManager == null) {
          ids.put(id, id);
        } else {
          // convert and store
          ids.put(id, id);
        }
      }
    }
    return ids;
  }

  // -------------------------------------------------------------------

  public void importCreators() throws JsonParseException, JsonMappingException, IOException {
    Set<String> ids = Sets.newTreeSet();
    File directory = new File(inputDir, "creators");
    for (File file : FileUtils.listFiles(directory, JSON_EXTENSION, true)) {
      System.out.println(file.getName());
      CreatorEntry[] entries = objectMapper.readValue(file, CreatorEntry[].class);
      for (CreatorEntry entry : entries) {
        Creator object = entry.creator;
        if (ids.contains(object._id)) {
          System.err.println("duplicate id " + object._id);
        } else {
          ids.add(object._id);
        }
      }
    }
    System.out.println("Number of entries = " + ids.size());
  }

  // -------------------------------------------------------------------

  private static final String WETGEVING_DIR = "wetgeving";

  public Map<String, String> importWetgevings() throws Exception {
    Map<String, String> ids = Maps.newHashMap();
    File directory = new File(inputDir, WETGEVING_DIR);
    for (File file : FileUtils.listFiles(directory, JSON_EXTENSION, true)) {
      for (WetgevingEntry entry : objectMapper.readValue(file, WetgevingEntry[].class)) {
        Wetgeving object = entry.wetgeving;
        String id = object._id;
        if (ids.containsKey(id)) {
          System.err.printf("## [%s] Duplicate id %s%n", file, id);
        } else if (storageManager == null) {
          ids.put(id, id);
        } else {
          ATLGLegislation document = convert(object);
          storageManager.addDocument(ATLGLegislation.class, document);
          ids.put(id, document.getId());
        }
      }
    }
    return ids;
  }

  public ATLGLegislation convert(Wetgeving object) {
    ATLGLegislation document = new ATLGLegislation();
    document.setTitle(object.titel);
    document.setOrigFilename(object.orig_filename);
    document.setReference(object.reference);
    document.setPages(object.pages);
    return document;
  }

  // -------------------------------------------------------------------
  // --- Data model defined in ING Forms -------------------------------
  // -------------------------------------------------------------------

  public static class Keyword {

    /** ### Assigned id (admin) */
    public String _id;

    public String type;

    public String label;

    public String onderwerp;

    public String regionaam;

    @Override
    public String toString() {
      return String.format("%-5s %-10s %-40s %-30s %s", _id, type, onderwerp, regionaam, label);
    }
  }

  // -------------------------------------------------------------------

  public static class Person {
    public String _id;
    public String type;
    public String voorl;
    public String tussenv;
    public String achternaam;
    public String toevoeging;
    public String[] verwijzing;
    public String[] label;
  }

  // -------------------------------------------------------------------

  public static class ArchiefMat {

    /** ### Assigned id (admin) */
    public String _id;

    /** ### Name of source file (admin) */
    public String orig_filename;

    /** "Ref. code country" ??? */
    public String[] countries;

    /** "Ref. code repository" ??? */
    public String rf_archive;

    /** "Reference code" */
    public String ref_code;

    /** "Code or indication of sub-fonds" */
    public String code_subfonds;

    /** "Indication of series, Nos." */
    public String series;

    /** "Item, No." */
    public String itemno;

    /** "Title" */
    public String titel;

    /** "English title" */
    public String titel_eng;

    /** ### "Begin date" and "End date" */
    public Period dates;

    /** "Period description" */
    public String period_description;

    /** "Extent" */
    public String extent;

    /** "Title related overhead level of description" */
    public String[] overhead_titles;

    /** "Additional finding aid" */
    public String finding_aid;

    /** "Name(s) of Creator(s)" */
    public String[] creators;

    /** "Scope and content" */
    public String scope;

    /** "Title(s) related underlying level(s) of description" ??? */
    public String relation;

    /** "Other related units of description" ??? */
    public String em;

    /** "Link legislation" */
    public String link_law;

    /** "Keyword(s) geography" */
    public String[] geography;

    /** "Keyword(s) subject" */
    public String[] keywords;

    /** "Keyword(s) person" */
    public String[] persons;

    /** "Remarks" ??? */
    public String notes;

    /** "Record made by-" */
    public String made_by;

    /** "Reminders" ??? */
    public String Aantekeningen;

    /** "Binnenkomende relaties" ??? */
    public Related[] related;
  }

  public static class ArchiefMatEntry {
    public ArchiefMat archiefmat;
  }

  // -------------------------------------------------------------------

  public static class Creator {

    /** ### Assigned id (admin) */
    public String _id;

    /** ### Name of source file (admin) */
    public String orig_filename;

    /** "Name" */
    public String name;

    /** "English name" */
    public String name_english;

    /** ### "Begin date" and "End date" */
    public Period dates;

    /** "Period description" */
    public String period_description;

    /** "History/functions/occupations/activities" */
    public String his_func;

    /** "Title(s) related archive(s)" */
    public String[] related_archives;

    /** "Title(s) related creator(s)" */
    public String[] related_creators;

    /** "Link legislation" */
    public String link_law;

    /** "Keyword(s) geography" */
    public String[] geography;

    /** "Keyword(s) subject" */
    public String[] keywords;

    /** "Keyword(s) person" */
    public String[] persons;

    /** "Remarks" */
    public String notes;

    /** "Literature" */
    public String literatuur;

    /** "Record made by-" */
    public String made_by;

    /** "Reminders" */
    public String Aantekeningen;

    /** "Binnenkomende relaties" */
    public Related[] related;

    /** ??? ("person", "family") */
    public String[] types;
  }

  public static class CreatorEntry {
    public Creator creator;
  }

  // -------------------------------------------------------------------

  /**
   * Data model defined in ING Forms.
   * Modifications are marked by "###".
   */
  public static class Wetgeving {

    /** ### Assigned id (admin) */
    public String _id;

    /** ### Name of source file (admin) */
    public String orig_filename;

    /** "Reference" */
    public String reference;

    /** "Pages" */
    public String pages;

    /** "Short title" */
    public String titel;

    /** "English title" */
    public String titel_eng;

    /** ### "Type of document" (missing)*/

    /** ### "Date" and "Date 2" */
    public Dates dates;

    /** "Keyword(s) geography" */
    public String[] geography;

    /** "Keyword(s) Group classification" */
    public String[] keywords;

    /** "Keyword(s) other subject" */
    public String[] keywords_extra;

    /** "Keyword(s) person" */
    public String[] persons;

    /** "Summary of contents" */
    public String contents;

    /** "See also" */
    public SeeAlso[] see_also;

    /** "Earlier/later publications" */
    public String[] other_publication;

    /** "Original archival source" */
    public String original_archival_source;

    /** "Link archival database" */
    public String link_archival_dbase;

    /** "Remarks" */
    public String remarks;

    /** "Scan" */
    public String scan;

    /** "Parts to scan" */
    public String partstoscan;

    /** "Record made by-" */
    public String made_by;

    /** ### "Reminders" ??? */
    public String Aantekeningen;

    /** "Binnenkomende relaties" */
    public Related[] related;
  }

  public static class WetgevingEntry {
    public Wetgeving wetgeving;
  }

  // -------------------------------------------------------------------

  public static class Dates {
    public String date1;
    public String date2;
  }

  public static class Period {
    public String begin_date;
    public String end_date;
  }

  public static class Related {
    public String type;
    public String[] ids;
  }

  public static class SeeAlso {
    public String ref_id;
    public String text_line;
  }

}
