package nl.knaw.huygens.timbuctoo.remote.rs.discover;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created on 2016-09-24 17:48.
 */
public class Tester {

  public static void main(String[] args) throws Exception {
    Map<String, Integer> prices1 = new HashMap<>();
    prices1.put("oranges", 3);
    prices1.put("bananas", 4);
    prices1.put("apples", 5);

    Map<String, Integer> prices2 = new HashMap<>();
    prices2.put("oranges", 8);
    prices2.put("bananas", 4);
    prices2.put("mangoes", 5);

    /*Map<String, Integer> mx = Stream.of(m1, m2)
      .map(Map::entrySet)          // converts each map into an entry set
      .flatMap(Collection::stream) // converts each set into an entry stream, then
      // "concatenates" it in place of the original set
      .collect(
        Collectors.toMap(        // collects into a map
          Map.Entry::getKey,   // where each entry is based
          Map.Entry::getValue, // on the entries in the stream
          Integer::max         // such that if a value already exist for
          // a given key, the max of the old
          // and new value is taken
        )
      )
      ;*/

    Map<String, Integer> pricesx = Stream.of(prices1, prices2)
      .map(Map::entrySet)
      .flatMap(Collection::stream)
      .collect(
        Collectors.toMap(
          Map.Entry::getKey,
          Map.Entry::getValue,
          Integer::max
        )
      );

    System.out.println(">> Prices1 map: " + prices1);
    System.out.println(">> Prices2 map: " + prices2);

    System.out.println(">> Pricesx map: " + pricesx);

  }

  private static Callable<String> callable(String result, long sleepSeconds) {
    return () -> {
      TimeUnit.SECONDS.sleep(sleepSeconds);
      return result;
    };
  }

  private static void test4() throws InterruptedException {
    ExecutorService executor = Executors.newWorkStealingPool();

    List<Callable<String>> callables = Arrays.asList(
      callable("task1", 5),
      callable("task2", 1),
      callable("task3", 8));

    executor.invokeAll(callables)
      .stream()
      .map(future -> {
        try {
          return future.get();
        } catch (Exception e) {
          throw new IllegalStateException(e);
        }
      })
      .forEach(System.out::println);

    executor.shutdown();
  }

}
