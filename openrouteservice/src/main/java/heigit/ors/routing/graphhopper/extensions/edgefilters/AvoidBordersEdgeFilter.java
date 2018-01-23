package heigit.ors.routing.graphhopper.extensions.edgefilters;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.EdgeIteratorState;
import heigit.ors.routing.RouteSearchParameters;
import heigit.ors.routing.graphhopper.extensions.storages.BordersGraphStorage;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.pathprocessors.BordersExtractor;
import org.apache.log4j.Logger;

public class AvoidBordersEdgeFilter implements EdgeFilter {
    private static Logger LOGGER = Logger.getLogger(AvoidFeaturesEdgeFilter.class);

    private final boolean _in;
    private final boolean _out;
    protected final FlagEncoder _encoder;

    private BordersExtractor.Avoid _avoidBorders = BordersExtractor.Avoid.NONE;
    private boolean _avoidCountries = false;

    private BordersExtractor _bordersExtractor;

    public AvoidBordersEdgeFilter(FlagEncoder encoder, RouteSearchParameters searchParams, GraphStorage graphStorage) {
        this(encoder, true, true, searchParams, graphStorage);
    }

    public AvoidBordersEdgeFilter(FlagEncoder encoder, boolean in, boolean out, RouteSearchParameters searchParams,
                                   GraphStorage graphStorage) {
        this(encoder, in, out);
        BordersGraphStorage extBorders = GraphStorageUtils.getGraphExtension(graphStorage, BordersGraphStorage.class);
        init(searchParams, extBorders);
    }

    public AvoidBordersEdgeFilter(FlagEncoder encoder, RouteSearchParameters searchParams, BordersGraphStorage graphStorage) {
        this(encoder, true, true, searchParams, graphStorage);
    }

    public AvoidBordersEdgeFilter(FlagEncoder encoder, boolean in, boolean out, RouteSearchParameters searchParams, BordersGraphStorage graphStorage) {
        this(encoder, in, out);
        init(searchParams, graphStorage);
    }

    private AvoidBordersEdgeFilter(FlagEncoder encoder, boolean in, boolean out) {
        this._in = in;
        this._out = out;

        this._encoder = encoder;
    }

    private void init(RouteSearchParameters searchParams, BordersGraphStorage extBorders) {
        // Init the graph storage
        if(extBorders != null) {
            int[] avoidCountries;
            if(searchParams.hasAvoidCountries())
                avoidCountries = searchParams.getAvoidCountries();
            else
                avoidCountries = new int[0];

            _avoidCountries = avoidCountries.length > 0;

            if(searchParams.hasAvoidBorders()) {
                _avoidBorders = searchParams.getAvoidBorders();
            }

            _bordersExtractor = new BordersExtractor(extBorders, searchParams.getProfileParameters(), avoidCountries);
        }
    }

    @Override
    public final boolean accept(EdgeIteratorState iter) {

        if (_out && iter.isForward(_encoder) || _in && iter.isBackward(_encoder)) {

            if (_avoidBorders != BordersExtractor.Avoid.NONE) {
                // We have been told to avoid some form of border
                switch(_avoidBorders) {
                    case ALL:
                        if(_bordersExtractor.isBorder(iter.getEdge())) {
                            // It is a border, and we want to avoid all borders
                            return false;
                        }
                    case CONTROLLED:
                        if(_bordersExtractor.isControlledBorder(iter.getEdge())) {
                            // We want to only avoid controlled borders
                            return false;
                        }
                        break;
                }
            }

            if(_avoidCountries) {
                if(_bordersExtractor.restrictedCountry(iter.getEdge())) {
                       return false;
                }
            }

            return true;
        }

        return false;
    }

}
