class DcarArchiverConfig
  def DcarArchiverConfig.get
    {
        :properties => [
            { :name => '_id',  :converted_name => 'id'},
            { :name => '@type',  :converted_name => 'type_s'},
            { :name => 'nameEng',  :converted_name => 'nameEng_tim'},
            { :name => 'nameNld',  :converted_name => 'nameNld_tim'},
            { :name => 'history',  :converted_name => 'history_tim'},
            { :name => 'notes',  :converted_name => 'notes_tim'},
            { :name => 'types',  :converted_name => 'archiverTypes_ss'},
            { :name => 'beginDate', :converted_name => 'beginDate_i'},
            { :name => 'endDate', :converted_name => 'endDate_i'},
        ], :relations => [
            { :relation_name => 'has_archiver_keyword', :property_name => 'displayName', :converted_name => 'subject_ss'},
            { :relation_name => 'has_archiver_person', :property_name => 'displayName', :converted_name => 'person_ss'},
            { :relation_name => 'has_archiver_place', :property_name => 'displayName', :converted_name => 'place_ss'},
        ]       
    }
  end
end
