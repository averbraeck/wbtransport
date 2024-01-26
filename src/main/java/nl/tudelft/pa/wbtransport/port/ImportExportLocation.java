package nl.tudelft.pa.wbtransport.port;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.media.j3d.Bounds;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;

import nl.tudelft.pa.wbtransport.road.LRP;
import nl.tudelft.pa.wbtransport.road.ODNode;
import nl.tudelft.simulation.dsol.animation.Locatable;
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
public class ImportExportLocation implements ODNode, Locatable
{
    /** id. */
    private final String id;

    /** name. */
    private final String name;

    /** imports per product. */
    private Map<String, Double> imports = new HashMap<>();

    /** exports per product. */
    private Map<String, Double> exports = new HashMap<>();

    /** the corresponding LRP. */
    private final LRP lrp;

    /** the location -- a bit higher than the LRP. */
    private DirectedPoint location;

    /** unsatisfied demand per product in tonnes. */
    private final Map<String, Double> unsatisfiedDemand = new HashMap<>();

    /** cumulative demand per product in tonnes. */
    private final Map<String, Double> cumulativeDemand = new HashMap<>();

    /**
     * @param id the id
     * @param name the name
     * @param lrp the corresponding LRP
     * @param simulator the simulator
     */
    public ImportExportLocation(final String id, final String name, final LRP lrp, final OTSSimulatorInterface simulator)
    {
        this.id = id;
        this.name = name;
        this.lrp = lrp;
        this.location = new DirectedPoint(lrp.getLocation().x, lrp.getLocation().y, lrp.getLocation().z + 1.0);
    }

    /** {@inheritDoc} */
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
     * @return imports
     */
    public final Map<String, Double> getImports()
    {
        return this.imports;
    }

    /**
     * @param imports set imports
     */
    public final void setImports(Map<String, Double> imports)
    {
        this.imports = imports;
    }

    /**
     * @return exports
     */
    public final Map<String, Double> getExports()
    {
        return this.exports;
    }

    /**
     * @param exports set exports
     */
    public final void setExports(Map<String, Double> exports)
    {
        this.exports = exports;
    }

    /** {@inheritDoc} */
    @Override
    public final String getId()
    {
        return this.id;
    }

    /**
     * @return name
     */
    public final String getName()
    {
        return this.name;
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
