package nl.knaw.huygens.timbuctoo.tools.importer.cnw;

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
import java.util.Collection;

import nl.knaw.huygens.tei.DelegatingVisitor;
import nl.knaw.huygens.tei.Document;
import nl.knaw.huygens.tei.Element;
import nl.knaw.huygens.tei.ElementHandler;
import nl.knaw.huygens.tei.Traversal;
import nl.knaw.huygens.tei.Visitor;
import nl.knaw.huygens.tei.XmlContext;
import nl.knaw.huygens.tei.handlers.DefaultElementHandler;
import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.Location;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.cnw.CNWLocation;
import nl.knaw.huygens.timbuctoo.model.cnw.CNWPerson;
import nl.knaw.huygens.timbuctoo.model.cnw.CNWRelation;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;
import nl.knaw.huygens.timbuctoo.model.util.PlaceName;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;
import nl.knaw.huygens.timbuctoo.tools.importer.CaptureHandler;
import nl.knaw.huygens.timbuctoo.tools.importer.DefaultImporter;
import nl.knaw.huygens.timbuctoo.util.Files;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import com.google.inject.Injector;

/**
 * Imports data of the CNW INGForms project into the repository.
 */
public class CNWImporter extends DefaultImporter {

	private static final Logger LOG = LoggerFactory.getLogger(CNWImporter.class);

	public static void main(String[] args) throws Exception {
		Stopwatch stopWatch = Stopwatch.createStarted();

		// Handle commandline arguments
		String directory = (args.length > 0) ? args[0] : "../../ingforms/netwerkverwey/ingforms/data";

		CNWImporter importer = null;
		try {
			Injector injector = ToolsInjectionModule.createInjector();
			Repository repository = injector.getInstance(Repository.class);
			IndexManager indexManager = injector.getInstance(IndexManager.class);

			importer = new CNWImporter(repository, indexManager, directory);
			importer.importAll();
		} finally {
			if (importer != null) {
				importer.close();
			}
			LOG.info("Time used: {}", stopWatch);
		}
	}

	// ---------------------------------------------------------------------------

	private static final String VRE_ID = "CNW";
	private static final String[] TEI_EXTENSIONS = { "xml" };
	// private static final String ORGANIZATIONS = "CNW-organizations.xml";

	private final File inputDir;

	// private final LocationConcordance concordance;

	public CNWImporter(Repository repository, IndexManager indexManager, String inputDirName) throws Exception {
		super(repository, indexManager, VRE_ID);

		inputDir = new File(inputDirName);
		if (inputDir.isDirectory()) {
			System.out.printf("%nImporting from %s%n", inputDir.getCanonicalPath());
		} else {
			System.out.printf("%nNot a directory: %s%n", inputDir.getAbsolutePath());
		}
	}

	private void importAll() throws Exception {
		try {
			Collection<File> files = FileUtils.listFiles(inputDir, TEI_EXTENSIONS, true);
			for (File file : Sets.newTreeSet(files)) {
				handleXmlFile(file);
			}

		} finally {
			displayStatus();
			closeImportLog();
		}
	}

	// ---------------------------------------------------------------------------

	private void handleXmlFile(File file) throws Exception {
		String fileName = file.getName();
		log(".. %s%n", fileName);
		String xml = Files.readTextFromFile(file);
		Visitor visitor = new PersonsVisitor();
		Document.createFromXml(xml).accept(visitor);
	}

	private class ImportContext extends XmlContext {
		public CNWPerson person;
		public PersonName personName;
		String placeString;
		String country;
		public String birthPlaceId;
		public String deathPlaceId;
		
		private CNWLocation getLocation() {
			CNWLocation place = new CNWLocation();
			PlaceName name = new PlaceName();
			name.setCountry(country);
			name.setSettlement(placeString);
			place.addName("Nld", name);
			return place;
		}

	}

	private class PersonsVisitor extends DelegatingVisitor<ImportContext> {
		public PersonsVisitor() {
			super(new ImportContext());
			addElementHandler(new PersonHandler(), "persoon");

			addElementHandler(new CNWPersNameHandler(), "persname");
			addElementHandler(new NameHandler(), "name");
			addElementHandler(new PrepositionHandler(), "preposition");
			addElementHandler(new FirstnameHandler(), "firstname");
			addElementHandler(new IntrapositionHandler(), "intraposition");
			addElementHandler(new FamilynameHandler(), "familyname");
			addElementHandler(new PostpositionHandler(), "postposition");

			addElementHandler(new KoppelnaamHandler(), "koppelnaam");

			addElementHandler(new AltnameHandler(), "altname");
			addElementHandler(new NamesHandler(), "names");
			addElementHandler(new NametypeHandler(), "nametype");

			addElementHandler(new GenderHandler(), "sex");

			addElementHandler(new BirthHandler(), "birth");
			addElementHandler(new DeathHandler(), "death");

			addElementHandler(new DateHandler(), "date");///in birth, death
			addElementHandler(new YearHandler(), "year");
			addElementHandler(new MonthHandler(), "month");
			addElementHandler(new DayHandler(), "day");

			addElementHandler(new ValuesHandler(), "values");
			addElementHandler(new ValHandler(), "val");

			addElementHandler(new PlaceHandler(), "place");
			addElementHandler(new CountryHandler(), "country");

			addElementHandler(new WoonplaatsHandler(), "woonplaats");
			addElementHandler(new EducationHandler(), "education");
			addElementHandler(new OccupationHandler(), "occupation");

			addElementHandler(new NetworkHandler(), "network");
			addElementHandler(new DomainHandler(), "domain"); // in network, domains

			addElementHandler(new RelativesHandler(), "relatives");
			addElementHandler(new RelatieHandler(), "relatie");
			addElementHandler(new ReltypeHandler(), "reltype");
			addElementHandler(new KoppelnameHandler(), "koppelname");

			addElementHandler(new CharacteristicHandler(), "characteristic");

			addElementHandler(new DomainsHandler(), "domains");

			addElementHandler(new ActivitiesHandler(), "activities");
			addElementHandler(new PoliticsHandler(), "politics");
			addElementHandler(new OpmPoliticsHandler(), "opm_politics");
			addElementHandler(new LevensbeschouwingHandler(), "levensbeschouwing");
			addElementHandler(new LiteratuurHandler(), "literatuur");
			addElementHandler(new BiodesurlHandler(), "biodesurl");
			addElementHandler(new DbnlUrlHandler(), "dbnl_url");
			addElementHandler(new BntlUrlHandler(), "bntl_url");
			addElementHandler(new DbngUrlHandler(), "dbng_url");
			addElementHandler(new CenUrlAfzHandler(), "cen_url_afz");
			addElementHandler(new CenUrlOntvHandler(), "cen_url_ontv");

			addElementHandler(new VerwijzingenHandler(), "verwijzingen");
			addElementHandler(new LinkHandler(), "link");
			addElementHandler(new UrlHandler(), "url");
			addElementHandler(new IdentifierHandler(), "identifier");
			addElementHandler(new SoortHandler(), "soort");
			addElementHandler(new OpmerkingenHandler(), "opmerkingen"); // in link, persoon

			addElementHandler(new NotitiesHandler(), "notities");
			addElementHandler(new AantekeningenHandler(), "aantekeningen");

		}
	}

	private class PersonHandler implements ElementHandler<ImportContext> {
		@Override
		public Traversal enterElement(Element element, ImportContext context) {
			context.person = new CNWPerson();
			return Traversal.NEXT;
		}

		@Override
		public Traversal leaveElement(Element element, ImportContext context) {
			try {
				String personId = addDomainEntity(CNWPerson.class, context.person);
				
				Reference brelType = getRelationTypeRef("hasBirthPlace", true);
				Reference bsourceRef = new Reference(CNWPerson.class, personId);
				Reference btargetRef = new Reference(CNWLocation.class, context.birthPlaceId);
				addRelation(CNWRelation.class, brelType, bsourceRef, btargetRef, change, "");

				Reference drelType = getRelationTypeRef("hasDeathPlace", true);
				Reference dsourceRef = new Reference(CNWPerson.class, personId);
				Reference dtargetRef = new Reference(CNWLocation.class, context.deathPlaceId);
				addRelation(CNWRelation.class, drelType, dsourceRef, dtargetRef, change, "");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return Traversal.NEXT;
		}
	}

	private class CNWPersNameHandler implements ElementHandler<ImportContext> {
		@Override
		public Traversal enterElement(Element element, ImportContext context) {
			context.personName = new PersonName();
			return Traversal.NEXT;
		}

		@Override
		public Traversal leaveElement(Element element, ImportContext context) {
			context.person.addName(context.personName);
			return Traversal.NEXT;
		}
	}

	private class GenderHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			if (text.equals("male")) {
				context.person.setGender(Person.Gender.MALE);
			} else if (text.equals("female")) {
				context.person.setGender(Person.Gender.FEMALE);
				//			} else if (text.equals("not applicable")) {
				//				context.person.setGender(Person.Gender.NOT_APPLICABLE);
			} else {
				context.person.setGender(Person.Gender.UNKNOWN);
			}
		}
	}

	private class BirthHandler extends DefaultElementHandler<ImportContext> {
		@Override
		public Traversal enterElement(Element element, ImportContext context) {
			String text = element.getAttribute("when");
			if (!text.isEmpty()) {
				context.person.setBirthDate(new Datable(text));
			}
			return Traversal.NEXT;
		}

		@Override
		public Traversal leaveElement(Element element, ImportContext context) {
			CNWLocation location = context.getLocation();
			context.birthPlaceId = addDomainEntity(CNWLocation.class, location);

			return super.leaveElement(element, context);
		}

	}

	private class DeathHandler extends DefaultElementHandler<ImportContext> {
		@Override
		public Traversal enterElement(Element element, ImportContext context) {
			String text = element.getAttribute("when");
			if (!text.isEmpty()) {
				context.person.setDeathDate(new Datable(text));
			}
			return Traversal.NEXT;
		}
	}

	private class OpmerkingenHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setOpmerkingen(text);
		}
	}

	private class VerwijzingenHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setVerwijzingen(text);
		}
	}

	private class NotitiesHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setNotities(text);
		}
	}

	private class AantekeningenHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setAantekeningen(text);
		}
	}

	private class ActivitiesHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setActivities(text);
		}
	}

	private class AltnameHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setAltname(text);
		}
	}

	private class BiodesurlHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setBiodesurl(text);
		}
	}

	private class BntlUrlHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setBntlUrl(text);
		}
	}

	private class CenUrlAfzHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setCenUrlAfz(text);
		}
	}

	private class CenUrlOntvHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setCenUrlOntv(text);
		}
	}

	private class CharacteristicHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setCharacteristic(text);
		}
	}

	private class DateHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setDate(text);
		}
	}

	private class DayHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setDay(text);
		}
	}

	private class DbngUrlHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setDbngUrl(text);
		}
	}

	private class DbnlUrlHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setDbnlUrl(text);
		}
	}

	private class DomainHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setDomain(text);
		}
	}

	private class DomainsHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setDomains(text);
		}
	}

	private class EducationHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setEducation(text);
		}
	}

	private class FamilynameHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setFamilyname(text);
		}
	}

	private class FirstnameHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setFirstname(text);
		}
	}

	private class IdentifierHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setIdentifier(text);
		}
	}

	private class IntrapositionHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setIntraposition(text);
		}
	}

	private class KoppelnaamHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setKoppelnaam(text);
		}
	}

	private class KoppelnameHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setKoppelname(text);
		}
	}

	private class LevensbeschouwingHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setLevensbeschouwing(text);
		}
	}

	private class LinkHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setLink(text);
		}
	}

	private class LiteratuurHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setLiteratuur(text);
		}
	}

	private class MonthHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setMonth(text);
		}
	}

	private class NameHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setName(text);
		}
	}

	private class NamesHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			//			context.person.setNames(text);
		}
	}

	private class NametypeHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setNametype(text);
		}
	}

	private class NetworkHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setNetwork(text);
		}
	}

	private class OccupationHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setOccupation(text);
		}
	}

	private class OpmPoliticsHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setOpmPolitics(text);
		}
	}

	private class PersnameHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setPersname(text);
		}
	}

	private class PersoonHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setPersoon(text);
		}
	}

	private class PlaceHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.placeString = text;
		}
	}

	private class CountryHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.country = text;
		}
	}

	private class PoliticsHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setPolitics(text);
		}
	}

	private class PostpositionHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setPostposition(text);
		}
	}

	private class PrepositionHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setPreposition(text);
		}
	}

	private class RelatieHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setRelatie(text);
		}
	}

	private class RelativesHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setRelatives(text);
		}
	}

	private class ReltypeHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setReltype(text);
		}
	}

	private class SexHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setSex(text);
		}
	}

	private class SoortHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setSoort(text);
		}
	}

	private class UrlHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setUrl(text);
		}
	}

	private class ValHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setVal(text);
		}
	}

	private class ValuesHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setValues(text);
		}
	}

	private class WoonplaatsHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setWoonplaats(text);
		}
	}

	private class YearHandler extends CaptureHandler<ImportContext> {
		@Override
		public void handleContent(Element element, ImportContext context, String text) {
			context.person.setYear(text);
		}
	}

}
