package nl.knaw.huygens.timbuctoo.storage;

public class IdCreator {
  public static String create(String prefix, long number) {
    return String.format("%s%012d", prefix, number);
  }
}
