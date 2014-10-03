package nl.knaw.huygens.timbuctoo.tools.importer.cnw;

import java.util.Map;

import nl.knaw.huygens.tei.XmlContext;

import com.google.common.collect.Maps;

public class ListContext extends XmlContext {

	private Map<String, String> map = Maps.newHashMap();
	private String name = "";
	private String listKey = "";

	public String getListKey() {
		return listKey;
	}

	protected Map<String, String> getMap() {
		return map;
	}

	protected String getName() {
		return name;
	}

	protected void setName(String name) {
		this.name = name;
	}

	public void setListKey(String listKey) {
		this.listKey = listKey;

	}
}
