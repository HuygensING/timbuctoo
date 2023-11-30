package nl.knaw.huygens.timbuctoo.berkeleydb.isclean;

public interface IsCleanHandler<TKey, TValue> {
  TKey getKey();

  TValue getValue();
}
