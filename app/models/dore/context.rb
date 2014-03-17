module Dore

  class Context < SimpleDelegator
  
    def self.create
      new(org.dspace.core.Context.new)
    end
    
    def self.create_for_user(user_id = nil)
      create.tap do |o|
        if user_id
          user = org.dspace.eperson.EPerson.find(o.__getobj__, user_id.to_i)
          o.set_current_user(user) if user
        end
      end
    end
  
    def is_admin?
      @is_admin ||= org.dspace.authorize.AuthorizeManager.is_admin(__getobj__)
    end
  
  end

end
