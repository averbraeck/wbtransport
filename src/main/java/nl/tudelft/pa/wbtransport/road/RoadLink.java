package nl.tudelft.pa.wbtransport.road;

import java.util.List;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSLink;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opensource.org/licenses/BSD-3-Clause">BSD 3-Clause License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Oct 1, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class RoadLink extends OTSLink
{
    /** */
    private static final long serialVersionUID = 1L;

    /** */
    private List<RoadSegment> segments;
    
    /** the road. */
    private final Road road;
    
    /**
     * Construct a new link.
     * @param id the link id
     * @param network the network to which the link belongs
     * @param startNode start node (directional)
     * @param endNode end node (directional)
     * @param road the road
     * @param designLine the OTSLine3D design line of the Link
     * @param simulator the simulator on which events can be scheduled
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, or if the start node
     *             or the end node of the link are not registered in the network.
     */
    public RoadLink(Network network, String id, RoadNode startNode, RoadNode endNode, final Road road, OTSLine3D designLine,
            OTSSimulatorInterface simulator) throws NetworkException
    {
        super(network, id, startNode, endNode, LinkType.ALL, designLine, simulator, LongitudinalDirectionality.DIR_BOTH);
        this.road = road;
    }

    /**
     * @param segment segment to add
     */
    public void addSegment(final RoadSegment segment)
    {
        this.segments.add(segment);
    }

    /**
     * @return segments
     */
    public final List<RoadSegment> getSegments()
    {
        return this.segments;
    }

    /**
     * @param segments set segments
     */
    public final void setSegments(List<RoadSegment> segments)
    {
        this.segments = segments;
    }

    /**
     * @return road
     */
    public final Road getRoad()
    {
        return this.road;
    }
    
}
