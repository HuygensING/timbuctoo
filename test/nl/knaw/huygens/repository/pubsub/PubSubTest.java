package nl.knaw.huygens.repository.pubsub;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class PubSubTest {

  private Hub hub;

  private class FooEvent {

  }

  private class BarEvent extends FooEvent {

  }

  private static class BogusSub {
    @Subscribe
    @SuppressWarnings("unused")
    public void onFoo(Object a, Object b) {
      // Nothing.
    }
  }

  private static class Foo {
    private List<FooEvent> events = Lists.newArrayList();
    private final boolean doThrow;
    public Foo(boolean doThrow) {
      this.doThrow = doThrow;
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onFooEvent(FooEvent x) {
      events.add(x);
      if (doThrow) {
        throw new RuntimeException("Blah");
      }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onBarEvent(BarEvent y) {
      events.add(y);
      if (doThrow) {
        throw new RuntimeException("Blah");
      }
    }

    public boolean verify(FooEvent x) {
      return verify(Lists.newArrayList(x));
    }

    public boolean verify(List<? extends FooEvent> xs) {
      return xs.equals(events);
    }
  }

  private Foo mockedFoo = new Foo(false);
  private Foo mockedFooThrower = new Foo(true);

  @Before
  public void setUp() throws Exception {
    hub = new Hub();
  }

  @Test
  public void test() {
    hub.subscribe(mockedFoo);
    FooEvent event = new FooEvent();
    try {
      hub.publish(event);
    } catch (Exception ex) {
      fail("Shouldn't throw");
    }
    assertTrue("Should have been called", mockedFoo.verify(event));

    assertTrue("Should let you unsubscribe", hub.unsubscribe(mockedFoo));
    try {
      hub.publish(event);
    } catch (Exception ex) {
      fail("Shouldn't throw");
    }
    assertTrue("Should have been called only once.", mockedFoo.verify(event));

    assertFalse("Should notify you the unsubscribe was useless", hub.unsubscribe(mockedFoo));

  }


  @Test
  public void testException() {
    hub.subscribe(mockedFooThrower);
    FooEvent event = new FooEvent();
    try {
      hub.publish(event);
    } catch (Exception ex) {
      return;
    }
    fail("Should have thrown.");
  }

  @Test
  public void testSafeVersion() {
    hub.subscribe(mockedFoo);
    FooEvent event = new FooEvent();
    hub.safePublish(event);
    assertTrue("Should have been called", mockedFoo.verify(event));

    hub.subscribe(mockedFooThrower);
    hub.safePublish(event);
    assertTrue("Should have been called", mockedFooThrower.verify(event));
    assertTrue("Should have called the old one, too",
               mockedFoo.verify(Lists.newArrayList(event, event)));
  }

  @Test
  public void testMultipleSubscribersPerClassAndInheritance() {
    hub.subscribe(mockedFoo);
    BarEvent event = new BarEvent();
    hub.safePublish(event);
    assertTrue("Should have been called twice (once for BarEvent, once for FooEvent)", mockedFoo.verify(Lists.newArrayList(event, event)));
  }

  @Test
  public void testMultipleExceptions() {
    hub.subscribe(mockedFooThrower);
    BarEvent event = new BarEvent();
    hub.safePublish(event);
    assertTrue("Should have been called twice (once for BarEvent, once for FooEvent, despite 2 exceptions)", mockedFooThrower.verify(Lists.newArrayList(event, event)));
  }

  @Test
  public void testErrorConditions() {
    hub.subscribe(hub);
    assertTrue("Should not have errored for subscribing item with no annotations", true);
    hub.subscribe(mockedFoo);
    assertFalse("Should return false for event without subscribers", hub.safePublish(new Object()));
    boolean threw = false;
    try {
      hub.subscribe(new BogusSub());
    } catch (RuntimeException ex) {
      threw = true;
    }
    assertTrue("Should throw for classes with annotation on strange method.", threw);

    assertFalse("Shouldn't know bogus object", hub.unsubscribe(new BogusSub()));

    assertFalse("Shouldn't know unknown object", hub.unsubscribe(new Object()));

    assertFalse("Shouldn't know unknown object", hub.unsubscribe(mockedFooThrower));
    assertTrue("Should deal with unsubscribing bogus / unknown objects OK.", true);
  }

}
