package nl.knaw.huygens.timbuctoo.database;

import java.util.function.Function;

public interface DbUpdateEntity extends Function<DataAccessMethods, TransactionStateAndResult<UpdateReturnMessage>> {
}
