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

import nl.knaw.huygens.timbuctoo.model.Lexicon;

public class EBNMLexicon extends Lexicon {
  private String lexiconId;
  private String ppn;
  private String soort;
  private String url;

  private String[] standaard_naam;
  private String[] adresgegevens;
  private String[] beroeps_aanduiding;
  private String[] beroep_vakgebied;
  private String[] bredere_term;
  private String[] indicatoren;
  private String[] ingang;
  private Object[] institute_bredere_term;
  private String[] instituut;
  private String[] landcode;
  private String[] meisjesnaam;
  private String[] naam_echtgenoot;
  private String[] naams_variant;
  private String[] naamsvariant;
  private String[] normaliserende_ingang;
  private Object[] relaties_overig;
  private String[] sec_ingang;
  private String[] sorteerveld;
  private String[] taalcode;
  private Object[] toelichtingen;
  private String[] toepassing;
  private String[] verwante_term;
  private String[] verwijzing_eigen;
  private Object[] verwijzing_institute;
  private Object[] verwijzing_pseudoniem;
  private String[] volledige_naam;
  private String[] years;

  public String getLabel() {
    return getValue();
  }

  public void setLabel(String string) {
    setValue(string);
  }

  public String getCodeId() {
    return lexiconId;
  }

  public void setCodeId(String _id) {
    this.lexiconId = _id;
  }

  public void setPpn(String ppn) {
    this.ppn = ppn;
  }

  public void setSoort(String soort) {
    this.soort = soort;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getPpn() {
    return ppn;
  }

  public String getSoort() {
    return soort;
  }

  public String getUrl() {
    return url;
  }

  public String[] getStandaard_naam() {
    return standaard_naam;
  }

  public void setStandaard_naam(String[] standaard_naam) {
    this.standaard_naam = standaard_naam;
  }

  public void setAdresgegevens(String[] adresgegevens) {
    this.adresgegevens = adresgegevens;
  }

  public void setBeroeps_aanduiding(String[] beroeps_aanduiding) {
    this.beroeps_aanduiding = beroeps_aanduiding;
  }

  public void setBeroep_vakgebied(String[] beroep_vakgebied) {
    this.beroep_vakgebied = beroep_vakgebied;
  }

  public void setBredere_term(String[] bredere_term) {
    this.bredere_term = bredere_term;
  }

  public void setIndicatoren(String[] indicatoren) {
    this.indicatoren = indicatoren;
  }

  public void setIngang(String[] ingang) {
    this.ingang = ingang;
  }

  public void setInstitute_bredere_term(Object[] institute_bredere_term) {
    this.institute_bredere_term = institute_bredere_term;
  }

  public void setInstituut(String[] instituut) {
    this.instituut = instituut;
  }

  public void setLandcode(String[] landcode) {
    this.landcode = landcode;
  }

  public void setMeisjesnaam(String[] meisjesnaam) {
    this.meisjesnaam = meisjesnaam;
  }

  public void setNaam_echtgenoot(String[] naam_echtgenoot) {
    this.naam_echtgenoot = naam_echtgenoot;
  }

  public void setNaams_variant(String[] naams_variant) {
    this.naams_variant = naams_variant;
  }

  public void setNaamsvariant(String[] naamsvariant) {
    this.naamsvariant = naamsvariant;
  }

  public void setNormaliserende_ingang(String[] normaliserende_ingang) {
    this.normaliserende_ingang = normaliserende_ingang;
  }

  public void setRelaties_overig(Object[] relaties_overig) {
    this.relaties_overig = relaties_overig;
  }

  public void setSec_ingang(String[] sec_ingang) {
    this.sec_ingang = sec_ingang;
  }

  public void setSorteerveld(String[] sorteerveld) {
    this.sorteerveld = sorteerveld;
  }

  public void setTaalcode(String[] taalcode) {
    this.taalcode = taalcode;
  }

  public void setToelichtingen(Object[] toelichtingen) {
    this.toelichtingen = toelichtingen;
  }

  public void setToepassing(String[] toepassing) {
    this.toepassing = toepassing;
  }

  public void setVerwante_term(String[] verwante_term) {
    this.verwante_term = verwante_term;
  }

  public void setVerwijzing_eigen(String[] verwijzing_eigen) {
    this.verwijzing_eigen = verwijzing_eigen;
  }

  public void setVerwijzing_institute(Object[] verwijzing_institute) {
    this.verwijzing_institute = verwijzing_institute;
  }

  public void setVerwijzing_pseudoniem(Object[] verwijzing_pseudoniem) {
    this.verwijzing_pseudoniem = verwijzing_pseudoniem;
  }

  public void setVolledige_naam(String[] volledige_naam) {
    this.volledige_naam = volledige_naam;
  }

  public void setYears(String[] years) {
    this.years = years;
  }

  public String[] getAdresgegevens() {
    return this.adresgegevens;
  }

  public String[] getBeroeps_aanduiding() {
    return this.beroeps_aanduiding;
  }

  public String[] getBeroep_vakgebied() {
    return this.beroep_vakgebied;
  }

  public String[] getBredere_term() {
    return this.bredere_term;
  }

  public String[] getIndicatoren() {
    return this.indicatoren;
  }

  public String[] getIngang() {
    return this.ingang;
  }

  public Object[] getInstitute_bredere_term() {
    return this.institute_bredere_term;
  }

  public String[] getInstituut() {
    return this.instituut;
  }

  public String[] getLandcode() {
    return this.landcode;
  }

  public String[] getMeisjesnaam() {
    return this.meisjesnaam;
  }

  public String[] getNaam_echtgenoot() {
    return this.naam_echtgenoot;
  }

  public String[] getNaams_variant() {
    return this.naams_variant;
  }

  public String[] getNaamsvariant() {
    return this.naamsvariant;
  }

  public String[] getNormaliserende_ingang() {
    return this.normaliserende_ingang;
  }

  public Object[] getRelaties_overig() {
    return this.relaties_overig;
  }

  public String[] getSec_ingang() {
    return this.sec_ingang;
  }

  public String[] getSorteerveld() {
    return this.sorteerveld;
  }

  public String[] getTaalcode() {
    return this.taalcode;
  }

  public Object[] getToelichtingen() {
    return this.toelichtingen;
  }

  public String[] getToepassing() {
    return this.toepassing;
  }

  public String[] getVerwante_term() {
    return this.verwante_term;
  }

  public String[] getVerwijzing_eigen() {
    return this.verwijzing_eigen;
  }

  public Object[] getVerwijzing_institute() {
    return this.verwijzing_institute;
  }

  public Object[] getVerwijzing_pseudoniem() {
    return this.verwijzing_pseudoniem;
  }

  public String[] getVolledige_naam() {
    return this.volledige_naam;
  }

  public String[] getYears() {
    return this.years;
  }
}
