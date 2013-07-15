package nl.knaw.huygens.repository.importer.database;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.repository.config.BasicInjectionModule;
import nl.knaw.huygens.repository.config.Configuration;
import nl.knaw.huygens.repository.config.DocTypeRegistry;
import nl.knaw.huygens.repository.index.IndexManager;
import nl.knaw.huygens.repository.index.IndexService;
import nl.knaw.huygens.repository.messages.Broker;
import nl.knaw.huygens.repository.model.Document;
import nl.knaw.huygens.repository.model.DocumentRef;
import nl.knaw.huygens.repository.model.atlg.ATLGArchive;
import nl.knaw.huygens.repository.model.atlg.ATLGArchiver;
import nl.knaw.huygens.repository.model.atlg.ATLGKeyword;
import nl.knaw.huygens.repository.model.atlg.ATLGLegislation;
import nl.knaw.huygens.repository.model.atlg.ATLGPerson;
import nl.knaw.huygens.repository.model.util.PersonName;
import nl.knaw.huygens.repository.model.util.PersonNameComponent.Type;
import nl.knaw.huygens.repository.storage.StorageManager;
import nl.knaw.huygens.repository.util.Token;
import nl.knaw.huygens.repository.util.TokenHandler;
import nl.knaw.huygens.repository.util.Tokens;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Imports data of the "Atlantische Gids" project.
 * 
 * Usage:
 *  java  -cp [specs]  nl.knaw.huygens.repository.importer.database.AtlantischeGidsImporter  importDirName  configFileName
 * 
 * The commandline arguments are optional.
 * 
 * Note that the Mongo database and the Solr index are built from scratch, any existing data is deleted.
 * Future versions of this importer must use a more subtle approach.
 */
public class AtlantischeGidsImporter {

  public static void main(String[] args) throws Exception {

    // Handle commandline argfuments
    String importDirName = (args.length > 0) ? args[0] : "../AtlantischeGids/work/";
    System.out.println("Import directory: " + importDirName);
    String configFileName = (args.length > 1) ? args[1] : "config.xml";
    System.out.println("Configuration file: " + configFileName);

    Configuration config = new Configuration(configFileName);
    Injector injector = Guice.createInjector(new BasicInjectionModule(config));

    Broker broker = null;
    StorageManager storageManager = null;
    IndexManager indexManager = null;

    try {
      broker = injector.getInstance(Broker.class);
      broker.start();

      storageManager = injector.getInstance(StorageManager.class);
      storageManager.clear();

      indexManager = injector.getInstance(IndexManager.class);
      indexManager.deleteAllDocuments();

      IndexService service = injector.getInstance(IndexService.class);
      Thread thread = new Thread(service);
      thread.start();

      long start = System.currentTimeMillis();

      DocTypeRegistry registry = injector.getInstance(DocTypeRegistry.class);
      DataPoster poster = new LocalDataPoster(storageManager);
      new AtlantischeGidsImporter(registry, poster, importDirName).importAll();

      storageManager.ensureIndices();

      // Signal we're done
      BulkImporter.sendEndOfDataMessage(broker);

      long time = (System.currentTimeMillis() - start) / 1000;
      System.out.printf("%n=== Used %d seconds%n%n", time);

      BulkImporter.waitForCompletion(thread, 5 * 60 * 1000);

      time = (System.currentTimeMillis() - start) / 1000;
      System.out.printf("%n=== Used %d seconds%n", time);

    } finally {
      // Close resources
      if (indexManager != null) {
        indexManager.close();
      }
      if (storageManager != null) {
        storageManager.close();
      }
      if (broker != null) {
        broker.close();
      }
    }
  }

  // -------------------------------------------------------------------

  private static final String[] JSON_EXTENSION = { "json" };

  private final ObjectMapper objectMapper;
  private final DocTypeRegistry docTypeRegistry;
  private final DataPoster dataPoster;
  private final File inputDir;

  private Map<String, DocumentRef> keywordRefMap;
  private Map<String, DocumentRef> personRefMap;
  private Map<String, DocumentRef> wetgevingRefMap;
  private Map<String, DocumentRef> archiefmatRefMap;
  private Map<String, DocumentRef> creatormatRefMap;
  private int errors;

  public AtlantischeGidsImporter(DocTypeRegistry registry, DataPoster poster, String inputDirName) {
    System.out.printf("%n.. Importing from %s%n", inputDirName);
    objectMapper = new ObjectMapper();
    docTypeRegistry = registry;
    dataPoster = poster;
    inputDir = new File(inputDirName);
    errors = 0;
  }

  private <T extends Document> DocumentRef newDocumentRef(Class<T> type, T document) {
    String typeString = docTypeRegistry.getTypeString(type);
    return new DocumentRef(typeString, document.getId(), document.getDisplayName());
  }

  private <T extends Document> DocumentRef newDocumentRef(Class<T> type, String id, String displayName) {
    String typeString = docTypeRegistry.getTypeString(type);
    return new DocumentRef(typeString, id, displayName);
  }

  private void handleError(String format, Object... args) {
    errors++;
    if (errors <= 40) {
      System.err.print("## ");
      System.err.printf(format, args);
      System.err.println();
    }
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

    System.out.printf("%n.. 'archiefmat' -- pass 2%n");
    resolveATLGArchiveRefs();

    System.out.printf("%n.. 'creator' -- pass 1%n");
    creatormatRefMap = importCreators();
    System.out.printf("Number of entries = %d%n", creatormatRefMap.size());

    System.out.printf("%n.. 'creator' -- pass 2%n");
    resolveATLGArchiverRefs();

    System.err.println("## Error count = " + errors);
  }

  // -------------------------------------------------------------------

  private static final String KEYWORD_DIR = "keywords";
  private static final String KEYWORD_FILE = "keywords.json";

  private Map<String, DocumentRef> importKeywords() throws Exception {
    Map<String, DocumentRef> refs = Maps.newHashMap();
    File file = new File(new File(inputDir, KEYWORD_DIR), KEYWORD_FILE);
    for (XKeyword xkeyword : objectMapper.readValue(file, XKeyword[].class)) {
      String id = xkeyword._id;
      if (refs.containsKey(id)) {
        System.err.printf("## [%s] Duplicate id %s%n", KEYWORD_FILE, id);
      } else if (dataPoster != null) {
        ATLGKeyword keyword = convert(xkeyword);
        dataPoster.addDocument(ATLGKeyword.class, keyword, true);
        refs.put(id, newDocumentRef(ATLGKeyword.class, keyword));
      }
    }
    return refs;
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
        handleError("[%s] Duplicate id %s", PERSON_FILE, id);
      } else if (dataPoster != null) {
        ATLGPerson person = convert(xperson);
        dataPoster.addDocument(ATLGPerson.class, person, true);
        refs.put(id, newDocumentRef(ATLGPerson.class, person));
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
        String id = wetgeving._id;
        if (refs.containsKey(id)) {
          handleError("[%s] Duplicate id %s", file.getName(), id);
        } else if (dataPoster != null) {
          ATLGLegislation legislation = convert(wetgeving);
          dataPoster.addDocument(ATLGLegislation.class, legislation, true);
          refs.put(id, newDocumentRef(ATLGLegislation.class, legislation));
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
      for (XSeeAlso item : wetgeving.see_also) {
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
    Tokens tokens = new Tokens();
    Map<String, DocumentRef> refs = Maps.newHashMap();
    File directory = new File(inputDir, ARCHIEFMAT_DIR);
    for (File file : FileUtils.listFiles(directory, JSON_EXTENSION, true)) {
      for (ArchiefMatEntry entry : objectMapper.readValue(file, ArchiefMatEntry[].class)) {
        ArchiefMat object = entry.archiefmat;
        String id = object._id;
        if (refs.containsKey(id)) {
          handleError("[%s] Duplicate id %s", file.getName(), id);
        } else if (dataPoster != null) {
          ATLGArchive archive = convert(object);
          dataPoster.addDocument(ATLGArchive.class, archive, false);
          refs.put(id, newDocumentRef(ATLGArchive.class, archive));
          tokens.increment(archive.getIndexedRefCode());
        }
      }
    }
    System.out.printf("%nGenerated reference codes (frequency and name)%n");
    tokens.handleSortedByText(new TokenHandler() {
      @Override
      public boolean handle(Token token) {
        System.out.printf("%4d :  %s%n", token.getCount(), token.getText());
        return true;
      }
    });
    return refs;
  }

  private ATLGArchive convert(ArchiefMat archiefmat) {
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
    if (archiefmat.dates != null) {
      archive.setBeginDate(archiefmat.dates.begin_date);
      archive.setEndDate(archiefmat.dates.end_date);
    }
    archive.setPeriodDescription(archiefmat.period_description);
    archive.setExtent(archiefmat.extent);
    archive.setFindingAid(archiefmat.finding_aid);
    if (archiefmat.creators != null) {
      for (String creator : archiefmat.creators) {
        archive.addCreator(creator);
      }
    }
    archive.setScope(archiefmat.scope);
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
    if (archiefmat.related != null) {
      for (XRelated item : archiefmat.related) {
        if ("overhead_title".equals(item.type)) {
          for (String id : item.ids) {
            archive.addOverheadArchive(newDocumentRef(ATLGArchive.class, id, "pending..."));
          }
        } else if ("underlying_levels_titels".equals(item.type)) {
          for (String id : item.ids) {
            archive.addUnderlyingArchive(newDocumentRef(ATLGArchive.class, id, "pending..."));
          }
        } else if ("unit".equals(item.type)) {
          for (String id : item.ids) {
            archive.addRelatedUnitArchive(newDocumentRef(ATLGArchive.class, id, "pending..."));
          }
        } else {
          handleError("Ignoring field 'related' with type '%s'", item.type);
        }
      }

      // Ignored fields
      if (archiefmat.relation != null) {
        handleError("Ignoring field 'relation':'%s'", archiefmat.relation);
      }
      if (archiefmat.em != null) {
        handleError("Ignoring field 'em':'%s'", archiefmat.em);
      }
    }

    return archive;
  }

  private void resolveATLGArchiveRefs() throws Exception {
    if (dataPoster != null) {
      for (String archiveId : getDocumentIds(archiefmatRefMap)) {
        ATLGArchive archive = dataPoster.getDocument(ATLGArchive.class, archiveId);
        List<DocumentRef> oldRefs = archive.getOverheadArchives();
        if (oldRefs != null && oldRefs.size() != 0) {
          List<DocumentRef> newRefs = resolveRefs(archiefmatRefMap, archive.getOrigFilename(), "overhead archive", oldRefs);
          archive.setOverheadArchives(newRefs);
        }
        oldRefs = archive.getUnderlyingArchives();
        if (oldRefs != null && oldRefs.size() != 0) {
          List<DocumentRef> newRefs = resolveRefs(archiefmatRefMap, archive.getOrigFilename(), "underlying archive", oldRefs);
          archive.setUnderlyingArchives(newRefs);
        }
        oldRefs = archive.getRelatedUnitArchives();
        if (oldRefs != null && oldRefs.size() != 0) {
          List<DocumentRef> newRefs = resolveRefs(archiefmatRefMap, archive.getOrigFilename(), "related unit archive", oldRefs);
          archive.setRelatedUnitArchives(newRefs);
        }
        dataPoster.modDocument(ATLGArchive.class, archive);
      }
    }
  }

  private List<String> getDocumentIds(Map<String, DocumentRef> refMap) {
    List<String> ids = Lists.newArrayList();
    for (Map.Entry<String, DocumentRef> entry : refMap.entrySet()) {
      ids.add(entry.getValue().getId());
    }
    return ids;
  }

  private List<DocumentRef> resolveRefs(Map<String, DocumentRef> refMap, String filename, String type, List<DocumentRef> oldRefs) {
    List<DocumentRef> newRefs = Lists.newArrayListWithCapacity(oldRefs.size());
    for (DocumentRef oldRef : oldRefs) {
      DocumentRef newRef = refMap.get(oldRef.getId());
      if (newRef == null) {
        handleError("[%s] No %s for id %s", filename, type, oldRef.getId());
      } else {
        newRefs.add(newRef);
      }
    }
    return newRefs;
  }

  // -------------------------------------------------------------------

  private static final String CREATORS_DIR = "creators";

  public Map<String, DocumentRef> importCreators() throws JsonParseException, JsonMappingException, IOException {
    Map<String, DocumentRef> refs = Maps.newHashMap();
    File directory = new File(inputDir, CREATORS_DIR);
    for (File file : FileUtils.listFiles(directory, JSON_EXTENSION, true)) {
      CreatorEntry[] entries = objectMapper.readValue(file, CreatorEntry[].class);
      for (CreatorEntry entry : entries) {
        Creator creator = entry.creator;
        String id = creator._id;
        if (refs.containsKey(id)) {
          System.err.println("duplicate id " + id);
        } else if (dataPoster != null) {
          ATLGArchiver archiver = convert(creator);
          dataPoster.addDocument(ATLGArchiver.class, archiver, false);
          refs.put(id, newDocumentRef(ATLGArchiver.class, archiver));
        }
      }
    }
    return refs;
  }

  private ATLGArchiver convert(Creator creator) {
    ATLGArchiver archiver = new ATLGArchiver();
    archiver.setOrigFilename(creator.orig_filename);
    archiver.setNameNld(creator.name);
    archiver.setNameEng(creator.name_english);
    if (creator.dates != null) {
      archiver.setBeginDate(creator.dates.begin_date);
      archiver.setEndDate(creator.dates.end_date);
    }
    archiver.setPeriodDescription(creator.period_description);
    archiver.setHistory(creator.his_func);
    if (creator.related_archives != null) {
      handleError("Ignoring field 'related_archives'");
    }
    if (creator.related_creators != null) {
      handleError("Ignoring field 'related_creators'");
      // for (String id : creator.related_creators) {
      //   archiver.addRelatedArchiver(new DocumentRef(ATLGArchiver.class, id, "pending..."));
      // }
    }
    if (creator.geography != null) {
      for (String keyword : creator.geography) {
        archiver.addPlaceKeyword(keywordRefMap.get(keyword));
      }
    }
    if (creator.keywords != null) {
      for (String keyword : creator.keywords) {
        archiver.addSubjectKeyword(keywordRefMap.get(keyword));
      }
    }
    if (creator.persons != null) {
      for (String keyword : creator.persons) {
        archiver.addPerson(personRefMap.get(keyword));
      }
    }
    archiver.setNotes(creator.notes);
    archiver.setLiterature(creator.literatuur);
    archiver.setMadeBy(creator.made_by);
    archiver.setReminders(creator.Aantekeningen);
    if (creator.related != null) {
      for (XRelated item : creator.related) {
        if ("archive".equals(item.type)) {
          for (String id : item.ids) {
            archiver.addRelatedArchive(newDocumentRef(ATLGArchive.class, id, "pending..."));
          }
        } else if ("creator".equals(item.type)) {
          for (String id : item.ids) {
            archiver.addRelatedArchiver(newDocumentRef(ATLGArchiver.class, id, "pending..."));
          }
        } else {
          handleError("Ignoring field 'related' with type '%s'", item.type);
        }
      }
    }
    if (creator.types != null) {
      for (String type : creator.types) {
        archiver.addType(type);
      }
    }
    return archiver;
  }

  private void resolveATLGArchiverRefs() throws Exception {
    if (dataPoster != null) {
      for (String id : getDocumentIds(creatormatRefMap)) {
        ATLGArchiver archiver = dataPoster.getDocument(ATLGArchiver.class, id);
        List<DocumentRef> oldRefs = archiver.getRelatedArchives();
        if (oldRefs != null && oldRefs.size() != 0) {
          List<DocumentRef> newRefs = resolveRefs(archiefmatRefMap, archiver.getOrigFilename(), "related archives", oldRefs);
          archiver.setRelatedArchives(newRefs);
        }
        oldRefs = archiver.getRelatedArchivers();
        if (oldRefs != null && oldRefs.size() != 0) {
          List<DocumentRef> newRefs = resolveRefs(creatormatRefMap, archiver.getOrigFilename(), "related archivers", oldRefs);
          archiver.setRelatedArchivers(newRefs);
        }
        dataPoster.modDocument(ATLGArchiver.class, archiver);
      }
    }
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
    public XDates dates;

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
    public XSeeAlso[] see_also;

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

    /** "Reminders" */
    public String Aantekeningen;

    // TODO Check: never used
    /** "Binnenkomende relaties" */
    public XRelated[] related;
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

    /** "Begin date" and "End date" */
    public XPeriod dates;

    /** "Period description" */
    public String period_description;

    /** "Extent" */
    public String extent;

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
    public XRelated[] related;

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append(_id);
      if (related != null) {
        for (XRelated entry : related) {
          builder.append(" ").append(entry.type);
        }
      }
      return builder.toString();
    }
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
    public XPeriod dates;

    /** "Period description" */
    public String period_description;

    /** "History/functions/occupations/activities" */
    public String his_func;

    /** "Title(s) related archive(s)" */
    public String[] related_archives;

    /** "Title(s) related creator(s)" */
    public String[] related_creators;

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
    public XRelated[] related;

    /** ??? ("person", "family") */
    public String[] types;
  }

  public static class CreatorEntry {
    public Creator creator;
  }

  // -------------------------------------------------------------------

  public static class XDates {
    public String date1;
    public String date2;
  }

  public static class XPeriod {
    public String begin_date;
    public String end_date;
  }

  public static class XRelated {
    public String type;
    public String[] ids;

    @Override
    public String toString() {
      return String.format("  %s: %s", type, StringUtils.join(ids, " ##"));
    }
  }

  public static class XSeeAlso {
    public String ref_id;
    public String text_line;

    @Override
    public String toString() {
      return (ref_id == null) ? text_line : String.format("%s: %s", ref_id, text_line);
    }
  }

}
