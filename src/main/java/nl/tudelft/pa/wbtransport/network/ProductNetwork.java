package nl.tudelft.pa.wbtransport.network;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opentrafficsim.core.network.OTSNetwork;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opensource.org/licenses/BSD-3-Clause">BSD 3-Clause License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Sep 29, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class ProductNetwork extends OTSNetwork
{
    /** */
    private static final long serialVersionUID = 1L;

    /** the max transport per product. */
    private Map<String, Double> maxProductTransportPerLink = new HashMap<>();
    
    /** the products. */
    private List<String> products;
    
    /**
     * @param id the id
     */
    public ProductNetwork(String id)
    {
        super(id);
    }

    /**
     * Store the amount (tonnes) of transported goods on a link, if it is higher than what has been stored before.
     * @param product the product
     * @param tonnes the amount (tonnes) of transported goods on a link.
     */
    public void setMaxProductTransport(final String product, final double tonnes)
    {
        Double maxTonnes = this.maxProductTransportPerLink.get(product);
        if (maxTonnes == null)
        {
            this.maxProductTransportPerLink.put(product, 0.0);
            maxTonnes = 0.0;
        }
        if (tonnes > maxTonnes)
        {
            this.maxProductTransportPerLink.put(product, tonnes);
        }
    }
 
    /**
     * @param product the product
     * @return the highest amount (tonnes) of transported goods on any link.
     */
    public double getMaxProductTransport(final String product)
    {
        return this.maxProductTransportPerLink.get(product);
    }

    /**
     * @return products
     */
    public List<String> getProducts()
    {
        return this.products;
    }

    /**
     * @param products set products
     */
    public void setProducts(final List<String> products)
    {
        this.products = products;
    }
}
