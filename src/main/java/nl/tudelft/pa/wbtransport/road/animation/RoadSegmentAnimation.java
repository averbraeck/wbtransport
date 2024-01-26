package nl.tudelft.pa.wbtransport.road.animation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import javax.media.j3d.Bounds;
import javax.naming.NamingException;

import org.opentrafficsim.core.animation.ClonableRenderable2DInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;

import nl.tudelft.pa.wbtransport.CentroidRoutesApp;
import nl.tudelft.pa.wbtransport.network.ProductNetwork;
import nl.tudelft.pa.wbtransport.road.RoadSegment;
import nl.tudelft.pa.wbtransport.road.animation.RoadSegmentAnimation.RoadSegmentProducts;
import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Draws a RoadN.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opensource.org/licenses/BSD-3-Clause">BSD 3-Clause License</a>.
 * <p>
 * $LastChangedDate: 2017-01-16 01:48:07 +0100 (Mon, 16 Jan 2017) $, @version $Revision: 3281 $, by $Author: averbraeck $,
 * initial version Sep 13, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class RoadSegmentAnimation extends Renderable2D<RoadSegmentProducts>
        implements ClonableRenderable2DInterface<RoadSegmentProducts>, Serializable
{
    /** */
    private static final long serialVersionUID = 20140000L;

    /** pointer to the network with the maximum transport info to make a fraction. */
    private final ProductNetwork network;

    /** products we can draw. */
    private final List<String> products;

    /** pointer to the transport map with the transport info. */
    private Map<String, Double> transport;

    /** place to retrieve toggle for animation, can be null. */
    private final CentroidRoutesApp app;

    /**
     * @param roadSegment road segment
     * @param simulator simulator
     * @param products products
     * @param app place to retrieve toggle for animation, can be null
     * @throws NamingException for problems with registering in context
     * @throws RemoteException on communication failure
     */
    public RoadSegmentAnimation(final RoadSegment roadSegment, final OTSSimulatorInterface simulator,
            final List<String> products, final CentroidRoutesApp app) throws NamingException, RemoteException
    {
        super(new RoadSegmentProducts(roadSegment), simulator);
        this.products = products;
        this.transport = roadSegment.getTransport();
        this.network = (ProductNetwork) roadSegment.getNetwork();
        this.app = app;
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException
    {
        if (this.app == null)
            return;

        String product = this.app.getSelectedProduct();
        if (product.equals("None"))
            return;

        Double volume = this.transport.get(product);
        if (volume == null)
            return;

        Color roadColor = volume == 0 ? Color.DARK_GRAY : Color.RED;

        float w = (float) (50.0 * volume / this.network.getMaxProductTransport(product) * 0.0005); // 0 - 50

        if (getSource().getRoadSegment().getGap().isRoad())
        {
            RoadSegmentAnimation.paintLine(graphics, roadColor, w, getSource().getLocation(),
                    getSource().getRoadSegment().getDesignLine(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        }
        else
        {
            RoadSegmentAnimation.paintLine(graphics, roadColor, w, getSource().getLocation(),
                    getSource().getRoadSegment().getDesignLine(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
        }
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public ClonableRenderable2DInterface<RoadSegmentProducts> clone(final RoadSegmentProducts newSource,
            final OTSSimulatorInterface newSimulator) throws NamingException, RemoteException
    {
        // the constructor also constructs the corresponding Text object
        return new RoadSegmentAnimation(newSource.getRoadSegment(), newSimulator, this.products, this.app);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "RoadSegmentAnimation [products=" + this.products + ", link=" + super.getSource() + "]";
    }

    /**
     * Paint line.
     * @param graphics Graphics2D; the graphics environment
     * @param color Color; the color to use
     * @param width the width to use
     * @param referencePoint DirectedPoint; the reference point
     * @param line array of points
     * @param cap cap, e.g. BasicStroke.CAP_BUTT
     * @param join join, e.g., BasicStroke.JOIN_MITER
     */
    public static void paintLine(final Graphics2D graphics, final Color color, final double width,
            final DirectedPoint referencePoint, final OTSLine3D line, final int cap, final int join)
    {
        graphics.setColor(color);
        Stroke oldStroke = graphics.getStroke();
        graphics.setStroke(new BasicStroke((float) width, cap, join));
        Path2D.Double path = new Path2D.Double();
        OTSPoint3D point = line.getFirst();
        path.moveTo(point.x - referencePoint.x, -point.y + referencePoint.y);
        for (int i = 1; i < line.getPoints().length; i++)
        {
            point = line.getPoints()[i];
            path.lineTo(point.x - referencePoint.x, -point.y + referencePoint.y);
        }
        graphics.draw(path);
        graphics.setStroke(oldStroke);
    }

    /** class to hold the locatable, not dependent on RoadSegment, so it will not trigger SegmentN, R, Z animation toggles. */
    public static class RoadSegmentProducts implements Locatable
    {
        /** the road segment to which this animation helper belongs. */
        private final RoadSegment roadSegment;

        /**
         * @param roadSegment the road segment to which this animation helper belongs.
         */
        public RoadSegmentProducts(RoadSegment roadSegment)
        {
            super();
            this.roadSegment = roadSegment;
        }

        /** {@inheritDoc} */
        @Override
        public DirectedPoint getLocation() throws RemoteException
        {
            return this.roadSegment.getLocation();
        }

        /** {@inheritDoc} */
        @Override
        public Bounds getBounds() throws RemoteException
        {
            return this.roadSegment.getBounds();
        }

        /**
         * @return roadSegment
         */
        public final RoadSegment getRoadSegment()
        {
            return this.roadSegment;
        }
    }
}
