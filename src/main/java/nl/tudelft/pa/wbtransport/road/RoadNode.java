package nl.tudelft.pa.wbtransport.road;

import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNode;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opensource.org/licenses/BSD-3-Clause">BSD 3-Clause License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Oct 1, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class RoadNode extends OTSNode
{
    /** */
    private static final long serialVersionUID = 1L;

    /** corresponding LRP. */
    private final LRP lrp;
    
    /**
     * Construction of a Node.
     * @param network the network.
     * @param lrp the corresponding LRP
     * @throws NetworkException if node already exists in the network, or if name of the node is not unique.
     */
    public RoadNode(final Network network, final LRP lrp) throws NetworkException
    {
        super(network, lrp.getId(), lrp.getPoint());
        this.lrp = lrp;
    }

    /**
     * @return lrp
     */
    public final LRP getLRP()
    {
        return this.lrp;
    }

}
