package nl.knaw.huygens.timbuctoo.model.ebnm;

import nl.knaw.huygens.timbuctoo.model.Documentatie;

public class EBNMDocumentatie extends Documentatie {

  private String codeId;
  private String cite_as;
  private String ppn;
  private String soort;
  private String type;
  private String regionaam;

  private Object[] annotatie_algemeen;
  private String[] annotatie_bibl_ref;
  private String[] annotatie_bibliografie;
  private Object[] annotatie_chron_rel;
  private Object[] annotatie_datering;
  private String[] annotatie_exemplaar;
  private String[] annotatie_exemplaar_alg;
  private Object[] annotatie_hor_rel;
  private String[] annotatie_incipit;
  private String[] annotatie_inhoud;
  private String[] annotatie_materiaal;
  private String[] annotatie_overig;
  private Object[] annotatie_rel__gr_kl;
  private String[] auteur;
  private String[] bandinformatie;
  private String[] basiscode_goo;
  private Object[] bezitter;
  private String[] binder;
  private String[] bnb_nr;
  private Object[] boodschap_alg;
  private String[] brinkman_nr;
  private String[] categorie;
  private String[] ccp_nr;
  private Object[] clc;
  private String[] clc_nr;
  private String[] coden;
  private String[] congres;
  private String[] ctc_nr;
  private String[] dewey;
  private String[] data_van_uitgave;
  private String[] datering_gecodeerd;
  private String[] datum_exempl;
  private String[] db_nr;
  private String[] deel_titel;
  private String[] deelvermelding;
  private String[] drukker;
  private Object[] drukker_ingang;
  private Object[] editieveld;
  private String[] epn;
  private String[] frequentie;
  private String[] frequentie_code;
  private String[] fysieke_beschrijving;
  private String[] herkomst;
  private String[] illustrator;
  private Object[] institute;
  private Object[] isbn;
  private String[] isbn_fout;
  private Object[] isbn_volgend;
  private String[] issn;
  private String[] issn_fout;
  private String[] jaar_van_uitgave;
  private Object[] jaargang;
  private String[] kill_verzoek;
  private String[] kopiist;
  private String[] landcode;
  private String[] landcode_goo;
  private String[] lc_nr;
  private String[] leverancier_prod_nr;
  private Object[] lokaal_trefwoord;
  private String[] nlm;
  private String[] nur;
  private String[] onbekende_code;
  private String[] onderwerp;
  private String[] opm_bibl_lokaal;
  private String[] other_title;
  private String[] part_of;
  private String[] periode_goo;
  private String[] ppn_link_papier_url;
  private String[] sectie_titel;
  private String[] selectie_sleutel;
  private String[] signalementcode;
  private String[] siso;
  private String[] siso_oud;
  private String[] soortcode;
  private String[] sorteerveld;
  private String[] swets_nr;
  private String[] taalcatalogisering;
  private Object[] taalcode;
  private String[] titel;
  private String[] titel_genormaliseerd;
  private String[] titel_md_publ_ppn;
  private String[] titel_reeks;
  private String[] titel_reeks_md_publ;
  private String[] titel_reeks_ppn;
  private Object[] trefwoord;
  private String[] trefwoord_extra;
  private String[] trefwoord_oc_artikelen;
  private String[] trefwoord_precis;
  private String[] tussen_titel;
  private String[] type_drager;
  private String[] type_inhoud;
  private String[] type_medium;
  private String[] udc;
  private String[] uitgever;
  private String[] uitgever_volgend;
  private String[] unesco_rubr;
  private String[] uniforme_titel;
  private String[] verw_onderdeel_ppn;

  public String getLabel() {
    return getValue();
  }

  public void setLabel(String string) {
    setValue(string);
  }

  public void setCodeId(String _id) {
    this.codeId = _id;
  }

  public String getCodeId() {
    return codeId;
  }

  public void setCite_as(String cite_as) {
    this.cite_as = cite_as;
  }

  public void setPpn(String ppn) {
    this.ppn = ppn;
  }

  public void setSoort(String soort) {
    this.soort = soort;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setRegionaam(String regionaam) {
    this.regionaam = regionaam;
  }

  public void setAnnotatie_algemeen(Object[] annotatie_algemeen) {
    this.annotatie_algemeen = annotatie_algemeen;
  }

  public void setAnnotatie_bibl_ref(String[] annotatie_bibl_ref) {
    this.annotatie_bibl_ref = annotatie_bibl_ref;
  }

  public void setAnnotatie_bibliografie(String[] annotatie_bibliografie) {
    this.annotatie_bibliografie = annotatie_bibliografie;
  }

  public void setAnnotatie_chron_rel(Object[] annotatie_chron_rel) {
    this.annotatie_chron_rel = annotatie_chron_rel;
  }

  public void setAnnotatie_datering(Object[] annotatie_datering) {
    this.annotatie_datering = annotatie_datering;
  }

  public void setAnnotatie_exemplaar(String[] annotatie_exemplaar) {
    this.annotatie_exemplaar = annotatie_exemplaar;
  }

  public void setAnnotatie_exemplaar_alg(String[] annotatie_exemplaar_alg) {
    this.annotatie_exemplaar_alg = annotatie_exemplaar_alg;
  }

  public void setAnnotatie_hor_rel(Object[] annotatie_hor_rel) {
    this.annotatie_hor_rel = annotatie_hor_rel;
  }

  public void setAnnotatie_incipit(String[] annotatie_incipit) {
    this.annotatie_incipit = annotatie_incipit;
  }

  public void setAnnotatie_inhoud(String[] annotatie_inhoud) {
    this.annotatie_inhoud = annotatie_inhoud;
  }

  public void setAnnotatie_materiaal(String[] annotatie_materiaal) {
    this.annotatie_materiaal = annotatie_materiaal;
  }

  public void setAnnotatie_overig(String[] annotatie_overig) {
    this.annotatie_overig = annotatie_overig;
  }

  public void setAnnotatie_rel__gr_kl(Object[] annotatie_rel__gr_kl) {
    this.annotatie_rel__gr_kl = annotatie_rel__gr_kl;
  }

  public void setAuteur(String[] auteur) {
    this.auteur = auteur;
  }

  public void setBandinformatie(String[] bandinformatie) {
    this.bandinformatie = bandinformatie;
  }

  public void setBasiscode_goo(String[] basiscode_goo) {
    this.basiscode_goo = basiscode_goo;
  }

  public void setBezitter(Object[] bezitter) {
    this.bezitter = bezitter;
  }

  public void setBinder(String[] binder) {
    this.binder = binder;
  }

  public void setBnb_nr(String[] bnb_nr) {
    this.bnb_nr = bnb_nr;
  }

  public void setBoodschap_alg(Object[] boodschap_alg) {
    this.boodschap_alg = boodschap_alg;
  }

  public void setBrinkman_nr(String[] brinkman_nr) {
    this.brinkman_nr = brinkman_nr;
  }

  public void setCategorie(String[] categorie) {
    this.categorie = categorie;
  }

  public void setCcp_nr(String[] ccp_nr) {
    this.ccp_nr = ccp_nr;
  }

  public void setClc(Object[] clc) {
    this.clc = clc;
  }

  public void setClc_nr(String[] clc_nr) {
    this.clc_nr = clc_nr;
  }

  public void setCoden(String[] coden) {
    this.coden = coden;
  }

  public void setCongres(String[] congres) {
    this.congres = congres;
  }

  public void setCtc_nr(String[] ctc_nr) {
    this.ctc_nr = ctc_nr;
  }

  public void setDewey(String[] dewey) {
    this.dewey = dewey;
  }

  public void setData_van_uitgave(String[] data_van_uitgave) {
    this.data_van_uitgave = data_van_uitgave;
  }

  public void setDatering_gecodeerd(String[] datering_gecodeerd) {
    this.datering_gecodeerd = datering_gecodeerd;
  }

  public void setDatum_exempl(String[] datum_exempl) {
    this.datum_exempl = datum_exempl;
  }

  public void setDb_nr(String[] db_nr) {
    this.db_nr = db_nr;
  }

  public void setDeel_titel(String[] deel_titel) {
    this.deel_titel = deel_titel;
  }

  public void setDeelvermelding(String[] deelvermelding) {
    this.deelvermelding = deelvermelding;
  }

  public void setDrukker(String[] drukker) {
    this.drukker = drukker;
  }

  public void setDrukker_ingang(Object[] drukker_ingang) {
    this.drukker_ingang = drukker_ingang;
  }

  public void setEditieveld(Object[] editieveld) {
    this.editieveld = editieveld;
  }

  public void setEpn(String[] epn) {
    this.epn = epn;
  }

  public void setFrequentie(String[] frequentie) {
    this.frequentie = frequentie;
  }

  public void setFrequentie_code(String[] frequentie_code) {
    this.frequentie_code = frequentie_code;
  }

  public void setFysieke_beschrijving(String[] fysieke_beschrijving) {
    this.fysieke_beschrijving = fysieke_beschrijving;
  }

  public void setHerkomst(String[] herkomst) {
    this.herkomst = herkomst;
  }

  public void setIllustrator(String[] illustrator) {
    this.illustrator = illustrator;
  }

  public void setInstitute(Object[] institute) {
    this.institute = institute;
  }

  public void setIsbn(Object[] isbn) {
    this.isbn = isbn;
  }

  public void setIsbn_fout(String[] isbn_fout) {
    this.isbn_fout = isbn_fout;
  }

  public void setIsbn_volgend(Object[] isbn_volgend) {
    this.isbn_volgend = isbn_volgend;
  }

  public void setIssn(String[] issn) {
    this.issn = issn;
  }

  public void setIssn_fout(String[] issn_fout) {
    this.issn_fout = issn_fout;
  }

  public void setJaar_van_uitgave(String[] jaar_van_uitgave) {
    this.jaar_van_uitgave = jaar_van_uitgave;
  }

  public void setJaargang(Object[] jaargang) {
    this.jaargang = jaargang;
  }

  public void setKill_verzoek(String[] kill_verzoek) {
    this.kill_verzoek = kill_verzoek;
  }

  public void setKopiist(String[] kopiist) {
    this.kopiist = kopiist;
  }

  public void setLandcode(String[] landcode) {
    this.landcode = landcode;
  }

  public void setLandcode_goo(String[] landcode_goo) {
    this.landcode_goo = landcode_goo;
  }

  public void setLc_nr(String[] lc_nr) {
    this.lc_nr = lc_nr;
  }

  public void setLeverancier_prod_nr(String[] leverancier_prod_nr) {
    this.leverancier_prod_nr = leverancier_prod_nr;
  }

  public void setLokaal_trefwoord(Object[] lokaal_trefwoord) {
    this.lokaal_trefwoord = lokaal_trefwoord;
  }

  public void setNlm(String[] nlm) {
    this.nlm = nlm;
  }

  public void setNur(String[] nur) {
    this.nur = nur;
  }

  public void setOnbekende_code(String[] onbekende_code) {
    this.onbekende_code = onbekende_code;
  }

  public void setOnderwerp(String[] onderwerp) {
    this.onderwerp = onderwerp;
  }

  public void setOpm_bibl_lokaal(String[] opm_bibl_lokaal) {
    this.opm_bibl_lokaal = opm_bibl_lokaal;
  }

  public void setOther_title(String[] other_title) {
    this.other_title = other_title;
  }

  public void setPart_of(String[] part_of) {
    this.part_of = part_of;
  }

  public void setPeriode_goo(String[] periode_goo) {
    this.periode_goo = periode_goo;
  }

  public void setPpn_link_papier_url(String[] ppn_link_papier_url) {
    this.ppn_link_papier_url = ppn_link_papier_url;
  }

  public void setSectie_titel(String[] sectie_titel) {
    this.sectie_titel = sectie_titel;
  }

  public void setSelectie_sleutel(String[] selectie_sleutel) {
    this.selectie_sleutel = selectie_sleutel;
  }

  public void setSignalementcode(String[] signalementcode) {
    this.signalementcode = signalementcode;
  }

  public void setSiso(String[] siso) {
    this.siso = siso;
  }

  public void setSiso_oud(String[] siso_oud) {
    this.siso_oud = siso_oud;
  }

  public void setSoortcode(String[] soortcode) {
    this.soortcode = soortcode;
  }

  public void setSorteerveld(String[] sorteerveld) {
    this.sorteerveld = sorteerveld;
  }

  public void setSwets_nr(String[] swets_nr) {
    this.swets_nr = swets_nr;
  }

  public void setTaalcatalogisering(String[] taalcatalogisering) {
    this.taalcatalogisering = taalcatalogisering;
  }

  public void setTaalcode(Object[] taalcode) {
    this.taalcode = taalcode;
  }

  public void setTitel(String[] titel) {
    this.titel = titel;
  }

  public void setTitel_genormaliseerd(String[] titel_genormaliseerd) {
    this.titel_genormaliseerd = titel_genormaliseerd;
  }

  public void setTitel_md_publ_ppn(String[] titel_md_publ_ppn) {
    this.titel_md_publ_ppn = titel_md_publ_ppn;
  }

  public void setTitel_reeks(String[] titel_reeks) {
    this.titel_reeks = titel_reeks;
  }

  public void setTitel_reeks_md_publ(String[] titel_reeks_md_publ) {
    this.titel_reeks_md_publ = titel_reeks_md_publ;
  }

  public void setTitel_reeks_ppn(String[] titel_reeks_ppn) {
    this.titel_reeks_ppn = titel_reeks_ppn;
  }

  public void setTrefwoord(Object[] trefwoord) {
    this.trefwoord = trefwoord;
  }

  public void setTrefwoord_extra(String[] trefwoord_extra) {
    this.trefwoord_extra = trefwoord_extra;
  }

  public void setTrefwoord_oc_artikelen(String[] trefwoord_oc_artikelen) {
    this.trefwoord_oc_artikelen = trefwoord_oc_artikelen;
  }

  public void setTrefwoord_precis(String[] trefwoord_precis) {
    this.trefwoord_precis = trefwoord_precis;
  }

  public void setTussen_titel(String[] tussen_titel) {
    this.tussen_titel = tussen_titel;
  }

  public void setType_drager(String[] type_drager) {
    this.type_drager = type_drager;
  }

  public void setType_inhoud(String[] type_inhoud) {
    this.type_inhoud = type_inhoud;
  }

  public void setType_medium(String[] type_medium) {
    this.type_medium = type_medium;
  }

  public void setUdc(String[] udc) {
    this.udc = udc;
  }

  public void setUitgever(String[] uitgever) {
    this.uitgever = uitgever;
  }

  public void setUitgever_volgend(String[] uitgever_volgend) {
    this.uitgever_volgend = uitgever_volgend;
  }

  public void setUnesco_rubr(String[] unesco_rubr) {
    this.unesco_rubr = unesco_rubr;
  }

  public void setUniforme_titel(String[] uniforme_titel) {
    this.uniforme_titel = uniforme_titel;
  }

  public void setVerw_onderdeel_ppn(String[] verw_onderdeel_ppn) {
    this.verw_onderdeel_ppn = verw_onderdeel_ppn;
  }

  public String getCite_as() {
    return cite_as;
  }

  public String getPpn() {
    return ppn;
  }

  public String getSoort() {
    return soort;
  }

  public String getType() {
    return type;
  }

  public String getRegionaam() {
    return regionaam;
  }

  public Object[] getAnnotatie_algemeen() {
    return annotatie_algemeen;
  }

  public String[] getAnnotatie_bibl_ref() {
    return annotatie_bibl_ref;
  }

  public String[] getAnnotatie_bibliografie() {
    return annotatie_bibliografie;
  }

  public Object[] getAnnotatie_chron_rel() {
    return annotatie_chron_rel;
  }

  public Object[] getAnnotatie_datering() {
    return annotatie_datering;
  }

  public String[] getAnnotatie_exemplaar() {
    return annotatie_exemplaar;
  }

  public String[] getAnnotatie_exemplaar_alg() {
    return annotatie_exemplaar_alg;
  }

  public Object[] getAnnotatie_hor_rel() {
    return annotatie_hor_rel;
  }

  public String[] getAnnotatie_incipit() {
    return annotatie_incipit;
  }

  public String[] getAnnotatie_inhoud() {
    return annotatie_inhoud;
  }

  public String[] getAnnotatie_materiaal() {
    return annotatie_materiaal;
  }

  public String[] getAnnotatie_overig() {
    return annotatie_overig;
  }

  public Object[] getAnnotatie_rel__gr_kl() {
    return annotatie_rel__gr_kl;
  }

  public String[] getAuteur() {
    return auteur;
  }

  public String[] getBandinformatie() {
    return bandinformatie;
  }

  public String[] getBasiscode_goo() {
    return basiscode_goo;
  }

  public Object[] getBezitter() {
    return bezitter;
  }

  public String[] getBinder() {
    return binder;
  }

  public String[] getBnb_nr() {
    return bnb_nr;
  }

  public Object[] getBoodschap_alg() {
    return boodschap_alg;
  }

  public String[] getBrinkman_nr() {
    return brinkman_nr;
  }

  public String[] getCategorie() {
    return categorie;
  }

  public String[] getCcp_nr() {
    return ccp_nr;
  }

  public Object[] getClc() {
    return clc;
  }

  public String[] getClc_nr() {
    return clc_nr;
  }

  public String[] getCoden() {
    return coden;
  }

  public String[] getCongres() {
    return congres;
  }

  public String[] getCtc_nr() {
    return ctc_nr;
  }

  public String[] getDewey() {
    return dewey;
  }

  public String[] getData_van_uitgave() {
    return data_van_uitgave;
  }

  public String[] getDatering_gecodeerd() {
    return datering_gecodeerd;
  }

  public String[] getDatum_exempl() {
    return datum_exempl;
  }

  public String[] getDb_nr() {
    return db_nr;
  }

  public String[] getDeel_titel() {
    return deel_titel;
  }

  public String[] getDeelvermelding() {
    return deelvermelding;
  }

  public String[] getDrukker() {
    return drukker;
  }

  public Object[] getDrukker_ingang() {
    return drukker_ingang;
  }

  public Object[] getEditieveld() {
    return editieveld;
  }

  public String[] getEpn() {
    return epn;
  }

  public String[] getFrequentie() {
    return frequentie;
  }

  public String[] getFrequentie_code() {
    return frequentie_code;
  }

  public String[] getFysieke_beschrijving() {
    return fysieke_beschrijving;
  }

  public String[] getHerkomst() {
    return herkomst;
  }

  public String[] getIllustrator() {
    return illustrator;
  }

  public Object[] getInstitute() {
    return institute;
  }

  public Object[] getIsbn() {
    return isbn;
  }

  public String[] getIsbn_fout() {
    return isbn_fout;
  }

  public Object[] getIsbn_volgend() {
    return isbn_volgend;
  }

  public String[] getIssn() {
    return issn;
  }

  public String[] getIssn_fout() {
    return issn_fout;
  }

  public String[] getJaar_van_uitgave() {
    return jaar_van_uitgave;
  }

  public Object[] getJaargang() {
    return jaargang;
  }

  public String[] getKill_verzoek() {
    return kill_verzoek;
  }

  public String[] getKopiist() {
    return kopiist;
  }

  public String[] getLandcode() {
    return landcode;
  }

  public String[] getLandcode_goo() {
    return landcode_goo;
  }

  public String[] getLc_nr() {
    return lc_nr;
  }

  public String[] getLeverancier_prod_nr() {
    return leverancier_prod_nr;
  }

  public Object[] getLokaal_trefwoord() {
    return lokaal_trefwoord;
  }

  public String[] getNlm() {
    return nlm;
  }

  public String[] getNur() {
    return nur;
  }

  public String[] getOnbekende_code() {
    return onbekende_code;
  }

  public String[] getOnderwerp() {
    return onderwerp;
  }

  public String[] getOpm_bibl_lokaal() {
    return opm_bibl_lokaal;
  }

  public String[] getOther_title() {
    return other_title;
  }

  public String[] getPart_of() {
    return part_of;
  }

  public String[] getPeriode_goo() {
    return periode_goo;
  }

  public String[] getPpn_link_papier_url() {
    return ppn_link_papier_url;
  }

  public String[] getSectie_titel() {
    return sectie_titel;
  }

  public String[] getSelectie_sleutel() {
    return selectie_sleutel;
  }

  public String[] getSignalementcode() {
    return signalementcode;
  }

  public String[] getSiso() {
    return siso;
  }

  public String[] getSiso_oud() {
    return siso_oud;
  }

  public String[] getSoortcode() {
    return soortcode;
  }

  public String[] getSorteerveld() {
    return sorteerveld;
  }

  public String[] getSwets_nr() {
    return swets_nr;
  }

  public String[] getTaalcatalogisering() {
    return taalcatalogisering;
  }

  public Object[] getTaalcode() {
    return taalcode;
  }

  public String[] getTitel() {
    return titel;
  }

  public String[] getTitel_genormaliseerd() {
    return titel_genormaliseerd;
  }

  public String[] getTitel_md_publ_ppn() {
    return titel_md_publ_ppn;
  }

  public String[] getTitel_reeks() {
    return titel_reeks;
  }

  public String[] getTitel_reeks_md_publ() {
    return titel_reeks_md_publ;
  }

  public String[] getTitel_reeks_ppn() {
    return titel_reeks_ppn;
  }

  public Object[] getTrefwoord() {
    return trefwoord;
  }

  public String[] getTrefwoord_extra() {
    return trefwoord_extra;
  }

  public String[] getTrefwoord_oc_artikelen() {
    return trefwoord_oc_artikelen;
  }

  public String[] getTrefwoord_precis() {
    return trefwoord_precis;
  }

  public String[] getTussen_titel() {
    return tussen_titel;
  }

  public String[] getType_drager() {
    return type_drager;
  }

  public String[] getType_inhoud() {
    return type_inhoud;
  }

  public String[] getType_medium() {
    return type_medium;
  }

  public String[] getUdc() {
    return udc;
  }

  public String[] getUitgever() {
    return uitgever;
  }

  public String[] getUitgever_volgend() {
    return uitgever_volgend;
  }

  public String[] getUnesco_rubr() {
    return unesco_rubr;
  }

  public String[] getUniforme_titel() {
    return uniforme_titel;
  }

  public String[] getVerw_onderdeel_ppn() {
    return verw_onderdeel_ppn;
  }

}
