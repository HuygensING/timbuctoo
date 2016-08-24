class PersonReception < Hash

  def initialize wanted_reception
    reception_doc_id = wanted_reception['reception_id']
    recepted_person_id = wanted_reception['person_id']
    relation_id = wanted_reception['relation_id']
    relation_type = wanted_reception['relationType']

    reception_doc = Documents.find reception_doc_id
    if reception_doc.nil?
      STDERR.puts "reception_doc: #{reception_doc_id} NOT FOUND!"
      return
    end
    recepted_person = Persons.find recepted_person_id
    if recepted_person.nil?
      STDERR.puts "recepted_doc: #{recepted_person_id} NOT FOUND!"
      return
    end

    self['type_s'] = "person_reception"
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


    self['person_id_s'] = recepted_person_id
    self['person_displayName_s'] = recepted_person['displayName_s']
    self['person_name_t'] = recepted_person['name_t'] if recepted_person['name_t'].is_a? String
    self['person_types_ss'] = recepted_person["types_ss"] if recepted_person["types_ss"].is_a? Array
    self['person_gender_s'] = recepted_person["gender_s"] if recepted_person["gender_s"].is_a? String
    self['person_birthDate_i'] = recepted_person["birthDate_i"] if recepted_person["birthDate_i"].is_a? Integer
    self['person_deathDate_i'] = recepted_person["deathDate_i"] if recepted_person["deathDate_i"].is_a? Integer
    self['person_notes_t'] = recepted_person["notes_t"] if recepted_person["notes_t"].is_a? String
    self['person_children_s']= recepted_person["children_s"] if recepted_person["children_s"].is_a? String
    self['person_nameSort_s'] = recepted_person['nameSort_s'] if recepted_person['nameSort_s'].is_a? String

    Person.new_rel_names.each do |name|
      self["person_#{name}"] = recepted_person[name]
    end
  end
end

