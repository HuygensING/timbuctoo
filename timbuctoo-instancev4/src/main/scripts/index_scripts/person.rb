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
              self[@@new_prop_names[ind]] = data[property].to_i
          else
              self[@@new_prop_names[ind]] = data[property]
          end
        end
      end
      self['modified_l'] = data['^modified']['timeStamp']
      if !data['names'].nil?
          self['name_t'] = build_name(data['names'])
      end

      build_relations data

      add_languages data
    end

    def build_name names
      new_names = Array.new
      names.each do |name|
          build_name = Hash.new
          name['components'].each do |component|
            if build_name[component['type']].nil?
                build_name[component['type']] = component['value']
            else
                build_name[component['type']] << " #{component['value']}"
            end
          end
          forename = build_name['FORENAME']
          gen_name = build_name['GEN_NAME']
          surname = build_name['SURNAME']
          add_name = build_name['ADD_NAME']
          role_name = build_name['ROLE_NAME']
          name_link = build_name['NAME_LINK']

          complete_name = "#{role_name} #{forename} #{gen_name} #{name_link} #{surname} #{add_name}"
          complete_name.strip!
          complete_name.gsub!(/  +/," ")
          new_names << complete_name
      end

      return new_names.join(" ")
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

    def add_languages data
=begin
      if !data['@relations'].nil? and !data["@relations"]["isCreatorOf"].nil?
          data["@relations"]["isCreatorOf"].each do |work|
            f = open("#{@@location}domain/wwdocuments/#{work["id"]}", {:read_timeout=>600})
            line = f.gets
            doc_data = JSON.parse(line)
            if !doc_data["@relations"].nil? and !doc_data["@relations"]["hasWorkLanguage"].nil?
              self["language_ss"] = doc_data["@relations"]["hasWorkLanguage"].map{|lang| lang["displayName"]}
            end
          end
      end
=end
    end

    def id
      self['id']
    end

    def Person.location= location
      @@location = location
    end

end

