package nl.knaw.huygens.repository.pubsub;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;

@Singleton
public class Hub {
  private Map<Class<?>, List<Subscriber>> subscribers = Maps.newHashMap();

  public void subscribe(Object subscriber) {
    Class<?> subscriberClass = subscriber.getClass();
    Method[] methods = subscriberClass.getMethods();
    for (Method m : methods) {
      if (m.getAnnotation(Subscribe.class) != null) {
        Class<?>[] parameterTypes = m.getParameterTypes();
        if (parameterTypes.length != 1) {
          throw new RuntimeException("Method " + m.getName() + " takes more than 1 paramter!");
        }
        Class<?> eventClass = parameterTypes[0];
        if (!subscribers.containsKey(eventClass)) {
          subscribers.put(eventClass, Lists.<Subscriber>newArrayList());
        }
        List<Subscriber> subscriberList = subscribers.get(eventClass);
        subscriberList.add(new Subscriber(subscriber, m.getName(), eventClass));
      }
    }
  }

  public boolean unsubscribe(Object unsubscriber) {
    Class<?> unsubscriberClass = unsubscriber.getClass();
    Method[] methods = unsubscriberClass.getMethods();
    boolean found = false;
    for (Method m : methods) {
      if (m.getAnnotation(Subscribe.class) != null) {
        Class<?>[] parameterTypes = m.getParameterTypes();
        if (parameterTypes.length != 1) {
          continue;
        }
        Class<?> eventClass = parameterTypes[0];
        if (!subscribers.containsKey(eventClass)) {
          continue;
        }
        List<Subscriber> subscriberList = subscribers.get(eventClass);
        found = subscriberList.remove(new Subscriber(unsubscriber, m.getName(), eventClass)) || found;
      }
    }
    return found;
  }

  public boolean safePublish(Object event) {
    try {
      return publish(event);
    } catch (Exception ex) {
      ex.printStackTrace();
      return false;
    }
  }

  public boolean publish(Object event) throws Exception {
    Class<?> eventClass = event.getClass();
    boolean published = false;
    Exception storedException = null;
    while (eventClass != null) {
      if (subscribers.containsKey(eventClass)) {
        List<Subscriber> subs = subscribers.get(eventClass);
        if (subs != null && !subs.isEmpty()) {
          for (Subscriber sub : subs) {
            try {
              sub.publish(event);
            } catch (Exception ex) {
              if (storedException == null) {
                storedException = ex;
              }
            }
          }
        }
      }
      eventClass = eventClass.getSuperclass();
    }
    if (storedException != null) {
      throw new Exception(storedException);
    }
    return published;
  }
}
