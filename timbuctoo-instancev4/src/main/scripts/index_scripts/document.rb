class Document

    @@location = ""

    @@wanted_properties = [
	"_id",
	"title",
	"date",
	"documentType",
	"notes",
    ]
    
    @@new_prop_names = [
	"id",
	"displayName_s",
	"date_i",
	"documentType_s",
	"notes_t"
    ]

    @@wanted_relations = [
	"hasPublishLocation",
	"hasWorkLanguage",
	"hasGenre",
	"hasDocumentSource"
    ]

    @@new_rel_names = [
	"publishLocation_ss",
	"language_ss",
	"genre_ss",
	"source_ss"
    ]

    attr_reader :document, :id

    def initialize data
	@document = Hash.new
	@document['type_s'] = "document"
	@@wanted_properties.each_with_index do |property,ind|
	    if !data[property].nil?
		if ind==2
		    @document[@@new_prop_names[ind]] = data[property].to_i
		else
		    @document[@@new_prop_names[ind]] = data[property]
		end
	    end
	end
	title_t = "#{data['title']} #{data['englishTitle']}".strip
	@document['title_t'] = title_t  if !title_t.empty?

	@document['modified_l'] = data['^modified']['timeStamp']

	build_relations data

	@document['_childDocuments_'] = Array.new
	add_creators data
    end

    def build_relations data
	@@new_rel_names.each_with_index do |rel,ind|
	    if !data['@relations'].nil?
		@document[rel] = Array.new
		add_relation data,rel,ind
	    end
	end
    end

    def add_relation data,rel,ind
	if !data['@relations'][@@wanted_relations[ind]].nil?
	    data['@relations'][@@wanted_relations[ind]].each do |rt|
		@document[rel] << rt['displayName']  if rt['accepted']
	    end
	end
    end

    def add_creators data
	if !data['@relations'].nil?
	    is_created_by = data['@relations']['isCreatedBy']
	end
	return if is_created_by.nil?
	is_created_by.each do |creator|
	    person = Person.find creator['path']
	    if !person.nil?
		person['id'] = "#{id}/#{person['id']}"
		@document['_childDocuments_'] << person.person
	    end
	end
    end

    def [] parameter
	@document[parameter]
    end

    def id
	@document['id']
    end

    def Document.location= location
	@@location = location
    end

end

