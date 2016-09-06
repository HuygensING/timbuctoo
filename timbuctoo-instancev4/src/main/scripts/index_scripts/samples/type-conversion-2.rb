require '../lib/timbuctoo_solr/timbuctoo_io'
require '../lib/timbuctoo_solr/default_mapper'

require '../lib/mixins/converters/to_names_converter'
require '../lib/mixins/converters/to_year_converter'

class PersonMapper < DefaultMapper
  include ToNamesConverter
  include ToYearConverter
end

@person_mapper = PersonMapper.new({
  :properties => [
    { :name => '_id', :converted_name => 'id' },
    { :name => 'birthDate', :converted_name => 'birthDate_i', :type => 'year' },
    { :name => 'names', :converted_name => 'name_t', :type => 'names'},
    { :name => 'names', :converted_name => 'nameSort_s', :type => 'name_sort'},
    { :name => 'names', :converted_name => 'displayName_s', :type => 'names_display_name'},
  ]
})

def process(record)
  p @person_mapper.convert(record)
end

timbuctoo_io = TimbuctooIO.new('http://test.repository.huygens.knaw.nl')

timbuctoo_io.scrape_collection('wwpersons', :process_record => method(:process))