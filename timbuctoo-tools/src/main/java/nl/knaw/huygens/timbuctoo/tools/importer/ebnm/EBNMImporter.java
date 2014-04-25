package nl.knaw.huygens.timbuctoo.tools.importer.ebnm;

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

import java.io.File;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.ebnm.EBNMDocumentatie;
import nl.knaw.huygens.timbuctoo.model.ebnm.EBNMLexicon;
import nl.knaw.huygens.timbuctoo.model.ebnm.EBNMPeriode;
import nl.knaw.huygens.timbuctoo.model.ebnm.EBNMRegiocode;
import nl.knaw.huygens.timbuctoo.model.ebnm.EBNMSignalementcode;
import nl.knaw.huygens.timbuctoo.model.ebnm.EBNMTaal;
import nl.knaw.huygens.timbuctoo.model.ebnm.EBNMTekst;
import nl.knaw.huygens.timbuctoo.model.ebnm.EBNMTekstdrager;
import nl.knaw.huygens.timbuctoo.model.ebnm.EBNMWatermerk;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;
import nl.knaw.huygens.timbuctoo.tools.importer.DefaultImporter;
import nl.knaw.huygens.timbuctoo.tools.importer.RelationTypeImporter;
import nl.knaw.huygens.timbuctoo.tools.util.EncodingFixer;
import nl.knaw.huygens.timbuctoo.util.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;

/**
 * Imports data of the "eBNM" project.
 * 
 * Usage:
 *  java  -cp  [specs]  ${package-name}.EBNMImporter  [importDirName]
 */
public class EBNMImporter extends DefaultImporter {

  private static final Logger LOG = LoggerFactory.getLogger(EBNMImporter.class);

  public static void main(String[] args) throws Exception {
    Stopwatch stopWatch = Stopwatch.createStarted();

    // Handle commandline arguments
    String importDirName = (args.length > 0) ? args[0] : "../../codl_data/data/";

    Repository repository = null;
    try {
      repository = ToolsInjectionModule.createRepositoryInstance();
      new EBNMImporter(repository, importDirName).importAll();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (repository != null) {
        repository.close();
      }
      LOG.info("Time used: {}", stopWatch);
    }
  }

  // -------------------------------------------------------------------

  private final ObjectMapper objectMapper;
  private final File inputDir;
  private final Change change;

  private final Map<String, Reference> documentatieRefMap = Maps.newHashMap();
  private final Map<String, Reference> lexiconRefMap = Maps.newHashMap();
  private final Map<String, Reference> tekstdragerRefMap = Maps.newHashMap();
  private final Map<String, Reference> tekstRefMap = Maps.newHashMap();
  private final Map<String, Reference> taalRefMap = Maps.newHashMap();
  private final Map<String, Reference> periodeRefMap = Maps.newHashMap();
  private final Map<String, Reference> regiocodeRefMap = Maps.newHashMap();
  private final Map<String, Reference> signalementcodeRefMap = Maps.newHashMap();
  private final Map<String, Reference> watermerkRefMap = Maps.newHashMap();

  public EBNMImporter(Repository repository, String inputDirName) {
    super(repository);
    objectMapper = new ObjectMapper();
    inputDir = new File(inputDirName);
    System.out.printf("%n.. Importing from %s%n", inputDir.getAbsolutePath());
    change = new Change("importer", "ebnm");
    setup(storageManager);
  }

  // File with {@code RelationType} definitions; must be present on classpath.
  private static final String RELATION_TYPE_DEFS = "relationtype-defs-codl.txt";

  private void setup(StorageManager storageManager) {
    try {
      new RelationTypeImporter(storageManager).importRelationTypes(RELATION_TYPE_DEFS);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
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

    removeNonPersistentEntities(EBNMDocumentatie.class);

    System.out.printf("%n.. Setup relation types%n");

    printBoxedText("2. Basic properties");

    System.out.println(".. Documentatie");
    importDocumentatie(documentatieRefMap);
    System.out.printf("Number of entries = %d%n", documentatieRefMap.size());

    System.out.println(".. Lexicon");
    importLexicon(lexiconRefMap);
    System.out.printf("Number of entries = %d%n", lexiconRefMap.size());

    System.out.println(".. Tekstdragers");
    importTekstdrager(tekstdragerRefMap);
    System.out.printf("Number of entries = %d%n", tekstdragerRefMap.size());

    System.out.println(".. Teksten");
    importTekst(tekstRefMap);
    System.out.printf("Number of entries = %d%n", tekstRefMap.size());

    System.out.println(".. Talen");
    importTaal(taalRefMap);
    System.out.printf("Number of entries = %d%n", taalRefMap.size());

    System.out.println(".. Periodes");
    importPeriode(periodeRefMap);
    System.out.printf("Number of entries = %d%n", periodeRefMap.size());

    System.out.println(".. Regiocodes");
    importRegiocode(regiocodeRefMap);
    System.out.printf("Number of entries = %d%n", regiocodeRefMap.size());

    System.out.println(".. Signalementcodes");
    importSignalementcode(signalementcodeRefMap);
    System.out.printf("Number of entries = %d%n", signalementcodeRefMap.size());

    System.out.println(".. Watermerken");
    importWatermerk(watermerkRefMap);
    System.out.printf("Number of entries = %d%n", watermerkRefMap.size());

    printBoxedText("3. Relations");

    printBoxedText("4. Indexing");

    displayStatus();
    displayErrorSummary();
  }

  // --- Documentatie ------------------------------------------------------

  private static final String DOCUMENTATIE_DIR = ".";
  private static final String DOCUMENTATIE_FILE = "documentatie.json";

  private void importDocumentatie(Map<String, Reference> referenceMap) throws Exception {
    File file = new File(new File(inputDir, DOCUMENTATIE_DIR), DOCUMENTATIE_FILE);
    for (XDocumentatie xdocumentatie : readJsonValue(file, XDocumentatie[].class)) {
      String jsonId = xdocumentatie._id;
      if (referenceMap.containsKey(jsonId)) {
        handleError("[%s] Duplicate documentatie id %s", DOCUMENTATIE_FILE, jsonId);
      } else {
        EBNMDocumentatie documentatie = convert(xdocumentatie);
        String storedId = addDomainEntity(EBNMDocumentatie.class, documentatie, change);
        referenceMap.put(jsonId, new Reference(EBNMDocumentatie.class, storedId));
      }
    }
  }

  private EBNMDocumentatie convert(XDocumentatie xdocumentatie) {
    EBNMDocumentatie documentatie = new EBNMDocumentatie();

    String type = xdocumentatie.type;
    documentatie.setType(type);

    if (type == null) {
      handleError("[%s] Missing type for id %s", DOCUMENTATIE_FILE, xdocumentatie._id);
    } else if (type.equals("boek") || type.equals("documentatie") || type.equals("tijdschrift of reeks") || type.equals("artikel in tijdschrift")) {
      documentatie.setValue(xdocumentatie.cite_as);
    } else {
      handleError("[%s] Unknown type %s", DOCUMENTATIE_FILE, type);
      documentatie.setValue("?");
    }

    if (xdocumentatie.titel != null) {
      documentatie.setLabel(xdocumentatie.titel[0]);
    }
    documentatie.setCodeId(xdocumentatie._id);
    documentatie.setCite_as(xdocumentatie.cite_as);
    documentatie.setPpn(xdocumentatie.ppn);
    documentatie.setSoort(xdocumentatie.soort);
    documentatie.setType(xdocumentatie.type);
    documentatie.setRegionaam(xdocumentatie.regionaam);

    documentatie.setAnnotatie_algemeen(xdocumentatie.annotatie_algemeen);
    documentatie.setAnnotatie_bibl_ref(xdocumentatie.annotatie_bibl_ref);
    documentatie.setAnnotatie_bibliografie(xdocumentatie.annotatie_bibliografie);
    documentatie.setAnnotatie_chron_rel(xdocumentatie.annotatie_chron_rel);
    documentatie.setAnnotatie_datering(xdocumentatie.annotatie_datering);
    documentatie.setAnnotatie_exemplaar(xdocumentatie.annotatie_exemplaar);
    documentatie.setAnnotatie_exemplaar_alg(xdocumentatie.annotatie_exemplaar_alg);
    documentatie.setAnnotatie_hor_rel(xdocumentatie.annotatie_hor_rel);
    documentatie.setAnnotatie_incipit(xdocumentatie.annotatie_incipit);
    documentatie.setAnnotatie_inhoud(xdocumentatie.annotatie_inhoud);
    documentatie.setAnnotatie_materiaal(xdocumentatie.annotatie_materiaal);
    documentatie.setAnnotatie_overig(xdocumentatie.annotatie_overig);
    documentatie.setAnnotatie_rel__gr_kl(xdocumentatie.annotatie_rel__gr_kl);
    documentatie.setAuteur(xdocumentatie.auteur);
    documentatie.setBandinformatie(xdocumentatie.bandinformatie);
    documentatie.setBasiscode_goo(xdocumentatie.basiscode_goo);
    documentatie.setBezitter(xdocumentatie.bezitter);
    documentatie.setBinder(xdocumentatie.binder);
    documentatie.setBnb_nr(xdocumentatie.bnb_nr);
    documentatie.setBoodschap_alg(xdocumentatie.boodschap_alg);
    documentatie.setBrinkman_nr(xdocumentatie.brinkman_nr);
    documentatie.setCategorie(xdocumentatie.categorie);
    documentatie.setCcp_nr(xdocumentatie.ccp_nr);
    documentatie.setClc(xdocumentatie.clc);
    documentatie.setClc_nr(xdocumentatie.clc_nr);
    documentatie.setCoden(xdocumentatie.coden);
    documentatie.setCongres(xdocumentatie.congres);
    documentatie.setCtc_nr(xdocumentatie.ctc_nr);
    documentatie.setDewey(xdocumentatie.dewey);
    documentatie.setData_van_uitgave(xdocumentatie.data_van_uitgave);
    documentatie.setDatering_gecodeerd(xdocumentatie.datering_gecodeerd);
    documentatie.setDatum_exempl(xdocumentatie.datum_exempl);
    documentatie.setDb_nr(xdocumentatie.db_nr);
    documentatie.setDeel_titel(xdocumentatie.deel_titel);
    documentatie.setDeelvermelding(xdocumentatie.deelvermelding);
    documentatie.setDrukker(xdocumentatie.drukker);
    documentatie.setDrukker_ingang(xdocumentatie.drukker_ingang);
    documentatie.setEditieveld(xdocumentatie.editieveld);
    documentatie.setEpn(xdocumentatie.epn);
    documentatie.setFrequentie(xdocumentatie.frequentie);
    documentatie.setFrequentie_code(xdocumentatie.frequentie_code);
    documentatie.setFysieke_beschrijving(xdocumentatie.fysieke_beschrijving);
    documentatie.setHerkomst(xdocumentatie.herkomst);
    documentatie.setIllustrator(xdocumentatie.illustrator);
    documentatie.setInstitute(xdocumentatie.institute);
    documentatie.setIsbn(xdocumentatie.isbn);
    documentatie.setIsbn_fout(xdocumentatie.isbn_fout);
    documentatie.setIsbn_volgend(xdocumentatie.isbn_volgend);
    documentatie.setIssn(xdocumentatie.issn);
    documentatie.setIssn_fout(xdocumentatie.issn_fout);
    documentatie.setJaar_van_uitgave(xdocumentatie.jaar_van_uitgave);
    documentatie.setJaargang(xdocumentatie.jaargang);
    documentatie.setKill_verzoek(xdocumentatie.kill_verzoek);
    documentatie.setKopiist(xdocumentatie.kopiist);
    documentatie.setLandcode(xdocumentatie.landcode);
    documentatie.setLandcode_goo(xdocumentatie.landcode_goo);
    documentatie.setLc_nr(xdocumentatie.lc_nr);
    documentatie.setLeverancier_prod_nr(xdocumentatie.leverancier_prod_nr);
    documentatie.setLokaal_trefwoord(xdocumentatie.lokaal_trefwoord);
    documentatie.setNlm(xdocumentatie.nlm);
    documentatie.setNur(xdocumentatie.nur);
    documentatie.setOnbekende_code(xdocumentatie.onbekende_code);
    documentatie.setOnderwerp(xdocumentatie.onderwerp);
    documentatie.setOpm_bibl_lokaal(xdocumentatie.opm_bibl_lokaal);
    documentatie.setOther_title(xdocumentatie.other_title);
    documentatie.setPart_of(xdocumentatie.part_of);
    documentatie.setPeriode_goo(xdocumentatie.periode_goo);
    documentatie.setPpn_link_papier_url(xdocumentatie.ppn_link_papier_url);
    documentatie.setSectie_titel(xdocumentatie.sectie_titel);
    documentatie.setSelectie_sleutel(xdocumentatie.selectie_sleutel);
    documentatie.setSignalementcode(xdocumentatie.signalementcode);
    documentatie.setSiso(xdocumentatie.siso);
    documentatie.setSiso_oud(xdocumentatie.siso_oud);
    documentatie.setSoortcode(xdocumentatie.soortcode);
    documentatie.setSorteerveld(xdocumentatie.sorteerveld);
    documentatie.setSwets_nr(xdocumentatie.swets_nr);
    documentatie.setTaalcatalogisering(xdocumentatie.taalcatalogisering);
    documentatie.setTaalcode(xdocumentatie.taalcode);
    documentatie.setTitel(xdocumentatie.titel);
    documentatie.setTitel_genormaliseerd(xdocumentatie.titel_genormaliseerd);
    documentatie.setTitel_md_publ_ppn(xdocumentatie.titel_md_publ_ppn);
    documentatie.setTitel_reeks(xdocumentatie.titel_reeks);
    documentatie.setTitel_reeks_md_publ(xdocumentatie.titel_reeks_md_publ);
    documentatie.setTitel_reeks_ppn(xdocumentatie.titel_reeks_ppn);
    documentatie.setTrefwoord(xdocumentatie.trefwoord);
    documentatie.setTrefwoord_extra(xdocumentatie.trefwoord_extra);
    documentatie.setTrefwoord_oc_artikelen(xdocumentatie.trefwoord_oc_artikelen);
    documentatie.setTrefwoord_precis(xdocumentatie.trefwoord_precis);
    documentatie.setTussen_titel(xdocumentatie.tussen_titel);
    documentatie.setType_drager(xdocumentatie.type_drager);
    documentatie.setType_inhoud(xdocumentatie.type_inhoud);
    documentatie.setType_medium(xdocumentatie.type_medium);
    documentatie.setUdc(xdocumentatie.udc);
    documentatie.setUitgever(xdocumentatie.uitgever);
    documentatie.setUitgever_volgend(xdocumentatie.uitgever_volgend);
    documentatie.setUnesco_rubr(xdocumentatie.unesco_rubr);
    documentatie.setUniforme_titel(xdocumentatie.uniforme_titel);
    documentatie.setVerw_onderdeel_ppn(xdocumentatie.verw_onderdeel_ppn);

    return documentatie;
  }

  // --- Lexicon ------------------------------------------------------

  private static final String LEXICON_DIR = ".";
  private static final String LEXICON_FILE = "lexicon.json";

  private void importLexicon(Map<String, Reference> referenceMap) throws Exception {
    File file = new File(new File(inputDir, LEXICON_DIR), LEXICON_FILE);
    for (XLexicon xlexicon : readJsonValue(file, XLexicon[].class)) {
      String jsonId = xlexicon._id;
      if (referenceMap.containsKey(jsonId)) {
        handleError("[%s] Duplicate lexicon id %s", LEXICON_FILE, jsonId);
      } else {
        EBNMLexicon lexicon = convert(xlexicon);
        String storedId = addDomainEntity(EBNMLexicon.class, lexicon, change);
        referenceMap.put(jsonId, new Reference(EBNMLexicon.class, storedId));
      }
    }
  }

  private EBNMLexicon convert(XLexicon xlexicon) {
    EBNMLexicon lexicon = new EBNMLexicon();

    String type = xlexicon.type;
    lexicon.setType(type);

    if (type == null) {
      handleError("[%s] Missing type for id %s", LEXICON_FILE, xlexicon._id);
    } else if (type.equals("person") || type.equals("tekst") || type.equals("institute") || type.equals("unknown")) {
      if (xlexicon.ingang != null)
        lexicon.setValue(xlexicon.ingang[0]);
      else
        lexicon.setValue("");
    } else {
      handleError("[%s] Unknown type %s", LEXICON_FILE, type);
      lexicon.setValue("?");
    }

    if (xlexicon.ingang != null) {
      lexicon.setLabel(xlexicon.ingang[0]);
    }
    lexicon.setPpn(xlexicon.ppn);
    lexicon.setSoort(xlexicon.soort);
    lexicon.setUrl(xlexicon.url);
    lexicon.setType(xlexicon.type);
    lexicon.setStandaard_naam(xlexicon.standaard_naam);
    lexicon.setAdresgegevens(xlexicon.adresgegevens);
    lexicon.setBeroeps_aanduiding(xlexicon.beroeps_aanduiding);
    lexicon.setBeroep_vakgebied(xlexicon.beroep_vakgebied);
    lexicon.setBredere_term(xlexicon.bredere_term);
    lexicon.setIndicatoren(xlexicon.indicatoren);
    lexicon.setIngang(xlexicon.ingang);
    lexicon.setInstitute_bredere_term(xlexicon.institute_bredere_term);
    lexicon.setInstituut(xlexicon.instituut);
    lexicon.setLandcode(xlexicon.landcode);
    lexicon.setMeisjesnaam(xlexicon.meisjesnaam);
    lexicon.setNaam_echtgenoot(xlexicon.naam_echtgenoot);
    lexicon.setNaams_variant(xlexicon.naams_variant);
    lexicon.setNaamsvariant(xlexicon.naamsvariant);
    lexicon.setNormaliserende_ingang(xlexicon.normaliserende_ingang);
    lexicon.setRelaties_overig(xlexicon.relaties_overig);
    lexicon.setSec_ingang(xlexicon.sec_ingang);
    lexicon.setSorteerveld(xlexicon.sorteerveld);
    lexicon.setTaalcode(xlexicon.taalcode);
    lexicon.setToelichtingen(xlexicon.toelichtingen);
    lexicon.setToepassing(xlexicon.toepassing);
    lexicon.setVerwante_term(xlexicon.verwante_term);
    lexicon.setVerwijzing_eigen(xlexicon.verwijzing_eigen);
    lexicon.setVerwijzing_institute(xlexicon.verwijzing_institute);
    lexicon.setVerwijzing_pseudoniem(xlexicon.verwijzing_pseudoniem);
    lexicon.setVolledige_naam(xlexicon.volledige_naam);
    lexicon.setYears(xlexicon.years);
    return lexicon;
  }

  // --- Tekstdrager ------------------------------------------------------

  private static final String TEKSTDRAGER_DIR = ".";
  private static final String TEKSTDRAGER_FILE = "tekstdragers.json";

  private void importTekstdrager(Map<String, Reference> referenceMap) throws Exception {
    File file = new File(new File(inputDir, TEKSTDRAGER_DIR), TEKSTDRAGER_FILE);
    for (XTekstdrager xtekstdrager : readJsonValue(file, XTekstdrager[].class)) {
      String jsonId = xtekstdrager._id;
      if (referenceMap.containsKey(jsonId)) {
        handleError("[%s] Duplicate tekstdrager id %s", TEKSTDRAGER_FILE, jsonId);
      } else {
        EBNMTekstdrager tekstdrager = convert(xtekstdrager);
        String storedId = addDomainEntity(EBNMTekstdrager.class, tekstdrager, change);
        referenceMap.put(jsonId, new Reference(EBNMTekstdrager.class, storedId));
      }
    }
  }

  private EBNMTekstdrager convert(XTekstdrager xtekstdrager) {
    EBNMTekstdrager tekstdrager = new EBNMTekstdrager();

    String type = xtekstdrager.type;
    tekstdrager.setType(type);

    if (type == null) {
      handleError("[%s] Missing type for id %s", TEKSTDRAGER_FILE, xtekstdrager._id);
    } else if (type.equals("tekstdrager_record")) {
      if (xtekstdrager.ingang != null)
        tekstdrager.setValue(xtekstdrager.ingang[0] + "");
      else
        tekstdrager.setValue("");
    } else {
      handleError("[%s] Unknown type %s", TEKSTDRAGER_FILE, type);
      tekstdrager.setValue("?");
    }

    if (xtekstdrager.ingang != null) {
      tekstdrager.setLabel((String) xtekstdrager.ingang[0]);
    }
    tekstdrager.setOverige_hs_aanduiding(xtekstdrager.overige_hs_aanduiding);
    tekstdrager.setPpn(xtekstdrager.ppn);
    tekstdrager.setSoort(xtekstdrager.soort);
    tekstdrager.setType(xtekstdrager.type);

    tekstdrager.setAnnotatie_colofon(xtekstdrager.annotatie_colofon);
    tekstdrager.setAnnotatie_datering(xtekstdrager.annotatie_datering);
    tekstdrager.setAnnotatie_documentatie(xtekstdrager.annotatie_documentatie);
    tekstdrager.setAnnotatie_incipit(xtekstdrager.annotatie_incipit);
    tekstdrager.setAnnotatie_inhoud(xtekstdrager.annotatie_inhoud);
    tekstdrager.setAnnotatie_materiaal(xtekstdrager.annotatie_materiaal);
    tekstdrager.setAnnotatie_overig(xtekstdrager.annotatie_overig);
    tekstdrager.setAnnotatie_rel__gr_kl(xtekstdrager.annotatie_rel__gr_kl);
    tekstdrager.setAnnotatie_schrift(xtekstdrager.annotatie_schrift);
    tekstdrager.setAuteur(xtekstdrager.auteur);
    tekstdrager.setBewaarplaats_en_signatuur(xtekstdrager.bewaarplaats_en_signatuur);
    tekstdrager.setBezitter(xtekstdrager.bezitter);
    tekstdrager.setBinder(xtekstdrager.binder);
    tekstdrager.setBoodschap_alg(xtekstdrager.boodschap_alg);
    tekstdrager.setCategorie(xtekstdrager.categorie);
    tekstdrager.setCorrector(xtekstdrager.corrector);
    tekstdrager.setDatering(xtekstdrager.datering);
    tekstdrager.setDatum_exempl(xtekstdrager.datum_exempl);
    tekstdrager.setDecorator(xtekstdrager.decorator);
    tekstdrager.setEpn(xtekstdrager.epn);
    tekstdrager.setIllustrator(xtekstdrager.illustrator);
    tekstdrager.setIngang(xtekstdrager.ingang);
    tekstdrager.setJournaalnummer(xtekstdrager.journaalnummer);
    tekstdrager.setKopiist(xtekstdrager.kopiist);
    tekstdrager.setLandcode(xtekstdrager.landcode);
    tekstdrager.setLayout_code(xtekstdrager.layout_code);
    tekstdrager.setLokalisering(xtekstdrager.lokalisering);
    tekstdrager.setOnbekende_code(xtekstdrager.onbekende_code);
    tekstdrager.setOnderwerp(xtekstdrager.onderwerp);
    tekstdrager.setOpdrachtgever(xtekstdrager.opdrachtgever);
    tekstdrager.setRegiocode(xtekstdrager.regiocode);
    tekstdrager.setSelectie_sleutel(xtekstdrager.selectie_sleutel);
    tekstdrager.setSignalementcode(xtekstdrager.signalementcode);
    tekstdrager.setSiso_oud(xtekstdrager.siso_oud);
    tekstdrager.setSleutelw_incipit(xtekstdrager.sleutelw_incipit);
    tekstdrager.setSoortcode(xtekstdrager.soortcode);
    tekstdrager.setSorteerveld(xtekstdrager.sorteerveld);
    tekstdrager.setTaalcode(xtekstdrager.taalcode);
    tekstdrager.setTitel_genormaliseerd(xtekstdrager.titel_genormaliseerd);
    tekstdrager.setTitel_hs_inc_form(xtekstdrager.titel_hs_inc_form);
    tekstdrager.setTrefwoord(xtekstdrager.trefwoord);
    tekstdrager.setUrl(xtekstdrager.url);
    tekstdrager.setWatermerk(xtekstdrager.watermerk);

    return tekstdrager;
  }

  // --- Teksten ------------------------------------------------------

  private static final String TEKST_DIR = ".";
  private static final String TEKST_FILE = "teksten.json";

  private void importTekst(Map<String, Reference> referenceMap) throws Exception {
    File file = new File(new File(inputDir, TEKST_DIR), TEKST_FILE);
    for (XTekst xtekst : readJsonValue(file, XTekst[].class)) {
      String jsonId = xtekst._id;
      if (referenceMap.containsKey(jsonId)) {
        handleError("[%s] Duplicate tekst id %s", TEKST_FILE, jsonId);
      } else {
        EBNMTekst tekst = convert(xtekst);
        String storedId = addDomainEntity(EBNMTekst.class, tekst, change);
        referenceMap.put(jsonId, new Reference(EBNMTekst.class, storedId));
      }
    }
  }

  private EBNMTekst convert(XTekst xtekst) {
    EBNMTekst tekst = new EBNMTekst();

    String type = xtekst.type;
    tekst.setType(type);

    if (type == null) {
      handleError("[%s] Missing type for id %s", TEKST_FILE, xtekst._id);
    } else if (type.equals("tekst_record")) {
      if (xtekst.ingang != null)
        tekst.setValue(xtekst.ingang[0] + "");
      else
        tekst.setValue("");
    } else {
      handleError("[%s] Unknown type %s", TEKST_FILE, type);
      tekst.setValue("?");
    }

    if (xtekst.ingang != null) {
      tekst.setLabel((String) xtekst.ingang[0]);
    }

    tekst.setOverige_hs_aanduiding(xtekst.overige_hs_aanduiding);
    tekst.setPpn(xtekst.ppn);
    tekst.setSoort(xtekst.soort);
    tekst.setType(xtekst.type);

    tekst.setAnnotatie_colofon(xtekst.annotatie_colofon);
    tekst.setAnnotatie_datering(xtekst.annotatie_datering);
    tekst.setAnnotatie_documentatie(xtekst.annotatie_documentatie);
    tekst.setAnnotatie_incipit(xtekst.annotatie_incipit);
    tekst.setAnnotatie_inhoud(xtekst.annotatie_inhoud);
    tekst.setAnnotatie_materiaal(xtekst.annotatie_materiaal);
    tekst.setAnnotatie_overig(xtekst.annotatie_overig);
    tekst.setAnnotatie_rel__gr_kl(xtekst.annotatie_rel__gr_kl);
    tekst.setAnnotatie_schrift(xtekst.annotatie_schrift);
    tekst.setAuteur(xtekst.auteur);
    tekst.setBewaarplaats_en_signatuur(xtekst.bewaarplaats_en_signatuur);
    tekst.setBezitter(xtekst.bezitter);
    tekst.setBinder(xtekst.binder);
    tekst.setBoodschap_alg(xtekst.boodschap_alg);
    tekst.setCategorie(xtekst.categorie);
    tekst.setCorrector(xtekst.corrector);
    tekst.setDatering(xtekst.datering);
    tekst.setDatum_exempl(xtekst.datum_exempl);
    tekst.setDecorator(xtekst.decorator);
    tekst.setDewey(xtekst.dewey);
    tekst.setEpn(xtekst.epn);
    tekst.setIllustrator(xtekst.illustrator);
    tekst.setIngang(xtekst.ingang);
    tekst.setJournaalnummer(xtekst.journaalnummer);
    tekst.setKopiist(xtekst.kopiist);
    tekst.setLandcode(xtekst.landcode);
    tekst.setLayout_code(xtekst.layout_code);
    tekst.setLokalisering(xtekst.lokalisering);
    tekst.setOnbekende_code(xtekst.onbekende_code);
    tekst.setOnderwerp(xtekst.onderwerp);
    tekst.setOpdrachtgever(xtekst.opdrachtgever);
    tekst.setPart_of(xtekst.part_of);
    tekst.setRegiocode(xtekst.regiocode);
    tekst.setSelectie_sleutel(xtekst.selectie_sleutel);
    tekst.setSignalementcode(xtekst.signalementcode);
    tekst.setSiso_oud(xtekst.siso_oud);
    tekst.setSleutelw_incipit(xtekst.sleutelw_incipit);
    tekst.setSoortcode(xtekst.soortcode);
    tekst.setSortcode(xtekst.sortcode);
    tekst.setSorteerveld(xtekst.sorteerveld);
    tekst.setTaalcode(xtekst.taalcode);
    tekst.setTitel(xtekst.titel);
    tekst.setTitel_genormaliseerd(xtekst.titel_genormaliseerd);
    tekst.setTitel_hs_inc_form(xtekst.titel_hs_inc_form);
    tekst.setTrefwoord(xtekst.trefwoord);
    tekst.setUrl(xtekst.url);
    tekst.setWatermerk(xtekst.watermerk);
    return tekst;
  }

  // --- Talen ------------------------------------------------------

  private static final String TAAL_DIR = ".";
  private static final String TAAL_FILE = "talen.json";

  private void importTaal(Map<String, Reference> referenceMap) throws Exception {
    File file = new File(new File(inputDir, TAAL_DIR), TAAL_FILE);
    for (XTaal xtaal : readJsonValue(file, XTaal[].class)) {
      String jsonId = xtaal._id;
      if (referenceMap.containsKey(jsonId)) {
        handleError("[%s] Duplicate taal id %s", TAAL_FILE, jsonId);
      } else {
        EBNMTaal taal = convert(xtaal);
        String storedId = addDomainEntity(EBNMTaal.class, taal, change);
        referenceMap.put(jsonId, new Reference(EBNMTaal.class, storedId));
      }
    }
  }

  private EBNMTaal convert(XTaal xtaal) {
    EBNMTaal taal = new EBNMTaal();

    if (xtaal.taal != null) {
      taal.setTaalId(xtaal._id);
      taal.setTaal(xtaal.taal);
    } else {
      taal.setValue("");
    }
    return taal;
  }

  // --- Periodes ------------------------------------------------------

  private static final String PERIODE_DIR = ".";
  private static final String PERIODE_FILE = "periodes.json";

  private void importPeriode(Map<String, Reference> referenceMap) throws Exception {
    File file = new File(new File(inputDir, PERIODE_DIR), PERIODE_FILE);
    for (XPeriode xperiode : readJsonValue(file, XPeriode[].class)) {
      String jsonId = xperiode._id;
      if (referenceMap.containsKey(jsonId)) {
        handleError("[%s] Duplicate periode id %s", PERIODE_FILE, jsonId);
      } else {
        EBNMPeriode periode = convert(xperiode);
        String storedId = addDomainEntity(EBNMPeriode.class, periode, change);
        referenceMap.put(jsonId, new Reference(EBNMPeriode.class, storedId));
      }
    }
  }

  private EBNMPeriode convert(XPeriode xperiode) {
    EBNMPeriode periode = new EBNMPeriode();

    if (xperiode.periode != null) {
      periode.setValue(xperiode.periode);
    } else {
      periode.setValue("");
    }
    periode.setCodeId(xperiode._id);
    periode.setPeriode(xperiode.periode);

    return periode;
  }

  // --- Regiocodes ------------------------------------------------------

  private static final String REGIOCODE_DIR = ".";
  private static final String REGIOCODE_FILE = "regiocodes.json";

  private void importRegiocode(Map<String, Reference> referenceMap) throws Exception {
    File file = new File(new File(inputDir, REGIOCODE_DIR), REGIOCODE_FILE);
    for (XRegiocode xregiocode : readJsonValue(file, XRegiocode[].class)) {
      String jsonId = xregiocode._id;
      if (referenceMap.containsKey(jsonId)) {
        handleError("[%s] Duplicate regiocode id %s", REGIOCODE_FILE, jsonId);
      } else {
        EBNMRegiocode regiocode = convert(xregiocode);
        String storedId = addDomainEntity(EBNMRegiocode.class, regiocode, change);
        referenceMap.put(jsonId, new Reference(EBNMRegiocode.class, storedId));
      }
    }
  }

  private EBNMRegiocode convert(XRegiocode xregiocode) {
    EBNMRegiocode regiocode = new EBNMRegiocode();

    if (xregiocode.regio != null) {
      regiocode.setValue(xregiocode.regio);
    } else {
      regiocode.setValue("");
    }
    regiocode.setCodeId(xregiocode._id);
    regiocode.setRegio(xregiocode.regio);
    return regiocode;
  }

  // --- Signalementcodes
  // ------------------------------------------------------

  private static final String SIGNALEMENTCODE_DIR = ".";
  private static final String SIGNALEMENTCODE_FILE = "signalementcodes.json";

  private void importSignalementcode(Map<String, Reference> referenceMap) throws Exception {
    File file = new File(new File(inputDir, SIGNALEMENTCODE_DIR), SIGNALEMENTCODE_FILE);
    for (XSignalementcode xsignalementcode : readJsonValue(file, XSignalementcode[].class)) {
      String jsonId = xsignalementcode._id;
      if (referenceMap.containsKey(jsonId)) {
        handleError("[%s] Duplicate signalementcode id %s", SIGNALEMENTCODE_FILE, jsonId);
      } else {
        EBNMSignalementcode signalementcode = convert(xsignalementcode);
        String storedId = addDomainEntity(EBNMSignalementcode.class, signalementcode, change);
        referenceMap.put(jsonId, new Reference(EBNMSignalementcode.class, storedId));
      }
    }
  }

  private EBNMSignalementcode convert(XSignalementcode xsignalementcode) {
    EBNMSignalementcode signalementcode = new EBNMSignalementcode();

    if (xsignalementcode.signalement != null) {
      signalementcode.setValue(xsignalementcode.signalement);
    } else {
      signalementcode.setValue("");
    }
    signalementcode.setCodeId(xsignalementcode._id);
    signalementcode.setSignalement(xsignalementcode.signalement);

    return signalementcode;
  }

  // --- Watermerken ------------------------------------------------------

  private static final String WATERMERK_DIR = ".";
  private static final String WATERMERK_FILE = "watermerken.json";

  private void importWatermerk(Map<String, Reference> referenceMap) throws Exception {
    File file = new File(new File(inputDir, WATERMERK_DIR), WATERMERK_FILE);
    for (XWatermerk xwatermerk : readJsonValue(file, XWatermerk[].class)) {
      String jsonId = xwatermerk._id;
      if (referenceMap.containsKey(jsonId)) {
        handleError("[%s] Duplicate watermerk id %s", WATERMERK_FILE, jsonId);
      } else {
        EBNMWatermerk watermerk = convert(xwatermerk);
        String storedId = addDomainEntity(EBNMWatermerk.class, watermerk, change);
        referenceMap.put(jsonId, new Reference(EBNMWatermerk.class, storedId));
      }
    }
  }

  private EBNMWatermerk convert(XWatermerk xwatermerk) {
    EBNMWatermerk watermerk = new EBNMWatermerk();

    if (xwatermerk.watermerk != null) {
      watermerk.setValue(xwatermerk.watermerk);
    } else {
      watermerk.setValue("");
    }
    watermerk.setCodeId(xwatermerk._id);
    watermerk.setWatermerk(xwatermerk.watermerk);
    return watermerk;
  }

  // -------------------------------------------------------------------
  // --- Data model defined in ING Forms -------------------------------
  // -------------------------------------------------------------------

  public static class XDocumentatie {
    /** ### Assigned id (admin) */

    public String _id;
    public String cite_as;
    public String ppn;
    public String soort;
    public String type;
    public String regionaam;

    public Object[] annotatie_algemeen;
    public String[] annotatie_bibl_ref;
    public String[] annotatie_bibliografie;
    public Object[] annotatie_chron_rel;
    public Object[] annotatie_datering;
    public String[] annotatie_exemplaar;
    public String[] annotatie_exemplaar_alg;
    public Object[] annotatie_hor_rel;
    public String[] annotatie_incipit;
    public String[] annotatie_inhoud;
    public String[] annotatie_materiaal;
    public String[] annotatie_overig;
    public Object[] annotatie_rel__gr_kl;
    public String[] auteur;
    public String[] bandinformatie;
    public String[] basiscode_goo;
    public Object[] bezitter;
    public String[] binder;
    public String[] bnb_nr;
    public Object[] boodschap_alg;
    public String[] brinkman_nr;
    public String[] categorie;
    public String[] ccp_nr;
    public Object[] clc;
    public String[] clc_nr;
    public String[] coden;
    public String[] congres;
    public String[] ctc_nr;
    public String[] dewey;
    public String[] data_van_uitgave;
    public String[] datering_gecodeerd;
    public String[] datum_exempl;
    public String[] db_nr;
    public String[] deel_titel;
    public String[] deelvermelding;
    public String[] drukker;
    public Object[] drukker_ingang;
    public Object[] editieveld;
    public String[] epn;
    public String[] frequentie;
    public String[] frequentie_code;
    public String[] fysieke_beschrijving;
    public String[] herkomst;
    public String[] illustrator;
    public Object[] institute;
    public Object[] isbn;
    public String[] isbn_fout;
    public Object[] isbn_volgend;
    public String[] issn;
    public String[] issn_fout;
    public String[] jaar_van_uitgave;
    public Object[] jaargang;
    public String[] kill_verzoek;
    public String[] kopiist;
    public String[] landcode;
    public String[] landcode_goo;
    public String[] lc_nr;
    public String[] leverancier_prod_nr;
    public Object[] lokaal_trefwoord;
    public String[] nlm;
    public String[] nur;
    public String[] onbekende_code;
    public String[] onderwerp;
    public String[] opm_bibl_lokaal;
    public String[] other_title;
    public String[] part_of;
    public String[] periode_goo;
    public String[] ppn_link_papier_url;
    public String[] sectie_titel;
    public String[] selectie_sleutel;
    public String[] signalementcode;
    public String[] siso;
    public String[] siso_oud;
    public String[] soortcode;
    public String[] sorteerveld;
    public String[] swets_nr;
    public String[] taalcatalogisering;
    public Object[] taalcode;
    public String[] titel;
    public String[] titel_genormaliseerd;
    public String[] titel_md_publ_ppn;
    public String[] titel_reeks;
    public String[] titel_reeks_md_publ;
    public String[] titel_reeks_ppn;
    public Object[] trefwoord;
    public String[] trefwoord_extra;
    public String[] trefwoord_oc_artikelen;
    public String[] trefwoord_precis;
    public String[] tussen_titel;
    public String[] type_drager;
    public String[] type_inhoud;
    public String[] type_medium;
    public String[] udc;
    public String[] uitgever;
    public String[] uitgever_volgend;
    public String[] unesco_rubr;
    public String[] uniforme_titel;
    public String[] verw_onderdeel_ppn;

    @Override
    public String toString() {
      return String.format("%-5s %-10s %-40s %-30s %s %s", _id, type, onderwerp, regionaam, landcode, titel);
    }
  }

  // -------------------------------------------------------------------

  public static class XLexicon {
    /** ### Assigned id (admin) */
    public String _id;
    // public String ingang;
    public String ppn;
    public String soort;
    public String type;
    public String url;

    public String[] adresgegevens;
    public String[] beroeps_aanduiding;
    public String[] beroep_vakgebied;
    public String[] bredere_term;
    public String[] indicatoren;
    public String[] ingang;
    public Object[] institute_bredere_term;
    public String[] instituut;
    public String[] landcode;
    public String[] meisjesnaam;
    public String[] naam_echtgenoot;
    public String[] naams_variant;
    public String[] naamsvariant;
    public String[] normaliserende_ingang;
    public Object[] relaties_overig;
    public String[] sec_ingang;
    public String[] sorteerveld;
    public String[] standaard_naam;
    public String[] taalcode;
    public Object[] toelichtingen;
    public String[] toepassing;
    public String[] verwante_term;
    public String[] verwijzing_eigen;
    public Object[] verwijzing_institute;
    public Object[] verwijzing_pseudoniem;
    public String[] volledige_naam;
    public String[] years;
  }

  // -------------------------------------------------------------------

  public static class XTekst {
    /** ### Assigned id (admin) */
    public String _id;
    // public String bewaarplaats_en_signatuur;
    public String overige_hs_aanduiding;
    public String ppn;
    public String soort;
    public String type;

    public String[] annotatie_colofon;
    public String[] annotatie_datering;
    public Object[] annotatie_documentatie;
    public String[] annotatie_incipit;
    public String[] annotatie_inhoud;
    public String[] annotatie_materiaal;
    public String[] annotatie_overig;
    public String[] annotatie_rel__gr_kl;
    public String[] annotatie_schrift;
    public Object[] auteur;
    public Object bewaarplaats_en_signatuur;
    public Object[] bezitter;
    public Object[] binder;
    public String[] boodschap_alg;
    public Object[] categorie;
    public Object[] corrector;
    public Object datering;
    public String[] datum_exempl;
    public Object[] decorator;
    public String[] dewey;
    public String[] epn;
    public Object[] illustrator;
    public Object[] ingang;
    public String[] journaalnummer;
    public Object[] kopiist;
    public String[] landcode;
    public String[] layout_code;
    public Object[] lokalisering;
    public String[] onbekende_code;
    public String[] onderwerp;
    public Object[] opdrachtgever;
    public String[] part_of;
    public String[] regiocode;
    public String[] selectie_sleutel;
    public String[] signalementcode;
    public String[] siso_oud;
    public String[] sleutelw_incipit;
    public String[] soortcode;
    public String sortcode;
    public String[] sorteerveld;
    public Object[] taalcode;
    public String[] titel;
    public Object[] titel_genormaliseerd;
    public String[] titel_hs_inc_form;
    public Object[] trefwoord;
    public String[] url;
    public String[] watermerk;
  }

  // -------------------------------------------------------------------

  public static class XTaal {
    /** ### Assigned id (admin) */
    public String _id;
    public String taal;
  }

  // -------------------------------------------------------------------

  public static class XPeriode {
    /** ### Assigned id (admin) */
    public String _id;
    public String periode;
  }

  // -------------------------------------------------------------------

  public static class XRegiocode {
    /** ### Assigned id (admin) */
    public String _id;
    public String regio;
  }

  // -------------------------------------------------------------------

  public static class XSignalementcode {
    /** ### Assigned id (admin) */
    public String _id;
    public String signalement;
  }

  // -------------------------------------------------------------------

  public static class XWatermerk {
    /** ### Assigned id (admin) */
    public String _id;
    public String watermerk;
  }

  // -------------------------------------------------------------------

  public static class XTekstdrager {
    /** ### Assigned id (admin) */
    public String _id;
    // public String bewaarplaats_en_signatuur;
    public String overige_hs_aanduiding;
    public String ppn;
    public String soort;
    public String type;

    public String[] annotatie_colofon;
    public String[] annotatie_datering;
    public Object[] annotatie_documentatie;
    public String[] annotatie_incipit;
    public String[] annotatie_inhoud;
    public String[] annotatie_materiaal;
    public String[] annotatie_overig;
    public String[] annotatie_rel__gr_kl;
    public String[] annotatie_schrift;
    public Object[] auteur;
    public Object bewaarplaats_en_signatuur;
    public Object[] bezitter;
    public Object[] binder;
    public String[] boodschap_alg;
    public Object[] categorie;
    public Object[] corrector;
    public Object datering;
    public String[] datum_exempl;
    public Object[] decorator;
    public String[] epn;
    public Object[] illustrator;
    public Object[] ingang;
    public String[] journaalnummer;
    public Object[] kopiist;
    public String[] landcode;
    public String[] layout_code;
    public Object[] lokalisering;
    public String[] onbekende_code;
    public String[] onderwerp;
    public Object[] opdrachtgever;
    public String[] regiocode;
    public String[] selectie_sleutel;
    public String[] signalementcode;
    public String[] siso_oud;
    public String[] sleutelw_incipit;
    public String[] soortcode;
    public String[] sorteerveld;
    public Object[] taalcode;
    public Object[] titel_genormaliseerd;
    public String[] titel_hs_inc_form;
    public Object[] trefwoord;
    public String[] url;
    public String[] watermerk;
  }

}
