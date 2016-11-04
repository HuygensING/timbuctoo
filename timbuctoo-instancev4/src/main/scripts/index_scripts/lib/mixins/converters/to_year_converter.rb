module ToYearConverter

  def convert_to_year (value)
    return nil if value.nil?
    parsed_date = 0
    if value.length > 4 and value.match(/.*([0-9]{4}).*/)
      parsed_str = value.sub(/.*([0-9]{4}).*/, '\1')
      parsed_date = parsed_str.to_i
    elsif value.match(/^-?[0-9]{4}$/) or value.match(/^-?[0-9]{3}$/) or value.match(/^-?[0-9]{2}$/) or value.match(/^-?[0-9]$/)
      parsed_date = value.to_i
    end

    return parsed_date if parsed_date != 0
    nil
  end

end