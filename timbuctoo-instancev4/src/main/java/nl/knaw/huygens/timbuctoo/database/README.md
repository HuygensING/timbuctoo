#DataAccess
> Wrapper around data access methods

##Context
We are using a database, but we're not convinced that it's the best one.
We would like to be able to switch to other databases and the *evaluate* whether other databases make more sense.

Furthermore Tinkerpop requires us to handle transactions thoughtfully but provides a rather lax API.
We'd like to access the database only in a correct manner.

##Goal
DataAccess will solve both problems by becoming the only way to access the database and by guaranteeing the following attributes.

 * The methods are only accessible while inside an AutoCloseable wrapper that manages the transaction. 
   (So that the client code is forced to think about transaction lifetime)
 * ~~no two transaction may run at the same time~~ (will be possible once no code uses graphWrapper directly anymore)
 * Each method returns an Immutable Value object without Tinkerpop or Neo4j references. 
   (So that timbuctoo is less dependent on one database implementation)
 * A value object will never hit the database to lazy load more data.
   (so that the client code can more easily reason about performance. Only db.something() calls will hit the database)
 * A method is quite tightly coupled to the client. While a method may be used by more then one client is has a very tightly defined implementation and re-use is not expected to be the rule. 
   (So that removing some client code will also clearly remove their constraints on the data model)
    * of course, internally methods may re-use each other freely (so that we do not have code duplication)
 * A mutation is a separate method adhering to the same practices as a retrieval method. 
   (a second requirement to make only db.something() calls hit the database)
 * You might have a mutation method that also retrieves data, but this is not the norm.
   (explicitly mentioned for people who expect full Command Query Separation) 
 * The dataAccess methods will only get or mutate state. They will not trigger or calculate state (i.e. they will not generate handle id's etc, they might require a parameter containing a handle id though)
   (This prevents scope creep because everything could be in dataAccess. It also makes the dataAccess methods easier to test)
     * But: The dataAccess class will perform authorization.
       (This prevents code duplication, and prevents security bugs)
     * But: The dataAccess class will generate the unique ID's
       (because otherwise it would still need to verify uniqueness and throw exceptions which would make the code needlessly complex)
 * ~~You may add a custom DataAccess implementation which only implements a few methods that will then be run in an experiment. So you can try out a new database without having to write a full implementation~~ might be added once we have one full implementation. 
 * initially there is no interface and only one implementation to facilitate easier refactoring

#TransactionFilter
> closes transactions at the end of a request

This is a stopgap method that should prevent the problems that will be really fixed by the DataAccess class.

 1. Dropwizard re-uses threads between requests.
 2. neo4j needs a transaction for read actions
 3. tinkerpop automatically creates a thread local transaction when 
    needed. (also for read actions)
 4. we did not know so we don't close read transactions at the end of 
    a read action

Together this results in a thread that will not see the changes of 
another thread. If it had a transaction open since before the other 
thread had a transaction open. 

I do not completely understand it though, because an open transaction
should be able to see committed data from another transaction (and I 
have verified that the data was indeed committed). However this filter 
does fix the bug.