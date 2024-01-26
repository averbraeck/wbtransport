package nl.tudelft.pa.wbtransport.util;

import org.opentrafficsim.core.geometry.OTSPoint3D;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opensource.org/licenses/BSD-3-Clause">BSD 3-Clause License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Sep 26, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Convert
{
    /** */
    private Convert()
    {
        // Utility class.
    }

    /**
     * Calculate distance between two lat/lon coordinates in meters.
     * See https://stackoverflow.com/questions/837872/calculate-distance-in-meters-when-you-know-longitude-and-latitude-in-java.
     * @param lat1 first lat
     * @param lng1 first lon
     * @param lat2 second lat
     * @param lng2 second lon
     * @return the distance in meters
     */
    public static double wgs84ToMeters(final double lat1, final double lng1, final double lat2, final double lng2)
    {
        double earthRadius = 6371000.0; // meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2.0) * Math.sin(dLng / 2.0);
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        return earthRadius * c;
    }

    /**
     * Calculate distance between two lat/lon coordinates in meters.
     * @param p1 first point (lon,lat)
     * @param p2 second point (lon,lat)
     * @return the distance in meters
     */
    public static double wgs84ToMeters(final OTSPoint3D p1, final OTSPoint3D p2)
    {
        return wgs84ToMeters(p1.y, p1.x, p2.y, p2.x);
    }
}
