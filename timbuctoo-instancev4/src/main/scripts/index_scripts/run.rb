require './lib/solr/timbuctoo_io'
require './lib/solr/default_mapper'

timbuctoo_io = TimbuctooIO.new('http://localhost:8089', {:dump_files => true, :dump_dir => '/home/rar011/tmp'})

default_mapper = DefaultMapper.new({:properties => [
    { :name => '_id',  :converted_name => 'id', :type => 'string'},
    { :name => '@displayName', :converted_name => 'displayName_s', :type => 'string'},
    { :name => 'types', :converted_name => 'types_ss', :type => 'list'},
    { :name => 'gender', :converted_name => 'gender_s', :type => 'string'},
    { :name => 'birthDate', :converted_name => 'date_i', :type => 'int'},
    { :name => 'deathDate', :converted_name => 'date_i', :type => 'int'},
    { :name => 'notes', :converted_name => 'notes_t', :type => 'string'},
    { :name => 'children', :converted_name => 'children_s', :type => 'string'},
]})



timbuctoo_io.scrape_collection('wwpersons', {:process_record => default_mapper.method(:convert) }) # fancy ruby shorthand
#timbuctoo_io.scrape_collection('wwpersons', {:process_record => -> (record) {process_record(record)}}) # short lambda syntax
#timbuctoo_io.scrape_collection('wwpersons', {:process_record => lambda {|record| process_record(record)}}) # lambda syntax
