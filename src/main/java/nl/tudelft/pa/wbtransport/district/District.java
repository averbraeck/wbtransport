package nl.tudelft.pa.wbtransport.district;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.j3d.Bounds;
import javax.naming.NamingException;
import javax.vecmath.Point3d;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.animation.PaintPolygons;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

import nl.tudelft.pa.wbtransport.district.animation.DistrictAnimation;
import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opensource.org/licenses/BSD-3-Clause">BSD 3-Clause License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version May 4, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class District implements Locatable, Comparable<District>
{
    /** geography. */
    private final MultiPolygon polygon;

    /** geography in OTS coordinates. */
    private final OTSLine3D border;

    /** code, e.g., "1". */
    private final String code;

    /** Name, e.g. "Barisal". */
    private final String name;

    /** 2-letter code of the district, e.g. "BS". */
    private final String code2;

    /** centroid. */
    private final OTSPoint3D centroid;

    /** district centroid. */
    private DistrictCentroid districtCentroid;

    /** population. */
    private double population;

    /** the simulator. */
    private final OTSDEVSSimulatorInterface simulator;

    /** flooded or not. */
    private boolean flooded;

    /** data. */
    private final Map<String, Double> data = new HashMap<>();

    /** truck capacity available for transport from this district in tonnes. */
    private double truckCapacityTonnes = 1E12;
    
    /** waterway capacity available for transport from this district in tonnes. */
    private double waterwayCapacityTonnes = 1E12;
    
    /** railcapacity available for transport from this district in tonnes. */
    private double railCapacityTonnes = 1E12;
    
    /**
     * @param simulator the simulator
     * @param code code
     * @param name name
     * @param code2 2-letter code
     * @param polygon multipolygon
     */
    public District(final OTSDEVSSimulatorInterface simulator, final String code, final String name, final String code2,
            final MultiPolygon polygon)
    {
        super();
        this.simulator = simulator;
        this.code = code;
        this.name = name;
        this.code2 = code2;
        this.polygon = polygon;
        this.centroid = new OTSPoint3D(polygon.getCentroid().getX(), polygon.getCentroid().getY());

        OTSLine3D pBorder = null;

        try
        {
            List<OTSPoint3D> pointList = new ArrayList<>();
            for (int geomNr = 0; geomNr < polygon.getNumGeometries(); geomNr++)
            {
                if (!pointList.isEmpty())
                {
                    pointList.add(PaintPolygons.NEWPATH);
                }
                Geometry geom = polygon.getGeometryN(geomNr);
                for (Coordinate c : geom.getCoordinates())
                {
                    pointList.add(new OTSPoint3D(c.x, c.y, -0.5));
                }
            }
            pBorder = OTSLine3D.createAndCleanOTSLine3D(pointList);

            new DistrictAnimation(this, simulator);
        }
        catch (RemoteException | NamingException | OTSGeometryException exception)
        {
            exception.printStackTrace();
        }

        this.border = pBorder;
    }

    /**
     * @return data
     */
    public final Map<String, Double> getData()
    {
        return this.data;
    }

    /**
     * Add data.
     * @param field field name
     * @param value data value (double)
     */
    public final void addData(final String field, final double value)
    {
        this.data.put(field, value);
    }

    /**
     * Get data.
     * @param field field name
     * @return data value (double)
     */
    public final double getData(final String field)
    {
        return this.data.get(field);
    }

    /**
     * @return polygon
     */
    public final MultiPolygon getPolygon()
    {
        return this.polygon;
    }

    /**
     * @return code
     */
    public final String getCode()
    {
        return this.code;
    }

    /**
     * @return name
     */
    public final String getName()
    {
        return this.name;
    }

    /**
     * @return code2
     */
    public final String getCode2()
    {
        return this.code2;
    }

    /**
     * @return centroid
     */
    public final OTSPoint3D getCentroid()
    {
        return this.centroid;
    }

    /**
     * @return border
     */
    public final OTSLine3D getBorder()
    {
        return this.border;
    }

    /**
     * @return simulator
     */
    public final OTSDEVSSimulatorInterface getSimulator()
    {
        return this.simulator;
    }

    /**
     * @return population
     */
    public final double getPopulation()
    {
        return this.population;
    }

    /**
     * @param population set population
     */
    public final void setPopulation(double population)
    {
        this.population = population;
    }

    /** {@inheritDoc} */
    @Override
    public DirectedPoint getLocation() throws RemoteException
    {
        return new DirectedPoint(this.centroid.x, this.centroid.y, -0.5);
    }

    /** {@inheritDoc} */
    @Override
    public Bounds getBounds() throws RemoteException
    {
        Geometry envelope = this.polygon.getEnvelope();
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -1;
        double maxY = -1;
        for (Coordinate c : envelope.getCoordinates())
        {
            minX = c.x < minX ? c.x : minX;
            minY = c.y < minY ? c.y : minY;
            maxX = c.x > maxX ? c.x : maxX;
            maxY = c.y > maxY ? c.y : maxY;
        }
        // double dx = (maxX - minX);
        // double dy = (maxY - minY);
        // return new BoundingBox(dx, dy, 0.0);
        return new BoundingBox(new Point3d(-100, -100, -1), new Point3d(200, 200, 1));
    }

    /**
     * @return flooded
     */
    public boolean isFlooded()
    {
        return this.flooded;
    }

    /**
     * @param flooded set flooded
     */
    public void setFlooded(boolean flooded)
    {
        this.flooded = flooded;
    }

    /**
     * @return centroid
     */
    public DistrictCentroid getDistrictCentroid()
    {
        return this.districtCentroid;
    }

    /**
     * @param centroid set centroid
     */
    public void setDistrictCentroid(DistrictCentroid centroid)
    {
        this.districtCentroid = centroid;
    }

    /**
     * @return truckCapacityTonnes
     */
    public double getTruckCapacityTonnes()
    {
        return this.truckCapacityTonnes;
    }

    /**
     * @param truckCapacityTonnes set truckCapacityTonnes
     */
    public void setTruckCapacityTonnes(double truckCapacityTonnes)
    {
        this.truckCapacityTonnes = truckCapacityTonnes;
    }

    /**
     * @param tonnes add to truckCapacityTonnes
     */
    public void addTruckCapacityTonnes(double tonnes)
    {
        this.truckCapacityTonnes += tonnes;
    }

    /**
     * @return waterwayCapacityTonnes
     */
    public double getWaterwayCapacityTonnes()
    {
        return this.waterwayCapacityTonnes;
    }

    /**
     * @param waterwayCapacityTonnes set waterwayCapacityTonnes
     */
    public void setWaterwayCapacityTonnes(double waterwayCapacityTonnes)
    {
        this.waterwayCapacityTonnes = waterwayCapacityTonnes;
    }

    /**
     * @param tonnes add to waterwayCapacityTonnes
     */
    public void addWaterwayCapacityTonnes(double tonnes)
    {
        this.waterwayCapacityTonnes += tonnes;
    }

    /**
     * @return railCapacityTonnes
     */
    public double getRailCapacityTonnes()
    {
        return this.railCapacityTonnes;
    }

    /**
     * @param railCapacityTonnes set railCapacityTonnes
     */
    public void setRailCapacityTonnes(double railCapacityTonnes)
    {
        this.railCapacityTonnes = railCapacityTonnes;
    }

    /**
     * @param tonnes add to railCapacityTonnes
     */
    public void addWailCapacityTonnes(double tonnes)
    {
        this.railCapacityTonnes += tonnes;
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(final District d)
    {
        return this.getCode2().compareTo(d.getCode2());
    }

}
