package test.model;

public class ObjectWithModifiers {

  private String plainString;
  private static String staticString;
  private transient String transientString;
  private volatile String volatileString;

  public String getPlainString() {
    return plainString;
  }

  public void setPlainString(String plainString) {
    this.plainString = plainString;
  }

  public static String getStaticString() {
    return staticString;
  }

  public static void setStaticString(String staticString) {
    ObjectWithModifiers.staticString = staticString;
  }

  public String getTransientString() {
    return transientString;
  }

  public void setTransientString(String transientString) {
    this.transientString = transientString;
  }

  public String getVolatileString() {
    return volatileString;
  }

  public void setVolatileString(String volatileString) {
    this.volatileString = volatileString;
  }

}
