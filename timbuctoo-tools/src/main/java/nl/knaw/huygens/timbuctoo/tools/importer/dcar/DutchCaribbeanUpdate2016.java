package nl.knaw.huygens.timbuctoo.tools.importer.dcar;

import com.google.inject.Injector;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.model.dcar.DCARArchive;
import nl.knaw.huygens.timbuctoo.model.dcar.DCARArchiver;
import nl.knaw.huygens.timbuctoo.model.dcar.DCARRelation;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;

import java.util.function.Consumer;

public class DutchCaribbeanUpdate2016 {

  public static final Change CHANGE = new Change("INGFORMS_UPDATE_2016", "DutchCaribbean");

  //for line in `grep '^/' differences.diff`; do neo4j-shell -c 'match node where node.dcararchive_origFilename = "'$line'" or node.dcararchiver_origFilename = "'$line'" return node.dcararchiver_origFilename,node.dcararchive_origFilename, node.tim_id  limit 100;'; done > ids.txt
  //cat ids.txt | grep '<null>' | sed 's/| <null> *| //' | sed 's/^| //' | sed 's/ \| //' | sed 's/ \| *$//' | sed 's|[/-]|_|g' | sed 's/"_data_data_Atlantische_wereld_Archie[^_]*_\([^"]*\)" *"\([^"]*\)"$/String \1 = "\2";/'

  //results in:
  static String Organisaties_Algemeen_Secretaris_Nederlandse_West_Indische_Bezittingen = "af461064-cd54-4af8-b51b-648932686f49";
  static String Organisaties_Evangelisch_lutherse_gemeente_Amsterdam = "b96d4275-336f-4d64-b64e-3b38d1d9fdf6";
  static String Organisaties_Notarisambt_Demerary_en_Essquebo = "72d1c45e-31c2-41e6-94e8-f5f89ea258d3";
  static String Organisaties_Notarisambt_St_Maarten = "d888d9e1-7490-4a21-adec-c2bef5a48a46";
  static String Organisaties_Notarisambt_Suriname = "1e95cb93-172d-4a8b-91d9-31ecf5ede57a";
  static String Organisaties_Portugees_Israelitische_gemeente_Amsterdam = "c6f1640c-9e32-45da-8631-e48f57bcf256";
  static String Organisaties_Portugees_Israelitische_gemeente_Curacao = "62bbe19a-8e0c-4899-8f1c-2d720ffdaef0";
  static String Organisaties_Portugees_Israelitische_gemeente_Suriname = "d84c9d8b-5959-41d7-b7c2-99b5fd58fa8e";
  static String Organisaties_Raad_Berbice = "81a42c47-b336-4416-9a60-6e19cd75d8aa";
  static String Organisaties_Weeskamer_Berbice = "47229e08-70bc-4470-9821-b116e77e6b64";
  static String Organisaties_Weeskamer_Curacao = "1baa172c-0c03-4d0a-80eb-acde8b3afd4d";
  static String Organisaties_Weeskamer_Demerary_en_Essequebo = "c43b931c-4eff-4ab9-ae1f-5e96f35017f9";
  static String Organisaties_Weeskamer_St_Eustatius = "21638d46-e653-4ecc-a6c5-89c4452c154c";
  static String Organisaties_Weeskamer_Suriname = "5a75485b-7151-4ef1-88fa-ec093dd7935e";

  static String Archief_Franciscanessen_van_Mariadal_Roosendaal_Franciscanessen_van_Mariadal_Roosendaal = "45c503c9-abdb-43f5-bf5e-22480009e601";
  static String Het_Utrechts_Archief_16_Aartspriesters_van_de_Hollandse_Zending_gedeponeerde_archieven_HUA_16_09_3_Mgr_CL_baron_van_Wijkerslooth = "a8287f3f-8e91-4c54-86c5-174141c73c47";
  static String Nationaal_Archief_1_05_03_Societeit_van_Suriname_HaNA_1_05_03_514 = "206d2737-b446-4156-9798-1c45b83174d3";
  static String Nationaal_Archief_1_05_11_18_Suriname_Nederlands_Portugees_Israelitische_Gemeente_HaNA_1_05_11_18_IV_489 = "0a1ffd8a-e49b-44b0-944c-c68869a2f87c";
  static String National_Archives_Guyana_PROTOCOLS_NOTARIES = "2d9b4701-6117-4a39-b1c0-56fbff83a9f9";
  static String SAL_Mongui_Maduro_Bibliotheek_Curacao_Gemeente_Mikve_Israel_Gemeente_Mikve_Israel = "f5c2b1ab-dc3a-4d2e-955d-02af73ca72f5";
  static String SAL_Mongui_Maduro_Bibliotheek_Curacao_Gemeente_Mikve_Israel_Mikve_Israel_99_101_113_114_143_155 = "a7008f73-d466-4aea-8492-08381c175ad2";
  static String Stadsarchief_Amsterdam_1455_Bank_Insinger_en_Co_1455_Bank_Insinger_en_Co_1446_1448 = "66f2f1e5-036d-4a0f-8151-18f10b3d93b7";
  static String The_National_Archives_London_Colonial_Office_CO_CORRESPONDENCE_WITH_THE_COLONIES_CO_278 = "ee2c01cd-5011-43f2-9ede-4de536e8b8a4";
  static String The_National_Archives_London_Colonial_Office_CO_CORRESPONDENCE_WITH_THE_COLONIES_CO_318_32 = "d09da56b-3e19-4c97-9627-95b2359f5199";
  static String The_National_Archives_London_Colonial_Office_CO_RECORDS_OF_LOCAL_BODIES_AND_MISCELLANEA_CO_116_068_069 = "ce04d546-3cb7-4de4-85bc-ed5d46ce0aff";
  static String The_National_Archives_London_Colonial_Office_CO_RECORDS_OF_LOCAL_BODIES_AND_MISCELLANEA_CO_116_118_127 = "0f1148f1-0ae0-4534-ac3f-0f9dfeb171af";
  static String The_National_Archives_London_Treasury_T_1_3481_3484 = "abed123a-5ac1-4863-a2c7-b9c6699b269a";
  static String Het_Utrechts_Archief_16_Aartspriesters_van_de_Hollandse_Zending_gedeponeerde_archieven_HUA_16_00_Aartspriesters_van_de_Hollandse_zending_gedeponeerde_archieven = "02aa075d-469b-4114-bb35-33e3992a5979";
  static String Nationaal_Archief_1_05_03_Societeit_van_Suriname_HaNA_1_05_03 = "834eeb9e-918a-42e9-8a85-5963bf0ed7e9";
  static String National_Archives_Guyana_MISCELLANEOUS_BOOKS = "5ec16bbc-4699-41ed-87c4-1c73225d66eb";

  static String Organisaties_Rooms_Katholieke_Kerk = "23ff1fbb-13c7-492d-9a29-021e70bb01cf";
  static String hasPlace = "cc1b1e90-c7ad-4aec-9512-56629e5a1287";
  static String hasArchivePlace = "acb9ae4a-3ec3-48d7-acbf-5c649c4f039a";
  static String hasParentArchive = "8a76add3-e9b3-4ab4-84ba-0981159399c8";
  static String isCreatorOf = "a69b5063-9b8b-46c0-b435-a6b05b3895d7";
  static String curacao = "f8361a28-3254-4e4d-a97e-daebc4527f57";
  static String st_eustatius = "39f262ab-ed8e-4637-aaac-0a6a1cea4c99";
  static String demarara = "70c416b2-3147-441f-b748-d88546f70942";
  static String guyana_netherlands = "9ba70696-3cf3-403d-84fb-7fefbdc1569e";
  static String antilles_netherlands = "101d29c3-6e1f-41ed-b670-a11caf5d31a2";


  public static void main(String[] args) throws Exception {
    Repository repository = null;
    IndexManager indexManager = null;
    try {
      Injector injector = ToolsInjectionModule.createInjector();
      repository = injector.getInstance(Repository.class);
      indexManager = injector.getInstance(IndexManager.class);

      updateItem(DCARArchiver.class, repository, indexManager, Organisaties_Algemeen_Secretaris_Nederlandse_West_Indische_Bezittingen, archiver -> {
        archiver.setNameNld("Algemeen Secretaris van de Nederlandse West-Indische Bezittingen ");
        archiver.setNameEng("General secretary of the Dutch West Indian Possessions ");
        archiver.setHistory("<p>The General Secretary of the Dutch West Indian possessions rendered administrative support to the Governor-General. He was at the same time Government secretary of Suriname.</p>");
      });
      updateItem(DCARArchiver.class, repository, indexManager, Organisaties_Evangelisch_lutherse_gemeente_Amsterdam, archiver -> {
        archiver.setNameEng("Evangelical-Lutheran congregation of Amsterdam");
      });
      updateItem(DCARArchiver.class, repository, indexManager, Organisaties_Notarisambt_Demerary_en_Essquebo, archiver -> {
        archiver.setNameEng("Notarial office in Demerara and Essequibo");
        archiver.setHistory("<p>There was no public notary in Essequibo or Demerara. Therefore, the secretaries of these settlements exercised the work of a notary and took the instruments.</p><p>&#160;</p>");
      });
      updateItem(DCARArchiver.class, repository, indexManager, Organisaties_Notarisambt_St_Maarten, archiver -> {
        archiver.setNameNld("Notarisambt op St. Maarten");
        archiver.setHistory("<p>So-called secretarial deeds, like mortgage deeds, deeds of conveyance of real estate, ships and slaves, and deeds of manumission of slaves were passed before the Commander or two Councillors. Notarial deeds like last wills, probate inventories, contracts, donations, marriage contracts, and procurations were drawn up by the secretary.</p>");
      });
      updateItem(DCARArchiver.class, repository, indexManager, Organisaties_Notarisambt_Suriname, archiver -> {
        archiver.setNameEng("Notarial office in Suriname");
        archiver.setHistory("<p>There was no notary public as a separate function in Suriname.</p><p>The government secretary took instruments to an amount of 1000 pounds of sugar. He was obliged to register deeds. Sworn clerks took over notary work from the secretaries more and more in the 18th century. From 1707 instruments of conveyance and mortgage had to be executed before two members of the Council of Policy and during the years 1828-1832 before the Municipal Council of Suriname. From the late 17th century jurators or sworn persons were appointed for the Jewish people in a certain division; from 1754 on they filled this function for the group of Portuguese Jews who were rather unfamiliar with the Dutch language. In 1828 the notorial practice was entirely delegated to six sworn clerks; five were working in Paramaribo and one at Nickerie.</p><p>In May 1869 a new regulation on notaries came into being.</p>");
      });
      updateItem(DCARArchiver.class, repository, indexManager, Organisaties_Portugees_Israelitische_gemeente_Amsterdam, archiver -> {
        archiver.setNameNld("Portugees-Israëlitische Gemeente van Amsterdam");
        archiver.setNameEng("Portuguese-Israelite congregation of Amsterdam");
      });
      updateItem(DCARArchiver.class, repository, indexManager, Organisaties_Portugees_Israelitische_gemeente_Curacao, archiver -> {
        archiver.setNameNld("Portugees-Isra&#235;litische gemeente van Cura&#231;ao");
      });
      updateItem(DCARArchiver.class, repository, indexManager, Organisaties_Portugees_Israelitische_gemeente_Suriname, archiver -> {
        archiver.setNameNld("Portugees-Israëlitische gemeente van Suriname");
        archiver.setNameEng("Portuguese-Israelite congregation of Suriname");
        archiver.setHistory("<p>The Dutch Portuguese Jewish congregation in Suriname was established in 1661/1662. It followed the habits and customs of the congregation of Amsterdam. The congregation in Suriname called itself \"Bechara ve Shalom\" (B.V.S., Blessing and Peace). A new prayer house was built in Paramaribo in the mid-18th century. It was called \"Sedek ve Shalom\" (S.V.S., Justice and Peace). The <em>Mahamad </em>acted as the church executive board and, until 1825, as a council for minor cases.</p>");
      });
      updateItem(DCARArchiver.class, repository, indexManager, Organisaties_Raad_Berbice, archiver -> {
        archiver.setNameNld("Raden / Hoven van politie en van justitie / fiscaal van Berbice");
        archiver.setNameEng("Councillors / Councils of Policy and Justice / fiscal of Berbice");
        archiver.setHistory("<p>The Governor presided over a political council or Council of Policy. This council also administered criminal justice. Besides the Governor (as president) the council had six members (councillors).</p><p>In addition, there was the Council of Civil Justice, consisting of the Governor in his capacity as president and six members (Councillors).</p><p>Almost all members of these councils were chosen from among the settlers: administrators or owners of plantations. The interests of the Governors and Councillors often did not match. A fiscal assisted; he was also secretary.</p>");
      });
      updateItem(DCARArchiver.class, repository, indexManager, Organisaties_Weeskamer_Berbice, archiver -> {
        archiver.setNameNld("Wees- en onbeheerde Boedelkamer van Berbice");
      });
      updateItem(DCARArchiver.class, repository, indexManager, Organisaties_Weeskamer_Curacao, archiver -> {
        archiver.setNameNld("Wees- en onbeheerde Boedelkamer van Cura&#231;ao");
        archiver.setNameEng("Orphans and Ownerless Estates Chamber of Cura&#231;ao");
        archiver.setHistory("<p>The Orphans Chamber of Cura&#231;ao was probably established in 1696. During the 18th century there were usually two orphan masters, sometimes assisted by a clerk and/or a bookkeeper. The orphan masters were entrusted with the supervision of minors and the direction of their estates, and the administration of left or abandoned estates, when it was not clear if there were any heirs. The Orphans Chamber also lent money on mortgage. The introduction of a new body of laws made many of the tasks of the Orphans Chamber regarding orphans and left estates redundant, except its function regarding mortgages. This was taken over by the Cura&#231;aose Hypotheek-, Spaar- en Beleenbank (Cura&#231;ao Mortgage Bank). In 1875 the Orphans Chamber was officially dissolved.</p>");
      });
      updateItem(DCARArchiver.class, repository, indexManager, Organisaties_Weeskamer_St_Eustatius, archiver -> {
        archiver.setNameNld("Wees- en Onbeheerde Boedelkamer van St. Eustatius");
        archiver.setHistory("<p>After the second British interregnum and the restoration of Dutch rule in 1816, an Orphans and Ownerless Estates Chamber was established. This body consisted of the bookkeeper general and two of the most capable citizens. The secretary of the Council of Policy supported the Chamber administratively. Formerly, the liquidations of <em>ab intestato</em> deceased persons and the control of estates inherited by minors was done by the Governor and the Councillors. With the administrative changes in 1828, the Orphans Chamber remained functioning. The second alderman of the municipal government replaced the bookkeeper general.</p>");
      });
      updateItem(DCARArchiver.class, repository, indexManager, Organisaties_Weeskamer_Suriname, archiver -> {
        archiver.setNameNld("Wees- en Onbeheerde Boedelkamer van Suriname");
        archiver.setHistory("<p>The Orphans and Ownerless Estates Chamber regulated the administration of insolvent and other estates. Orphan masters were entrusted with the supervision of minors. They were obliged to keep a regular administration. In the 18th and 19th centuries there were some changes in the organization and/or powers. Individual denominations had their own Orphans Chambers until the 19th century.</p><p>From 1835 to 1876 the \"<em>Pupillaire Raad</em>\" had the supervision over all minors.</p><p>&#160;</p>");
      });
      updateItem(DCARArchive.class, repository, indexManager, Archief_Franciscanessen_van_Mariadal_Roosendaal_Franciscanessen_van_Mariadal_Roosendaal, archiver -> {
        archiver.setNotes("<p>The photo archive is in the Erfgoedcentrum Nederlands Kloosterleven - St Agatha.</p>");
      });
      updateItem(DCARArchive.class, repository, indexManager, Nationaal_Archief_1_05_11_18_Suriname_Nederlands_Portugees_Israelitische_Gemeente_HaNA_1_05_11_18_IV_489, archiver -> {
        archiver.setTitleEng("Documents from I. Bueno de Mesquita, member and correspondent of the Main committee for the Affairs of Israelites for the Dutch Portuguese-Israelite congregation in Suriname");
      });
      updateItem(DCARArchive.class, repository, indexManager, National_Archives_Guyana_PROTOCOLS_NOTARIES, archiver -> {
        archiver.setNotes("<p>The whole series up to 1858 consists of 127 volumes. Last approximately 30 volumes, from about 1814 onwards, mostly written in English.</p><p>Berbice 1762-1858.</p>");
      });
      updateItem(DCARArchive.class, repository, indexManager, SAL_Mongui_Maduro_Bibliotheek_Curacao_Gemeente_Mikve_Israel_Gemeente_Mikve_Israel, archiver -> {
        archiver.setTitleNld("Archief van de Nederlands Portugees-Isra&#235;litische Gemeente Mikv&#233; Isra&#235;l te Cura&#231;ao, (1652) 1711 - 1963");
      });
      updateItem(DCARArchive.class, repository, indexManager, SAL_Mongui_Maduro_Bibliotheek_Curacao_Gemeente_Mikve_Israel_Mikve_Israel_99_101_113_114_143_155, archiver -> {
        archiver.setTitleNld("Financiën");
      });
      updateItem(DCARArchive.class, repository, indexManager, Stadsarchief_Amsterdam_1455_Bank_Insinger_en_Co_1455_Bank_Insinger_en_Co_1446_1448, archiver -> {
        archiver.setTitleNld("Stukken betreffende het beheer van de plantages Resolutie, Zeewijk en Stolkwijk, deel uitmakend van de betwiste boedels van A. de Meij enerzijds en W. Wichers anderzijds");
      });
      updateItem(DCARArchive.class, repository, indexManager, The_National_Archives_London_Colonial_Office_CO_CORRESPONDENCE_WITH_THE_COLONIES_CO_278, archiver -> {
        archiver.setTitleEng("Colonial Office and predecessors: Suriname original correspondence, etc.");
      });
      updateItem(DCARArchive.class, repository, indexManager, The_National_Archives_London_Colonial_Office_CO_CORRESPONDENCE_WITH_THE_COLONIES_CO_318_32, archiver -> {
        archiver.setTitleEng("Report of the Board of Health on West Indian stations (includes Suriname)");
      });
      updateItem(DCARArchive.class, repository, indexManager, The_National_Archives_London_Colonial_Office_CO_RECORDS_OF_LOCAL_BODIES_AND_MISCELLANEA_CO_116_068_069, archiver -> {
        archiver.setTitleEng("Ordinances, citations, etc. regarding Berbice");
      });
      updateItem(DCARArchive.class, repository, indexManager, The_National_Archives_London_Colonial_Office_CO_RECORDS_OF_LOCAL_BODIES_AND_MISCELLANEA_CO_116_118_127, archiver -> {
        archiver.setTitleEng("Statistics of soldiers, slaves, buildings, etc.");
      });
      updateItem(DCARArchive.class, repository, indexManager, The_National_Archives_London_Treasury_T_1_3481_3484, archiver -> {
        archiver.setTitleNld("Berbice: Winkel [= Shop] Department (slavery)");
      });

      addRelation(
        repository,
        indexManager,
        Het_Utrechts_Archief_16_Aartspriesters_van_de_Hollandse_Zending_gedeponeerde_archieven_HUA_16_09_3_Mgr_CL_baron_van_Wijkerslooth,
        Het_Utrechts_Archief_16_Aartspriesters_van_de_Hollandse_Zending_gedeponeerde_archieven_HUA_16_00_Aartspriesters_van_de_Hollandse_zending_gedeponeerde_archieven,
        hasParentArchive,
        "archive",
        "archive"
      );
      deleteRelation(
        repository,
        indexManager,
        Nationaal_Archief_1_05_03_Societeit_van_Suriname_HaNA_1_05_03,
        Nationaal_Archief_1_05_03_Societeit_van_Suriname_HaNA_1_05_03_514,
        hasParentArchive
      );
      addRelation(
        repository,
        indexManager,
        Nationaal_Archief_1_05_03_Societeit_van_Suriname_HaNA_1_05_03_514,
        Nationaal_Archief_1_05_03_Societeit_van_Suriname_HaNA_1_05_03,
        hasParentArchive,
        "archive",
        "archive"
      );
      addRelation(
        repository,
        indexManager,
        Organisaties_Weeskamer_Demerary_en_Essequebo,
        National_Archives_Guyana_MISCELLANEOUS_BOOKS,
        isCreatorOf,
        "archiver",
        "archive"
      );
      addRelation(
        repository,
        indexManager,
        Organisaties_Rooms_Katholieke_Kerk,
        antilles_netherlands,
        hasPlace,
        "archiver",
        "keyword"
      );

      deleteRelation(
        repository,
        indexManager,
        Organisaties_Rooms_Katholieke_Kerk,
        curacao,
        hasPlace
      );
      deleteRelation(
        repository,
        indexManager,
        Organisaties_Rooms_Katholieke_Kerk,
        st_eustatius,
        hasPlace
      );

      addRelation(
        repository,
        indexManager,
        Organisaties_Rooms_Katholieke_Kerk,
        demarara,
        hasPlace,
        "archiver",
        "keyword"
      );
      addRelation(
        repository,
        indexManager,
        National_Archives_Guyana_PROTOCOLS_NOTARIES,
        guyana_netherlands,
        hasArchivePlace,
        "archive",
        "keyword"
      );
    }
    finally {
      if (repository != null) {
        repository.close();
      }
      if (indexManager != null) {
        indexManager.close();
      }
    }

  }

  private static <T extends DomainEntity> void updateItem(Class<T> clazz, Repository repository, IndexManager indexManager, String id, Consumer<T> updateAction) throws nl.knaw.huygens.timbuctoo.storage.StorageException, nl.knaw.huygens.timbuctoo.index.IndexException {
    T archiver = repository.getEntityOrDefaultVariation(clazz, id);
    updateAction.accept(archiver);
    repository.updateDomainEntity(clazz, archiver, CHANGE);
    indexManager.updateEntity(clazz, archiver.getId());
  }

  private static void addRelation(Repository repository, IndexManager indexManager, String sourceId, String targetId, String typeId, String sourceType, String targetType) throws nl.knaw.huygens.timbuctoo.storage.StorageException, nl.knaw.huygens.timbuctoo.index.IndexException, ValidationException {
    DCARRelation dcarRelation = new DCARRelation();
    dcarRelation.setSourceId(sourceId);
    dcarRelation.setTargetId(targetId);
    dcarRelation.setSourceType(sourceType);
    dcarRelation.setTargetType(targetType);
    dcarRelation.setTypeId(typeId);
    dcarRelation.setTypeType("relationtype");

    repository.addDomainEntity(DCARRelation.class, dcarRelation, CHANGE);
    indexManager.addEntity(DCARRelation.class, dcarRelation.getId());
  }

  private static void deleteRelation(Repository repository, IndexManager indexManager, String sourceId, String targetId, String typeId) throws nl.knaw.huygens.timbuctoo.storage.StorageException, nl.knaw.huygens.timbuctoo.index.IndexException {
    Relation relation = repository.findRelations(sourceId, targetId, typeId).next();
    updateItem(DCARRelation.class, repository, indexManager, relation.getId(), rel -> rel.setAccepted(false));
  }


}