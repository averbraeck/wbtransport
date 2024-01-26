package nl.tudelft.pa.wbtransport.port;

import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;

import nl.tudelft.pa.wbtransport.port.animaton.LandPortAnimation;
import nl.tudelft.pa.wbtransport.road.LRP;
import nl.tudelft.simulation.dsol.simulators.AnimatorInterface;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opensource.org/licenses/BSD-3-Clause">BSD 3-Clause License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Oct 1, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class LandPort extends ImportExportLocation
{
    /**
     * @param id the id
     * @param name the name
     * @param lrp the corresponding LRP
     * @param simulator the simulator
     */
    public LandPort(final String id, final String name, final LRP lrp, final OTSSimulatorInterface simulator)
    {
        super(id, name, lrp, simulator);
        if (simulator instanceof AnimatorInterface)
        {
            try
            {
                new LandPortAnimation(this, simulator);
            }
            catch (RemoteException | NamingException exception)
            {
                exception.printStackTrace();
            }
        }
    }
}
