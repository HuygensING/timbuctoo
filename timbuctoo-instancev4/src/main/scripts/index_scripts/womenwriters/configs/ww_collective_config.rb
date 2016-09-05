class WwCollectiveConfig
  def WwCollectiveConfig.get
    {
        :properties => [
            { :name => '_id',  :converted_name => 'id'},
            { :name => 'name',  :converted_name => 'name_t'},
            { :name => 'type',  :converted_name => 'type_s'},
            { :name => 'name',  :converted_name => 'displayName_s'},
        ]
    }
  end
end