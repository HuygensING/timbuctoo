package nl.knaw.huygens.timbuctoo.tools.importer.database;

import java.io.File;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.DocTypeRegistry;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.index.IndexService;
import nl.knaw.huygens.timbuctoo.messages.Broker;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.atlg.ATLGArchive;
import nl.knaw.huygens.timbuctoo.model.atlg.ATLGArchiver;
import nl.knaw.huygens.timbuctoo.model.atlg.ATLGKeyword;
import nl.knaw.huygens.timbuctoo.model.atlg.ATLGLegislation;
import nl.knaw.huygens.timbuctoo.model.atlg.ATLGPerson;
import nl.knaw.huygens.timbuctoo.model.atlg.XRelated;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;
import nl.knaw.huygens.timbuctoo.model.util.PersonNameComponent.Type;
import nl.knaw.huygens.timbuctoo.storage.RelationManager;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;
import nl.knaw.huygens.timbuctoo.tools.util.EncodingFixer;
import nl.knaw.huygens.timbuctoo.tools.util.Files;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Imports data of the "Atlantische Gids" project.
 * 
 * Usage:
 *  java  -cp [specs]  nl.knaw.huygens.timbuctoo.importer.database.DutchCaribbeanImporter  importDirName  configFileName
 * 
 * The commandline arguments are optional.
 * 
 * Note that the Mongo database and the Solr index are built from scratch, any existing data is deleted.
 * Future versions of this importer must use a more subtle approach.
 */
public class DutchCaribbeanImporter extends DefaultImporter {

  public static void main(String[] args) throws Exception {

    // Handle commandline argfuments
    String importDirName = (args.length > 0) ? args[0] : "../../AtlantischeGids/work/";
    System.out.println("Import directory: " + importDirName);
    String configFileName = (args.length > 1) ? args[1] : "config.xml";
    System.out.println("Configuration file: " + configFileName);

    Configuration config = new Configuration(configFileName);
    Injector injector = Guice.createInjector(new ToolsInjectionModule(config));

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
      RelationManager relationManager = new RelationManager(registry, storageManager);
      new DutchCaribbeanImporter(registry, relationManager, storageManager, importDirName).importAll();

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
      // If the application is not explicitly closed a finalizer thread of Guice keeps running.
      System.exit(0);
    }
  }

  // -------------------------------------------------------------------

  private static final String[] JSON_EXTENSION = { "json" };

  private final ObjectMapper objectMapper;
  private final RelationManager relationManager;
  private final File inputDir;

  private final Map<String, Reference> keywordRefMap = Maps.newHashMap();
  private final Map<String, Reference> personRefMap = Maps.newHashMap();
  private final Map<String, Reference> placeRefMap = Maps.newHashMap();
  private final Map<String, Reference> legislationRefMap = Maps.newHashMap();
  private final Map<String, Reference> archiveRefMap = Maps.newHashMap();
  private final Map<String, Reference> archiverRefMap = Maps.newHashMap();

  Reference isCreatorRef;
  Reference hasKeywordRef;
  Reference hasPersonRef;
  Reference hasPlaceRef;
  Reference hasParentArchive;
  Reference hasSiblingArchive;
  Reference hasChildArchive;

  public DutchCaribbeanImporter(DocTypeRegistry registry, RelationManager relationManager, StorageManager storageManager, String inputDirName) {
    super(registry, storageManager);
    objectMapper = new ObjectMapper();
    this.relationManager = relationManager;
    inputDir = new File(inputDirName);
    System.out.printf("%n.. Importing from %s%n", inputDir.getAbsolutePath());
  }

  private <T> T readJsonValue(File file, Class<T> valueType) throws Exception {
    String text = Files.readTextFromFile(file);
    // For Atlantische Gids it seems OK to map "Ã " --> "à "
    String converted = EncodingFixer.convert2(text).replaceAll("Ã ", "à ");
    if (!converted.equals(text)) {
      int n = text.length() - converted.length();
      handleError("Fixed %d character encoding error%s in '%s'", n, (n == 1) ? "" : "s", file.getName());
    }
    return objectMapper.readValue(converted, valueType);
  }

  public void importAll() throws Exception {
    System.out.printf("%n.. Setup relation types%n");
    setupRelationTypes();

    System.out.println();
    System.out.println("---------------------------------");
    System.out.println("--  Pass 1 - basic properties  --");
    System.out.println("---------------------------------");

    System.out.printf("%n.. Keywords%n");
    importKeywords(keywordRefMap);
    System.out.printf("Number of entries = %d%n", keywordRefMap.size());

    System.out.printf("%n.. Persons%n");
    importPersons(personRefMap);
    System.out.printf("Number of entries = %d%n", personRefMap.size());

    System.out.printf("%n.. Places%n");
    System.out.printf("Number of entries = %d%n", placeRefMap.size());

    System.out.printf("%n.. Legislation%n");
    importLegislation(legislationRefMap);
    System.out.printf("Number of entries = %d%n", legislationRefMap.size());

    System.out.printf("%n.. Archives%n");
    importArchives(archiveRefMap);
    System.out.printf("Number of entries = %d%n", archiveRefMap.size());

    System.out.printf("%n.. Archivers%n");
    importArchivers(archiverRefMap);
    System.out.printf("Number of entries = %d%n", archiverRefMap.size());

    System.out.println();
    System.out.println("--------------------------");
    System.out.println("--  Pass 2 - relations  --");
    System.out.println("--------------------------");

    System.out.printf("%n.. Legislation%n");
    importLegislationRelations(legislationRefMap);

    System.out.printf("%n.. Archives%n");
    importArchiveRelations(archiveRefMap);

    System.out.printf("%n.. Archivers%n");
    importArchiverRelations(archiverRefMap);

    System.out.println();
    System.out.println("------------------------");
    System.out.println("-- Pass 3 - indexing  --");
    System.out.println("------------------------");

    displayErrorSummary();
  }

  // --- relations -----------------------------------------------------

  private void setupRelationTypes() {
    isCreatorRef = addRelationType("is_creator_of", ATLGArchiver.class, ATLGArchive.class);
    hasKeywordRef = addRelationType("has_keyword", DomainEntity.class, ATLGKeyword.class);
    hasPersonRef = addRelationType("has_person", DomainEntity.class, ATLGPerson.class);
    hasPlaceRef = addRelationType("has_place", DomainEntity.class, ATLGKeyword.class);
    hasParentArchive = addRelationType("has_parent_archive", ATLGArchive.class, ATLGArchive.class);
    hasSiblingArchive = addRelationType("has_sibling_archive", ATLGArchive.class, ATLGArchive.class, true);
    hasChildArchive = addRelationType("has_child_archive", ATLGArchive.class, ATLGArchive.class);
  }

  private Reference addRelationType(String name, Class<? extends DomainEntity> sourceType, Class<? extends DomainEntity> targetType, boolean symmetric) {
    RelationType type = new RelationType(name, sourceType, targetType);
    type.setSymmetric(symmetric);
    addEntity(RelationType.class, type, false); // no need to index
    return new Reference(RelationType.class, type.getId());
  }

  private Reference addRelationType(String name, Class<? extends DomainEntity> sourceType, Class<? extends DomainEntity> targetType) {
    return addRelationType(name, sourceType, targetType, false);
  }

  void addRelations(Reference sourceRef, Reference relTypeRef, Map<String, Reference> map, String[] keys) {
    if (keys != null) {
      for (String key : keys) {
        relationManager.storeRelation(sourceRef, relTypeRef, map.get(key));
      }
    }
  }

  //  private void addRelation(Reference sourceRef, Reference relTypeRef, Reference targetRef) {
  //    RelationBuilder builder = relationManager.getBuilder();
  //    Relation relation = builder.source(sourceRef).type(relTypeRef).target(targetRef).build();
  //    if (relation != null) {
  //      addDocument(Relation.class, relation, true);
  //    }
  //  }

  // --- Keywords ------------------------------------------------------

  private static final String KEYWORD_DIR = "keywords";
  private static final String KEYWORD_FILE = "keywords.json";

  private void importKeywords(Map<String, Reference> referenceMap) throws Exception {
    File file = new File(new File(inputDir, KEYWORD_DIR), KEYWORD_FILE);
    for (XKeyword xkeyword : readJsonValue(file, XKeyword[].class)) {
      String jsonId = xkeyword._id;
      if (referenceMap.containsKey(jsonId)) {
        handleError("[%s] Duplicate keyword id %s", KEYWORD_FILE, jsonId);
      } else {
        ATLGKeyword keyword = convert(xkeyword);
        String storedId = addEntity(ATLGKeyword.class, keyword, true);
        referenceMap.put(jsonId, new Reference(ATLGKeyword.class, storedId));
      }
    }
  }

  private ATLGKeyword convert(XKeyword xkeyword) {
    ATLGKeyword keyword = new ATLGKeyword();

    String type = xkeyword.type;
    keyword.setType(type);

    if (type == null) {
      handleError("[%s] Missing type", KEYWORD_FILE);
    } else if (type.equals("subject")) {
      keyword.setValue(xkeyword.onderwerp);
    } else if (type.equals("geography")) {
      keyword.setValue(xkeyword.regionaam);
    } else {
      handleError("[%s] Unknown type %s", KEYWORD_FILE, type);
      keyword.setValue("?");
    }

    if (xkeyword.label != null) {
      keyword.setLabel(xkeyword.label);
    }

    return keyword;
  }

  // --- Persons -------------------------------------------------------

  private static final String PERSON_DIR = "keywords";
  private static final String PERSON_FILE = "persons.json";

  private void importPersons(Map<String, Reference> referenceMap) throws Exception {
    File file = new File(new File(inputDir, PERSON_DIR), PERSON_FILE);
    for (XPerson xperson : readJsonValue(file, XPerson[].class)) {
      String jsonId = xperson._id;
      if (referenceMap.containsKey(jsonId)) {
        handleError("[%s] Duplicate person id %s", PERSON_FILE, jsonId);
      } else {
        ATLGPerson person = convert(xperson);
        String storedId = addEntity(ATLGPerson.class, person, true);
        referenceMap.put(jsonId, new Reference(ATLGPerson.class, storedId));
      }
    }
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

  // --- Legislation ---------------------------------------------------

  private static final String WETGEVING_DIR = "wetgeving";

  private void importLegislation(Map<String, Reference> referenceMap) throws Exception {
    File directory = new File(inputDir, WETGEVING_DIR);
    for (File file : FileUtils.listFiles(directory, JSON_EXTENSION, true)) {
      for (WetgevingEntry entry : readJsonValue(file, WetgevingEntry[].class)) {
        Wetgeving wetgeving = entry.wetgeving;
        String jsonId = wetgeving._id;
        if (referenceMap.containsKey(jsonId)) {
          handleError("[%s] Duplicate wetgeving id %s", file.getName(), jsonId);
        } else {
          ATLGLegislation legislation = convert(wetgeving);
          String storedId = addEntity(ATLGLegislation.class, legislation, false);
          referenceMap.put(jsonId, new Reference(ATLGLegislation.class, storedId));
        }
      }
    }
  }

  private ATLGLegislation convert(Wetgeving wetgeving) {
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

  private void importLegislationRelations(Map<String, Reference> referenceMap) throws Exception {
    File directory = new File(inputDir, WETGEVING_DIR);
    for (File file : FileUtils.listFiles(directory, JSON_EXTENSION, true)) {
      for (WetgevingEntry entry : readJsonValue(file, WetgevingEntry[].class)) {
        Wetgeving wetgeving = entry.wetgeving;
        Reference reference = referenceMap.get(wetgeving._id);
        addLegislationRelations(reference, wetgeving);
      }
    }
  }

  private void addLegislationRelations(Reference reference, Wetgeving wetgeving) {
    Preconditions.checkNotNull(reference, "Missing legislation reference");
    addRelations(reference, hasPlaceRef, keywordRefMap, wetgeving.geography);
    addRelations(reference, hasKeywordRef, keywordRefMap, wetgeving.keywords);
    addRelations(reference, hasKeywordRef, keywordRefMap, wetgeving.keywords_extra);
    addRelations(reference, hasPersonRef, personRefMap, wetgeving.persons);
  }

  // --- Archives ------------------------------------------------------

  private static final String ARCHIEFMAT_DIR = "archiefmat";

  private void importArchives(Map<String, Reference> referenceMap) throws Exception {
    File directory = new File(inputDir, ARCHIEFMAT_DIR);
    for (File file : FileUtils.listFiles(directory, JSON_EXTENSION, true)) {
      for (ArchiefMatEntry entry : readJsonValue(file, ArchiefMatEntry[].class)) {
        ArchiefMat archiefmat = entry.archiefmat;
        String jsonId = archiefmat._id;
        if (referenceMap.containsKey(jsonId)) {
          handleError("[%s] Duplicate entry %s", file.getName(), jsonId);
        } else {
          ATLGArchive archive = convert(archiefmat);
          String storedId = addEntity(ATLGArchive.class, archive, false);
          referenceMap.put(jsonId, new Reference(ATLGArchive.class, storedId));
        }
      }
    }
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
    archive.setScope(archiefmat.scope);
    archive.setNotes(archiefmat.notes);
    archive.setMadeBy(archiefmat.made_by);
    archive.setReminders(archiefmat.Aantekeningen);

    // Ignored fields
    if (archiefmat.creators != null) {
      for (String creator : archiefmat.creators) {
        handleError("[%s] Ignoring implied creator '%s'", archiefmat.orig_filename, creator);
      }
    }
    if (archiefmat.relation != null) {
      handleError("[%s] Ignoring field 'relation':'%s'", archiefmat.orig_filename, archiefmat.relation);
    }
    if (archiefmat.em != null) {
      handleError("[%s] Ignoring field 'em':'%s'", archiefmat.orig_filename, archiefmat.em);
    }

    return archive;
  }

  private void importArchiveRelations(Map<String, Reference> referenceMap) throws Exception {
    File directory = new File(inputDir, ARCHIEFMAT_DIR);
    for (File file : FileUtils.listFiles(directory, JSON_EXTENSION, true)) {
      for (ArchiefMatEntry entry : readJsonValue(file, ArchiefMatEntry[].class)) {
        ArchiefMat archiefmat = entry.archiefmat;
        Reference reference = referenceMap.get(archiefmat._id);
        addArchiveRelations(reference, archiefmat);
      }
    }
  }

  private void addArchiveRelations(Reference reference, ArchiefMat archiefmat) {
    Preconditions.checkNotNull(reference, "Missing archive reference");
    addRelations(reference, hasPlaceRef, keywordRefMap, archiefmat.geography);
    addRelations(reference, hasKeywordRef, keywordRefMap, archiefmat.keywords);
    addRelations(reference, hasPersonRef, personRefMap, archiefmat.persons);

    if (archiefmat.related != null) {
      for (XRelated item : archiefmat.related) {
        if ("overhead_title".equals(item.type)) {
          addRelations(reference, hasParentArchive, archiveRefMap, item.ids);
        } else if ("underlying_levels_titels".equals(item.type)) {
          addRelations(reference, hasChildArchive, archiveRefMap, item.ids);
        } else if ("unit".equals(item.type)) {
          addRelations(reference, hasSiblingArchive, archiveRefMap, item.ids);
        } else {
          handleError("Ignoring field 'related' with type '%s'", item.type);
        }
      }
    }
  }

  // --- Archivers -----------------------------------------------------

  private static final String CREATORS_DIR = "creators";

  private void importArchivers(Map<String, Reference> referenceMap) throws Exception {
    File directory = new File(inputDir, CREATORS_DIR);
    for (File file : FileUtils.listFiles(directory, JSON_EXTENSION, true)) {
      for (CreatorEntry entry : readJsonValue(file, CreatorEntry[].class)) {
        Creator creator = entry.creator;
        String jsonId = creator._id;
        if (referenceMap.containsKey(jsonId)) {
          handleError("[%s] Duplicate entry %s", file.getName(), jsonId);
        } else {
          ATLGArchiver archiver = convert(creator);
          String storedId = addEntity(ATLGArchiver.class, archiver, false);
          referenceMap.put(jsonId, new Reference(ATLGArchiver.class, storedId));
        }
      }
    }
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
      handleError("[%s] Ignoring field 'related_archives'", creator.orig_filename);
    }
    if (creator.related_creators != null) {
      handleError("[%s] Ignoring field 'related_creators'", creator.orig_filename);
    }

    archiver.setNotes(creator.notes);
    archiver.setLiterature(creator.literatuur);
    archiver.setMadeBy(creator.made_by);
    archiver.setReminders(creator.Aantekeningen);
    if (creator.types != null) {
      for (String type : creator.types) {
        archiver.addType(type);
      }
    }
    return archiver;
  }

  private void importArchiverRelations(Map<String, Reference> referenceMap) throws Exception {
    File directory = new File(inputDir, CREATORS_DIR);
    for (File file : FileUtils.listFiles(directory, JSON_EXTENSION, true)) {
      for (CreatorEntry entry : readJsonValue(file, CreatorEntry[].class)) {
        Creator creator = entry.creator;
        Reference reference = referenceMap.get(creator._id);
        addArchiverRelations(reference, creator);
      }
    }
  }

  private void addArchiverRelations(Reference reference, Creator creator) {
    Preconditions.checkNotNull(reference, "Missing archiver reference");
    addRelations(reference, hasPlaceRef, keywordRefMap, creator.geography);
    addRelations(reference, hasKeywordRef, keywordRefMap, creator.keywords);
    addRelations(reference, hasPersonRef, personRefMap, creator.persons);
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

  private static class XDates {
    public String date1;
    public String date2;
  }

  private static class XPeriod {
    public String begin_date;
    public String end_date;
  }

  private static class XSeeAlso {
    public String ref_id;
    public String text_line;

    @Override
    public String toString() {
      return (ref_id == null) ? text_line : String.format("%s: %s", ref_id, text_line);
    }
  }

}
