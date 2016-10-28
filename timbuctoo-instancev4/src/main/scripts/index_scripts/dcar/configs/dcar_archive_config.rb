class DcarArchiveConfig
  def DcarArchiveConfig.get
    {
      :properties => [
        { :name => '_id',  :converted_name => 'id'},
        { :name => '@type',  :converted_name => 'type_s'},
        { :name => 'titleEng',  :converted_name => 'titleEng_tim'},
        { :name => 'titleNld',  :converted_name => 'titleNld_tim'},
        { :name => 'documentType', :converted_name => 'documentType_s'},
        { :name => 'notes', :converted_name => 'notes_tim'},
        { :name => 'refCodeArchive', :converted_name => 'refcode_s'},
        { :name => 'beginDate', :converted_name => 'beginDate_dt', :type => 'date'},
        { :name => 'endDate', :converted_name => 'endDate_dt', :type => 'date'},
      ], :relations => [
       { :relation_name => 'is_created_by', :property_name => 'displayName', :converted_name => 'creator_ss'},
       { :relation_name => 'has_archive_keyword', :property_name => 'displayName', :converted_name => 'subject_ss'},
       { :relation_name => 'has_archive_person', :property_name => 'displayName', :converted_name => 'person_ss'},
       { :relation_name => 'has_archive_place', :property_name => 'displayName', :converted_name => 'place_ss'},
      ]
    }
  end
end
