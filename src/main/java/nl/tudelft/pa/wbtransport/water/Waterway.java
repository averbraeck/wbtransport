package nl.tudelft.pa.wbtransport.water;

import org.opentrafficsim.core.network.Network;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opensource.org/licenses/BSD-3-Clause">BSD 3-Clause License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Feb 19, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Waterway
{
    /** the network to which the waterway belongs. */
    final Network network;

    /** the id. */
    final String id;

    /** the name. */
    final String name;

    /**
     * Construct a new waterway.
     * @param network the network to which the waterway belongs
     * @param id the link id
     * @param name the name
     */
    public Waterway(final Network network, final String id, final String name)
    {
        this.id = id;
        this.network = network;
        this.name = name;
    }

    /**
     * @return network
     */
    public Network getNetwork()
    {
        return this.network;
    }

    /**
     * @return id
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * @return name
     */
    public String getName()
    {
        return this.name;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Waterway other = (Waterway) obj;
        if (this.id == null)
        {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        if (this.name == null)
        {
            if (other.name != null)
                return false;
        }
        else if (!this.name.equals(other.name))
            return false;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Waterway [id=" + this.id + ", name=" + this.name + "]";
    }

}
