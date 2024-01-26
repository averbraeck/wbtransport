package nl.tudelft.pa.wbtransport.water.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.opentrafficsim.core.animation.ClonableRenderable2DInterface;
import org.opentrafficsim.core.animation.TextAlignment;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.animation.PaintLine;

import nl.tudelft.pa.wbtransport.water.WaterwayLink;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

/**
 * Draws a Waterway.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opensource.org/licenses/BSD-3-Clause">BSD 3-Clause License</a>.
 * <p>
 * $LastChangedDate: 2017-01-16 01:48:07 +0100 (Mon, 16 Jan 2017) $, @version $Revision: 3281 $, by $Author: averbraeck $,
 * initial version Sep 13, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class WaterwayAnimation extends Renderable2D<WaterwayLink>
        implements ClonableRenderable2DInterface<WaterwayLink>, Serializable
{
    /** */
    private static final long serialVersionUID = 20140000L;

    /** */
    private float width;

    /**
     * @param waterwayLink WaterwayLink
     * @param simulator simulator
     * @param width width
     * @throws NamingException for problems with registering in context
     * @throws RemoteException on communication failure
     */
    public WaterwayAnimation(final WaterwayLink waterwayLink, final OTSSimulatorInterface simulator, final float width)
            throws NamingException, RemoteException
    {
        super(waterwayLink, simulator);
        this.width = width;

        WaterwayTextAnimation wta = new WaterwayTextAnimation(waterwayLink, waterwayLink.getId(), 0.0f, 9.0f,
                TextAlignment.CENTER, Color.BLACK, 10.0f, simulator);
        wta.setRotate(false);
        wta.setScale(false);
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer) throws RemoteException
    {
        PaintLine.paintLine(graphics, Color.BLUE, this.width, getSource().getLocation(), getSource().getDesignLine());
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public ClonableRenderable2DInterface<WaterwayLink> clone(final WaterwayLink newSource,
            final OTSSimulatorInterface newSimulator) throws NamingException, RemoteException
    {
        // the constructor also constructs the corresponding Text object
        return new WaterwayAnimation(newSource, newSimulator, this.width);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "WaterwayAnimation [width=" + this.width + ", link=" + super.getSource() + "]";
    }

}
