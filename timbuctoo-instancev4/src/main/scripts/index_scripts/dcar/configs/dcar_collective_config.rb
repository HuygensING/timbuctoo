class DcarCollectiveConfig
  def DcarCollectiveConfig.get
    {
        :properties => [
            { :name => '_id',  :converted_name => 'id'},
            { :name => 'type',  :converted_name => 'type_s'},
#            { :name => 'nameEng',  :converted_name => 'displayName_s'},
            { :name => 'nameEng',  :converted_name => 'nameEng_t'},
            { :name => 'nameNld',  :converted_name => 'nameNld_t'},
            { :name => 'history',  :converted_name => 'history_t'},
            { :name => 'notes',  :converted_name => 'notes_t'},
        ]
    }
  end
end
