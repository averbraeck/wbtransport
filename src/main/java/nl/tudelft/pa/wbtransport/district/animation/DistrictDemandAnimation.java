package nl.tudelft.pa.wbtransport.district.animation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.media.j3d.Bounds;
import javax.naming.NamingException;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.animation.PaintPolygons;

import nl.tudelft.pa.wbtransport.CentroidRoutesApp;
import nl.tudelft.pa.wbtransport.district.District;
import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opensource.org/licenses/BSD-3-Clause">BSD 3-Clause License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jan 5, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class DistrictDemandAnimation extends Renderable2D<DistrictDemandAnimation.DistrictProducts> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150130L;

    /** place to retrieve toggle for animation, can be null. */
    private final CentroidRoutesApp app;

    /**
     * Construct a District Animation.
     * @param district the district to draw
     * @param simulator OTSSimulatorInterface; the simulator to schedule on
     * @param app place to retrieve toggle for animation, can be null
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException in case of remote registration failure of the animation
     */
    public DistrictDemandAnimation(final District district, final OTSSimulatorInterface simulator, final CentroidRoutesApp app)
            throws NamingException, RemoteException
    {
        super(new DistrictProducts(district), simulator);
        this.app = app;
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        if (this.app == null)
            return;

        String product = this.app.getSelectedProduct();
        if (product.equals("None"))
            return;

        District district = getSource().getDistrict();
        double fractionUnsatisfiedDemand = district.getDistrictCentroid().getFractionUnsatisfiedDemand(product);
        int r, g, b;
        b = 180;
        if (fractionUnsatisfiedDemand < 0.5)
        {
            r = 180 + (int) ((255 - 180) * 2 * fractionUnsatisfiedDemand);
            g = 255;
        }
        else
        {
            r = 255;
            g = 180 + (int) ((255 - 180) * 2 * (fractionUnsatisfiedDemand - 1.0));
        }
        Color color = new Color(r, g, b);
        try
        {
            Paint p = graphics.getPaint();
            PaintPolygons.paintMultiPolygon(graphics, color, district.getLocation(), district.getBorder(), true);
            Stroke oldStroke = graphics.getStroke();
            graphics.setStroke(new BasicStroke(0.0005f));
            PaintPolygons.paintMultiPolygon(graphics, Color.DARK_GRAY, district.getLocation(), district.getBorder(), false);
            graphics.setStroke(oldStroke);
            graphics.setPaint(p);
        }
        catch (RemoteException exception)
        {
            exception.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(final Point2D pointWorldCoordinates, final Rectangle2D extent, final Dimension screen)
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DistrictAnimation [getSource()=" + this.getSource() + "]";
    }
    
    /** class to hold the locatable, not dependent on District, so it will not trigger district animation toggles. */
    public static class DistrictProducts implements Locatable
    {
        /** the road segment to which this animation helper belongs. */
        private final District district;

        /**
         * @param district the district to which this animation helper belongs.
         */
        public DistrictProducts(District district)
        {
            super();
            this.district = district;
        }

        /** {@inheritDoc} */
        @Override
        public DirectedPoint getLocation() throws RemoteException
        {
            return this.district.getLocation();
        }

        /** {@inheritDoc} */
        @Override
        public Bounds getBounds() throws RemoteException
        {
            return this.district.getBounds();
        }

        /**
         * @return district
         */
        public final District getDistrict()
        {
            return this.district;
        }
    }
}
