package heigit.ors.routing.graphhopper.extensions.core;

import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.storage.GraphHopperStorage;
import heigit.ors.routing.graphhopper.extensions.edgefilters.AvoidFeaturesEdgeFilter;
import heigit.ors.routing.graphhopper.extensions.edgefilters.EdgeFilterSequence;
import heigit.ors.routing.graphhopper.extensions.edgefilters.core.AvoidFeaturesCoreEdgeFilter;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CoreLMOptions {
    List<String> coreLMSets;
    List<EdgeFilterSequence> filters = new ArrayList<>();
    GraphHopperStorage ghStorage;

    public CoreLMOptions(GraphHopperStorage ghStorage){
        this.ghStorage = ghStorage;
    }

    /**
     * Set the filters that are used while calculating landmarks and their distance
     * @param tmpCoreLMSets
     */
    public void setRestrictionFilters(List<String> tmpCoreLMSets) {
        this.coreLMSets = tmpCoreLMSets;
    }

    public void createRestrictionFilters(){
        for(String set : coreLMSets) {
            List<String> filters = Arrays.asList(set.split(","));
            EdgeFilterSequence edgeFilterSequence = new EdgeFilterSequence();
            int filter = 0;
            for (String filterType : filters) {
                //Do not add any filter if it is allow_all
                if (filterType.equalsIgnoreCase("allow_all")) {
                    edgeFilterSequence.appendName("allow_all");
//                    if(filters.size() > 1)
//                        throw new IllegalArgumentException("Cannot use more edgefilters in combination with 'allow_all'");
                    break;
                }
                if (filterType.equalsIgnoreCase("highways")) {
                    filter = filter | 1;
                    edgeFilterSequence.appendName("highways");
                }
                if (filterType.equalsIgnoreCase("tollways")) {
                    filter = filter | 2;
                    edgeFilterSequence.appendName("tollways");
                }
                if (filter == 0)
                    throw new IllegalArgumentException("Currently unsupported filter type: " + filterType);
            }
            if(filter != 0)
                edgeFilterSequence.add(new AvoidFeaturesCoreEdgeFilter(ghStorage, -1, filter));
            if(edgeFilterSequence != null)
                this.filters.add(edgeFilterSequence);
        }
    }

    public List<EdgeFilterSequence> getFilters(){
        return filters;
    }

    public void setGhStorage(GraphHopperStorage ghStorage){
        this.ghStorage = ghStorage;
    }
}
