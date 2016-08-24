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
 * ~~no two transaction may run at the same time~~ will be possible once no code uses graphWrapper directly anymore
 * Each method returns an Immutable Value object without Tinkerpop or Neo4j references
 * A value object will never hit the database to lazy load more data. 
   If needed more variants are provided to handle the differen clients needs
 * A method is quite tightly coupled to the client. While a method may be used by more then one client is has a very tightly defined implementation and re-use is not expected to be the rule.
 * of course, internally methods may re-use each other freely.
 * A mutation is a separate method adhering to the same practices as a retrieval method.
 * You might have a mutation method that also retrieves data, but this is not the norm.
 * You may implement the interface DataAccess methods and register your implementation with DataAccess to have your class run in an experiment.
 * The methods will only get or mutate state. They will not trigger or calculate state (i.e. they will not generate handle id's etc, they might require a parameter containing a handle id though)

 * The dataAccess class will perform authorization.
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