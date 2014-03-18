module Dore
  module ApplicationHelper
  
    def nav_li_link(title, path)
      opts = current_page?(path) ? { :class => 'active' } : {}
      content_tag(:li, opts) { link_to title, path }
    end
  
    def breadcrumbs_home
      content_tag(:ol, :class => 'breadcrumb') do
        content_tag(:li) do
          content_tag(:span, "Home")
        end
      end
    end
  
    def title(key)
      "#{ Utility.dspace_name }: #{ t(key) }"
    end
  
    def current_user?
      @service.try(:current_user?)
    end
  
    def current_user_is_admin?
      @service.try(:current_user_is_admin?)
    end
  
    def item_link(item)
      link_to item.get_name, item_path(item.get_id)
    end
    
    def community_link(community)
      link_to community.get_name, community_path(community.get_id)
    end
  
    def top_communities(communities)
      content_tag(:ul) do
        communities.map do |community|
          content_tag(:li) do
            community_link community
          end
        end.join.html_safe
      end
    end
  
    def facet_panel(facet_name, facet_values, facet_limit)
      facet_display = t("dore.display.facets.#{facet_name}") || facet_name.titleize
      content_tag(:div, :class => 'panel panel-default') do
        head = content_tag(:div, :class => 'panel-heading') do
          content_tag(:h4, facet_display, :class => 'panel-title')
        end
        head + content_tag(:div, :class => 'panel-body') do
          content_tag(:table, :class => 'facets-table') do
            facet_values.first(facet_limit).map do |facet_value|
              content_tag(:tr) do
                link = link_to truncate(facet_value.get_displayed_value), search_path({ 
                  :filterquery => facet_value.get_as_filter_query, 
                  :filtername  => facet_name,
                  :filtertype  => facet_value.get_filter_type
                })
                content_tag(:td, link) + content_tag(:td, facet_value.get_count, :class => 'count')
              end
            end.join.html_safe
          end
        end
      end
    end
  
    def home_facets(sidebar_facets, sidebar_facet_results)
      sidebar_facets.map do |facet|      
        facet_name = facet.get_index_field_name
        next unless facet_values = sidebar_facet_results[facet_name]
        content_tag(:div, :class => 'col-md-4') do
          facet_panel(facet_name, facet_values, facet.get_facet_limit)
        end
      end.join.html_safe
    end
    
  
  end
end
