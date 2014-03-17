module Dore
  class Utility
    class << self
      
      def get_context(user_id = nil)
        org.dspace.core.Context.new.tap do |o|
          if user_id
            user = org.dspace.eperson.EPerson.find(o, user_id.to_i)
            o.set_current_user(user) if user
          end
        end
      end
      
      def authorize(context, type, dso)
        org.dspace.authorize.AuthorizeManager.authorize_action(context, dso, org.dspace.core.Constants.const_get(type))
      end
      
      def dspace_name
        org.dspace.core.ConfigurationManager.get_property("dspace.name")
      end
      
      def context_for_user(user_id = nil)
        org.dspace.core.Context.new.tap do |o|
          if user_id
            user = org.dspace.eperson.EPerson.find(o, user_id.to_i)
            o.set_current_user(user) if user
          end
        end
      end
      
      def current_user_is_admin?(context)
        org.dspace.authorize.AuthorizeManager.is_admin(context)
      end
      
      def top_communities(context)
        org.dspace.content.Community.find_all_top(context)
      end
      
      def recent_submissions(context, dso = nil)
        rsm = org.dore.components.RecentSubmissionsManager.new(context)
        rsm.get_recent_submissions(dso)
      end
      
      def sidebar_facet_processor(context, params, scope = nil)
        org.dore.discovery.SideBarFacetProcessor.new(context, java.util.HashMap.new(params), scope)
      end
      
      def feed_enabled?
        org.dspace.core.ConfigurationManager.get_boolean_property("webui.feed.enable")
      end
      
      def feed_formats
        ff = org.dspace.core.ConfigurationManager.getProperty("webui.feed.formats")
        ff.split(/\s*,\s*/)
      end
      
      def item_thumbnail(item = nil)
        index = item.get_bundles('ORIGINAL').map do |bundle|
          next unless bundle
        end        
      end      
      
      def find_bitstream(context, id = nil)
        return unless id
        org.dspace.content.Bitstream.find(context, id.to_i).tap do |bs|
          return nil unless org.dspace.authorize.AuthorizeManager.authorize_action_boolean(context, bs, org.dspace.core.Constants::READ)
        end
      end
      
    end
  end
end
