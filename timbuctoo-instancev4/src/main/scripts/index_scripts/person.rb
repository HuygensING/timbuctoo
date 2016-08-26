require 'open-uri'
class Person < Hash

    @@location = ""

    @@wanted_properties = [
      "_id",
      "@displayName",
      "types",
      "gender",
      "birthDate",
      "deathDate",
      "notes",
      "children"
  ]

    @@new_prop_names = [
      "id",
      "displayName_s",
      "types_ss",
      "gender_s",
      "birthDate_i",
      "deathDate_i",
      "notes_t",
      "children_s"
  ]

    @@wanted_relations = [
      "hasResidenceLocation",
      "hasBirthPlace",
      "hasDeathPlace",
      "hasMaritalStatus",
      "hasSocialClass",
      "hasEducation",
      "hasReligion",
      "hasProfession",
      "hasFinancialSituation",
      "isMemberOf"
    ]

    @@new_rel_names = [
      "relatedLocations_ss",
      "birthPlace_ss",
      "deathPlace_ss",
      "maritalStatus_ss",
      "socialClass_ss",
      "education_ss",
      "religion_ss",
      "profession_ss",
      "financialSituation_ss",
      "memberships_ss"
    ]

    def initialize data
      super
      self["type_s"] = "person"
      @@wanted_properties.each_with_index do |property,ind|
        if !data[property].nil?
          if (ind==4 || ind==5)
              parsedDate = 0
              if data[property].length > 4 and data[property].match(/.*([0-9]{4}).*/)
                pStr = data[property].sub(/.*([0-9]{4}).*/, '\1')
                parsedDate = pStr.to_i
              elsif data[property].match(/^-?[0-9]{4}$/) or data[property].match(/^-?[0-9]{3}$/) or data[property].match(/^-?[0-9]{2}$/) or data[property].match(/^-?[0-9]$/)
                parsedDate = data[property].to_i
              end
              self[@@new_prop_names[ind]] = parsedDate if parsedDate != 0
          else
              self[@@new_prop_names[ind]] = data[property]
          end
        end
      end
      self['modified_l'] = data['^modified']['timeStamp']
      self['modifiedBy_s'] = data['^modified']['username'] || data['^modified']['userId']

      if !data['names'].nil? and data['names'].length > 0
          self['name_t'] = build_name(data['names'])
          self['nameSort_s'] = build_name_sort(data['names'])
      else
          self['name_t'] = data['@displayName'].sub('[TEMP] ', '')
          self['nameSort_s'] =  data['@displayName'].sub('[TEMP] ', '')
      end
      build_relations data
      add_work_id_s data
    end

    def build_name names
      names.map{|name| name['components'].map{|component| component['value']}.join(" ")}.join(" ")
    end

    def build_name_sort names
      names[0]['components'].each do |component|
        return component['value'] if component['type'].eql?("SURNAME")
      end
      ''
    end

    def build_relations data
      @@new_rel_names.each_with_index do |rel,ind|
          self[rel] = Array.new
          if !data['@relations'].nil?
            if ind==0
                (0..2).each do |ind_2|
                  # adds all related locations because these are the first 3 entries in the @@wanted_relations array
                  # and the @@new_relations array
                  if !data['@relations'][@@wanted_relations[ind_2]].nil?
                      add_relation data,rel,ind_2
                  end
                end
            else
                add_relation data,rel,ind
            end
          end
          self[rel].uniq!
      end
    end

    def add_relation data,rel,ind
      if !data['@relations'][@@wanted_relations[ind]].nil?
          data['@relations'][@@wanted_relations[ind]].each do |rt|
            self[rel] << rt['displayName']  if rt['accepted']
          end
      end
    end

    def add_work_id_s data
      if !data['@relations'].nil? and !data["@relations"]["isCreatorOf"].nil?
        self["work_id_ss"] = data["@relations"]["isCreatorOf"].map {|work| work["id"]}
      else
        self["work_id_ss"] = Array.new
      end
    end

    def add_languages
      self["language_ss"] = Array.new
      self["work_id_ss"].each do |work_id|
        document = Documents.find work_id
        puts "problem! with finding document id #{work_id} in person id #{self["id"]}" if document.nil?
        self["language_ss"] += document["language_ss"] unless document.nil?
      end
      self["language_ss"].uniq!
      self["work_id_ss"] = Array.new
    end

    def id
      self['id']
    end

    def Person.location= location
      @@location = location
    end

    def Person.new_rel_names
      @@new_rel_names
    end
end

