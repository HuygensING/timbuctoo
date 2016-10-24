class DcarCollectiveConfig
  def DcarCollectiveConfig.get
    {
        :properties => [
            { :name => '_id',  :converted_name => 'id'},
            { :name => 'type',  :converted_name => 'type_s'},
            { :name => 'titleNld',  :converted_name => 'displayName_s'},
            { :name => 'titleEng',  :converted_name => 'titleEng_s_tim'},
            { :name => 'titleNld',  :converted_name => 'titleNld_s_tim'},
        ]
    }
  end
end
