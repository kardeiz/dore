module Dore
  class BaseService
    
    delegate :session, :params, :to => :@view_context
    
    def initialize(view_context)
      @view_context = view_context   
    end
    
    def context
      @context ||= Utility.context_for_user(1) # session[:user_id]
    end
    
    def current_user
      @current_user ||= context.get_current_user
    end
    
    def current_user?; !!current_user; end    
    
    def current_user_is_admin?
      @current_user_is_admin ||= Utility.current_user_is_admin?(context)
    end
    
  end
end

