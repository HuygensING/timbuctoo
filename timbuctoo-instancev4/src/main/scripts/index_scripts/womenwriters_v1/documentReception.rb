class DocumentReception < Hash

    def initialize wanted_reception
      reception_doc_id = wanted_reception['reception_id']
      recepted_doc_id = wanted_reception['document_id']
      relation_id = wanted_reception['relation_id']
      relation_type = wanted_reception['relationType']

      reception_doc = Documents.find reception_doc_id
      if reception_doc.nil?
          STDERR.puts "reception_doc: #{reception_doc_id} NOT FOUND!"
          return
      end
      recepted_doc = Documents.find recepted_doc_id
      if recepted_doc.nil?
          STDERR.puts "recepted_doc: #{recepted_doc_id} NOT FOUND!"
          return
      end

      self['type_s'] = "document_reception"
      self['id'] = relation_id
      self['reception_id_s'] = reception_doc_id
      self['relationType_s'] = relation_type
      self['displayName_s'] = reception_doc['displayName_s']
      self['title_t'] = reception_doc["title_t"] if reception_doc['title_t'].is_a? String
      self['date_i'] = reception_doc["date_i"] if reception_doc['date_i'].is_a? Integer
      self['notes_t'] = reception_doc["notes_t"] if reception_doc['notes_t'].is_a? String
      self['authorGender_ss'] = reception_doc["authorGender_ss"] if reception_doc["authorGender_ss"].is_a? Array
      self['authorName_ss'] = reception_doc["authorName_ss"] if reception_doc["authorName_ss"].is_a? Array
      self['authorNameSort_s'] = reception_doc['authorNameSort_s'] if reception_doc['authorNameSort_s'].is_a? String
      self["documentType_s"] = reception_doc["documentType_s"]

      Document.new_rel_names.each do |name|
          self[name] = reception_doc[name]
      end

      self['document_id_s'] = recepted_doc_id
      self['document_displayName_s'] = recepted_doc['displayName_s']
      self['document_documentType_s'] = recepted_doc['documentType_s']
      self['document_date_i'] = recepted_doc['date_i'] if recepted_doc['date_i'].is_a? Integer
      self['document_notes_t'] = recepted_doc['notes_t'] if recepted_doc['notes_t'].is_a? String
      self['document_title_t'] = recepted_doc['title_t'] if recepted_doc['title_t'].is_a? String
      self['document_authorGender_ss'] = recepted_doc["authorGender_ss"] if recepted_doc["authorGender_ss"].is_a? Array
      self['document_authorName_ss'] = recepted_doc["authorName_ss"] if recepted_doc["authorName_ss"].is_a? Array
      self['document_authorNameSort_s'] = recepted_doc['authorNameSort_s'] if recepted_doc['authorNameSort_s'].is_a? String

      Document.new_rel_names.each do |name|
          self["document_#{name}"] = recepted_doc[name]
      end
      self['_childDocuments_'] = recepted_doc['_childDocuments_']
  end
end

