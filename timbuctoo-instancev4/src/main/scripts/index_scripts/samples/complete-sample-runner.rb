require '../lib/timbuctoo_solr/timbuctoo_io'
require '../lib/timbuctoo_solr/solr_io'
require '../lib/timbuctoo_solr/default_mapper'
require '../lib/mixins/converters/to_year_converter'
require '../lib/mixins/converters/to_names_converter'



@sample_default_config = {
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
}

@sample_custom_config = {
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
}



class WwPersonConverter < DefaultMapper
  include ToYearConverter
  include ToNamesConverter
end


class SampleRunner

  def initialize(sample_default_config, sample_custom_config)
    @default_batch = []
    @custom_batch = []
    @timbuctoo_io = TimbuctooIO.new('http://localhost:8089', {:dump_files => true, :dump_dir => '/home/rar011/tmp'})
    @default_mapper = DefaultMapper.new(sample_default_config)
    @custom_mapper = WwPersonConverter.new(sample_custom_config)
    @solr_io = SolrIO.new('http://localhost:8983/solr')
  end


  def process_record (record)
    @default_batch << @default_mapper.convert(record)
    @custom_batch << post_process_custom_mapping(record)

    if @custom_batch.length >= 100
      @solr_io.update('test_custom_index', @custom_batch)
      @custom_batch = []
    end

    if @default_batch.length >= 100
      @solr_io.update('test_default_index', @default_batch)
      @default_batch = []
    end

  end

  def post_process_custom_mapping(record)
    converted_by_custom_mapper = @custom_mapper.convert(record)
    if converted_by_custom_mapper['displayName_s'].empty?
      converted_by_custom_mapper['name_t'] = converted_by_custom_mapper['@displayName'].sub('[TEMP] ', '')
      converted_by_custom_mapper['nameSort_s'] = converted_by_custom_mapper['@displayName'].sub('[TEMP] ', '')
      converted_by_custom_mapper['displayName_s'] = converted_by_custom_mapper['@displayName'].sub('[TEMP] ', '')
    end
    converted_by_custom_mapper.delete('@displayName')
    converted_by_custom_mapper
  end

  def run
    @solr_io.delete_index("test_custom_index")
    @solr_io.delete_index("test_default_index")

    @solr_io.create("test_custom_index")
    @solr_io.create("test_default_index")

    @solr_io.delete_data("test_custom_index")
    @solr_io.delete_data("test_default_index")

    @timbuctoo_io.scrape_collection('wwpersons', {:process_record => method(:process_record), :with_relations => true, :from_file => false}) # fancy ruby shorthand
    #@timbuctoo_io.scrape_collection('wwpersons', {:process_record => -> (record) {process_record(record)}}) # short lambda syntax
    #@timbuctoo_io.scrape_collection('wwpersons', {:process_record => lambda {|record| process_record(record)}}) # lambda syntax

    @solr_io.update("test_custom_index", @custom_batch)
    @solr_io.update("test_default_index", @default_batch)
    @solr_io.commit("test_custom_index")
    @solr_io.commit("test_default_index")
  end
end

SampleRunner.new(@sample_default_config, @sample_custom_config).run


