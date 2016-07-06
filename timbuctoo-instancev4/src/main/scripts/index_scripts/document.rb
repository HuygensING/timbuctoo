class Document

    attr_reader :languages, :data

    def initialize(data)
#	@data = data
	find_languages data
    end

    def find_languages data
	@languages = Array.new
	if !data['@relations'].nil? &&
	    !data['@relations']['hasWorkLanguage'].nil?
	    hasWorkLanguage = data['@relations']['hasWorkLanguage']
	    hasWorkLanguage.each do |language|
		@languages << language['displayName']  if language['accepted']
	    end
	end
    end

end

