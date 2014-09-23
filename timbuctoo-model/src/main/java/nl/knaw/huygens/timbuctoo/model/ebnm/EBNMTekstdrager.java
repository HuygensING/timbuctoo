package nl.knaw.huygens.timbuctoo.model.ebnm;

/*
 * #%L
 * Timbuctoo core
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

import nl.knaw.huygens.timbuctoo.model.Tekstdrager;

public class EBNMTekstdrager extends Tekstdrager {
  private String overige_hs_aanduiding;
  private String ppn;
  private String soort;
  private String type;
  private String tekstdragerId;

  private String[] annotatie_colofon;
  private String[] annotatie_datering;
  private Object[] annotatie_documentatie;
  private String[] annotatie_incipit;
  private String[] annotatie_inhoud;
  private String[] annotatie_materiaal;
  private String[] annotatie_overig;
  private String[] annotatie_rel__gr_kl;
  private String[] annotatie_schrift;
  private Object[] auteur;
  private Object bewaarplaats_en_signatuur;
  private Object[] bezitter;
  private Object[] binder;
  private String[] boodschap_alg;
  private Object[] categorie;
  private Object[] corrector;
  private Object datering;
  private String[] datum_exempl;
  private Object[] decorator;
  private String[] epn;
  private Object[] illustrator;
  private Object[] ingang;
  private String[] journaalnummer;
  private Object[] kopiist;
  private String[] landcode;
  private String[] layout_code;
  private Object[] lokalisering;
  private String[] onbekende_code;
  private String[] onderwerp;
  private Object[] opdrachtgever;
  private String[] regiocode;
  private String[] selectie_sleutel;
  private String[] signalementcode;
  private String[] siso_oud;
  private String[] sleutelw_incipit;
  private String[] soortcode;
  private String[] sorteerveld;
  private Object[] taalcode;
  private Object[] titel_genormaliseerd;
  private String[] titel_hs_inc_form;
  private Object[] trefwoord;
  private String[] url;
  private String[] watermerk;

  public String getLabel() {
    return getValue();
  }

  public void setLabel(String string) {
    setValue(string);
  }

  public String getCodeId() {
    return tekstdragerId;
  }

  public void setCodeId(String _id) {
    this.tekstdragerId = _id;
  }

  public String getoverige_hs_aanduiding() {
    return overige_hs_aanduiding;
  }

  public String getppn() {
    return ppn;
  }

  public String getsoort() {
    return soort;
  }

  public String gettype() {
    return type;
  }

  public String[] getannotatie_colofon() {
    return annotatie_colofon;
  }

  public String[] getannotatie_datering() {
    return annotatie_datering;
  }

  public Object[] getannotatie_documentatie() {
    return annotatie_documentatie;
  }

  public String[] getannotatie_incipit() {
    return annotatie_incipit;
  }

  public String[] getannotatie_inhoud() {
    return annotatie_inhoud;
  }

  public String[] getannotatie_materiaal() {
    return annotatie_materiaal;
  }

  public String[] getannotatie_overig() {
    return annotatie_overig;
  }

  public String[] getannotatie_rel__gr_kl() {
    return annotatie_rel__gr_kl;
  }

  public String[] getannotatie_schrift() {
    return annotatie_schrift;
  }

  public Object[] getauteur() {
    return auteur;
  }

  public Object getbewaarplaats_en_signatuur() {
    return bewaarplaats_en_signatuur;
  }

  public Object[] getbezitter() {
    return bezitter;
  }

  public Object[] getbinder() {
    return binder;
  }

  public String[] getboodschap_alg() {
    return boodschap_alg;
  }

  public Object[] getcategorie() {
    return categorie;
  }

  public Object[] getcorrector() {
    return corrector;
  }

  public Object getdatering() {
    return datering;
  }

  public String[] getdatum_exempl() {
    return datum_exempl;
  }

  public Object[] getdecorator() {
    return decorator;
  }

  public String[] getepn() {
    return epn;
  }

  public Object[] getillustrator() {
    return illustrator;
  }

  public Object[] getingang() {
    return ingang;
  }

  public String[] getjournaalnummer() {
    return journaalnummer;
  }

  public Object[] getkopiist() {
    return kopiist;
  }

  public String[] getlandcode() {
    return landcode;
  }

  public String[] getlayout_code() {
    return layout_code;
  }

  public Object[] getlokalisering() {
    return lokalisering;
  }

  public String[] getonbekende_code() {
    return onbekende_code;
  }

  public String[] getonderwerp() {
    return onderwerp;
  }

  public Object[] getopdrachtgever() {
    return opdrachtgever;
  }

  public String[] getregiocode() {
    return regiocode;
  }

  public String[] getselectie_sleutel() {
    return selectie_sleutel;
  }

  public String[] getsignalementcode() {
    return signalementcode;
  }

  public String[] getsiso_oud() {
    return siso_oud;
  }

  public String[] getsleutelw_incipit() {
    return sleutelw_incipit;
  }

  public String[] getsoortcode() {
    return soortcode;
  }

  public String[] getsorteerveld() {
    return sorteerveld;
  }

  public Object[] gettaalcode() {
    return taalcode;
  }

  public Object[] gettitel_genormaliseerd() {
    return titel_genormaliseerd;
  }

  public String[] gettitel_hs_inc_form() {
    return titel_hs_inc_form;
  }

  public Object[] gettrefwoord() {
    return trefwoord;
  }

  public String[] geturl() {
    return url;
  }

  public String[] getwatermerk() {
    return watermerk;
  }

  public void setOverige_hs_aanduiding(String overige_hs_aanduiding) {
    this.overige_hs_aanduiding = overige_hs_aanduiding;
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

  public void setAnnotatie_colofon(String[] annotatie_colofon) {
    this.annotatie_colofon = annotatie_colofon;
  }

  public void setAnnotatie_datering(String[] annotatie_datering) {
    this.annotatie_datering = annotatie_datering;
  }

  public void setAnnotatie_documentatie(Object[] annotatie_documentatie) {
    this.annotatie_documentatie = annotatie_documentatie;
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

  public void setAnnotatie_rel__gr_kl(String[] annotatie_rel__gr_kl) {
    this.annotatie_rel__gr_kl = annotatie_rel__gr_kl;
  }

  public void setAnnotatie_schrift(String[] annotatie_schrift) {
    this.annotatie_schrift = annotatie_schrift;
  }

  public void setAuteur(Object[] auteur) {
    this.auteur = auteur;
  }

  public void setBewaarplaats_en_signatuur(Object bewaarplaats_en_signatuur) {
    this.bewaarplaats_en_signatuur = bewaarplaats_en_signatuur;
  }

  public void setBezitter(Object[] bezitter) {
    this.bezitter = bezitter;
  }

  public void setBinder(Object[] binder) {
    this.binder = binder;
  }

  public void setBoodschap_alg(String[] boodschap_alg) {
    this.boodschap_alg = boodschap_alg;
  }

  public void setCategorie(Object[] categorie) {
    this.categorie = categorie;
  }

  public void setCorrector(Object[] corrector) {
    this.corrector = corrector;
  }

  public void setDatering(Object datering) {
    this.datering = datering;
  }

  public void setDatum_exempl(String[] datum_exempl) {
    this.datum_exempl = datum_exempl;
  }

  public void setDecorator(Object[] decorator) {
    this.decorator = decorator;
  }

  public void setEpn(String[] epn) {
    this.epn = epn;
  }

  public void setIllustrator(Object[] illustrator) {
    this.illustrator = illustrator;
  }

  public void setIngang(Object[] ingang) {
    this.ingang = ingang;
  }

  public void setJournaalnummer(String[] journaalnummer) {
    this.journaalnummer = journaalnummer;
  }

  public void setKopiist(Object[] kopiist) {
    this.kopiist = kopiist;
  }

  public void setLandcode(String[] landcode) {
    this.landcode = landcode;
  }

  public void setLayout_code(String[] layout_code) {
    this.layout_code = layout_code;
  }

  public void setLokalisering(Object[] lokalisering) {
    this.lokalisering = lokalisering;
  }

  public void setOnbekende_code(String[] onbekende_code) {
    this.onbekende_code = onbekende_code;
  }

  public void setOnderwerp(String[] onderwerp) {
    this.onderwerp = onderwerp;
  }

  public void setOpdrachtgever(Object[] opdrachtgever) {
    this.opdrachtgever = opdrachtgever;
  }

  public void setRegiocode(String[] regiocode) {
    this.regiocode = regiocode;
  }

  public void setSelectie_sleutel(String[] selectie_sleutel) {
    this.selectie_sleutel = selectie_sleutel;
  }

  public void setSignalementcode(String[] signalementcode) {
    this.signalementcode = signalementcode;
  }

  public void setSiso_oud(String[] siso_oud) {
    this.siso_oud = siso_oud;
  }

  public void setSleutelw_incipit(String[] sleutelw_incipit) {
    this.sleutelw_incipit = sleutelw_incipit;
  }

  public void setSoortcode(String[] soortcode) {
    this.soortcode = soortcode;
  }

  public void setSorteerveld(String[] sorteerveld) {
    this.sorteerveld = sorteerveld;
  }

  public void setTaalcode(Object[] taalcode) {
    this.taalcode = taalcode;
  }

  public void setTitel_genormaliseerd(Object[] titel_genormaliseerd) {
    this.titel_genormaliseerd = titel_genormaliseerd;
  }

  public void setTitel_hs_inc_form(String[] titel_hs_inc_form) {
    this.titel_hs_inc_form = titel_hs_inc_form;
  }

  public void setTrefwoord(Object[] trefwoord) {
    this.trefwoord = trefwoord;
  }

  public void setUrl(String[] url) {
    this.url = url;
  }

  public void setWatermerk(String[] watermerk) {
    this.watermerk = watermerk;
  }

}
