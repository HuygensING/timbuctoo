class DefaultMapper

  def initialize(properties: [{:name => '_id', :converted_name => 'id', :type => 'id'}], relations: [])
    @properties = properties
    @relations = relations
  end

  def convert(record)
    converted = {}
    @properties.each do |prop|
      method_name = "convert_to_#{prop[:type]}"
      raise "Type '#{prop[:type]}' not supported" unless self.respond_to?(method_name)
      converted[prop[:converted_name]] = self.send(method_name, record[prop[:name]])
    end

    @relations.each do |relation|

    end

    puts converted.inspect
    converted
  end

  def convert_to_string(value)
    value
  end

  def convert_to_int(value)
    return nil if value.nil?
    value.to_i
  end

  def convert_to_list(value)
    value
  end
end