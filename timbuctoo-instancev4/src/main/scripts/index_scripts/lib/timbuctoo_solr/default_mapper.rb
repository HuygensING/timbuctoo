class DefaultMapper

  def initialize(properties: [], relations: [])
    @properties = properties
    @relations = relations
  end

  def convert(record)
    converted = {}
    @properties.each do |prop|
      input_value = locate_prop(prop[:name], record)
      converted_value = convert_value(input_value, prop[:type])
      converted[prop[:converted_name]] = converted_value unless converted_value.nil?
    end

    record_relations = record["@relations"]

    unless record_relations.nil?
      @relations.each do |relation|
        relation_names = relation[:relation_name].is_a?(String) ? [relation[:relation_name]] : relation[:relation_name]
        values = []
        relation_names.each do |relation_name|
          next if record_relations[relation_name].nil?
          values.concat(record_relations[relation_name]
               .map{|current_related_object| locate_prop(relation[:property_name], current_related_object) }
               .map{|current_value| convert_value(current_value, relation[:type]) })
        end
        converted[relation[:converted_name]] = values.uniq
      end
    end

    converted
  end

  def convert_to_int(value)
    return nil if value.nil?
    value.to_i
  end

  private
  def locate_prop(name, record)
    return record[name] if name.is_a? String
    value = nil
    name.each do |cur_name|
      value = value[cur_name] unless value.nil?
      value = record[cur_name] if value.nil?
    end
    value
  end

  def convert_value(input_value, type)
    return input_value if type.nil?

    method_name = "convert_to_#{type}"
    raise "Type '#{type}' not supported please define method #{method_name}" unless self.respond_to?(method_name)

    self.send(method_name, input_value)
  end
end