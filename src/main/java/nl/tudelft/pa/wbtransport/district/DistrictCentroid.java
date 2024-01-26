package nl.tudelft.pa.wbtransport.district;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.media.j3d.Bounds;
import javax.naming.NamingException;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;

import nl.tudelft.pa.wbtransport.district.animation.DistrictCentroidAnimation;
import nl.tudelft.pa.wbtransport.road.LRP;
import nl.tudelft.pa.wbtransport.road.ODNode;
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
public class DistrictCentroid implements ODNode, Locatable
{
    /** the corresponding LRP. */
    private final LRP lrp;

    /** the district. */
    final District district;

    /** the location -- a bit higher than the LRP. */
    private DirectedPoint location;

    /** unsatisfied demand per product in tonnes. */
    private final Map<String, Double> unsatisfiedDemand = new HashMap<>();

    /** cumulative demand per product in tonnes. */
    private final Map<String, Double> cumulativeDemand = new HashMap<>();

    /**
     * @param lrp the corresponding LRP
     * @param district the district
     * @param simulator the simulator
     */
    public DistrictCentroid(final LRP lrp, final District district, final OTSSimulatorInterface simulator)
    {
        this.lrp = lrp;
        this.district = district;
        this.location = new DirectedPoint(lrp.getLocation().x, lrp.getLocation().y, lrp.getLocation().z + 1.0);
        if (simulator instanceof AnimatorInterface)
        {
            try
            {
                new DistrictCentroidAnimation(this, simulator);
            }
            catch (RemoteException | NamingException exception)
            {
                exception.printStackTrace();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getId()
    {
        return this.district.getCode2();
    }

    /**
     * @return lrp
     */
    @Override
    public final LRP getLRP()
    {
        return this.lrp;
    }

    /** {@inheritDoc} */
    @Override
    public final void initializeDemand(final String product)
    {
        this.cumulativeDemand.put(product, 0.0);
        this.unsatisfiedDemand.put(product, 0.0);
    }

    /** {@inheritDoc} */
    @Override
    public final void addDailyDemand(final String product, final double dailyDemand, final double dailyUnsatisfiedDemand)
    {
        this.cumulativeDemand.put(product, this.cumulativeDemand.get(product) + dailyDemand);
        this.unsatisfiedDemand.put(product, this.unsatisfiedDemand.get(product) + dailyUnsatisfiedDemand);
    }

    /** {@inheritDoc} */
    @Override
    public final double getFractionUnsatisfiedDemand(final String product)
    {
        if (this.cumulativeDemand.get(product) == 0.0)
            return 0.0;
        return this.unsatisfiedDemand.get(product) / this.cumulativeDemand.get(product);
    }

    /**
     * @return district
     */
    public final District getDistrict()
    {
        return this.district;
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

}
