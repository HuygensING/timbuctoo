package nl.knaw.huygens.timbuctoo.v5.serializable;

import java.io.IOException;

public class SerializableValue implements Serializable {
  private final Object value;
  private final String valueType;

  public SerializableValue(Object value, String valueType) {
    this.value = value;
    this.valueType = valueType;
  }

  @Override
  public void serialize(Serialization serialization) throws IOException {
    serialization.onRdfValue(value, valueType);
  }

  @Override
  public void generateToC(ResultToC siblingEntity) {
    siblingEntity.notifyValueField("value");
    siblingEntity.notifyValueField("type");
  }

}
