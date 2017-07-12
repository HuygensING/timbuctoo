package nl.knaw.huygens.timbuctoo.v5.serializable;

import java.io.IOException;

public class SerializableUntypedValue implements Serializable {
  private final Object value;

  public SerializableUntypedValue(Object value) {
    this.value = value;
  }

  @Override
  public void serialize(Serialization serialization) throws IOException {
    serialization.onValue(value);
  }

  @Override
  public void generateToC(ResultToC siblingEntity) {
    siblingEntity.notifyValueField("value");
  }
}
