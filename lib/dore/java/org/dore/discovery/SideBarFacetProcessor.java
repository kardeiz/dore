package org.dore.discovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.discovery.DiscoverQuery;
import org.dspace.discovery.DiscoverResult;
import org.dspace.discovery.SearchServiceException;
import org.dspace.discovery.SearchUtils;
import org.dspace.discovery.configuration.DiscoveryConfiguration;
import org.dspace.discovery.configuration.DiscoverySearchFilterFacet;
import org.dspace.plugin.CollectionHomeProcessor;
import org.dspace.plugin.CommunityHomeProcessor;
import org.dspace.plugin.PluginException;
import org.dspace.plugin.SiteHomeProcessor;

public class SideBarFacetProcessor
{
    /** log4j category */
    private static Logger log = Logger.getLogger(SideBarFacetProcessor.class);

    private Context context;
    private HashMap<String,String> params;
    private DSpaceObject scope;

    public SideBarFacetProcessor(Context context, HashMap<String,String> params, DSpaceObject scope)
	  {
		  this.context = context;
		  this.params  = params;
		  this.scope   = scope;
	  }


    public DiscoverResult getResults()
    {
        DiscoverQuery queryArgs = DiscoverUtility.getDiscoverQuery(context, params, scope, true);
        queryArgs.setMaxResults(0);
        DiscoverResult qResults = null;
        try
        {
            qResults = SearchUtils.getSearchService().search(context, scope, queryArgs);
        }
        catch (SearchServiceException e)
        {
            log.error(LogManager.getHeader(context, "discovery-process-sidebar", "scope=" + scope));
        }
        return qResults;
    }
    
    public List<DiscoverySearchFilterFacet> getSidebarFacets()
    {
        DiscoveryConfiguration discoveryConfiguration = SearchUtils.getDiscoveryConfiguration(scope);
        List<DiscoverySearchFilterFacet> availableFacets = discoveryConfiguration.getSidebarFacets();
        return availableFacets;
    }
    
    
}
