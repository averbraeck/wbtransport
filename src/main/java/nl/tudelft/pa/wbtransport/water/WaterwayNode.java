package nl.tudelft.pa.wbtransport.water;

import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNode;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opensource.org/licenses/BSD-3-Clause">BSD 3-Clause License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Oct 30, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class WaterwayNode extends OTSNode
{
    /** */
    private static final long serialVersionUID = 20171028;

    /** name of the waterway. */
    private final String waterwayName;

    /** class of the waterway (class I to class IV where class I is largest). */
    private final int waterwayClass;

    /**
     * @param network the network.
     * @param id the id of the Node.
     * @param point the point with usually an x and y setting.
     * @param waterwayName name of the waterway
     * @param waterwayClass class of the waterway (class I to class IV where class I is largest)
     * @throws NetworkException if node already exists in the network, or if name of the node is not unique.
     */
    public WaterwayNode(final Network network, final String id, final OTSPoint3D point, final String waterwayName,
            final int waterwayClass) throws NetworkException
    {
        super(network, id, point);
        this.waterwayName = waterwayName;
        this.waterwayClass = waterwayClass;
    }

    /**
     * @return waterwayName
     */
    public String getWaterwayName()
    {
        return this.waterwayName;
    }

    /**
     * @return class of the waterway (class I to class IV where class I is largest)
     */
    public int getWaterwayClass()
    {
        return this.waterwayClass;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "WaterwayNode [" + "id=" + this.getId() + ", wwName=" + this.waterwayName + ", wwClass="
                + this.waterwayClass + ", point=" + this.getPoint() + "]";
    }

}
