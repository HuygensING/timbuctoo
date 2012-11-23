package nl.knaw.huygens.repository.pubsub;

import java.lang.reflect.Method;

public class Subscriber {
  private Object subObj;
  private String methodName = null;
  private Class<?> cls;
  private Class<?> eventClass;

  public Subscriber(Object subObj, String methodName, Class<?> eventClass) {
    super();
    if (subObj == null) {
      throw new RuntimeException("Null object passed to subscriber constructor");
    }
    this.subObj = subObj;
    this.methodName = methodName;
    this.eventClass = eventClass;
    cls = subObj.getClass();
  }

  public void publish(Object event) throws Exception {
    Method m = null;
    try {
      m = cls.getMethod(methodName, eventClass);
    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
    if (m == null) {
      return;
    }
    try {
      m.invoke(subObj, event);
    } catch (Exception ex) {
      throw ex;
    }
  }

  @Override
  public boolean equals(Object obj) {
    return obj != null &&
           obj instanceof Subscriber &&
           subObj.equals(((Subscriber) obj).subObj);
  }
}
