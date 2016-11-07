class ArchetypeConfig
  attr_reader :name

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
      :links => [{ :postfix => "_t",  :converter_type => "links" }]
    }
  end

  def initialize(name, properties)
    @name = name
    @properties = properties
  end

  def resolve_type_mapping (prop)
    {:raw_type => prop[:type]}
  end

  def resolve_property_configs
    property_configs = [
      {:name => "_id", :converted_name => "id", :type => "collection_bound_id"},
      {:name => '_id', :converted_name => 'uuid_s'},
      {:name => "^rdfUri", :converted_name => 'rdfUri_s'},
      {:name => '@displayName', :converted_name => 'displayName_s'},
      {:name => '@displayName', :converted_name => 'displayName_t'},
    ]
    @properties.reject { |prop| prop[:type].eql?("relation") }.each do |prop|
      ArchetypeConfig.conversion_configs[prop[:type].to_sym].each do |conf|
        property_configs << {
            :name => prop[:name],
            :converted_name => "#{prop[:name]}#{conf[:postfix]}",
            :type => conf.key?(:converter_type) ? conf[:converter_type] : nil
        }
      end
    end

    property_configs
  end

  def get
    {
        :properties => resolve_property_configs,
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