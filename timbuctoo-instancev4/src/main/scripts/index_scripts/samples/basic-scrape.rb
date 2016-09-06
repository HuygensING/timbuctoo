require '../lib/timbuctoo_solr/timbuctoo_io'

timbuctoo_io = TimbuctooIO.new('http://test.repository.huygens.knaw.nl')
timbuctoo_io.scrape_collection('dcararchives')