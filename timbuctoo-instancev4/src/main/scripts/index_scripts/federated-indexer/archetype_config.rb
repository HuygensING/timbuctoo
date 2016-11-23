class ArchetypeConfig
  attr_reader :name

  # Contains postfix and converter types per timbuctoo data type
  def ArchetypeConfig.conversion_configs
    {
      :text => [{ :postfix => "_t" }],
      :select => [{ :postfix => "_s" }],
      :multiselect => [{ :postfix => "_ss", }],
      :names => [{ :postfix => "_s", :converter_type => "names" }],
      :datable => [
        { :postfix => "_i", :converter_type => "year" },
        { :postfix => "_s" }
      ],
      :links => [{ :postfix => "_t",  :converter_type => "links" }],
      "list-of-strings".to_sym => [{ :postfix => "_ss"} ]
    }
  end

  def initialize(name, properties)
    @name = name
    @properties = properties
  end



  def make_property_configs
    property_configs = [
      # unique id collectionName/tim_id
      {:name => "_id", :converted_name => "id", :type => "collection_bound_id"},
      {:name => '_id', :converted_name => 'uuid_s'},
      {:name => "^rdfUri", :converted_name => 'rdfUri_s'},
      {:name => "^rdfAlternatives", :converted_name => 'rdfAlternatives_ss'},
      {:name => '@displayName', :converted_name => 'displayName_s'},
      {:name => '@displayName', :converted_name => 'displayName_t'},
    ]

    # looks up the correct solr postfix and converter in conversions_configs
    @properties.reject { |prop| prop[:type].eql?("relation") }.each do |prop|
      begin
        ArchetypeConfig.conversion_configs[prop[:type].to_sym].each do |conf|
          property_configs << {
              :name => prop[:name],
              :converted_name => "#{prop[:name]}#{conf[:postfix]}",
              :type => conf.key?(:converter_type) ? conf[:converter_type] : nil
          }
        end
      rescue
        raise "failure for #{prop[:type]}"
      end
    end

    property_configs
  end

  def get
    {
        :properties => make_property_configs,
        :relations => @properties
                          .select { |prop| prop[:type].eql?("relation") }
                          .map { |rel| {
            :relation_name => rel[:name],
            :property_name => 'displayName',
            :converted_name => "#{rel[:name]}_ss"
        }
        }
    }
  end
end