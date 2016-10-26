class DcarArchiverConfig
  def DcarArchiverConfig.get
    {
        :properties => [
            { :name => '_id',  :converted_name => 'id'},
            { :name => 'type',  :converted_name => 'type_s'},
#            { :name => 'nameEng',  :converted_name => 'displayName_s'},
            { :name => 'nameEng',  :converted_name => 'nameEng_t'},
            { :name => 'nameNld',  :converted_name => 'nameNld_t'},
            { :name => 'history',  :converted_name => 'history_t'},
            { :name => 'notes',  :converted_name => 'notes_t'},
        ], :relations => [
            { :relation_name => 'has_archiver_keyword', :property_name => 'displayName', :converted_name => 'subject_ss'},
            { :relation_name => 'has_archiver_person', :property_name => 'displayName', :converted_name => 'person_ss'},
            { :relation_name => 'has_archiver_place', :property_name => 'displayName', :converted_name => 'place_ss'},
        ]       
    }
  end
end
