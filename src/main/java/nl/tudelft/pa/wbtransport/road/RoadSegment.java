package nl.tudelft.pa.wbtransport.road;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;

import nl.tudelft.pa.wbtransport.bridge.BridgeBGD;
import nl.tudelft.pa.wbtransport.network.ComparableLink;
import nl.tudelft.pa.wbtransport.road.animation.RoadSegmentAnimation;
import nl.tudelft.pa.wbtransport.util.Convert;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.language.Throw;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Bangladesh road, with possible gap for e.g., bridge or ferry.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opensource.org/licenses/BSD-3-Clause">BSD 3-Clause License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Feb 19, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class RoadSegment extends ComparableLink
{
    /** */
    private static final long serialVersionUID = 20170219L;

    /** gap. */
    private Gap gap;

    /** road name. */
    private Road road;

    /** the average truck speed. */
    private Speed avgTruckSpeed;

    /** time to traverse this segment for a truck, regular situation. */
    private Duration avgRegularTruckDuration;

    /** time to traverse this segment for a truck, disturbed situation. */
    private Duration avgDisturbedTruckDuration;

    /** length in (SI) units. */
    private final Length lengthM;

    /** bridges on this segment. */
    private final List<BridgeBGD> bridges = new ArrayList<>();

    /** transport over the route, tonnes per product. */
    private Map<String, Double> transport = new HashMap<>();

    /** animation (for removal due to override). */
    private Renderable2D<? extends RoadSegment> animation = null;

    /** segment animation (for removal due to override). */
    private Renderable2D<RoadSegmentAnimation.RoadSegmentProducts> segmentAnimation = null;

    /** parameters to recalculate the average disturbed truck duration. */
    private final Map<String, Object> parameters;

    /** the (changeable) road class. */
    private String roadClass;

    /**
     * Construct a new link.
     * @param id the link id
     * @param network the network to which the link belongs
     * @param road the road
     * @param startNode start node (directional)
     * @param endNode end node (directional)
     * @param linkType Link type to indicate compatibility with GTU types
     * @param designLine the OTSLine3D design line of the Link
     * @param simulator the simulator on which events can be scheduled
     * @param directionalityMap the directions (FORWARD, BACKWARD, BOTH, NONE) that GTUtypes can traverse this link
     * @param gap gap in case of ferry, road, or bridge
     * @param parameters the params about flooding etc
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, or if the start node
     *             or the end node of the link are not registered in the network.
     */
    public RoadSegment(Network network, String id, Road road, Node startNode, Node endNode, LinkType linkType,
            OTSLine3D designLine, OTSSimulatorInterface simulator, Map<GTUType, LongitudinalDirectionality> directionalityMap,
            final Gap gap, final Map<String, Object> parameters) throws NetworkException
    {
        super(network, id, startNode, endNode, linkType, designLine, simulator, directionalityMap);
        this.parameters = parameters;
        Throw.whenNull(road, "Road cannot be null");
        if (road.getId().contains("_") || road.getId().contains("-"))
        {
            System.err.println("roadId " + road + " contains strange characters");
            throw new RuntimeException("roadId " + road + " contains strange characters");
        }
        this.roadClass = id.substring(0, 1);
        this.gap = gap;
        this.road = road;
        this.avgTruckSpeed = road.getAvgTruckSpeed();

        // calculate length
        this.lengthM = new Length(Convert.wgs84ToMeters(startNode.getPoint(), endNode.getPoint()), LengthUnit.METER);

        // calculate travel duration
        this.avgRegularTruckDuration = calcRegularTravelDuration();
        this.avgDisturbedTruckDuration = calcDisturbedTravelDuration();
    }

    /**
     * Construct a new link, with a directionality for all GTUs as provided.
     * @param id the link id
     * @param network the network to which the link belongs
     * @param road the road
     * @param startNode start node (directional)
     * @param endNode end node (directional)
     * @param linkType Link type to indicate compatibility with GTU types
     * @param designLine the OTSLine3D design line of the Link
     * @param simulator the simulator on which events can be scheduled
     * @param directionality the directionality for all GTUs
     * @param gap gap in case of ferry, road, or bridge
     * @param parameters the params about flooding etc
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, or if the start node
     *             or the end node of the link are not registered in the network.
     */
    public RoadSegment(Network network, String id, Road road, LRP startNode, LRP endNode, LinkType linkType,
            OTSLine3D designLine, OTSSimulatorInterface simulator, LongitudinalDirectionality directionality, final Gap gap,
            final Map<String, Object> parameters) throws NetworkException
    {
        super(network, id, startNode, endNode, linkType, designLine, simulator, directionality);
        this.parameters = parameters;
        Throw.whenNull(road, "Road cannot be null");
        if (road.getId().contains("_") || road.getId().contains("-"))
        {
            System.err.println("roadId " + road + " contains strange characters");
            throw new RuntimeException("roadId " + road + " contains strange characters");
        }
        this.roadClass = id.substring(0, 1);
        this.gap = gap;
        this.road = road;
        this.avgTruckSpeed = road.getAvgTruckSpeed();

        // calculate length
        this.lengthM = new Length(Convert.wgs84ToMeters(startNode.getPoint(), endNode.getPoint()), LengthUnit.METER);

        // calculate travel duration
        this.avgRegularTruckDuration = calcRegularTravelDuration();
        this.avgDisturbedTruckDuration = calcDisturbedTravelDuration();
    }

    /**
     * Clone a link for a new network.
     * @param newNetwork the new network to which the clone belongs
     * @param newSimulator the new simulator for this network
     * @param animation whether to (re)create animation or not. Could be used in subclasses.
     * @param link the link to clone from
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, or if the start node
     *             or the end node of the link are not registered in the network.
     */
    protected RoadSegment(Network newNetwork, OTSSimulatorInterface newSimulator, boolean animation, RoadSegment link)
            throws NetworkException
    {
        super(newNetwork, newSimulator, animation, link);
        this.parameters = link.parameters;
        this.roadClass = link.roadClass;
        this.gap = link.gap;
        this.road = link.road;
        this.avgTruckSpeed = link.avgTruckSpeed;
        this.lengthM = link.lengthM;
        this.bridges.addAll(link.bridges);
        this.avgRegularTruckDuration = link.avgRegularTruckDuration;
        this.avgDisturbedTruckDuration = link.avgDisturbedTruckDuration;
    }

    /**
     * Add the bridges on this segment to the list of segment-bridges.
     * @param roadBridges the bridges on this road
     */
    public final void addBridges(final List<BridgeBGD> roadBridges)
    {
        this.bridges.clear();
        for (BridgeBGD bridge : roadBridges)
        {
            if (bridge.getChainage() >= this.getStartLRP().getChainage()
                    && bridge.getChainage() < this.getEndLRP().getChainage())
            {
                this.bridges.add(bridge);
            }
        }

        // recalculate travel times
        this.avgRegularTruckDuration = calcRegularTravelDuration();
        this.avgDisturbedTruckDuration = calcDisturbedTravelDuration();
    }

    /**
     * Add the transport on this segment to total transport.
     * @param product the product
     * @param tonnes the volume to add for the product
     */
    public final void addTransport(final String product, final double tonnes)
    {
        if (!this.transport.containsKey(product))
        {
            this.transport.put(product, 0.0);
        }
        this.transport.put(product, this.transport.get(product) + tonnes);
    }

    /**
     * Calculate the regular travel time without disturbances on this segment.
     * @return the Duration
     */
    private final Duration calcRegularTravelDuration()
    {
        double durationSI = getLengthM().si / getAvgTruckSpeed().si;

        switch (this.gap)
        {
            case GAP:
                durationSI = 1E12;
                break;

            case FERRY:
                Speed ferrySpeed = new Speed((double) this.parameters.get("CONST_FERRY_SPEED"), SpeedUnit.KM_PER_HOUR);
                durationSI = this.lengthM.si / ferrySpeed.si + 3600.0 * (double) this.parameters.get("CONST_FERRY_WAIT");
                break;

            default:
                break;
        }

        return new Duration(durationSI / 3600.0, DurationUnit.HOUR);
    }

    /**
     * Calculate the regular travel time with disturbances on this segment. This assumes the road segment is in a flooded
     * district.
     * @return the Duration
     */
    private final Duration calcDisturbedTravelDuration()
    {
        double durationSI = getLengthM().si / getAvgTruckSpeed().si;
        switch (this.gap)
        {
            case GAP:
                durationSI = 1E12;
                break;

            case FERRY:
                Speed ferrySpeed = new Speed((double) this.parameters.get("CONST_FERRY_SPEED"), SpeedUnit.KM_PER_HOUR);
                durationSI = this.lengthM.si / ferrySpeed.si + 3600.0 * (double) this.parameters.get("CONST_FERRY_WAIT");
                if (getStartLRP().getDistrict().isFlooded() || getEndLRP().getDistrict().isFlooded())
                {
                    // Damage = 0 -> no disruption
                    // Damage = 1 -> Fully disrupted, no functional capacity
                    // Damage = 0.5 -> 50% functionality
                    double damage = ((Number) this.parameters.get("DAMAGE_FERRY")).doubleValue();
                    if (damage > 0.999)
                        durationSI = 1E12;
                    else
                        durationSI = durationSI / (1.0 - damage);
                }
                break;

            case BRIDGE:
                // handled by the BMMS bridges and culverts
                break;

            case ROAD:
                // Damage = 0 -> no disruption
                // Damage = 1 -> Fully disrupted, no functional capacity
                // Damage = 0.5 -> 50% functionality
                if (getStartLRP().getDistrict().isFlooded() || getEndLRP().getDistrict().isFlooded())
                {
                    if (this.roadClass.startsWith("X"))
                    {
                        // no effect of damage -- special road class
                    }
                    else if (this.roadClass.startsWith("N"))
                    {
                        double damage = ((Number) this.parameters.get("DAMAGE_ROAD_N")).doubleValue();
                        if (damage > 0.999)
                            durationSI = 1E12;
                        else
                            durationSI = durationSI / (1.0 - damage);
                    }
                    else if (this.roadClass.startsWith("R"))
                    {
                        double damage = ((Number) this.parameters.get("DAMAGE_ROAD_R")).doubleValue();
                        if (damage > 0.999)
                            durationSI = 1E12;
                        else
                            durationSI = durationSI / (1.0 - damage);
                    }
                    else if (this.roadClass.startsWith("Z"))
                    {
                        double damage = ((Number) this.parameters.get("DAMAGE_ROAD_Z")).doubleValue();
                        if (damage > 0.999)
                            durationSI = 1E12;
                        else
                            durationSI = durationSI / (1.0 - damage);
                    }
                }
                break;

            default:
                break;
        }

        // Bridge damage
        if (getStartLRP().getDistrict().isFlooded() || getEndLRP().getDistrict().isFlooded())
        {
            for (BridgeBGD bridge : this.bridges)
            {
                double damage = 0;
                switch (bridge.getCondition())
                {
                    case "A":
                        damage = (double) this.parameters.get("DAMAGE_BRIDGE_A");
                        break;

                    case "B":
                        damage = (double) this.parameters.get("DAMAGE_BRIDGE_B");
                        break;

                    case "C":
                        damage = (double) this.parameters.get("DAMAGE_BRIDGE_C");
                        break;

                    case "D":
                        damage = (double) this.parameters.get("DAMAGE_BRIDGE_D");
                        break;

                    default:
                        break;
                }
                if (damage > 0.999)
                    durationSI = 1E12;
                else
                    durationSI = durationSI / (1.0 - damage); // now based on worst bridge in the segment
            }
        }

        return new Duration(durationSI / 3600.0, DurationUnit.HOUR);
    }

    /**
     * @return gap
     */
    public final Gap getGap()
    {
        return this.gap;
    }

    /**
     * @param gap set gap
     */
    public final void setGap(Gap gap)
    {
        this.gap = gap;
    }

    /**
     * @return the start LRP
     */
    public LRP getStartLRP()
    {
        return (LRP) getStartNode();
    }

    /**
     * @return the end LRP
     */
    public LRP getEndLRP()
    {
        return (LRP) getEndNode();
    }

    /**
     * @return roadName
     */
    public final Road getRoad()
    {
        return this.road;
    }

    /**
     * @return regular travelDuration
     */
    public final Duration getAvgRegularTruckDuration()
    {
        return this.avgRegularTruckDuration;
    }

    /**
     * @return disturbed travelDuration
     */
    public final Duration getAvgDisturbedTruckDuration()
    {
        return this.avgDisturbedTruckDuration;
    }

    /**
     * @return avgTruckSpeed
     */
    public Speed getAvgTruckSpeed()
    {
        return this.avgTruckSpeed;
    }

    /**
     * @param avgTruckSpeed set avgTruckSpeed
     */
    public void setAvgTruckSpeed(Speed avgTruckSpeed)
    {
        this.avgTruckSpeed = avgTruckSpeed;
        this.avgRegularTruckDuration = calcRegularTravelDuration();
        this.avgDisturbedTruckDuration = calcDisturbedTravelDuration();
    }

    /**
     * @return length
     */
    public final Length getLengthM()
    {
        return this.lengthM;
    }

    /**
     * @return bridges
     */
    public final List<BridgeBGD> getBridges()
    {
        return this.bridges;
    }

    /**
     * @return transport
     */
    public final Map<String, Double> getTransport()
    {
        return this.transport;
    }

    /**
     * @return roadClass
     */
    public String getRoadClass()
    {
        return this.roadClass;
    }

    /**
     * @param roadClass set roadClass
     */
    public void setRoadClass(String roadClass)
    {
        this.roadClass = roadClass;

        // recalculate travel times
        this.avgRegularTruckDuration = calcRegularTravelDuration();
        this.avgDisturbedTruckDuration = calcDisturbedTravelDuration();
    }

    /**
     * @return animation
     */
    public Renderable2D<? extends RoadSegment> getAnimation()
    {
        return this.animation;
    }

    /**
     * @param animation set animation
     */
    public void setAnimation(Renderable2D<? extends RoadSegment> animation)
    {
        this.animation = animation;
    }

    /**
     * @return segmentAnimation
     */
    public Renderable2D<RoadSegmentAnimation.RoadSegmentProducts> getSegmentAnimation()
    {
        return this.segmentAnimation;
    }

    /**
     * @param segmentAnimation set segmentAnimation
     */
    public void setSegmentAnimation(Renderable2D<RoadSegmentAnimation.RoadSegmentProducts> segmentAnimation)
    {
        this.segmentAnimation = segmentAnimation;
    }

    /** {@inheritDoc} */
    @Override
    public DirectedPoint getLocation()
    {
        if (this.gap.isRoad())
        {
            return super.getLocation();
        }
        return new DirectedPoint(super.getLocation().x, super.getLocation().y, super.getLocation().z + 0.01,
                super.getLocation().getRotX(), super.getLocation().getRotY(), super.getLocation().getRotZ());
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "RoadBGD [roadName=" + this.road + ", id=" + getId() + "]";
    }

}
