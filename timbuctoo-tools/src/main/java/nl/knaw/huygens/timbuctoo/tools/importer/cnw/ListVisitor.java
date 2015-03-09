package nl.knaw.huygens.timbuctoo.tools.importer.cnw;

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
