package nl.knaw.huygens.repository.importer.database;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.repository.managers.StorageManager;
import nl.knaw.huygens.repository.model.DocumentRef;
import nl.knaw.huygens.repository.model.atlg.ATLGArchive;
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

  private Map<String, DocumentRef> keywordRefMap;
  private Map<String, DocumentRef> personRefMap;
  private Map<String, DocumentRef> wetgevingRefMap;
  private Map<String, DocumentRef> archiefmatRefMap;

  public AtlantischeGidsImporter(StorageManager manager, String inputDirName) {
    System.out.printf("%n.. Importing from %s%n", inputDirName);
    objectMapper = new ObjectMapper();
    storageManager = manager;
    inputDir = new File(inputDirName);
  }

  public void importAll() throws Exception {
    System.out.printf("%n.. 'keyword'%n");
    keywordRefMap = importKeywords();
    System.out.printf("Number of entries = %d%n", keywordRefMap.size());

    System.out.printf("%n.. 'person'%n");
    personRefMap = importPersons();
    System.out.printf("Number of entries = %d%n", personRefMap.size());

    System.out.printf("%n.. 'wetgeving'%n");
    wetgevingRefMap = importWetgevings();
    System.out.printf("Number of entries = %d%n", wetgevingRefMap.size());

    System.out.printf("%n.. 'archiefmat' -- pass 1%n");
    archiefmatRefMap = importArchiefMats();
    System.out.printf("Number of entries = %d%n", archiefmatRefMap.size());

    System.out.printf("%n.. 'creator' -- pass 1%n");
    importCreators();
  }

  // -------------------------------------------------------------------

  private static final String KEYWORD_DIR = "keywords";
  private static final String KEYWORD_FILE = "keywords.json";

  private Map<String, DocumentRef> importKeywords() throws Exception {
    Map<String, DocumentRef> ids = Maps.newHashMap();
    File file = new File(new File(inputDir, KEYWORD_DIR), KEYWORD_FILE);
    for (XKeyword xkeyword : objectMapper.readValue(file, XKeyword[].class)) {
      String id = xkeyword._id;
      if (ids.containsKey(id)) {
        System.err.printf("## [%s] Duplicate id %s%n", KEYWORD_FILE, id);
      } else if (storageManager != null) {
        ATLGKeyword keyword = convert(xkeyword);
        storageManager.addDocument(ATLGKeyword.class, keyword);
        ids.put(id, DocumentRef.newInstance(ATLGKeyword.class, keyword));
      }
    }
    return ids;
  }

  private ATLGKeyword convert(XKeyword xkeyword) {
    ATLGKeyword keyword = new ATLGKeyword();

    String type = xkeyword.type;
    keyword.setType(type);

    if (type == null) {
      System.err.println("Missing type");
    } else if (type.equals("subject")) {
      keyword.setValue(xkeyword.onderwerp);
    } else if (type.equals("geography")) {
      keyword.setValue(xkeyword.regionaam);
    } else {
      System.err.println("Unknown type" + type);
      keyword.setValue("?");
    }

    if (xkeyword.label != null) {
      keyword.setLabel(xkeyword.label);
    }

    return keyword;
  }

  // -------------------------------------------------------------------

  private static final String PERSON_DIR = "keywords";
  private static final String PERSON_FILE = "persons.json";

  private Map<String, DocumentRef> importPersons() throws Exception {
    Map<String, DocumentRef> refs = Maps.newHashMap();
    File file = new File(new File(inputDir, PERSON_DIR), PERSON_FILE);
    for (XPerson xperson : objectMapper.readValue(file, XPerson[].class)) {
      String id = xperson._id;
      if (refs.containsKey(id)) {
        System.err.printf("## [%s] Duplicate id %s%n", PERSON_FILE, id);
      } else if (storageManager != null) {
        ATLGPerson person = convert(xperson);
        storageManager.addDocument(ATLGPerson.class, person);
        refs.put(id, DocumentRef.newInstance(ATLGPerson.class, person));
      }
    }
    return refs;
  }

  private ATLGPerson convert(XPerson xperson) {
    ATLGPerson person = new ATLGPerson();

    PersonName name = new PersonName();
    if (xperson.voorl != null) {
      name.addNameComponent(Type.FORENAME, xperson.voorl);
    }
    if (xperson.tussenv != null) {
      name.addNameComponent(Type.NAME_LINK, xperson.tussenv);
    }
    if (xperson.achternaam != null) {
      name.addNameComponent(Type.SURNAME, xperson.achternaam);
    }
    if (xperson.toevoeging != null) {
      name.addNameComponent(Type.ADD_NAME, xperson.toevoeging);
    }
    person.setName(name);

    if (xperson.label != null) {
      String value = StringUtils.join(xperson.label, "; ");
      person.setLabel(value);
    }

    if (xperson.verwijzing != null) {
      String value = StringUtils.join(xperson.verwijzing, "; ");
      person.setReference(value);
    }

    return person;
  }

  // -------------------------------------------------------------------

  private static final String WETGEVING_DIR = "wetgeving";

  public Map<String, DocumentRef> importWetgevings() throws Exception {
    Map<String, DocumentRef> refs = Maps.newHashMap();
    File directory = new File(inputDir, WETGEVING_DIR);
    for (File file : FileUtils.listFiles(directory, JSON_EXTENSION, true)) {
      for (WetgevingEntry entry : objectMapper.readValue(file, WetgevingEntry[].class)) {
        Wetgeving wetgeving = entry.wetgeving;
        // System.out.println(wetgeving);
        String id = wetgeving._id;
        if (refs.containsKey(id)) {
          System.err.printf("## [%s] Duplicate id %s%n", file, id);
        } else if (storageManager != null) {
          ATLGLegislation legislation = convert(wetgeving);
          storageManager.addDocument(ATLGLegislation.class, legislation);
          refs.put(id, DocumentRef.newInstance(ATLGLegislation.class, legislation));
        }
      }
    }
    return refs;
  }

  public ATLGLegislation convert(Wetgeving wetgeving) {
    ATLGLegislation legislation = new ATLGLegislation();
    legislation.setOrigFilename(wetgeving.orig_filename);
    legislation.setReference(wetgeving.reference);
    legislation.setPages(wetgeving.pages);
    legislation.setTitleNld(wetgeving.titel);
    legislation.setTitleEng(wetgeving.titel_eng);
    if (wetgeving.dates != null) {
      legislation.setDate1(wetgeving.dates.date1);
      legislation.setDate2(wetgeving.dates.date2);
    }
    if (wetgeving.geography != null) {
      for (String keyword : wetgeving.geography) {
        legislation.addPlaceKeyword(keywordRefMap.get(keyword));
      }
    }
    if (wetgeving.keywords != null) {
      for (String keyword : wetgeving.keywords) {
        legislation.addGroupKeyword(keywordRefMap.get(keyword));
      }
    }
    if (wetgeving.keywords_extra != null) {
      for (String keyword : wetgeving.keywords_extra) {
        legislation.addOtherKeyword(keywordRefMap.get(keyword));
      }
    }
    if (wetgeving.persons != null) {
      for (String keyword : wetgeving.persons) {
        legislation.addPerson(personRefMap.get(keyword));
      }
    }
    legislation.setContents(wetgeving.contents);
    if (wetgeving.see_also != null) {
      for (SeeAlso item : wetgeving.see_also) {
        legislation.addSeeAlso(item.toString());
      }
    }
    if (wetgeving.other_publication != null) {
      for (String publication : wetgeving.other_publication) {
        legislation.addOtherPublication(publication);
      }
    }
    legislation.setOriginalArchivalSource(wetgeving.original_archival_source);
    legislation.setLinkArchivalDBase(wetgeving.link_archival_dbase);
    legislation.setRemarks(wetgeving.remarks);
    legislation.setScan(wetgeving.scan);
    legislation.setPartsToScan(wetgeving.partstoscan);
    legislation.setMadeBy(wetgeving.made_by);
    legislation.setReminders(wetgeving.Aantekeningen);
    return legislation;
  }

  // -------------------------------------------------------------------

  private static final String ARCHIEFMAT_DIR = "archiefmat";

  public Map<String, DocumentRef> importArchiefMats() throws Exception {
    Map<String, DocumentRef> refs = Maps.newHashMap();
    File directory = new File(inputDir, ARCHIEFMAT_DIR);
    for (File file : FileUtils.listFiles(directory, JSON_EXTENSION, true)) {
      for (ArchiefMatEntry entry : objectMapper.readValue(file, ArchiefMatEntry[].class)) {
        ArchiefMat object = entry.archiefmat;
        String id = object._id;
        if (refs.containsKey(id)) {
          System.err.printf("## [%s] Duplicate id %s%n", file.getName(), id);
        } else if (storageManager != null) {
          ATLGArchive archive = convert(object);
          storageManager.addDocument(ATLGArchive.class, archive);
          refs.put(id, DocumentRef.newInstance(ATLGArchive.class, archive));
        }
      }
    }
    return refs;
  }

  public ATLGArchive convert(ArchiefMat archiefmat) {
    ATLGArchive archive = new ATLGArchive();
    archive.setOrigFilename(archiefmat.orig_filename);
    if (archiefmat.countries != null) {
      for (String country : archiefmat.countries) {
        archive.addCountry(country);
      }
    }
    archive.setRefCodeArchive(archiefmat.rf_archive);
    archive.setRefCode(archiefmat.ref_code);
    archive.setSubCode(archiefmat.code_subfonds);
    archive.setSeries(archiefmat.series);
    archive.setItemNo(archiefmat.itemno);
    archive.setTitleNld(archiefmat.titel);
    archive.setTitleEng(archiefmat.titel_eng);
    // ### "Begin date" and "End date"
    // public Period dates;
    archive.setPeriodDescription(archiefmat.period_description);
    archive.setExtent(archiefmat.extent);
    if (archiefmat.overhead_titles != null) {
      for (String title : archiefmat.overhead_titles) {
        archive.addOverheadTitle(title);
      }
    }
    archive.setFindingAid(archiefmat.finding_aid);
    if (archiefmat.creators != null) {
      for (String creator : archiefmat.creators) {
        archive.addCreator(creator);
      }
    }
    archive.setScope(archiefmat.scope);
    archive.setRelation(archiefmat.relation);
    archive.setEm(archiefmat.em);
    if (archiefmat.link_law != null) {
      archive.setLinkLegislation(wetgevingRefMap.get(archiefmat.link_law));
    }
    if (archiefmat.geography != null) {
      for (String keyword : archiefmat.geography) {
        archive.addPlaceKeyword(keywordRefMap.get(keyword));
      }
    }
    if (archiefmat.keywords != null) {
      for (String keyword : archiefmat.keywords) {
        archive.addSubjectKeyword(keywordRefMap.get(keyword));
      }
    }
    if (archiefmat.persons != null) {
      for (String keyword : archiefmat.persons) {
        archive.addPerson(personRefMap.get(keyword));
      }
    }
    archive.setNotes(archiefmat.notes);
    archive.setMadeBy(archiefmat.made_by);
    archive.setReminders(archiefmat.Aantekeningen);
    // "Binnenkomende relaties"
    // public Related[] related;
    return archive;
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
  // --- Data model defined in ING Forms -------------------------------
  // -------------------------------------------------------------------

  public static class XKeyword {
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

  public static class XPerson {
    /** ### Assigned id (admin) */
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

  // TODO remove markup - various items contain paragraph tags
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

    // TODO Provide more meaningful names
    /** "Date" and "Date 2" */
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

    // TODO Check: correct identification
    /** "Reminders" */
    public String Aantekeningen;

    // TODO Check: never used
    /** "Binnenkomende relaties" */
    public Related[] related;
  }

  public static class WetgevingEntry {
    public Wetgeving wetgeving;
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

    @Override
    public String toString() {
      return String.format("%s: %s", type, StringUtils.join(ids, " ##"));
    }
  }

  public static class SeeAlso {
    public String ref_id;
    public String text_line;

    @Override
    public String toString() {
      return (ref_id == null) ? text_line : String.format("%s: %s", ref_id, text_line);
    }
  }

}
