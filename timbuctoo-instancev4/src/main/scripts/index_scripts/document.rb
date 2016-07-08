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

    attr_reader :document, :languages, :data

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


    def Document.location= location
	@@location = location
    end

end

#  "publishLocation_ss": [
#    "Netherlands" // uit @relations["hasPublishLocation"][0]["displayName"]
#  ],
#  "language_ss": [
#    "Dutch" // uit @relations["hasWorkLanguage"][0]["displayName"]
#  ],
#  "genre_ss": [
#    "Periodical press: contribution" // uit @relations["hasGenre"][0]["displayName"]
#  ],
#  "source_ss": [
#    "Lelie- en Rozeknoppen ()" // uit @relations["hasDocumentSource"][0]["displayName"]
#  ],




