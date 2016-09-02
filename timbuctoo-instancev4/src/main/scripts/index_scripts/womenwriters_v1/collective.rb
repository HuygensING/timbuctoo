require 'open-uri'
class Collective < Hash

  @@location = ""

  @@wanted_properties = [
      "_id",
      "name",
      "type",
      "name"
  ]

  @@new_prop_names = [
      "id",
      "name_t",
      "type_s",
      "displayName_s"
  ]

  def initialize data
    super
    @@wanted_properties.each_with_index do |property,ind|
      if !data[property].nil?
        self[@@new_prop_names[ind]] = data[property]
      end
    end
  end

  def id
    self['id']
  end

  def Collective.location= location
    @@location = location
  end

end

