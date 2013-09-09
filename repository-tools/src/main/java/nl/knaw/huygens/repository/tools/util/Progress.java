package nl.knaw.huygens.repository.tools.util;

/**
 * Displays progress on console.
 */
public class Progress {

  private int count;

  public Progress() {
    count = 0;
  }

  public void step() {
    if (count % 10 == 0) {
      if (count % 1000 == 0) {
        System.out.printf("%n%05d ", count);
      }
      System.out.print(".");
    }
    count++;
  }

  public void done() {
    System.out.printf("%n%05d%n", count);
  }

}
