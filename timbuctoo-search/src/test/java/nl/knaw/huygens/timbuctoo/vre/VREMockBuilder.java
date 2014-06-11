package nl.knaw.huygens.timbuctoo.vre;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

public class VREMockBuilder {
  private String name;
  private Class<? extends DomainEntity>[] baseTypes;

  private VREMockBuilder() {}

  public static VREMockBuilder newVRE() {
    return new VREMockBuilder();
  }

  public VREMockBuilder withName(String name) {
    this.name = name;

    return this;
  }

  public VRE create() {
    VRE vreMock = mock(VRE.class);
    when(vreMock.getName()).thenReturn(name);

    return vreMock;
  }
}
