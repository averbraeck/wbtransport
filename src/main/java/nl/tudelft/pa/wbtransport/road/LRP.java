package nl.tudelft.pa.wbtransport.road;

import java.util.HashSet;
import java.util.Set;

import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Direction;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNode;

import nl.tudelft.pa.wbtransport.district.District;
import nl.tudelft.simulation.language.Throw;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opensource.org/licenses/BSD-3-Clause">BSD 3-Clause License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Feb 20, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class LRP extends OTSNode
{
    /** */
    private static final long serialVersionUID = 1L;

    /** the chainage in km. */
    private final double chainage;

    /** the type. */
    private final String type;

    /** the name. */
    private final String name;

    /** the road name. */
    private final Set<Road> roads = new HashSet<>();

    /** bridge start, bridge end, road start, road end, gap start, gap end. */
    private final GapPoint gapPoint;
    
    /** district the LRP belongs to. */
    private final District district;
    
    /** centroid? */
    private boolean centroid = false;
    
    /** terminal? */
    private boolean terminalLink = false;
    
    /** port? */
    private boolean port = false;
    
    /** land port? */
    private boolean landPort = false;
    
    /** the road to which the LRP originally belongs. */
    private String road;

    /**
     * Construction of a Node.
     * @param network the network.
     * @param id the id of the Node.
     * @param point the point with usually an x and y setting.
     * @param direction the 3D direction. "East" is 0 degrees. "North" is 90 degrees (1/2 pi radians).
     * @param slope the slope as an angle. Horizontal is 0 degrees.
     * @param road the (initial) road
     * @param chainage chainage
     * @param type type
     * @param name name
     * @param gapPoint bridge start, bridge end, road start, road end, gap start, gap end
     * @param district district the LRP belongs to
     * @throws NetworkException if node already exists in the network, or if name of the node is not unique.
     */
    public LRP(Network network, String id, OTSPoint3D point, Direction direction, Angle slope, final Road road, final double chainage,
            final String type, final String name, final GapPoint gapPoint, final District district) throws NetworkException
    {
        super(network, id, point, direction, slope);
        Throw.whenNull(type, "type cannot be null");
        Throw.whenNull(name, "name cannot be null");
        Throw.whenNull(gapPoint, "gapPoint cannot be null");
        Throw.whenNull(district, "district cannot be null");
        
        this.chainage = chainage;
        this.type = type;
        this.name = name;
        this.gapPoint = gapPoint;
        this.district = district;
        if (road != null)
        {
            this.roads.add(road);
        }
        
        if (id.contains("_"))
            this.road = id.substring(0, id.indexOf('_'));
        else
            this.road = id;
    }

    /**
     * Construction of a Node.
     * @param network the network.
     * @param id the id of the Node.
     * @param point the point with usually an x and y setting.
     * @param road the (initial) road
     * @param chainage chainage
     * @param type type
     * @param name name
     * @param gapPoint gap
     * @param district district the LRP belongs to
     * @throws NetworkException if node already exists in the network, or if name of the node is not unique.
     */
    public LRP(Network network, String id, OTSPoint3D point, final Road road, final double chainage, final String type, final String name,
            final GapPoint gapPoint, final District district) throws NetworkException
    {
        this(network, id, point, Direction.ZERO, Angle.ZERO, road, chainage, type, name, gapPoint, district);
    }

    /**
     * @return road to which the LRP originally belongs
     */
    public final String getRoad()
    {
        return this.road;
    }

    /**
     * @return chainage
     */
    public final double getChainage()
    {
        return this.chainage;
    }

    /**
     * @return type
     */
    public final String getType()
    {
        return this.type;
    }

    /**
     * @return name
     */
    public final String getName()
    {
        return this.name;
    }

    /**
     * @return gap
     */
    public final GapPoint getGapPoint()
    {
        return this.gapPoint;
    }

    /**
     * @return district
     */
    public final District getDistrict()
    {
        return this.district;
    }

    /**
     * @param newRoad the road to add (connect) to this LRP
     */
    public final void addRoad(final Road newRoad)
    {
        this.roads.add(newRoad);
    }
    
    /**
     * @return road
     */
    public final Set<Road> getRoads()
    {
        return this.roads;
    }

    /**
     * @return centroid
     */
    public final boolean isCentroid()
    {
        return this.centroid;
    }

    /**
     * @param centroid set centroid
     */
    public final void setCentroid(boolean centroid)
    {
        this.centroid = centroid;
    }

    /**
     * @return terminalLink
     */
    public final boolean isTerminalLink()
    {
        return this.terminalLink;
    }

    /**
     * @param terminalLink set terminalLink
     */
    public final void setTerminalLink(boolean terminalLink)
    {
        this.terminalLink = terminalLink;
    }

    /**
     * @return port
     */
    public final boolean isPort()
    {
        return this.port;
    }

    /**
     * @param port set port
     */
    public final void setPort(boolean port)
    {
        this.port = port;
    }

    /**
     * @return landPort
     */
    public final boolean isLandPort()
    {
        return this.landPort;
    }

    /**
     * @param landPort set landPort
     */
    public final void setLandPort(boolean landPort)
    {
        this.landPort = landPort;
    }

    /**
     * @return the cardinality of the node, i.e. the number of connected links.
     */
    public final int getCardinality()
    {
        return getLinks().size();
    }
    
    /** {@inheritDoc} */
    @Override
    public DirectedPoint getLocation()
    {
        return new DirectedPoint(super.getLocation().x, super.getLocation().y, 1.0);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public Bounds getBounds()
    {
        return new BoundingBox(new Point3d(-0.0025, -0.0025, -10), new Point3d(0.005, 0.005, 10));
    }

}
