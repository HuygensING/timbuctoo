package nl.knaw.huygens.timbuctoo.tools.importer.cnw;

/*
 * #%L
 * Timbuctoo tools
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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

import nl.knaw.huygens.tei.DelegatingVisitor;
import nl.knaw.huygens.tei.Element;
import nl.knaw.huygens.tei.Traversal;
import nl.knaw.huygens.tei.handlers.DefaultElementHandler;
import nl.knaw.huygens.timbuctoo.tools.importer.CaptureHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListVisitor extends DelegatingVisitor<ListContext> {
	private static final Logger LOG = LoggerFactory.getLogger(ListVisitor.class);
	String currentKey = "";
	String currentValue = "";

	public ListVisitor(ListContext listContext) {
		super(listContext);
		addElementHandler(new ListKeyHandler(), "list_key");
		addElementHandler(new NameHandler(), "name");
		addElementHandler(new ItemHandler(), "item");
		addElementHandler(new ItemKeyHandler(), "item_key");
		addElementHandler(new ItemValueHandler(), "item_value");
	}

	private class ListKeyHandler extends CaptureHandler<ListContext> {
		@Override
		public void handleContent(Element element, ListContext context, String text) {
			context.setListKey(text);
		}
	}

	private class NameHandler extends CaptureHandler<ListContext> {
		@Override
		public void handleContent(Element element, ListContext context, String text) {
			context.setName(text);
		}
	}

	private class ItemHandler extends DefaultElementHandler<ListContext> {
		@Override
		public Traversal enterElement(Element element, ListContext context) {
			return Traversal.NEXT;
		}

		@Override
		public Traversal leaveElement(Element element, ListContext context) {
			context.getMap().put(currentKey, currentValue);
			currentKey = "";
			currentValue = "";
			return super.leaveElement(element, context);
		}

	}

	private class ItemKeyHandler extends CaptureHandler<ListContext> {
		@Override
		public void handleContent(Element element, ListContext context, String text) {
			currentKey = text;
		}
	}

	private class ItemValueHandler extends CaptureHandler<ListContext> {
		@Override
		public void handleContent(Element element, ListContext context, String text) {
			currentValue = text;
		}
	}

}
