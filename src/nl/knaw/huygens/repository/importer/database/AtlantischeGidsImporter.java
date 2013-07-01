package nl.knaw.huygens.repository.importer.database;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;

public class AtlantischeGidsImporter {

  private static final String[] JSON_EXTENSION = { "json" };

  public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
    AtlantischeGidsImporter importer = new AtlantischeGidsImporter("../AtlantischeGids/work/");
    importer.importKeywords();
    importer.importPersons();
    importer.importArchiefMats();
    importer.importCreators();
    importer.importWetgevings();
    System.out.printf("%n.. done%n");
  }

  private final File baseDirectory;
  private final ObjectMapper mapper;

  public AtlantischeGidsImporter(String directoryName) throws JsonProcessingException {
    System.out.println(".. Importing from " + directoryName);
    baseDirectory = new File(directoryName);
    mapper = new ObjectMapper();
  }

  public void importKeywords() throws JsonParseException, JsonMappingException, IOException {
    System.out.printf("%n.. Importing 'keyword's%n");
    Set<String> ids = Sets.newTreeSet();
    File directory = new File(baseDirectory, "keywords");
    File file = new File(directory, "keywords.json");
    System.out.println(file.getName());
    Keyword[] entries = mapper.readValue(file, Keyword[].class);
    for (Keyword object : entries) {
      if (ids.contains(object._id)) {
        System.err.println("duplicate id " + object._id);
      } else {
        ids.add(object._id);
      }
    }
    System.out.println("Number of entries = " + ids.size());
  }

  public void importPersons() throws JsonParseException, JsonMappingException, IOException {
    System.out.printf("%n.. Importing 'person's%n");
    Set<String> ids = Sets.newTreeSet();
    File directory = new File(baseDirectory, "keywords");
    File file = new File(directory, "persons.json");
    System.out.println(file.getName());
    Person[] entries = mapper.readValue(file, Person[].class);
    for (Person object : entries) {
      if (ids.contains(object._id)) {
        System.err.println("duplicate id " + object._id);
      } else {
        ids.add(object._id);
      }
    }
    System.out.println("Number of entries = " + ids.size());
  }

  public void importArchiefMats() throws JsonParseException, JsonMappingException, IOException {
    System.out.printf("%n.. Importing 'archiefmat's%n");
    Set<String> ids = Sets.newTreeSet();
    File directory = new File(baseDirectory, "archiefmat");
    for (File file : FileUtils.listFiles(directory, JSON_EXTENSION, true)) {
      System.out.println(file.getName());
      ArchiefMatEntry[] entries = mapper.readValue(file, ArchiefMatEntry[].class);
      for (ArchiefMatEntry entry : entries) {
        ArchiefMat object = entry.archiefmat;
        if (ids.contains(object._id)) {
          System.err.println("duplicate id " + object._id);
        } else {
          ids.add(object._id);
        }
      }
    }
    System.out.println("Number of entries = " + ids.size());
  }

  public void importCreators() throws JsonParseException, JsonMappingException, IOException {
    System.out.printf("%n.. Importing 'creator's%n");
    Set<String> ids = Sets.newTreeSet();
    File directory = new File(baseDirectory, "creators");
    for (File file : FileUtils.listFiles(directory, JSON_EXTENSION, true)) {
      System.out.println(file.getName());
      CreatorEntry[] entries = mapper.readValue(file, CreatorEntry[].class);
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

  public void importWetgevings() throws JsonParseException, JsonMappingException, IOException {
    System.out.printf("%n.. Importing 'wetgeving's%n");
    Set<String> ids = Sets.newTreeSet();
    File directory = new File(baseDirectory, "wetgeving");
    for (File file : FileUtils.listFiles(directory, JSON_EXTENSION, true)) {
      System.out.println(file.getName());
      WetgevingEntry[] entries = mapper.readValue(file, WetgevingEntry[].class);
      for (WetgevingEntry entry : entries) {
        Wetgeving object = entry.wetgeving;
        if (ids.contains(object._id)) {
          System.err.println("duplicate id " + object._id);
        } else {
          ids.add(object._id);
        }
      }
    }
    System.out.println("Number of entries = " + ids.size());
  }

  // --- Helper classes ------------------------------------------------

  public static class Keyword {
    public String _id;
    public String type;
    public String label;
    public String onderwerp;
    public String regionaam;
  }

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

  public static class ArchiefMat {
    public String _id;
    public String titel;
    public String titel_eng;
    public String made_by;
    public String orig_filename;
    public String finding_aid;
    public String series;
    public String itemno;
    public String scope;
    public String extent;
    public String ref_code;
    public String rf_archive;
    public String link_law;
    public String em;
    public String relation;
    public String code_subfonds;
    public String notes;
    public String Aantekeningen;
    public String[] overhead_titles;
    public String[] keywords;
    public String[] persons;
    public String[] geography;
    public String[] countries;
    public String[] creators;
    public Period dates;
    public String period_description;
    public Related[] related;
  }

  public static class ArchiefMatEntry {
    public ArchiefMat archiefmat;
  }

  public static class Creator {
    public String _id;
    public String name;
    public String name_english;
    public String made_by;
    public String orig_filename;
    public String link_law;
    public String his_func;
    public String notes;
    public String Aantekeningen;
    public String literatuur;
    public String[] keywords;
    public String[] persons;
    public String[] related_archives;
    public String[] related_creators;
    public String[] types;
    public String[] geography;
    public Period dates;
    public String period_description;
    public Related[] related;
  }

  public static class CreatorEntry {
    public Creator creator;
  }

  public static class Wetgeving {
    public String _id;
    public String titel;
    public String titel_eng;
    public String made_by;
    public String orig_filename;
    public String original_archival_source;
    public String scan;
    public String partstoscan;
    public String reference;
    public String pages;
    public String contents;
    public String remarks;
    public String Aantekeningen;
    public String link_archival_dbase;
    public String[] other_publication;
    public String[] keywords;
    public String[] keywords_extra;
    public String[] persons;
    public String[] geography;
    public Period dates;
    public Related[] related;
    public SeeAlso[] see_also;
  }

  public static class WetgevingEntry {
    public Wetgeving wetgeving;
  }

  public static class Period {
    public String date1; // wetgeving: should be begin_date
    public String date2; // wetgeving: should be end_date
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
