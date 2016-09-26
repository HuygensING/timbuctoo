package nl.knaw.huygens.timbuctoo.database;

import java.util.function.Function;

public interface DbCreateEntity extends Function<DataAccessMethods, TransactionStateAndResult<TransactionState>> {

}
