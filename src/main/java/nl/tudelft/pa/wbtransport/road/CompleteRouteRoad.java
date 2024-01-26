package nl.tudelft.pa.wbtransport.road;

import java.util.ArrayList;
import java.util.List;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.CompleteRoute;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opensource.org/licenses/BSD-3-Clause">BSD 3-Clause License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Sep 24, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class CompleteRouteRoad extends CompleteRoute
{
    /** */
    private static final long serialVersionUID = 20170922L;

    /** cached length. */
    private Length length;

    /** cached regular duration. */
    private Duration avgRegularTruckDuration;

    /** cached disturbed duration. */
    private Duration avgDisturbedTruckDuration;

    /** links. */
    private List<RoadLink> links;

    /**
     * Create a route based on an initial list of nodes. <br>
     * This constructor makes a defensive copy of the provided List.
     * @param id the name of the route.
     * @param nodes the initial list of nodes.
     * @throws NetworkException if intermediate nodes are missing in the route.
     */
    public CompleteRouteRoad(final String id, final List<Node> nodes) throws NetworkException
    {
        super(id, GTUType.ALL, nodes);
    }

    /**
     * Create an empty route for the given GTUType.
     * @param id the name of the route
     */
    public CompleteRouteRoad(final String id)
    {
        super(id, GTUType.ALL);
    }

    /**
     * Make the links and cache the result.
     */
    private void makeLinks()
    {
        if (this.links == null)
        {
            this.links = new ArrayList<>();
            Node lastNode = null;
            for (Node node : getNodes())
            {
                if (lastNode != null)
                {
                    Link link = null;
                    for (Link l : lastNode.getLinks())
                    {
                        if (l.getStartNode().equals(node) || l.getEndNode().equals(node))
                        {
                            link = l;
                            break;
                        }
                    }
                    this.links.add((RoadLink) link);
                }
                lastNode = node;
            }
        }
    }

    /**
     * Calculate the roads over that this route uses.
     * @return a list of road names.
     */
    public List<String> routeRoadNames()
    {
        makeLinks();
        List<String> result = new ArrayList<>();
        Road lastRoad = null;
        for (RoadLink link : this.links)
        {
            Road road = link.getRoad();
            if (!road.equals(lastRoad))
            {
                lastRoad = road;
                result.add(road.getId());
            }
        }
        return result;
    }

    /**
     * Calculate the roads over that this route uses.
     * @return a list of roads.
     */
    public List<Road> routeRoads()
    {
        makeLinks();
        List<Road> result = new ArrayList<>();
        Road lastRoad = null;
        for (RoadLink link : this.links)
        {
            Road road = link.getRoad();
            if (!road.equals(lastRoad))
            {
                lastRoad = road;
                result.add(road);
            }
        }
        return result;
    }

    /**
     * Calculate the length of the route.
     * @return the length of the route
     */
    public Length getLength()
    {
        if (this.length == null)
        {
            makeLinks();
            double lengthSI = 0.0;
            for (RoadLink link : this.links)
            {
                for (RoadSegment segment : link.getSegments())
                {
                    lengthSI += segment.getLengthM().si;
                }
            }
            this.length = new Length(lengthSI / 1000.0, LengthUnit.KILOMETER);
        }
        return this.length;
    }

    /**
     * Calculate the travel duration for a truck, regular network.
     * @return the travel duration for a truck, regular network.
     */
    public Duration getAvgRegularTruckDuration()
    {
        if (this.avgRegularTruckDuration == null)
        {
            makeLinks();
            double durationSI = 0.0;
            for (RoadLink link : this.links)
            {
                for (RoadSegment segment : link.getSegments())
                {
                    durationSI += segment.getAvgRegularTruckDuration().si;
                }
            }
            this.avgRegularTruckDuration = new Duration(durationSI / 3600.0, DurationUnit.HOUR);
        }
        return this.avgRegularTruckDuration;
    }

    /**
     * Calculate the travel duration for a truck, disturbed network.
     * @return the travel duration for a truck, disturbed network.
     */
    public Duration getAvgDisturbedTruckDuration()
    {
        if (this.avgDisturbedTruckDuration == null)
        {
            makeLinks();
            double durationSI = 0.0;
            for (RoadLink link : this.links)
            {
                for (RoadSegment segment : link.getSegments())
                {
                    durationSI += segment.getAvgDisturbedTruckDuration().si;
                }
            }
            this.avgDisturbedTruckDuration = new Duration(durationSI / 3600.0, DurationUnit.HOUR);
        }
        return this.avgDisturbedTruckDuration;
    }

    /**
     * @return links
     */
    public final List<RoadLink> getLinks()
    {
        return this.links;
    }

}
