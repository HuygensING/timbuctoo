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
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

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
import nl.knaw.huygens.timbuctoo.model.cnw.CNWLink;
import nl.knaw.huygens.timbuctoo.model.cnw.CNWLocation;
import nl.knaw.huygens.timbuctoo.model.cnw.CNWPerson;
import nl.knaw.huygens.timbuctoo.model.cnw.CNWRelation;
import nl.knaw.huygens.timbuctoo.model.cwno.CWNOPerson;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;
import nl.knaw.huygens.timbuctoo.model.util.PersonNameComponent.Type;
import nl.knaw.huygens.timbuctoo.model.util.PlaceName;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;
import nl.knaw.huygens.timbuctoo.tools.importer.CaptureHandler;
import nl.knaw.huygens.timbuctoo.tools.importer.DefaultConverter;
import nl.knaw.huygens.timbuctoo.tools.importer.RelationTypeImporter;
import nl.knaw.huygens.timbuctoo.tools.importer.neww.CobwwwebNoConverter;
import nl.knaw.huygens.timbuctoo.tools.process.Pipeline;
import nl.knaw.huygens.timbuctoo.tools.process.Progress;
import nl.knaw.huygens.timbuctoo.util.Files;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Injector;

/**
 * Converts xml data of the CNW INGForms project to json.
 */
public class CNWConverter extends DefaultConverter {

	private static final Logger LOG = LoggerFactory.getLogger(CNWConverter.class);

	public static void main(String[] args) throws Exception {
		String directory = (args.length > 0) ? args[0] : "../../ingforms/netwerkverwey/ingforms/data";
		Pipeline.execute(new CNWConverter(directory));
	}

	// ---------------------------------------------------------------------------

	private static final String VRE_ID = "CNW";
	private static final String[] TEI_EXTENSIONS = { "xml" };
	// private static final String ORGANIZATIONS = "CNW-organizations.xml";

	private final File inputDir;
	private Set<String> relationTypeNames;

	public CNWConverter(String inputDirName) throws Exception {
		super(VRE_ID);

		inputDir = new File(inputDirName);
		if (inputDir.isDirectory()) {
			System.out.printf("%nImporting from %s%n", inputDir.getCanonicalPath());
		} else {
			System.out.printf("%nNot a directory: %s%n", inputDir.getAbsolutePath());
		}
	}

	@Override
	public String getDescription() {
		return "Convert CNW INGforms data";
	}

	@Override
	public void call() throws Exception {
		try {
			openLog(getClass().getSimpleName() + ".txt");

			RelationTypeImporter importer = new RelationTypeImporter();
			importer.call(RelationTypeImporter.RELATION_TYPE_DEFS);
			relationTypeNames = importer.getNames();

			printBoxedText("Persons");
			convertPersons();

		} finally {
			displayErrorSummary();
			closeLog();
		}
	}

	private void convertPersons() throws Exception {
		Progress progress = new Progress();
		PrintWriter out = createPrintWriter(CNWPerson.class);
		try {
			Collection<File> files = FileUtils.listFiles(inputDir, TEI_EXTENSIONS, true);
			for (File file : Sets.newTreeSet(files)) {
				progress.step();
				CNWPerson person = convertPersonFromXML(file);
				jsonConverter.appendTo(out, person);
			}

		} finally {
			out.close();
			progress.done();
		}
	}

	// ---------------------------------------------------------------------------

	private CNWPerson convertPersonFromXML(File file) throws Exception {
		String fileName = file.getName();
		log(".. %s%n", fileName);
		String xml = Files.readTextFromFile(file);
		LOG.info("xml={}", xml);
		PersonContext personContext = new PersonContext();
		Visitor visitor = new PersonVisitor(personContext);
		Document.createFromXml(xml).accept(visitor);
		return personContext.person;
	}

}
