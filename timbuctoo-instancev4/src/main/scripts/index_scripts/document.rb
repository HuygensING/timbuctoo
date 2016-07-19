class Document < Hash

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

    attr_reader :id

    def initialize data
	super
	self['type_s'] = "document"
	@@wanted_properties.each_with_index do |property,ind|
	    if !data[property].nil?
		if ind==2
		    self[@@new_prop_names[ind]] = data[property].to_i
		else
		    self[@@new_prop_names[ind]] = data[property]
		end
	    end
	end
	title_t = "#{data['title']} #{data['englishTitle']}".strip
	self['title_t'] = title_t  if !title_t.empty?

	self['modified_l'] = data['^modified']['timeStamp']

	build_relations data

	self['_childDocuments_'] = Array.new
	add_creators data
    end

    def build_relations data
	@@new_rel_names.each_with_index do |rel,ind|
	    if !data['@relations'].nil?
		self[rel] = Array.new
		add_relation data,rel,ind
	    end
	end
    end

    def add_relation data,rel,ind
	if !data['@relations'][@@wanted_relations[ind]].nil?
	    data['@relations'][@@wanted_relations[ind]].each do |rt|
		self[rel] << rt['displayName']  if rt['accepted']
	    end
	end
    end

    def add_creators data
	if !data['@relations'].nil?
	    is_created_by = data['@relations']['isCreatedBy']
	end
	return if is_created_by.nil?
	is_created_by.each do |creator|
	    person = Persons.find creator['id']
	    if !person.nil?
		# avoid building id's like : "id/id/id/..."
		new_person = person.dup
		new_person['id'] = "#{id}/#{person.id}"
		old_keys = new_person.keys
		old_keys.delete('id')
		old_keys.each do |old_key|
		    new_key = "person_#{old_key}"
		    new_person[new_key] = new_person.delete(old_key)
		end
		self['_childDocuments_'] << new_person
	    end
	end
    end

    def id
	self['id']
    end

    def Document.location= location
	@@location = location
    end

end

