require './lib/timbuctoo_solr/timbuctoo_io'
require './lib/timbuctoo_solr/default_mapper'
require './mixins/converters/to_year_converter'
require './mixins/converters/to_names_converter'

timbuctoo_io = TimbuctooIO.new('http://localhost:8089', {:dump_files => true, :dump_dir => '/home/rar011/tmp'})



@default_mapper = DefaultMapper.new({
  :properties => [
        { :name => '_id',  :converted_name => 'id'},
        { :name => '@displayName', :converted_name => 'displayName_s'},
        { :name => 'types', :converted_name => 'types_ss'},
        { :name => 'gender', :converted_name => 'gender_s'},
        { :name => 'birthDate', :converted_name => 'birthDate_i', :type => 'int'},
        { :name => 'deathDate', :converted_name => 'deathDate_i', :type => 'int'},
        { :name => 'notes', :converted_name => 'notes_t'},
        { :name => 'children', :converted_name => 'children_s'},
        { :name => ['^modified', 'timeStamp'], :converted_name => 'modified_l' }
    ], :relations => [
        { :relation_name => ['hasResidenceLocation', 'hasBirthPlace', 'hasDeathPlace'], :property_name => 'displayName', :converted_name => 'relatedLocations_ss' },
        { :relation_name => 'hasBirthPlace', :property_name => 'displayName', :converted_name => 'birthPlace_ss' },
        { :relation_name => 'hasDeathPlace', :property_name => 'displayName', :converted_name => 'deathPlace_ss' },
    ]
})

class WwPersonConverter < DefaultMapper
  include ToYearConverter
  include ToNamesConverter
end

@custom_mapper = WwPersonConverter.new({
     :properties => [
         { :name => '_id',  :converted_name => 'id'},
         { :name => 'names', :converted_name => 'name_t', :type => 'names'},
         { :name => 'names', :converted_name => 'nameSort_s', :type => 'name_sort'},
         { :name => 'names', :converted_name => 'displayName_s', :type => 'names_display_name'},
         { :name => 'types', :converted_name => 'types_ss'},
         { :name => 'gender', :converted_name => 'gender_s'},
         { :name => 'birthDate', :converted_name => 'birthDate_i', :type => 'year'},
         { :name => 'deathDate', :converted_name => 'deathDate_i', :type => 'year'},
         { :name => 'notes', :converted_name => 'notes_t'},
         { :name => 'children', :converted_name => 'children_s'},
         { :name => ['^modified', 'timeStamp'], :converted_name => 'modified_l' },
         { :name => '@displayName', :converted_name => '@displayName'}
     ], :relations => [
        { :relation_name => ['hasResidenceLocation', 'hasBirthPlace', 'hasDeathPlace'], :property_name => 'displayName', :converted_name => 'relatedLocations_ss' },
        { :relation_name => 'hasBirthPlace', :property_name => 'displayName', :converted_name => 'birthPlace_ss' },
        { :relation_name => 'hasDeathPlace', :property_name => 'displayName', :converted_name => 'deathPlace_ss' },
     ]
})

def dump_converted (record)
  converted_by_default_mapper = @default_mapper.convert(record)
  converted_by_custom_mapper = @custom_mapper.convert(record)
#  puts converted_by_default_mapper.inspect
  if converted_by_custom_mapper['displayName_s'].empty?
    converted_by_custom_mapper['name_t'] = converted_by_custom_mapper['@displayName'].sub('[TEMP] ', '')
    converted_by_custom_mapper['nameSort_s'] =  converted_by_custom_mapper['@displayName'].sub('[TEMP] ', '')
    converted_by_custom_mapper['displayName_s'] =  converted_by_custom_mapper['@displayName'].sub('[TEMP] ', '')
    puts converted_by_custom_mapper['@displayName']
  end
  converted_by_custom_mapper.delete('@displayName')
  puts converted_by_custom_mapper.inspect
  puts converted_by_default_mapper.inspect
end



timbuctoo_io.scrape_collection('wwpersons', {:process_record => method(:dump_converted), :with_relations => true, :from_file => true}) # fancy ruby shorthand


#timbuctoo_io.scrape_collection('wwpersons', {:process_record => -> (record) {process_record(record)}}) # short lambda syntax
#timbuctoo_io.scrape_collection('wwpersons', {:process_record => lambda {|record| process_record(record)}}) # lambda syntax
