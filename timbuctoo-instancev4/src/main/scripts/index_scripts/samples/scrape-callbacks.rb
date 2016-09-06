require 'json'

require '../lib/timbuctoo_solr/timbuctoo_io'
require '../lib/timbuctoo_solr/default_mapper'

timbuctoo_io = TimbuctooIO.new('http://test.repository.huygens.knaw.nl')


def process_record_callback(record)
  puts record.to_json
end

timbuctoo_io.scrape_collection('dcararchives', {
    :process_record => lambda {|record| process_record_callback(record) } # lambda syntax
})

timbuctoo_io.scrape_collection('dcararchives', {
    :process_record => -> (record) { process_record_callback(record) } # lambda shorthand syntax
})

timbuctoo_io.scrape_collection('dcararchives', {
    :process_record => method(:process_record_callback) # using lambda generator utility method 'method'
})


class RecordProcessor
  def process_record_callback(record)
    puts record.to_json
  end
end

record_processor = RecordProcessor.new
timbuctoo_io.scrape_collection('dcararchives', {
    :process_record => record_processor.method(:process_record_callback) # referencing a method in a different class instance
})