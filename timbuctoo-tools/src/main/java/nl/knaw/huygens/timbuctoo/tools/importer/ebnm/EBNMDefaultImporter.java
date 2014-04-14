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

import java.io.IOException;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageManager;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.tools.importer.DefaultImporter;
import nl.knaw.huygens.timbuctoo.tools.importer.RelationTypeImporter;

/**
 * A class that contains the base functionality used in both the
 * {@code DutchCaribbeanImporter} as the now removed
 * {@code AtlantischeGidsImporter}. Those importers use(d) the data from the
 * AtlantischeGids project.
 */
public abstract class EBNMDefaultImporter extends DefaultImporter {

	/**
	 * File with {@code RelationType} definitions; must be present on classpath.
	 */
	private static final String RELATION_TYPE_DEFS = "relationtype-defs-codl.txt";

	private String prevMessage;
	private int errors;

	public EBNMDefaultImporter(TypeRegistry registry,
			StorageManager storageManager, IndexManager indexManager) {
		super(registry, storageManager, indexManager);
		prevMessage = "";
		errors = 0;
		setup(storageManager);
	}

	protected void setup(StorageManager storageManager) {
		try {
			new RelationTypeImporter(typeRegistry, storageManager).importRelationTypes(RELATION_TYPE_DEFS);
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// --- error handling ------------------------------------------------

	protected void handleError(String format, Object... args) {
		errors++;
		String message = String.format(format, args);
		if (!message.equals(prevMessage)) {
			System.out.print("## ");
			System.out.printf(message);
			System.out.println();
			prevMessage = message;
		}
	}

	protected void displayErrorSummary() {
		if (errors > 0) {
			System.out.printf("%n## Error count = %d%n", errors);
		}
	}

	// --- storage -------------------------------------------------------

	protected <T extends Entity> T getEntity(Class<T> type, String id) {
		return storageManager.getEntity(type, id);
	}

	protected <T extends DomainEntity> String addDomainEntity(Class<T> type,
			T entity) {
		try {
			try {
				storageManager.addDomainEntity(type, entity, new Change());
			} catch (ValidationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return entity.getId();
		} catch (IOException e) {
			handleError("Failed to add %s; %s", entity.getDisplayName(),
					e.getMessage());
			return null;
		}
	}

	protected <T extends DomainEntity> T updateDomainEntity(Class<T> type,
			T entity) {
		try {
			storageManager
					.updateProjectDomainEntity(type, entity, new Change());
			return entity;
		} catch (IOException e) {
			handleError("Failed to modify %s; %s", entity.getDisplayName(),
					e.getMessage());
			return null;
		}
	}

	// --- indexing ------------------------------------------------------

	protected <T extends DomainEntity> void indexEntities(Class<T> type)
			throws IndexException {
		System.out.println(".. " + type.getSimpleName());
		StorageIterator<T> iterator = null;
		try {
			iterator = storageManager.getAll(type);
			while (iterator.hasNext()) {
				T entity = iterator.next();
				indexManager.addEntity(type, entity.getId());
			}
		} finally {
			if (iterator != null) {
				iterator.close();
			}
		}
	}

	// -------------------------------------------------------------------

	/**
	 * Displays the status of the Mongo database and the Solr indexes.
	 */
	protected void displayStatus() throws IndexException {
		// Make sure the Solr indexes are up-to-date
		indexManager.commitAll();

		System.out.println();
		System.out.println(storageManager.getStatus());
		System.out.println(indexManager.getStatus());
	}

}
