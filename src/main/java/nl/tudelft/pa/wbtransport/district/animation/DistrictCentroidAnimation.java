package nl.tudelft.pa.wbtransport.district.animation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.opentrafficsim.core.animation.TextAlignment;
import org.opentrafficsim.core.animation.TextAnimation;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;

import nl.tudelft.pa.wbtransport.district.DistrictCentroid;
import nl.tudelft.simulation.dsol.animation.Locatable;
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
public class DistrictCentroidAnimation extends Renderable2D<DistrictCentroid> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150130L;

    /**
     * Construct a DistrictCentroidAnimation.
     * @param districtCentroid the centroid draw
     * @param simulator the simulator
     * @throws NamingException in case of registration failure of the animation
     * @throws RemoteException in case of remote registration failure of the animation
     */
    public DistrictCentroidAnimation(final DistrictCentroid districtCentroid, final OTSSimulatorInterface simulator)
            throws NamingException, RemoteException
    {
        super(districtCentroid, simulator);
        this.setScale(false);

        DistrictCentroidTextAnimation dcta = new DistrictCentroidTextAnimation(districtCentroid,
                districtCentroid.getDistrict().getCode2(), 0.0f, 14.0f, TextAlignment.CENTER, Color.BLACK, 16.0f, simulator);
        dcta.setRotate(false);
        dcta.setScale(false);
    }

    /** {@inheritDoc} */
    @Override
    public final void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        graphics.setColor(Color.BLACK);
        Rectangle2D rectangle = new Rectangle2D.Double(-4, -4, 8, 8);
        graphics.fill(rectangle);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DistrictCentroidAnimation [getSource()=" + this.getSource() + "]";
    }

    /** */
    public class DistrictCentroidTextAnimation extends TextAnimation
    {
        /** */
        private static final long serialVersionUID = 20161211L;

        /**
         * @param source the object for which the text is displayed
         * @param text the text to display
         * @param dx the horizontal movement of the text, in meters
         * @param dy the vertical movement of the text, in meters
         * @param textPlacement where to place the text
         * @param color the color of the text
         * @param fontSize the font size. Default value is 2.0.
         * @param simulator the simulator
         * @throws NamingException when animation context cannot be created or retrieved
         * @throws RemoteException - when remote context cannot be found
         */
        public DistrictCentroidTextAnimation(final Locatable source, final String text, final float dx, final float dy,
                final TextAlignment textPlacement, final Color color, final float fontSize,
                final OTSSimulatorInterface simulator) throws RemoteException, NamingException
        {
            super(source, text, dx, dy, textPlacement, color, fontSize, simulator);
        }

        /** {@inheritDoc} */
        @Override
        @SuppressWarnings("checkstyle:designforextension")
        public TextAnimation clone(final Locatable newSource, final OTSSimulatorInterface newSimulator)
                throws RemoteException, NamingException
        {
            return new DistrictTextAnimation(newSource, getText(), getDx(), getDy(), getTextAlignment(), getColor(),
                    getFontSize(), newSimulator);
        }
    }
}
