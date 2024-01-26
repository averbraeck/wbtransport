package nl.tudelft.pa.wbtransport.water;

import java.util.HashMap;
import java.util.Map;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSLink;

import nl.tudelft.pa.wbtransport.util.Convert;
import nl.tudelft.pa.wbtransport.water.animation.WaterwayProductAnimation;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opensource.org/licenses/BSD-3-Clause">BSD 3-Clause License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Oct 1, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class WaterwayLink extends OTSLink
{
    /** */
    private static final long serialVersionUID = 1L;

    /** the road. */
    private final Waterway waterway;

    /** class of the waterwayLink (class I to class IV where class I is largest). */
    private final int waterwayClass;

    /** time to traverse this segment for a barge, regular situation. */
    private final Duration avgRegularBargeDuration;

    /** time to traverse this segment for a barge, disturbed situation. */
    private final Duration avgDisturbedBargeDuration;

    /** length in (SI) units. */
    private final Length lengthM;

    /** transport over the route, tonnes per product. */
    private Map<String, Double> transport = new HashMap<>();

    /** animation (for removal due to override). */
    private Renderable2D<? extends WaterwayLink> animation = null;

    /** product animation (for removal due to override). */
    private Renderable2D<WaterwayProductAnimation.WaterwayLinkProducts> productAnimation = null;

    /**
     * Construct a new link.
     * @param id the link id
     * @param network the network to which the link belongs
     * @param startNode start node (directional)
     * @param endNode end node (directional)
     * @param waterway the waterway
     * @param waterwayClass class of the waterway (class I to class IV where class I is largest)
     * @param designLine the OTSLine3D design line of the Link
     * @param simulator the simulator on which events can be scheduled
     * @param parameters the params about flooding etc
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, or if the start node
     *             or the end node of the link are not registered in the network.
     */
    public WaterwayLink(final Network network, final String id, final WaterwayNode startNode, final WaterwayNode endNode,
            final Waterway waterway, final int waterwayClass, final OTSLine3D designLine, final OTSSimulatorInterface simulator,
            final Map<String, Object> parameters) throws NetworkException
    {
        super(network, id, startNode, endNode, LinkType.ALL, designLine, simulator, LongitudinalDirectionality.DIR_BOTH);
        this.waterway = waterway;
        this.waterwayClass = waterwayClass;

        // calculate length
        this.lengthM = new Length(Convert.wgs84ToMeters(startNode.getPoint(), endNode.getPoint()), LengthUnit.METER);

        // calculate travel duration
        this.avgRegularBargeDuration = calcRegularTravelDuration(parameters);
        this.avgDisturbedBargeDuration = calcDisturbedTravelDuration(parameters);
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
     * @param parameters the relevant parameters to use
     * @return the Duration
     */
    private final Duration calcRegularTravelDuration(final Map<String, Object> parameters)
    {
        double speedKmH = 0.001;
        switch (this.waterwayClass)
        {
            case 1:
                speedKmH = (double) parameters.get("CONST_BARGE_SPEED_CAT1");
                break;
            case 2:
                speedKmH = (double) parameters.get("CONST_BARGE_SPEED_CAT2");
                break;
            case 3:
                speedKmH = (double) parameters.get("CONST_BARGE_SPEED_CAT3");
                break;
            case 4:
                speedKmH = (double) parameters.get("CONST_BARGE_SPEED_CAT4");
                break;
        }
        return this.lengthM.divideBy(new Speed(speedKmH, SpeedUnit.KM_PER_HOUR));
    }

    /**
     * Calculate the regular travel time with disturbances on this segment. This assumes the road segment is in a flooded
     * district.
     * @param parameters the relevant parameters to use
     * @return the Duration
     */
    private final Duration calcDisturbedTravelDuration(final Map<String, Object> parameters)
    {
        double speedKmH = 0.001;
        double damage = 0;
        switch (this.waterwayClass)
        {
            // Damage = 0 -> no disruption
            // Damage = 1 -> Fully disrupted, no functional capacity
            // Damage = 0.5 -> 50% functionality
            case 1:
                speedKmH = (double) parameters.get("CONST_BARGE_SPEED_CAT1");
                damage = ((Number) parameters.get("DAMAGE_WATERWAY_1")).doubleValue();
                break;
            case 2:
                speedKmH = (double) parameters.get("CONST_BARGE_SPEED_CAT2");
                damage = ((Number) parameters.get("DAMAGE_WATERWAY_2")).doubleValue();
                break;
            case 3:
                speedKmH = (double) parameters.get("CONST_BARGE_SPEED_CAT3");
                damage = ((Number) parameters.get("DAMAGE_WATERWAY_3")).doubleValue();
                break;
            case 4:
                speedKmH = (double) parameters.get("CONST_BARGE_SPEED_CAT4");
                damage = ((Number) parameters.get("DAMAGE_WATERWAY_4")).doubleValue();
                break;
        }
        Duration duration;
        if (damage > 0.999)
            duration = new Duration(1E12, DurationUnit.SI);
        else
            duration = new Duration(this.lengthM.divideBy(new Speed(speedKmH, SpeedUnit.KM_PER_HOUR)).si / (1.0 - damage),
                    DurationUnit.SI);
        return duration;
    }

    /**
     * @return waterway
     */
    public final Waterway getWaterway()
    {
        return this.waterway;
    }

    /**
     * @return class of the waterway (class I to class IV where class I is largest)
     */
    public int getWaterwayClass()
    {
        return this.waterwayClass;
    }

    /**
     * @return length
     */
    public final Length getLengthM()
    {
        return this.lengthM;
    }

    /**
     * @return transport
     */
    public final Map<String, Double> getTransport()
    {
        return this.transport;
    }

    /**
     * @return animation
     */
    public Renderable2D<? extends WaterwayLink> getAnimation()
    {
        return this.animation;
    }

    /**
     * @param animation set animation
     */
    public void setAnimation(Renderable2D<? extends WaterwayLink> animation)
    {
        this.animation = animation;
    }

    /**
     * @return productAnimation
     */
    public Renderable2D<WaterwayProductAnimation.WaterwayLinkProducts> getProductAnimation()
    {
        return this.productAnimation;
    }

    /**
     * @param productAnimation set productAnimation
     */
    public void setProductAnimation(Renderable2D<WaterwayProductAnimation.WaterwayLinkProducts> productAnimation)
    {
        this.productAnimation = productAnimation;
    }

    /**
     * @return avgRegularBargeDuration
     */
    public Duration getAvgRegularBargeDuration()
    {
        return this.avgRegularBargeDuration;
    }

    /**
     * @return avgDisturbedBargeDuration
     */
    public Duration getAvgDisturbedBargeDuration()
    {
        return this.avgDisturbedBargeDuration;
    }

    /** {@inheritDoc} */
    @Override
    public DirectedPoint getLocation()
    {
        return new DirectedPoint(super.getLocation().x, super.getLocation().y, super.getLocation().z + 0.01,
                super.getLocation().getRotX(), super.getLocation().getRotY(), super.getLocation().getRotZ());
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "WaterwayLink [waterway=" + this.waterway + ", Class=" + this.waterwayClass + ", lengthM=" + this.lengthM
                + ", from " + this.getStartNode().getId() + " to " + this.getEndNode().getId() + "]";
    }

}
