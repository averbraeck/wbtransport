package nl.tudelft.pa.wbtransport.bridge.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.opentrafficsim.core.animation.TextAlignment;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;

import nl.tudelft.pa.wbtransport.bridge.Bridge;
import nl.tudelft.pa.wbtransport.bridge.BridgeBGD;
import nl.tudelft.simulation.dsol.animation.D2.Renderable2D;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opensource.org/licenses/BSD-3-Clause">BSD 3-Clause License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jan 5, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class BridgeAnimation extends Renderable2D<Bridge> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150130L;

    /**
     * Construct a SensorAnimation.
     * @param bridge the bridge to draw
     * @param simulator OTSSimulatorInterface; the simulator to schedule on
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException in case of remote registration failure of the animation
     */
    public BridgeAnimation(final Bridge bridge, final OTSSimulatorInterface simulator)
            throws NamingException, RemoteException
    {
        super(bridge, simulator);
        this.setScale(false);

        BridgeTextAnimation bta = new BridgeTextAnimation(bridge, bridge.getName(), 0.0f, 9.0f, TextAlignment.CENTER,
                Color.BLACK, 10.0f, simulator);
        bta.setRotate(false);
        bta.setScale(false);
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        Color color;
        switch (((BridgeBGD) getSource()).getCondition())
        {
            case "A":
                color = Color.GREEN;
                break;

            case "B":
                color = Color.ORANGE;
                break;

            case "C":
                color = Color.RED;
                break;

            case "D":
                color = Color.BLACK;
                break;

            default:
                color = Color.DARK_GRAY;
                break;
        }
        graphics.setColor(color);
        Rectangle2D rectangle = new Rectangle2D.Double(-3, -3, 6, 6);
        graphics.fill(rectangle);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "BridgeAnimation [getSource()=" + this.getSource() + "]";
    }
}
