package nl.knaw.huygens.timbuctoo.rdf;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.PersonName;
import nl.knaw.huygens.timbuctoo.model.PersonNameComponent;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

// FIXME: add uri property to PersonName
// According to René it is blocked by a failing (de)serialization in the JsonCrudService at this moment.
public final class UriBearingPersonNames {
  private static final List<PersonNameComponent.Type> NATURAL_ORDER = Lists.newArrayList(
    PersonNameComponent.Type.ROLE_NAME,
    PersonNameComponent.Type.FORENAME,
    PersonNameComponent.Type.NAME_LINK,
    PersonNameComponent.Type.SURNAME,
    PersonNameComponent.Type.GEN_NAME,
    PersonNameComponent.Type.ADD_NAME
  );

  // TODO make private
  public List<PersonName> list;
  // a map that contains the uri strings of of the PersonNames in list. The value corresponds with the position of
  // the PersonName in the list.
  // TODO make private
  public Map<String, Integer> nameUris;

  public UriBearingPersonNames() {
    list = Lists.newArrayList();
    nameUris = Maps.newHashMap();
  }

  @Override
  public String toString() {
    return "UriBearingPersonNames{" +
      "list=" + list +
      ", nameUris=" + nameUris +
      '}';
  }

  public UriBearingPersonNames addNameComponent(String nameUri, PersonNameComponent.Type nameType, String value) {
    PersonName personName;
    if (nameUris.containsKey(nameUri)) {
      personName = list.get(nameUris.get(nameUri));
    } else {
      personName = new PersonName();
      list.add(personName);
      nameUris.put(nameUri, list.indexOf(personName));
    }

    insertNameComponentAtNaturalPosition(nameType, value, personName);

    return this;
  }

  private void insertNameComponentAtNaturalPosition(PersonNameComponent.Type nameType, String value,
                                                    PersonName currentPersonName) {
    final List<PersonNameComponent> currentPersonNameComponents = currentPersonName.getComponents();
    int currentIndex = getNaturalComponentPos(nameType, currentPersonNameComponents);
    final PersonNameComponent newNameComponent = new PersonNameComponent(nameType, value);
    currentPersonNameComponents.add(currentIndex, newNameComponent);
  }

  private int getNaturalComponentPos(PersonNameComponent.Type nameType,
                                     List<PersonNameComponent> currentPersonNameComponents) {

    for (int currentIndex = 0; currentIndex < currentPersonNameComponents.size(); currentIndex++) {
      final int currentOrderIndex = NATURAL_ORDER.indexOf(currentPersonNameComponents.get(currentIndex).getType());
      final int newOrderIndex = NATURAL_ORDER.indexOf(nameType);
      if (currentOrderIndex > newOrderIndex) {
        return currentIndex;
      }
    }

    return currentPersonNameComponents.size();
  }

  public void removeComponent(String nameUri, PersonNameComponent.Type nameType, String value) {
    if (!nameUris.containsKey(nameUri)) {
      LoggerFactory.getLogger(UriBearingPersonNames.class).error("Uri '{}' not known", nameUri);
      return;
    }

    PersonName personName = list.get(nameUris.get(nameUri));
    personName.getComponents().remove(new PersonNameComponent(nameType, value));

    if (personName.getComponents().isEmpty()) {
      list.remove(personName);
      Integer indexOfRemoved = nameUris.remove(nameUri);

      // reindex the name uri's, because the list will do this automatically.
      Map<String, Integer> newNameUris = Maps.newHashMap();
      nameUris.forEach((key, val) -> {
        if (val > indexOfRemoved) {
          val--;
        }
        newNameUris.put(key, val);
      });

      nameUris = newNameUris;
    }
  }
}
