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

    @@wanted_person_reception_relations = [
      "isBiographyOf",
      "commentsOnPerson",
      "isDedicatedTo",
      "isAwardForPerson",
      "listsPerson",
      "mentionsPerson",
      "isObituaryOf",
      "quotesPerson",
      "referencesPerson"
    ]

    #  eerst e.e.a. van deze relations opslaan;
    #  nadat alle documenten zijn gelezen de relations
    #  verder uitwerken en indexeren
    @@wanted_document_relations = [
      "isEditionOf",
      "isSequelOf",
      "isTranslationOf",
      "isAdaptationOf",
      "isPlagiarismOf",
      "hasAnnotationsOn",
      "isBibliographyOf",
      "isCensoringOf",
      "commentsOnWork",
      "isAnthologyContaining",
      "isCopyOf",
      "isAwardForWork",
      "isPrefaceOf",
      "isIntertextualTo",
      "listsWork",
      "mentionsWork",
      "isParodyOf",
      "quotesWork",
      "referencesWork"
    ]

    attr_reader :id

    def initialize data
      super
      self['type_s'] = "document"
      @@wanted_properties.each_with_index do |property,ind|
          if !data[property].nil?
            if ind==2
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
      title_t = "#{data['title']} #{data['englishTitle']}".strip
      self['title_t'] = title_t  if !title_t.empty?

      self['modified_l'] = data['^modified']['timeStamp']
      self['modifiedBy_s'] = data['^modified']['username'] || data['^modified']['userId']

      build_relations data

      add_creator_ids data
      add_person_receptions data
      add_document_receptions data
    end

    def build_relations data
      @@new_rel_names.each_with_index do |rel,ind|
          if !data['@relations'].nil?
            self[rel] = Array.new
            add_relation data['@relations'],rel,ind
          end
      end
    end

    def add_relation relations,rel,ind
      if !relations[@@wanted_relations[ind]].nil?
          relations[@@wanted_relations[ind]].each do |rt|
            self[rel] << rt['displayName']  if rt['accepted']
          end
      end
    end

    def add_creator_ids data
      if !data['@relations'].nil? and !data['@relations']['isCreatedBy'].nil?
        self["creator_ids"] = data['@relations']['isCreatedBy'].map{|creator| creator["id"]}
      else
        self["creator_ids"] = Array.new
      end
    end

    def add_creators
      self['_childDocuments_'] = Array.new
      self['authorGender_ss'] = Array.new
      self['authorName_ss'] = Array.new
      self['authorNameSort_s'] = ''
      namesorts = Array.new

      # TODO copy author name sort!
      self["creator_ids"].each do |creator_id|
          person = Persons.find creator_id
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
            self['authorGender_ss'] << person['gender_s']
            self['authorName_ss'] << person['displayName_s']
            namesorts << person['nameSort_s']
            # TODO copy author name sort!
          end
      end
      self['authorNameSort_s'] = namesorts.sort.first if namesorts.length > 0
      self["creator_ids"] = Array.new
    end

    def add_person_receptions data
      doc_id = self['id']
      if !data['@relations'].nil?
        @@wanted_person_reception_relations.each do |rec_rel|
          if !data['@relations'][rec_rel].nil?
            data['@relations'][rec_rel].each do |rr_data|
              wanted_reception = Hash.new
              wanted_reception['reception_id'] = doc_id
              wanted_reception['person_id'] = rr_data['id']
              wanted_reception['relation_id'] = rr_data['relationId']
              wanted_reception['relationType'] = rec_rel
              PersonReceptions.add wanted_reception
            end
          end
        end
      end
    end

    def add_document_receptions data
      doc_id = self['id']
      if !data['@relations'].nil?
          @@wanted_document_relations.each do |rec_rel|
            if !data['@relations'][rec_rel].nil?
                data['@relations'][rec_rel].each do |rr_data|
                  wanted_reception = Hash.new
                  wanted_reception['reception_id'] = doc_id
                  wanted_reception['document_id'] = rr_data['id']
                  wanted_reception['relation_id'] = rr_data['relationId']
                  wanted_reception['relationType'] = rec_rel
                  DocumentReceptions.add wanted_reception
                end
            end
          end
      end
    end

    def id
     self['id']
    end

    def Document.location= location
      @@location = location
    end

    def Document.new_rel_names
     @@new_rel_names
    end

end

