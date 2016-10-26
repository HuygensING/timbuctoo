class DcarArchiveConfig
  def DcarArchiveConfig.get
    {
      :properties => [
        { :name => '_id',  :converted_name => 'id'},
        { :name => 'type',  :converted_name => 'type_s'},
        { :name => 'titleEng',  :converted_name => 'titleEng_t'},
        { :name => 'titleNld',  :converted_name => 'titleNld_t'},
        { :name => 'documentType', :converted_name => 'documentType_s'},
        { :name => 'notes', :converted_name => 'notes_t'},
        { :name => '"refCodeArchive"', :converted_name => 'refcode_s'},
      ], :relations => [
       { :relation_name => 'is_created_by', :property_name => 'displayName', :converted_name => 'creator_ss'},
       { :relation_name => 'has_child_archive', :property_name => 'displayName', :converted_name => 'child_archive_ss'},
       { :relation_name => 'has_archive_person', :property_name => 'displayName', :converted_name => 'person_ss'},
      ]
    }
  end
end
