require '../lib/timbuctoo_solr/timbuctoo_io'

# basic scrape
# timbuctoo_io = TimbuctooIO.new('http://test.repository.huygens.knaw.nl')
# timbuctoo_io.scrape_collection('dcararchives')

# Will dump scraped files (json) to specified :dump_dir
timbuctoo_io = TimbuctooIO.new('http://test.repository.huygens.knaw.nl', {
    :dump_files => true,
    :dump_dir => './'
})
timbuctoo_io.scrape_collection('dcararchives', {
    :with_relations => true, # also scrape direct relations
    :batch_size => 1000, # scrape in batches of 1000
    :from_file => true # scrape from local file dump in stead of Timbuctoo, if files are present
})