package org.dore.discovery;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.discovery.DiscoverFacetField;
import org.dspace.discovery.DiscoverQuery;
import org.dspace.discovery.DiscoverQuery.SORT_ORDER;
import org.dspace.discovery.DiscoverResult;
import org.dspace.discovery.SearchServiceException;
import org.dspace.discovery.SearchUtils;
import org.dspace.discovery.configuration.DiscoveryConfiguration;
import org.dspace.discovery.configuration.DiscoveryConfigurationParameters;
import org.dspace.discovery.configuration.DiscoverySearchFilterFacet;
import org.dspace.discovery.configuration.DiscoverySortConfiguration;
import org.dspace.discovery.configuration.DiscoverySortFieldConfiguration;
import org.dspace.handle.HandleManager;

public class DiscoverUtility
{
    /** log4j category */
    private static Logger log = Logger.getLogger(DiscoverUtility.class);

    /**
     * Build a DiscoverQuery object using the parameter in the request
     * 
     * @param request
     * @return the query.
     * @throws SearchServiceException
     */
    public static DiscoverQuery getDiscoverQuery(Context context,
            HashMap<String,String> params, DSpaceObject scope, boolean enableFacet)
    {
        DiscoverQuery queryArgs = new DiscoverQuery();
        DiscoveryConfiguration discoveryConfiguration = SearchUtils
                .getDiscoveryConfiguration(scope);

        List<String> userFilters = setupBasicQuery(context,
                discoveryConfiguration, params, queryArgs);

        setPagination(params, queryArgs, discoveryConfiguration);

        if (enableFacet)
        {
            setFacet(context, params, scope, queryArgs, discoveryConfiguration, userFilters);
        }

        return queryArgs;
    }

    /**
     * Setup the basic query arguments: the main query and all the filters
     * (default + user). Return the list of user filter
     * 
     * @param context
     * @param request
     * @param queryArgs
     *            the query object to populate
     * @return the list of user filer (as filter query)
     */
    private static List<String> setupBasicQuery(Context context,
            DiscoveryConfiguration discoveryConfiguration,
            HashMap<String,String> params, DiscoverQuery queryArgs)
    {
        // Get the query
        String query = params.get("query");
        if (StringUtils.isNotBlank(query))
        {
            queryArgs.setQuery(query);
        }

        List<String> defaultFilterQueries = discoveryConfiguration
                .getDefaultFilterQueries();
        if (defaultFilterQueries != null)
        {
            for (String f : defaultFilterQueries)
            {
                queryArgs.addFacetQuery(f);
            }
        }
        List<String[]> filters = getFilters(params);
        List<String> userFilters = new ArrayList<String>();
        for (String[] f : filters)
        {
            try
            {
            String newFilterQuery = SearchUtils.getSearchService()
                    .toFilterQuery(context, f[0], f[1], f[2])
                    .getFilterQuery();
            if (newFilterQuery != null)
            {
                queryArgs.addFilterQueries(newFilterQuery);
                userFilters.add(newFilterQuery);
            }
            }
            catch (SQLException e)
            {
                log.error(LogManager.getHeader(context,
                        "Error in discovery while setting up user facet query",
                        "filter_field: " + f[0] + ",filter_type:"
                                + f[1] + ",filer_value:"
                                + f[2]), e);
            }

        }

        return userFilters;

    }

    private static void setPagination(HashMap<String,String> params,
            DiscoverQuery queryArgs,
            DiscoveryConfiguration discoveryConfiguration)
    {
        String sStart = params.get("start");
        Integer start = -1;
        if (sStart != null) { 
          start = Integer.parseInt(sStart);
        }        
        // can't start earlier than 0 in the results!
        if (start < 0)
        {
            start = 0;
        }

        String sortBy = params.get("sort_by");
        String sortOrder = params.get("order");

        DiscoverySortConfiguration searchSortConfiguration = discoveryConfiguration.getSearchSortConfiguration();
        if (sortBy == null)
        {
            // Attempt to find the default one, if none found we use SCORE
            sortBy = "score";
            if (searchSortConfiguration != null)
            {
                for (DiscoverySortFieldConfiguration sortFieldConfiguration : searchSortConfiguration
                        .getSortFields())
                {
                    if (sortFieldConfiguration.equals(searchSortConfiguration
                            .getDefaultSort()))
                    {
                        sortBy = SearchUtils
                                .getSearchService()
                                .toSortFieldIndex(
                                        sortFieldConfiguration
                                                .getMetadataField(),
                                        sortFieldConfiguration.getType());
                    }
                }
            }
        }

        if (sortOrder == null && searchSortConfiguration != null)
        {
            sortOrder = searchSortConfiguration.getDefaultSortOrder()
                    .toString();
        }
        if (sortBy != null)
        {
            if ("asc".equalsIgnoreCase(sortOrder))
            {
                queryArgs.setSortField(sortBy, SORT_ORDER.asc);
            }
            else
            {
                queryArgs.setSortField(sortBy, SORT_ORDER.desc);
            }
        }
        String sRpp = params.get("rpp");
        Integer rpp = -1;
        if (sRpp != null) {
          rpp = Integer.parseInt(sRpp);
        }

        if (rpp > 0)
        {
            queryArgs.setMaxResults(rpp);
        }
        else
        {
            queryArgs.setMaxResults(discoveryConfiguration.getDefaultRpp());
        }
        queryArgs.setStart(start);
    }

    private static void setFacet(Context context, HashMap<String,String> params,
            DSpaceObject scope, DiscoverQuery queryArgs,
            DiscoveryConfiguration discoveryConfiguration,
            List<String> userFilters)
    {
        List<DiscoverySearchFilterFacet> facets = discoveryConfiguration
                .getSidebarFacets();

        log.info("facets for scope, " + scope + ": "
                + (facets != null ? facets.size() : null));
        if (facets != null)
        {
            queryArgs.setFacetMinCount(1);
        }

        /** enable faceting of search results */
        if (facets != null)
        {
            queryArgs.setFacetMinCount(1);
            for (DiscoverySearchFilterFacet facet : facets)
            {
                if (facet.getType().equals(
                        DiscoveryConfigurationParameters.TYPE_DATE))
                {
                    String dateFacet = facet.getIndexFieldName() + ".year";
                    List<String> filterQueriesList = queryArgs
                            .getFilterQueries();
                    String[] filterQueries = new String[0];
                    if (filterQueriesList != null)
                    {
                        filterQueries = new String[filterQueries.length];
                        filterQueries = filterQueriesList
                                .toArray(filterQueries);
                    }
                    try
                    {
                        // Get a range query so we can create facet
                        // queries
                        // ranging from out first to our last date
                        // Attempt to determine our oldest & newest year
                        // by
                        // checking for previously selected filters
                        int oldestYear = -1;
                        int newestYear = -1;

                        for (String filterQuery : filterQueries)
                        {
                            if (filterQuery.startsWith(dateFacet + ":"))
                            {
                                // Check for a range
                                Pattern pattern = Pattern
                                        .compile("\\[(.*? TO .*?)\\]");
                                Matcher matcher = pattern.matcher(filterQuery);
                                boolean hasPattern = matcher.find();
                                if (hasPattern)
                                {
                                    filterQuery = matcher.group(0);
                                    // We have a range
                                    // Resolve our range to a first &
                                    // endyear
                                    int tempOldYear = Integer
                                            .parseInt(filterQuery.split(" TO ")[0]
                                                    .replace("[", "").trim());
                                    int tempNewYear = Integer
                                            .parseInt(filterQuery.split(" TO ")[1]
                                                    .replace("]", "").trim());

                                    // Check if we have a further filter
                                    // (or
                                    // a first one found)
                                    if (tempNewYear < newestYear
                                            || oldestYear < tempOldYear
                                            || newestYear == -1)
                                    {
                                        oldestYear = tempOldYear;
                                        newestYear = tempNewYear;
                                    }

                                }
                                else
                                {
                                    if (filterQuery.indexOf(" OR ") != -1)
                                    {
                                        // Should always be the case
                                        filterQuery = filterQuery.split(" OR ")[0];
                                    }
                                    // We should have a single date
                                    oldestYear = Integer.parseInt(filterQuery
                                            .split(":")[1].trim());
                                    newestYear = oldestYear;
                                    // No need to look further
                                    break;
                                }
                            }
                        }
                        // Check if we have found a range, if not then
                        // retrieve our first & last year by using solr
                        if (oldestYear == -1 && newestYear == -1)
                        {

                            DiscoverQuery yearRangeQuery = new DiscoverQuery();
                            yearRangeQuery.setFacetMinCount(1);
                            yearRangeQuery.setMaxResults(1);
                            // Set our query to anything that has this
                            // value
                            yearRangeQuery.addFieldPresentQueries(dateFacet);
                            // Set sorting so our last value will appear
                            // on
                            // top
                            yearRangeQuery.setSortField(dateFacet + "_sort",
                                    DiscoverQuery.SORT_ORDER.asc);
                            yearRangeQuery.addFilterQueries(filterQueries);
                            yearRangeQuery.addSearchField(dateFacet);
                            DiscoverResult lastYearResult = SearchUtils
                                    .getSearchService().search(context, scope,
                                            yearRangeQuery);

                            if (0 < lastYearResult.getDspaceObjects().size())
                            {
                                java.util.List<DiscoverResult.SearchDocument> searchDocuments = lastYearResult
                                        .getSearchDocument(lastYearResult
                                                .getDspaceObjects().get(0));
                                if (0 < searchDocuments.size()
                                        && 0 < searchDocuments
                                                .get(0)
                                                .getSearchFieldValues(dateFacet)
                                                .size())
                                {
                                    oldestYear = Integer
                                            .parseInt(searchDocuments
                                                    .get(0)
                                                    .getSearchFieldValues(
                                                            dateFacet).get(0));
                                }
                            }
                            // Now get the first year
                            yearRangeQuery.setSortField(dateFacet + "_sort",
                                    DiscoverQuery.SORT_ORDER.desc);
                            DiscoverResult firstYearResult = SearchUtils
                                    .getSearchService().search(context, scope,
                                            yearRangeQuery);
                            if (0 < firstYearResult.getDspaceObjects().size())
                            {
                                java.util.List<DiscoverResult.SearchDocument> searchDocuments = firstYearResult
                                        .getSearchDocument(firstYearResult
                                                .getDspaceObjects().get(0));
                                if (0 < searchDocuments.size()
                                        && 0 < searchDocuments
                                                .get(0)
                                                .getSearchFieldValues(dateFacet)
                                                .size())
                                {
                                    newestYear = Integer
                                            .parseInt(searchDocuments
                                                    .get(0)
                                                    .getSearchFieldValues(
                                                            dateFacet).get(0));
                                }
                            }
                            // No values found!
                            if (newestYear == -1 || oldestYear == -1)
                            {
                                continue;
                            }

                        }

                        int gap = 1;
                        // Attempt to retrieve our gap by the algorithm
                        // below
                        int yearDifference = newestYear - oldestYear;
                        if (yearDifference != 0)
                        {
                            while (10 < ((double) yearDifference / gap))
                            {
                                gap *= 10;
                            }
                        }
                        // We need to determine our top year so we can
                        // start
                        // our count from a clean year
                        // Example: 2001 and a gap from 10 we need the
                        // following result: 2010 - 2000 ; 2000 - 1990
                        // hence
                        // the top year
                        int topYear = (int) (Math.ceil((float) (newestYear)
                                / gap) * gap);

                        if (gap == 1)
                        {
                            // We need a list of our years
                            // We have a date range add faceting for our
                            // field
                            // The faceting will automatically be
                            // limited to
                            // the 10 years in our span due to our
                            // filterquery
                            queryArgs.addFacetField(new DiscoverFacetField(
                                    facet.getIndexFieldName(), facet.getType(),
                                    10, facet.getSortOrder()));
                        }
                        else
                        {
                            java.util.List<String> facetQueries = new ArrayList<String>();
                            // Create facet queries but limit then to 11
                            // (11
                            // == when we need to show a show more url)
                            for (int year = topYear; year > oldestYear
                                    && (facetQueries.size() < 11); year -= gap)
                            {
                                // Add a filter to remove the last year
                                // only
                                // if we aren't the last year
                                int bottomYear = year - gap;
                                // Make sure we don't go below our last
                                // year
                                // found
                                if (bottomYear < oldestYear)
                                {
                                    bottomYear = oldestYear;
                                }

                                // Also make sure we don't go above our
                                // newest year
                                int currentTop = year;
                                if ((year == topYear))
                                {
                                    currentTop = newestYear;
                                }
                                else
                                {
                                    // We need to do -1 on this one to
                                    // get a
                                    // better result
                                    currentTop--;
                                }
                                facetQueries.add(dateFacet + ":[" + bottomYear
                                        + " TO " + currentTop + "]");
                            }
                            for (String facetQuery : facetQueries)
                            {
                                queryArgs.addFacetQuery(facetQuery);
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        log.error(
                                LogManager
                                        .getHeader(
                                                context,
                                                "Error in discovery while setting up date facet range",
                                                "date facet: " + dateFacet), e);
                    }
                }
                else
                {
                    int facetLimit = facet.getFacetLimit();

                    String sFacetPage = params.get(facet.getIndexFieldName() + "_page");
                    Integer facetPage = -1;
                    if (sFacetPage != null) {
                      facetPage = Integer.parseInt(sFacetPage);
                    }
                    if (facetPage < 0)
                    {
                        facetPage = 0;
                    }
                    // at most all the user filters belong to this facet
                    int alreadySelected = userFilters.size();

                    // Add one to our facet limit to make sure that if
                    // we
                    // have more then the shown facets that we show our
                    // show
                    // more url
                    // add the already selected facet so to have a full
                    // top list
                    // if possible
                    queryArgs.addFacetField(new DiscoverFacetField(facet
                            .getIndexFieldName(),
                            DiscoveryConfigurationParameters.TYPE_TEXT,
                            facetLimit + 1 + alreadySelected, facet
                                    .getSortOrder(), facetPage * facetLimit));
                }
            }
        }
    }

    public static List<String[]> getFilters(HashMap<String,String> params)
    {
//        String submit = UIUtil.getSubmitButton(request, "submit");
        int ignore = -1;
//        if (submit.startsWith("submit_filter_remove_"))
//        {
//            ignore = Integer.parseInt(submit.substring("submit_filter_remove_".length()));
//        }
        List<String[]> appliedFilters = new ArrayList<String[]>();
        
        List<String> filterValue = new ArrayList<String>();
        List<String> filterOp = new ArrayList<String>();
        List<String> filterField = new ArrayList<String>();
        for (int idx = 1; ; idx++)
        {
            String op = params.get("filter_type_"+idx);
            if (StringUtils.isBlank(op))
            {
                break;
            }
            else if (idx != ignore)
            {
                filterOp.add(op);
                filterField.add(params.get("filter_field_"+idx));
                filterValue.add(params.get("filter_value_"+idx));
            }
        }
        
        String op = params.get("filtertype");
        if (StringUtils.isNotBlank(op))
        {
            filterOp.add(op);
            filterField.add(params.get("filtername"));
            filterValue.add(params.get("filterquery"));
        }
        
        for (int idx = 0; idx < filterOp.size(); idx++)
        {
            appliedFilters.add(new String[] { filterField.get(idx),
                    filterOp.get(idx), filterValue.get(idx) });
        }
        return appliedFilters;
    }

}
