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

import nl.knaw.huygens.timbuctoo.model.Tekst;

public class EBNMTekst extends Tekst {

  private String overige_hs_aanduiding;
  private String ppn;
  private String soort;
  private String type;
  private String tekstId;

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
  private String[] dewey;
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
  private String[] part_of;
  private String[] regiocode;
  private String[] selectie_sleutel;
  private String[] signalementcode;
  private String[] siso_oud;
  private String[] sleutelw_incipit;
  private String[] soortcode;
  private String sortcode;
  private String[] sorteerveld;
  private Object[] taalcode;
  private String[] titel;
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
    return tekstId;
  }

  public void setCodeId(String _id) {
    this.tekstId = _id;
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

  public void setDewey(String[] dewey) {
    this.dewey = dewey;
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

  public void setPart_of(String[] part_of) {
    this.part_of = part_of;
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

  public void setSortcode(String sortcode) {
    this.sortcode = sortcode;
  }

  public void setSorteerveld(String[] sorteerveld) {
    this.sorteerveld = sorteerveld;
  }

  public void setTaalcode(Object[] taalcode) {
    this.taalcode = taalcode;
  }

  public void setTitel(String[] titel) {
    this.titel = titel;
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

  public String getOverige_hs_aanduiding() {
    return overige_hs_aanduiding;
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

  public String[] getAnnotatie_colofon() {
    return annotatie_colofon;
  }

  public String[] getAnnotatie_datering() {
    return annotatie_datering;
  }

  public Object[] getAnnotatie_documentatie() {
    return annotatie_documentatie;
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

  public String[] getAnnotatie_rel__gr_kl() {
    return annotatie_rel__gr_kl;
  }

  public String[] getAnnotatie_schrift() {
    return annotatie_schrift;
  }

  public Object[] getAuteur() {
    return auteur;
  }

  public Object getBewaarplaats_en_signatuur() {
    return bewaarplaats_en_signatuur;
  }

  public Object[] getBezitter() {
    return bezitter;
  }

  public Object[] getBinder() {
    return binder;
  }

  public String[] getBoodschap_alg() {
    return boodschap_alg;
  }

  public Object[] getCategorie() {
    return categorie;
  }

  public Object[] getCorrector() {
    return corrector;
  }

  public Object getDatering() {
    return datering;
  }

  public String[] getDatum_exempl() {
    return datum_exempl;
  }

  public Object[] getDecorator() {
    return decorator;
  }

  public String[] getDewey() {
    return dewey;
  }

  public String[] getEpn() {
    return epn;
  }

  public Object[] getIllustrator() {
    return illustrator;
  }

  public Object[] getIngang() {
    return ingang;
  }

  public String[] getJournaalnummer() {
    return journaalnummer;
  }

  public Object[] getKopiist() {
    return kopiist;
  }

  public String[] getLandcode() {
    return landcode;
  }

  public String[] getLayout_code() {
    return layout_code;
  }

  public Object[] getLokalisering() {
    return lokalisering;
  }

  public String[] getOnbekende_code() {
    return onbekende_code;
  }

  public String[] getOnderwerp() {
    return onderwerp;
  }

  public Object[] getOpdrachtgever() {
    return opdrachtgever;
  }

  public String[] getPart_of() {
    return part_of;
  }

  public String[] getRegiocode() {
    return regiocode;
  }

  public String[] getSelectie_sleutel() {
    return selectie_sleutel;
  }

  public String[] getSignalementcode() {
    return signalementcode;
  }

  public String[] getSiso_oud() {
    return siso_oud;
  }

  public String[] getSleutelw_incipit() {
    return sleutelw_incipit;
  }

  public String[] getSoortcode() {
    return soortcode;
  }

  public String getSortcode() {
    return sortcode;
  }

  public String[] getSorteerveld() {
    return sorteerveld;
  }

  public Object[] getTaalcode() {
    return taalcode;
  }

  public String[] getTitel() {
    return titel;
  }

  public Object[] getTitel_genormaliseerd() {
    return titel_genormaliseerd;
  }

  public String[] getTitel_hs_inc_form() {
    return titel_hs_inc_form;
  }

  public Object[] getTrefwoord() {
    return trefwoord;
  }

  public String[] getUrl() {
    return url;
  }

  public String[] getWatermerk() {
    return watermerk;
  }

}
