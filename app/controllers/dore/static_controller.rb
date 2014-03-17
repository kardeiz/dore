module Dore
  class StaticController < ApplicationController
  
    def home
      @service = HomeService.new(view_context)
    end
  
    def search; end
  
  end
end
