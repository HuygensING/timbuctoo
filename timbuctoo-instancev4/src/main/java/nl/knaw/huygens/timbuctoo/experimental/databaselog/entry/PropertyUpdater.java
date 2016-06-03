package nl.knaw.huygens.timbuctoo.experimental.databaselog.entry;

import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.DatabaseLog;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Property;

import java.util.Objects;
import java.util.Set;

class PropertyUpdater {
  private final Element element;
  private final Element prevElement;
  private final Set<String> newKeys;
  private final Set<String> oldKeys;

  public PropertyUpdater(Element element, Element prevElement) {
    this.element = element;
    this.prevElement = prevElement;
    this.newKeys = element.keys();
    this.oldKeys = prevElement.keys();
  }

  private void addNewProperties(DatabaseLog dbLog) {
    Set<String> newProperties = Sets.difference(newKeys, oldKeys);
    newProperties.forEach(key -> dbLog.newProperty(element.property(key)));
  }

  private void updateExistingProperties(DatabaseLog dbLog) {
    Set<String> existing = Sets.intersection(newKeys, oldKeys);
    existing.forEach(key -> {
      Property<Object> latestProperty = element.property(key);
      if (!Objects.equals(latestProperty.value(), prevElement.value(key))) {
        dbLog.updateProperty(latestProperty);
      }
    });
  }

  private void removeDeletedProperties(DatabaseLog dbLog) {
    Set<String> deletedProperties = Sets.difference(oldKeys, newKeys);
    deletedProperties.forEach(dbLog::deleteProperty);
  }

  public void updateProperties(DatabaseLog dbLog) {
    addNewProperties(dbLog);
    updateExistingProperties(dbLog);
    removeDeletedProperties(dbLog);
  }
}
