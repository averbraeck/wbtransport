package nl.tudelft.pa.wbtransport.water;

import java.rmi.RemoteException;
import java.util.Map;

import javax.media.j3d.Bounds;
import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;

import nl.tudelft.pa.wbtransport.district.District;
import nl.tudelft.pa.wbtransport.road.LRP;
import nl.tudelft.pa.wbtransport.water.animation.WaterwayTerminalAnimation;
import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.simulators.AnimatorInterface;
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
public class WaterwayTerminal implements Comparable<WaterwayTerminal>, Locatable
{
    /** id. */
    private final String id;

    /** name. */
    private final String name;

    /** index number. */
    private final int index;

    /** the corresponding waterway node. */
    private final WaterwayNode waterwayNode;

    /** the corresponding LRP. */
    private final LRP lrp;

    /** the district. */
    private final District district;

    /** the location -- a bit higher than the LRP. */
    private DirectedPoint location;
    
    /** (un)loading duration, regular. */
    private final Duration loadingDurationRegular;

    /** (un)loading duration, disturbed. */
    private final Duration loadingDurationDisturbed;

    /**
     * @param id the id
     * @param name the name
     * @param index the index number or the O/D matrices
     * @param waterwayNode the corresponding waterway node
     * @param lrp the corresponding LRP
     * @param district the district
     * @param simulator the simulator
     * @param parameters parameters for loading/unloading duration
     */
    public WaterwayTerminal(final String id, final String name, final int index, final WaterwayNode waterwayNode, final LRP lrp,
            final District district, final OTSSimulatorInterface simulator, final Map<String, Object> parameters)
    {
        this.id = id;
        this.name = name;
        this.index = index;
        this.lrp = lrp;
        this.waterwayNode = waterwayNode;
        this.district = district;
        
        // a bit above the rest
        this.location = new DirectedPoint(lrp.getLocation().x, lrp.getLocation().y, lrp.getLocation().z + 1.0);
        
        // calculate (un)loading times
        int waterwayClass = waterwayNode.getWaterwayClass();
        double durationH = (double) parameters.get("CONST_BARGE_TRANSHIP_CAT" + waterwayClass);
        this.loadingDurationRegular = new Duration(durationH, DurationUnit.HOUR);
        double disturbFactor = (double) parameters.get("DAMAGE_TERMINALS");
        if (disturbFactor >= 1.0)
            durationH = 1E12;
        else
            durationH /= (1.0 - disturbFactor);
        this.loadingDurationDisturbed = new Duration(durationH, DurationUnit.HOUR);
        
        // animation
        if (simulator instanceof AnimatorInterface)
        {
            try
            {
                new WaterwayTerminalAnimation(this, simulator);
            }
            catch (RemoteException | NamingException exception)
            {
                exception.printStackTrace();
            }
        }
    }

    /**
     * @return id
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * @return waterwayNode
     */
    public WaterwayNode getWaterwayNode()
    {
        return this.waterwayNode;
    }

    /**
     * @return lrp
     */
    public LRP getLRP()
    {
        return this.lrp;
    }

    /**
     * @return district
     */
    public District getDistrict()
    {
        return this.district;
    }

    /**
     * @return name
     */
    public final String getName()
    {
        return this.name;
    }

    /**
     * @return index
     */
    public int getIndex()
    {
        return this.index;
    }
    
    /** 
     * Calculate the loading / unloading time.
     * @return loading / unloading duration.
     */
    public Duration getLoadingDuration()
    {
        return this.district.isFlooded() ? this.loadingDurationDisturbed : this.loadingDurationRegular;
    }
    
    /** {@inheritDoc} */
    @Override
    public DirectedPoint getLocation() throws RemoteException
    {
        return this.location;
    }

    /** {@inheritDoc} */
    @Override
    public Bounds getBounds() throws RemoteException
    {
        return this.lrp.getBounds();
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(WaterwayTerminal wt)
    {
        return this.id.compareTo(wt.id);
    }

}
