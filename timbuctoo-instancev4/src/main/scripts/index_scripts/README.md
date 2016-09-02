#Solr index script

This folder contains scripts that scrape timbuctoo and send the data to solr.


##Open issues

### 1. No control over text indexes
Our current solr index is the default data driven index. 
This one does not handle accented words very well. 
We also have no control over how capitalization is handled.
We're pretty sure that our users need us to handle these cases in a way that differs on a project by project basis.

#### Development steps
- Run a local solr 6 instance (docker or directly under windows, you'll need to be able to access the files)
- create the index `wwpersons_accent_research` on your local solr
- Fill this index with the fulltext records (i.e. `*_t`) uit wwpersons
- Configure it so that 
 1. a search for `Bronte` and a search fot `Brontë` both return one instance for each of the three Brontë sisters
 2. a search for `de*` return both "Descartes" and "Eugénie Avril de Sainte Croix". A Search for `De*` returns only Descartes
 3. Find out how we could make the index fully case-sensitive
- How can we make this approach work for all `*_t` fields?
- How can we make this approach work for only a specific field?
