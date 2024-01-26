package nl.tudelft.pa.wbtransport;

import java.awt.Color;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;

import nl.tudelft.pa.wbtransport.CentroidRoutesApp.RailGISLayer;
import nl.tudelft.pa.wbtransport.CentroidRoutesApp.RoadGISLayer;
import nl.tudelft.pa.wbtransport.CentroidRoutesApp.WaterGISLayer;
import nl.tudelft.pa.wbtransport.gis.BackgroundLayer;
import nl.tudelft.pa.wbtransport.gis.GISLayer;
import nl.tudelft.pa.wbtransport.gis.ShapeFileLayer;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.D2.GisRenderable2D;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opensource.org/licenses/BSD-3-Clause">BSD 3-Clause License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Aug 27, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class CentroidRoutesAnimationModel extends CentroidRoutesModel
{
    /** */
    private static final long serialVersionUID = 1L;

    /** the GIS map. */
    protected GisRenderable2D gisMap;

    /** the wfp-river map. */
    private ShapeFileLayer wfpLayer;

    /**
     * @param fileFolder the location of the static input files of the simulation
     * @param parameters the parameters to run the simulation with
     * @param statistics the location where the statistics will be written
     * @param app the calling program that might be used to retrieve animation settings. can be null
     */
    public CentroidRoutesAnimationModel(final String fileFolder, final Map<String, Object> parameters,
            final Map<String, Object> statistics, final CentroidRoutesApp app)
    {
        super(fileFolder, parameters, statistics, app);
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel(SimulatorInterface<Time, Duration, OTSSimTimeDouble> pSimulator) throws SimRuntimeException
    {
        setAnimation(true);
        super.constructModel(pSimulator);
        OTSSimulatorInterface otsSimulator = (OTSSimulatorInterface) pSimulator;

        try
        {
            // background
            Color darkBlue = new Color(0, 0, 127);
            new BackgroundLayer(otsSimulator, -10.0, new Color(0, 0, 127));
            new RoadGISLayer("/gis/osm/roads.shp", otsSimulator, -0.5, Color.GRAY, 0f);
            new RailGISLayer("/gis/osm/railways.shp", otsSimulator, -0.5, Color.BLACK, 0f);
            new WaterGISLayer("/gis/osm/waterways.shp", otsSimulator, -0.5, Color.BLUE, 0.00005f);
            this.wfpLayer = new ShapeFileLayer("WFP-water", "/gis/wfp/BGD_WFP3.shp", otsSimulator, -0.5, darkBlue, darkBlue);

            Color countryColor = new Color(220, 220, 220);
            new GISLayer("/gis/gadm/BGD_adm2.shp", otsSimulator, -1.0, countryColor, 0, countryColor);
            new ShapeFileLayer("india", "/gis/osm-countries/india/INDIA.shp", otsSimulator, -1.0, Color.DARK_GRAY,
                    countryColor);
            new GISLayer("/gis/osm-countries/china/adminareas.shp", otsSimulator, -1.0, Color.DARK_GRAY, 0, countryColor);
            new GISLayer("/gis/osm-countries/nepal/NPL_adm1.shp", otsSimulator, -1.0, Color.DARK_GRAY, 0, countryColor);
            new GISLayer("/gis/osm-countries/srilanka/adminareas_lvl02.shp", otsSimulator, -1.0, Color.DARK_GRAY, 0,
                    countryColor);
            new GISLayer("/gis/osm-countries/bhutan/BTN_adm1.shp", otsSimulator, -1.0, Color.DARK_GRAY, 0, countryColor);
            new GISLayer("/gis/osm-countries/myanmar/mmr_polbnda_adm2_250k_mimu.shp", otsSimulator, -1.0, Color.DARK_GRAY, 0,
                    countryColor);
        }
        catch (Exception nwe)
        {
            nwe.printStackTrace();
        }

        // URL gisURL = URLResource.getResource("/gis/map.xml");
        // System.out.println("GIS-map file: " + gisURL.toString());
        // this.gisMap = new WBGisRenderable2D(otsSimulator, gisURL);
    }

    /**
     * @return gisMap
     */
    public final GisRenderable2D getGisMap()
    {
        return this.gisMap;
    }

    /**
     * @return wfpLayer
     */
    public final ShapeFileLayer getWfpLayer()
    {
        return this.wfpLayer;
    }


}
