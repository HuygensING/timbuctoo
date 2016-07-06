class Person

    @@location = ""

    @@documents = Hash.new

    @@num_found_in_table = 0

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

    @@relation_types = [
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

    @@wanted_relations = [
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

    attr_reader :person

    def Person.location= location
	@@location = location
    end

    def Person.all_documents_size
	@@documents.size
    end

    def Person.num_found_in_table
	@@num_found_in_table
    end
    
    def initialize obj
	@person = Hash.new
	@person['type_s'] = "person"
	@@wanted_properties.each do |property|
	    if (property.eql?("birthDate") || property.eql?("deathDate")) && !obj[property].nil?
		@person[@@new_prop_names[@@wanted_properties.index(property)]] = obj[property].to_i
	    else
		@person[@@new_prop_names[@@wanted_properties.index(property)]] = obj[property]
	    end
	end
	@person['modified_l'] = obj['^modified']['timeStamp']
	if !obj['names'].nil?
	    @person['name_t'] = build_name obj['names']
	end

	build_relations obj

	creator_of = obj['@relations']['isCreatorOf']  if !obj['@relations'].nil?
	if !creator_of.nil? && !creator_of.empty?
	    languages = find_languages_in_works creator_of
	    @person['language_ss'] = languages
	end
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

    def build_relations old_person
	@@wanted_relations.each_with_index do |rel,ind|
	    @person[rel] = Array.new
	    if !old_person['@relations'].nil?
		if ind==0
		    (0..2).each do |ind_2|
			if !old_person['@relations'][@@relation_types[ind_2]].nil?
			    add_relation old_person,rel,ind_2
			end
		    end
		else
		    add_relation old_person,rel,ind
		end
	    end
	    @person[rel].uniq!
	end
    end

    def add_relation old_person,rel,ind
	if !old_person['@relations'][@@relation_types[ind]].nil?
	    old_person['@relations'][@@relation_types[ind]].each do |rt|
		@person[rel] << rt['displayName']  if rt['accepted']
	    end
	end
    end

    def find_languages_in_works creator_of
	languages = Array.new
	creator_of.each do |work|
	    if @@documents[work['path']].nil?
		f = open("#{@@location}#{work['path']}", {:read_timeout=>600})
		line = f.gets
		array = JSON.parse(line)
		@@documents[work['path']] = Document.new(array)
	    else
		@@num_found_in_table += 1
	    end
	    languages += @@documents[work['path']].languages
	end
	languages.uniq!
	return languages
    end

end

