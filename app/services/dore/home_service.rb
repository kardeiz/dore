module Dore
  class HomeService < BaseService
    
    def top_communities
      @communities ||= Utility.top_communities(context)
    end
    
    def recent_submissions
      @recent_submissions ||= Utility.recent_submissions(context)
    end
    
    def sidebar_facet_processor
      @sidebar_facet_processor ||= Utility.sidebar_facet_processor(context, params)
    end
    
    def sidebar_facets
      @sidebar_facets ||= sidebar_facet_processor.get_sidebar_facets
    end
    
    def sidebar_facet_results
      @sidebar_facet_results ||= sidebar_facet_processor.get_results.facet_results
    end
    
  end
end

