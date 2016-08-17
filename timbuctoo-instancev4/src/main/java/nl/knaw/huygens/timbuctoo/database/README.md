close transactions at the end of a request

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