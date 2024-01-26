package nl.tudelft.pa.wbtransport.water.animation;

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
import nl.tudelft.pa.wbtransport.water.WaterwayLink;
import nl.tudelft.pa.wbtransport.water.animation.WaterwayProductAnimation.WaterwayLinkProducts;
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
public class WaterwayProductAnimation extends Renderable2D<WaterwayLinkProducts>
        implements ClonableRenderable2DInterface<WaterwayLinkProducts>, Serializable
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

    /** light blue color. */
    private static Color DARK_BLUE = new Color(138, 43, 226);

    /**
     * @param waterwayLink waterway link
     * @param simulator simulator
     * @param products products
     * @param app place to retrieve toggle for animation, can be null
     * @throws NamingException for problems with registering in context
     * @throws RemoteException on communication failure
     */
    public WaterwayProductAnimation(final WaterwayLink waterwayLink, final OTSSimulatorInterface simulator,
            final List<String> products, final CentroidRoutesApp app) throws NamingException, RemoteException
    {
        super(new WaterwayLinkProducts(waterwayLink), simulator);
        this.products = products;
        this.transport = waterwayLink.getTransport();
        this.network = (ProductNetwork) waterwayLink.getNetwork();
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

        Color roadColor = volume == 0 ? Color.DARK_GRAY : DARK_BLUE;

        float w = (float) (50.0 * volume / this.network.getMaxProductTransport(product) * 0.0005); // 0 - 50

        WaterwayProductAnimation.paintLine(graphics, roadColor, w, getSource().getLocation(),
                getSource().getWaterwayLink().getDesignLine(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public ClonableRenderable2DInterface<WaterwayLinkProducts> clone(final WaterwayLinkProducts newSource,
            final OTSSimulatorInterface newSimulator) throws NamingException, RemoteException
    {
        // the constructor also constructs the corresponding Text object
        return new WaterwayProductAnimation(newSource.getWaterwayLink(), newSimulator, this.products, this.app);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "WaterwayLinkAnimation [products=" + this.products + ", link=" + super.getSource() + "]";
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

    /** class to hold the locatable, not dependent on WaterwayLink, so it will not trigger standard animation toggles. */
    public static class WaterwayLinkProducts implements Locatable
    {
        /** the waterway link to which this animation helper belongs. */
        private final WaterwayLink waterwayLink;

        /**
         * @param waterwayLink the waterway link to which this animation helper belongs.
         */
        public WaterwayLinkProducts(WaterwayLink waterwayLink)
        {
            super();
            this.waterwayLink = waterwayLink;
        }

        /** {@inheritDoc} */
        @Override
        public DirectedPoint getLocation() throws RemoteException
        {
            return this.waterwayLink.getLocation();
        }

        /** {@inheritDoc} */
        @Override
        public Bounds getBounds() throws RemoteException
        {
            return this.waterwayLink.getBounds();
        }

        /**
         * @return waterwayLink
         */
        public final WaterwayLink getWaterwayLink()
        {
            return this.waterwayLink;
        }
    }
}
