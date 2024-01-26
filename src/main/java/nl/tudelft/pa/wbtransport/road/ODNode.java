package nl.tudelft.pa.wbtransport.road;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opensource.org/licenses/BSD-3-Clause">BSD 3-Clause License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Oct 1, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public interface ODNode
{
    /** @return the corresponding LRP. */
    LRP getLRP();

    /** @return the id. */
    String getId();

    /**
     * Initialize the demand to 0.0 for all products.
     * @param product the product to initialize the demand for
     */
    void initializeDemand(final String product);

    /**
     * Add the demand for one day to the node (district, port, land port).
     * @param product the product
     * @param dailyDemand the daily demand
     * @param dailyUnsatisfiedDemand the unsatisfied demand for this day
     */
    void addDailyDemand(String product, double dailyDemand, double dailyUnsatisfiedDemand);

    /**
     * Get the fraction unsatisfied demand.
     * @param product the product
     * @return unsatisfied demand for that product
     */
    double getFractionUnsatisfiedDemand(String product);
}
