module ToNamesConverter

  def convert_to_names (names)
    names.map{|name| name['components'].map{|component| component['value']}.join(" ")}.join(" ")
  end

  def convert_to_name_sort (names)
    return '' if names.length == 0
    names[0]['components'].each do |component|
      return component['value'] if component['type'].eql?("SURNAME")
    end
    ''
  end

  def convert_to_names_display_name (names)
     names.map{|name| name['components'].map{|component| component['value']}.join(" ")}.join(", ")
  end
end