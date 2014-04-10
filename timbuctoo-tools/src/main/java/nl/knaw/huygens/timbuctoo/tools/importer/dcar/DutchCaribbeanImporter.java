package nl.knaw.huygens.timbuctoo.tools.importer.dcar;

/*
 * #%L
 * Timbuctoo tools
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static com.google.common.base.Preconditions.checkNotNull;
import static nl.knaw.huygens.timbuctoo.model.dcar.RelTypeNames.HAS_ARCHIVER_KEYWORD;
import static nl.knaw.huygens.timbuctoo.model.dcar.RelTypeNames.HAS_ARCHIVER_PERSON;
import static nl.knaw.huygens.timbuctoo.model.dcar.RelTypeNames.HAS_ARCHIVER_PLACE;
import static nl.knaw.huygens.timbuctoo.model.dcar.RelTypeNames.HAS_ARCHIVE_KEYWORD;
import static nl.knaw.huygens.timbuctoo.model.dcar.RelTypeNames.HAS_ARCHIVE_PERSON;
import static nl.knaw.huygens.timbuctoo.model.dcar.RelTypeNames.HAS_ARCHIVE_PLACE;
import static nl.knaw.huygens.timbuctoo.model.dcar.RelTypeNames.HAS_LEGISLATION_KEYWORD;
import static nl.knaw.huygens.timbuctoo.model.dcar.RelTypeNames.HAS_LEGISLATION_PERSON;
import static nl.knaw.huygens.timbuctoo.model.dcar.RelTypeNames.HAS_LEGISLATION_PLACE;
import static nl.knaw.huygens.timbuctoo.model.dcar.RelTypeNames.HAS_PARENT_ARCHIVE;
import static nl.knaw.huygens.timbuctoo.model.dcar.RelTypeNames.HAS_SIBLING_ARCHIVE;
import static nl.knaw.huygens.timbuctoo.model.dcar.RelTypeNames.HAS_SIBLING_ARCHIVER;
import static nl.knaw.huygens.timbuctoo.model.dcar.RelTypeNames.IS_CREATOR_OF;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.Archive;
import nl.knaw.huygens.timbuctoo.model.Archiver;
import nl.knaw.huygens.timbuctoo.model.Keyword;
import nl.knaw.huygens.timbuctoo.model.Legislation;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.RelationType;
import nl.knaw.huygens.timbuctoo.model.dcar.DCARArchive;
import nl.knaw.huygens.timbuctoo.model.dcar.DCARArchiver;
import nl.knaw.huygens.timbuctoo.model.dcar.DCARKeyword;
import nl.knaw.huygens.timbuctoo.model.dcar.DCARLegislation;
import nl.knaw.huygens.timbuctoo.model.dcar.DCARPerson;
import nl.knaw.huygens.timbuctoo.model.dcar.DCARRelation;
import nl.knaw.huygens.timbuctoo.model.dcar.XRelated;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;
import nl.knaw.huygens.timbuctoo.model.util.PersonNameComponent.Type;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;
import nl.knaw.huygens.timbuctoo.tools.util.EncodingFixer;
import nl.knaw.huygens.timbuctoo.util.Files;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Imports data of the "Dutch Caribbean" project.
 * 
 * Usage:
 *  java  -cp  [specs]  ${package-name}.DutchCaribbeanImporter  importDirName  configFileName
 * 
 * The commandline arguments are optional.
 * 
 * Note that the Mongo database and the Solr index are built from scratch, any existing data is deleted.
 * Future versions of this importer must use a more subtle approach.
 */
public class DutchCaribbeanImporter extends DutchCaribbeanDefaultImporter {

  private static final Logger LOG = LoggerFactory.getLogger(DutchCaribbeanImporter.class);

  public static void main(String[] args) throws Exception {

    // Handle commandline arguments
    String importDirName = (args.length > 0) ? args[0] : "../../AtlantischeGids/work/";
    String configFileName = (args.length > 1) ? args[1] : "config.xml";

    Configuration config = new Configuration(configFileName);
    Injector injector = Guice.createInjector(new ToolsInjectionModule(config));

    StorageManager storageManager = null;
    IndexManager indexManager = null;

    try {
      long start = System.currentTimeMillis();

      storageManager = injector.getInstance(StorageManager.class);
      indexManager = injector.getInstance(IndexManager.class);

      TypeRegistry registry = injector.getInstance(TypeRegistry.class);
      new DutchCaribbeanImporter(registry, storageManager, indexManager, importDirName).importAll();

      long time = (System.currentTimeMillis() - start) / 1000;
      System.out.printf("%n=== Used %d seconds%n%n", time);

    } catch (Exception e) {
      // for debugging
      e.printStackTrace();
    } finally {
      // Close resources
      if (indexManager != null) {
        indexManager.close();
      }
      if (storageManager != null) {
        storageManager.logCacheStats();
        storageManager.close();
      }
      // If the application is not explicitly closed a finalizer thread of Guice keeps running.
      System.exit(0);
    }
  }

  // -------------------------------------------------------------------

  private static final String[] JSON_EXTENSION = { "json" };

  private final ObjectMapper objectMapper;
  private final File inputDir;

  private final Map<String, Reference> keywordRefMap = Maps.newHashMap();
  private final Map<String, Reference> personRefMap = Maps.newHashMap();
  private final Map<String, Reference> legislationRefMap = Maps.newHashMap();
  private final Map<String, Reference> archiveRefMap = Maps.newHashMap();
  private final Map<String, Reference> archiverRefMap = Maps.newHashMap();

  private Reference isCreatorRef;
  private Reference hasArchiveKeywordRef;
  private Reference hasArchiverKeywordRef;
  private Reference hasLegislationKeywordRef;
  private Reference hasArchivePersonRef;
  private Reference hasArchiverPersonRef;
  private Reference hasLegislationPersonRef;
  private Reference hasArchivePlaceRef;
  private Reference hasArchiverPlaceRef;
  private Reference hasLegislationPlaceRef;
  private Reference hasParentArchive;
  private Reference hasSiblingArchive;
  private Reference hasSiblingArchiver;

  public DutchCaribbeanImporter(TypeRegistry registry, StorageManager storageManager, IndexManager indexManager, String inputDirName) {
    super(registry, storageManager, indexManager);
    objectMapper = new ObjectMapper();
    inputDir = new File(inputDirName);
    System.out.printf("%n.. Importing from %s%n", inputDir.getAbsolutePath());
  }

  private <T> T readJsonValue(File file, Class<T> valueType) throws Exception {
    String text = Files.readTextFromFile(file);
    // For Dutch Caribbean it seems OK to map "Ã " --> "à "
    String converted = EncodingFixer.convert2(text).replaceAll("Ã ", "à ");
    if (!converted.equals(text)) {
      int n = text.length() - converted.length();
      handleError("Fixed %d character encoding error%s in '%s'", n, (n == 1) ? "" : "s", file.getName());
    }
    return objectMapper.readValue(converted, valueType);
  }

  public void importAll() throws Exception {

    printBoxedText("1. Initialization");

    displayStatus();

    removeNonPersistentEnties(storageManager, indexManager);

    displayStatus();

    System.out.printf("%n.. Setup relation types%n");
    // FIXME system entities shouldn't have been removed!
    importRelationTypes();
    setupRelationTypes();

    printBoxedText("2. Basic properties");

    System.out.println(".. Keywords");
    importKeywords(keywordRefMap);
    System.out.printf("Number of entries = %d%n", keywordRefMap.size());

    System.out.println(".. Persons");
    importPersons(personRefMap);
    System.out.printf("Number of entries = %d%n", personRefMap.size());

    System.out.println(".. Legislation");
    importLegislation(legislationRefMap);
    System.out.printf("Number of entries = %d%n", legislationRefMap.size());

    System.out.println(".. Archives");
    importArchives(archiveRefMap);
    System.out.printf("Number of entries = %d%n", archiveRefMap.size());

    System.out.println(".. Archivers");
    importArchivers(archiverRefMap);
    System.out.printf("Number of entries = %d%n", archiverRefMap.size());

    printBoxedText("3. Relations");

    System.out.println(".. Legislation");
    importLegislationRelations(legislationRefMap);

    System.out.println(".. Archives");
    importArchiveRelations(archiveRefMap);

    System.out.println(".. Archivers");
    importArchiverRelations(archiverRefMap);

    printBoxedText("4. Indexing");

    indexEntities(DCARKeyword.class);
    indexEntities(DCARPerson.class);
    indexEntities(DCARLegislation.class);
    indexEntities(DCARArchive.class);
    indexEntities(DCARArchiver.class);

    displayStatus();

    displayErrorSummary();
  }

  private void removeNonPersistentEnties(StorageManager storageManager, IndexManager indexManager) throws IOException, IndexException {
    removeNonPersistentEntities(DCARKeyword.class);
    removeNonPersistentEntities(DCARPerson.class);
    removeNonPersistentEntities(DCARArchive.class);
    removeNonPersistentEntities(DCARArchiver.class);
    removeNonPersistentEntities(DCARLegislation.class);
  }

  // --- relations -----------------------------------------------------

  private void setupRelationTypes() {
    Map<String, RelationType> map = storageManager.getRelationTypeMap();
    isCreatorRef = retrieveRelationType(map, IS_CREATOR_OF.regular);
    hasArchiveKeywordRef = retrieveRelationType(map, HAS_ARCHIVE_KEYWORD.regular);
    hasArchiverKeywordRef = retrieveRelationType(map, HAS_ARCHIVER_KEYWORD.regular);
    hasLegislationKeywordRef = retrieveRelationType(map, HAS_LEGISLATION_KEYWORD.regular);
    hasArchivePersonRef = retrieveRelationType(map, HAS_ARCHIVE_PERSON.regular);
    hasArchiverPersonRef = retrieveRelationType(map, HAS_ARCHIVER_PERSON.regular);
    hasLegislationPersonRef = retrieveRelationType(map, HAS_LEGISLATION_PERSON.regular);
    hasArchivePlaceRef = retrieveRelationType(map, HAS_ARCHIVE_PLACE.regular);
    hasArchiverPlaceRef = retrieveRelationType(map, HAS_ARCHIVER_PLACE.regular);
    hasLegislationPlaceRef = retrieveRelationType(map, HAS_LEGISLATION_PLACE.regular);
    hasParentArchive = retrieveRelationType(map, HAS_PARENT_ARCHIVE.regular);
    hasSiblingArchive = retrieveRelationType(map, HAS_SIBLING_ARCHIVE.regular);
    hasSiblingArchiver = retrieveRelationType(map, HAS_SIBLING_ARCHIVER.regular);
  }

  private Reference retrieveRelationType(Map<String, RelationType> map, String name) {
    RelationType type = map.get(name);
    if (type != null) {
      return new Reference(RelationType.class, type.getId());
    } else {
      LOG.error("Failed to retrieve relation type {}", name);
      throw new IllegalStateException("Initialization error");
    }
  }

  private void addRegularRelations(Reference sourceRef, Reference relTypeRef, Map<String, Reference> map, String[] keys) {
    if (keys != null) {
      for (String key : keys) {
        addRelation(DCARRelation.class, relTypeRef, sourceRef, map.get(key), change, "");
      }
    }
  }

  private void addInverseRelations(Reference targetRef, Reference relTypeRef, Map<String, Reference> map, String[] keys) {
    if (keys != null) {
      for (String key : keys) {
        addRelation(DCARRelation.class, relTypeRef, map.get(key), targetRef, change, "");
      }
    }
  }

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
        DCARKeyword keyword = convert(xkeyword);
        String storedId = addDomainEntity(DCARKeyword.class, keyword);
        referenceMap.put(jsonId, new Reference(Keyword.class, storedId));
      }
    }
  }

  private DCARKeyword convert(XKeyword xkeyword) {
    DCARKeyword keyword = new DCARKeyword();

    String type = xkeyword.type;
    keyword.setType(type);

    if (type == null) {
      handleError("[%s] Missing type for id %s", KEYWORD_FILE, xkeyword._id);
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
        DCARPerson person = convert(xperson);
        String storedId = addDomainEntity(DCARPerson.class, person);
        referenceMap.put(jsonId, new Reference(Person.class, storedId));
      }
    }
  }

  private DCARPerson convert(XPerson xperson) {
    DCARPerson person = new DCARPerson();

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
    // should be prefix and suffix
    if (xperson.toevoeging != null) {
      name.addNameComponent(Type.ADD_NAME, xperson.toevoeging);
    }
    person.addName(name);

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
          handleError("[%s] Duplicate 'wetgeving' id %s", file.getName(), jsonId);
        } else {
          DCARLegislation legislation = convert(wetgeving);
          String storedId = addDomainEntity(DCARLegislation.class, legislation);
          referenceMap.put(jsonId, new Reference(Legislation.class, storedId));
        }
      }
    }
  }

  private DCARLegislation convert(Wetgeving wetgeving) {
    DCARLegislation legislation = new DCARLegislation();
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
        if (reference == null) {
          throw new IllegalStateException("Failed to retrieve legislation " + wetgeving._id);
        }
        addLegislationRelations(reference, wetgeving);
      }
    }
  }

  private void addLegislationRelations(Reference reference, Wetgeving wetgeving) {
    checkNotNull(reference, "Missing legislation reference");
    addRegularRelations(reference, hasLegislationPlaceRef, keywordRefMap, wetgeving.geography);
    addRegularRelations(reference, hasLegislationKeywordRef, keywordRefMap, wetgeving.keywords);
    addRegularRelations(reference, hasLegislationKeywordRef, keywordRefMap, wetgeving.keywords_extra);
    addRegularRelations(reference, hasLegislationPersonRef, personRefMap, wetgeving.persons);
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
          handleError("[%s] Duplicate 'archiefmat' id %s", file.getName(), jsonId);
        } else {
          DCARArchive archive = convert(archiefmat);
          String storedId = addDomainEntity(DCARArchive.class, archive);
          referenceMap.put(jsonId, new Reference(Archive.class, storedId));
        }
      }
    }
  }

  private DCARArchive convert(ArchiefMat archiefmat) {
    DCARArchive archive = new DCARArchive();
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
    if (archiefmat.relation != null) {
      handleError("[%s] Ignoring field 'relation': '%s'", archiefmat.orig_filename, archiefmat.relation);
    }
    if (archiefmat.em != null) {
      handleError("[%s] Ignoring field 'em': '%s'", archiefmat.orig_filename, archiefmat.em);
    }
    return archive;
  }

  private void importArchiveRelations(Map<String, Reference> referenceMap) throws Exception {
    File directory = new File(inputDir, ARCHIEFMAT_DIR);
    for (File file : FileUtils.listFiles(directory, JSON_EXTENSION, true)) {
      for (ArchiefMatEntry entry : readJsonValue(file, ArchiefMatEntry[].class)) {
        ArchiefMat archiefmat = entry.archiefmat;
        Reference reference = referenceMap.get(archiefmat._id);
        if (reference == null) {
          throw new IllegalStateException("Failed to retrieve archive " + archiefmat._id);
        }
        addArchiveRelations(reference, archiefmat);
      }
    }
  }

  private void addArchiveRelations(Reference reference, ArchiefMat archiefmat) {
    checkNotNull(reference, "Missing archive reference");
    addRegularRelations(reference, hasArchivePlaceRef, keywordRefMap, archiefmat.geography);
    addRegularRelations(reference, hasArchiveKeywordRef, keywordRefMap, archiefmat.keywords);
    addRegularRelations(reference, hasArchivePersonRef, personRefMap, archiefmat.persons);

    if (archiefmat.related != null) {
      for (XRelated item : archiefmat.related) {
        if ("overhead_title".equals(item.type)) {
          // hasChildArchive relation is implied
          addRegularRelations(reference, hasParentArchive, archiveRefMap, item.ids);
        } else if ("underlying_levels_titels".equals(item.type)) {
          // hasChildArchive relation is implied
          addInverseRelations(reference, hasParentArchive, archiveRefMap, item.ids);
        } else if ("unit".equals(item.type)) {
          // symmetric, stored in canonical form
          addRegularRelations(reference, hasSiblingArchive, archiveRefMap, item.ids);
        } else {
          handleError("[%s] Ignoring field 'archiefmat.related' with type '%s'", archiefmat.orig_filename, item.type);
        }
      }
    }
    addInverseRelations(reference, isCreatorRef, archiverRefMap, archiefmat.creators);
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
          handleError("[%s] Duplicate 'creator' id %s", file.getName(), jsonId);
        } else {
          DCARArchiver archiver = convert(creator);
          String storedId = addDomainEntity(DCARArchiver.class, archiver);
          referenceMap.put(jsonId, new Reference(Archiver.class, storedId));
        }
      }
    }
  }

  private DCARArchiver convert(Creator creator) {
    DCARArchiver archiver = new DCARArchiver();
    archiver.setOrigFilename(creator.orig_filename);
    archiver.setNameNld(creator.name);
    archiver.setNameEng(creator.name_english);
    if (creator.dates != null) {
      archiver.setBeginDate(creator.dates.begin_date);
      archiver.setEndDate(creator.dates.end_date);
    }
    archiver.setPeriodDescription(creator.period_description);
    archiver.setHistory(creator.his_func);
    archiver.setNotes(creator.notes);
    archiver.setLiterature(creator.literatuur);
    archiver.setMadeBy(creator.made_by);
    archiver.setReminders(creator.Aantekeningen);
    if (creator.types != null) {
      for (String type : creator.types) {
        archiver.addType(type);
      }
    }
    // Ignored fields; should have been preprocessed
    if (creator.related_archives != null) {
      handleError("[%s] Ignoring field 'creator.related_archives'", creator.orig_filename);
    }
    return archiver;
  }

  private void importArchiverRelations(Map<String, Reference> referenceMap) throws Exception {
    File directory = new File(inputDir, CREATORS_DIR);
    for (File file : FileUtils.listFiles(directory, JSON_EXTENSION, true)) {
      for (CreatorEntry entry : readJsonValue(file, CreatorEntry[].class)) {
        Creator creator = entry.creator;
        Reference reference = referenceMap.get(creator._id);
        if (reference == null) {
          throw new IllegalStateException("Failed to retrieve archive " + creator._id);
        }
        addArchiverRelations(reference, creator);
      }
    }
  }

  private void addArchiverRelations(Reference reference, Creator creator) {
    checkNotNull(reference, "Missing archiver reference");
    addRegularRelations(reference, hasArchiverPlaceRef, keywordRefMap, creator.geography);
    addRegularRelations(reference, hasArchiverKeywordRef, keywordRefMap, creator.keywords);
    addRegularRelations(reference, hasArchiverPersonRef, personRefMap, creator.persons);

    if (creator.related != null) {
      for (XRelated item : creator.related) {
        if ("archive".equals(item.type)) {
          // isCreatedByRef relation is implied
          addRegularRelations(reference, isCreatorRef, archiveRefMap, item.ids);
        } else if ("creator".equals(item.type)) {
          // symmetric, stored in canonical form
          addRegularRelations(reference, hasSiblingArchiver, archiverRefMap, item.ids);
        } else {
          handleError("[%s] Ignoring field 'creator.related' with type '%s'", creator.orig_filename, item.type);
        }
      }
    }
    // symmetric, stored in canonical form
    addRegularRelations(reference, hasSiblingArchiver, archiverRefMap, creator.related_creators);
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
