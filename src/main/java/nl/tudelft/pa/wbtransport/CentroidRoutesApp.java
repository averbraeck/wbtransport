package nl.tudelft.pa.wbtransport;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.gui.OTSAnimationPanel;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.OTSSimulationException;
import org.opentrafficsim.simulationengine.SimpleSimulatorInterface;

import com.opencsv.CSVReader;

import nl.tudelft.pa.wbtransport.bridge.BridgeBGD;
import nl.tudelft.pa.wbtransport.bridge.animation.BridgeTextAnimation;
import nl.tudelft.pa.wbtransport.district.District;
import nl.tudelft.pa.wbtransport.district.DistrictCentroid;
import nl.tudelft.pa.wbtransport.district.animation.DistrictDemandAnimation;
import nl.tudelft.pa.wbtransport.district.animation.DistrictTextAnimation;
import nl.tudelft.pa.wbtransport.gis.GISLayer;
import nl.tudelft.pa.wbtransport.port.LandPort;
import nl.tudelft.pa.wbtransport.port.Port;
import nl.tudelft.pa.wbtransport.road.LRP;
import nl.tudelft.pa.wbtransport.road.RoadSegment;
import nl.tudelft.pa.wbtransport.road.SegmentN;
import nl.tudelft.pa.wbtransport.road.SegmentR;
import nl.tudelft.pa.wbtransport.road.SegmentZ;
import nl.tudelft.pa.wbtransport.road.animation.LRPTextAnimation;
import nl.tudelft.pa.wbtransport.road.animation.RoadNTextAnimation;
import nl.tudelft.pa.wbtransport.road.animation.RoadRTextAnimation;
import nl.tudelft.pa.wbtransport.road.animation.RoadZTextAnimation;
import nl.tudelft.pa.wbtransport.water.WaterwayLink;
import nl.tudelft.pa.wbtransport.water.WaterwayTerminal;
import nl.tudelft.pa.wbtransport.water.animation.WaterwayTerminalAnimation;
import nl.tudelft.pa.wbtransport.water.animation.WaterwayTextAnimation;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * App version with a GUI and animation for the WorldBank CentroidRoutes model.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opensource.org/licenses/BSD-3-Clause">BSD 3-Clause License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jan 5, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class CentroidRoutesApp extends AbstractWrappableAnimation
{
    /** */
    private static final long serialVersionUID = 1L;

    /** */
    private CentroidRoutesAnimationModel centroidRoutesModel;

    /** the model parameters. */
    Map<String, Object> parameters = new HashMap<>();

    /** the output statistics. */
    private Map<String, Object> statistics = new HashMap<>();

    /** user interface params. */
    private Map<String, String[]> uiParams = new HashMap<>();

    /** selected product in UI. */
    String selectedProduct = "None";

    /** folder name. */
    private String path;

    /**
     * Main program.
     * @param args String[]; the command line arguments (not used)
     */
    public static void main(final String[] args)
    {
        if (args.length < 1)
        {
            System.err.println("Use as: java -jar bgd-app.jar path");
            System.err.println(
                    "where path is the parth to parameters.tsv as well as to the od, economics, gis, and other folders");
            System.exit(-1);
        }
        String path = args[0];
        String filename = path + "/parameters.tsv";
        CentroidRoutesApp worldbankModel = new CentroidRoutesApp(path);
        try
        {
            FileInputStream fis;
            if (new File(filename).canRead())
                fis = new FileInputStream(filename);
            else
                fis = new FileInputStream(CentroidRoutesModel.class.getResource(filename).getFile());
            try (CSVReader reader = new CSVReader(new InputStreamReader(fis), '\t', '"', 0))
            {
                String[] parts;
                while ((parts = reader.readNext()) != null)
                {
                    worldbankModel.uiParams.put(parts[0].toUpperCase(), parts);
                    if (parts.length > 0)
                    {
                        if (parts[0].equals("Flood_area"))
                            worldbankModel.parameters.put(parts[0].toUpperCase(), parts[1]);
                        else
                            worldbankModel.parameters.put(parts[0].toUpperCase(), Double.parseDouble(parts[1]));
                    }
                }
            }
            worldbankModel.uiParams.put("Const_WarmupDays".toUpperCase(), new String[] { "Const_WarmupDays", "10.0", "Double",
                    "0.0", "365.0", "Simulation warmup period", "(days)" });
            worldbankModel.uiParams.put("Const_RunLength".toUpperCase(), new String[] { "Const_RunLength", "30.0", "Double",
                    "0.0", "365.0", "Simulation run length", "(days); includes warmup period" });
        }
        catch (IOException exception)
        {
            System.err.println("Could not read from " + args[0] + "/parameters.tsv");
            exception.printStackTrace();
            System.exit(-1);
        }

        new ParameterDialog(worldbankModel.parameters, worldbankModel.uiParams);

        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    double warmup =
                            Double.parseDouble(worldbankModel.parameters.get("Const_WarmupDays".toUpperCase()).toString());
                    double runlength =
                            Double.parseDouble(worldbankModel.parameters.get("Const_RunLength".toUpperCase()).toString());
                    worldbankModel.buildAnimator(Time.ZERO, new Duration(warmup, DurationUnit.DAY),
                            new Duration(runlength, DurationUnit.DAY),
                            new ArrayList<org.opentrafficsim.base.modelproperties.Property<?>>(), null, true);
                }
                catch (SimRuntimeException | NamingException | OTSSimulationException | PropertyException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
    }

    /**
     * @param path the path of the files.
     */
    public CentroidRoutesApp(final String path)
    {
        super();
        this.path = path;
        if (!this.path.endsWith("/"))
            this.path += "/";
    }

    /** {@inheritDoc} */
    @Override
    public final String shortName()
    {
        return "CentroidRoutes";
    }

    /** {@inheritDoc} */
    @Override
    public final String description()
    {
        return "CentroidRoutes";
    }

    /** {@inheritDoc} */
    @Override
    public final void stopTimersThreads()
    {
        super.stopTimersThreads();
    }

    /** {@inheritDoc} */
    @Override
    protected final void addTabs(final SimpleSimulatorInterface simulator)
    {
        return;
    }

    /** {@inheritDoc} */
    @Override
    protected final OTSModelInterface makeModel(final GTUColorer colorer)
    {
        this.centroidRoutesModel = new CentroidRoutesAnimationModel(this.path, this.parameters, this.statistics, this);
        return this.centroidRoutesModel;
    }

    /** {@inheritDoc} */
    @Override
    protected final java.awt.geom.Rectangle2D.Double makeAnimationRectangle()
    {
        return new Rectangle2D.Double(87.8, 20.2, 5.0, 6.6);
    }

    /** {@inheritDoc} */
    @Override
    protected void addAnimationToggles()
    {
        this.addToggleAnimationButtonText("Districts", District.class, "Show/hide Districts", true);
        this.addToggleAnimationButtonText("DistrictId", DistrictTextAnimation.class, "Show/hide District Ids", false);
        this.addToggleAnimationButtonText("LRPs", LRP.class, "Show/hide LRPs", false);
        this.addToggleAnimationButtonText("LRPId", LRPTextAnimation.class, "Show/hide LRP Ids", false);
        this.addToggleAnimationButtonText("Waterways", WaterwayLink.class, "Show/hide waterways", true);
        this.addToggleAnimationButtonText("WaterwayId", WaterwayTextAnimation.class, "Show/hide waterway Ids", false);
        this.addToggleAnimationButtonText("WwTerm", WaterwayTerminal.class, "Show/hide waterway terminals", false);
        this.addToggleAnimationButtonText("WwTermId", WaterwayTerminalAnimation.WaterwayTerminalTextAnimation.class,
                "Show/hide waterway terminal Ids", false);
        this.addToggleAnimationButtonText("N-Roads", SegmentN.class, "Show/hide N-roads", true);
        this.addToggleAnimationButtonText("N-RoadId", RoadNTextAnimation.class, "Show/hide N-road Ids", false);
        this.addToggleAnimationButtonText("R-Roads", SegmentR.class, "Show/hide R-roads", true);
        this.addToggleAnimationButtonText("R-RoadId", RoadRTextAnimation.class, "Show/hide R-road Ids", false);
        this.addToggleAnimationButtonText("Z-Roads", SegmentZ.class, "Show/hide Z-roads", true);
        this.addToggleAnimationButtonText("Z-RoadId", RoadZTextAnimation.class, "Show/hide Z-road Ids", false);
        this.addToggleAnimationButtonText("Bridge", BridgeBGD.class, "Show/hide bridges", false);
        this.addToggleAnimationButtonText("BridgeId", BridgeTextAnimation.class, "Show/hide bridge Ids", false);

        this.panel.addToggleText(" ");
        this.panel.addToggleText(" GIS Layers");
        this.panel.addToggleAnimationButtonText("Roads", RoadGISLayer.class, "Show/hide GIS road layer", false);
        this.panel.addToggleAnimationButtonText("Railways", RailGISLayer.class, "Show/hide GIS rail layer", false);
        this.panel.addToggleAnimationButtonText("Rivers", WaterGISLayer.class, "Show/hide GIS waterway layer", false);
        this.panel.addToggleGISButtonText("WFP-water", "WFP-water", this.centroidRoutesModel.getWfpLayer().getGisRenderable2D(),
                "Show/hide WFP waterway layer");

        this.panel.addToggleText(" ");
        this.panel.addToggleText(" Transport");
        ButtonGroup group = new ButtonGroup();
        this.addToggleProductButtonText(group, "None", RoadSegment.class, "Garment transport", false);
        this.addToggleProductButtonText(group, "Garment", RoadSegment.class, "Garment transport", false);
        this.addToggleProductButtonText(group, "Textile", RoadSegment.class, "Textile transport", false);
        this.addToggleProductButtonText(group, "Food", RoadSegment.class, "Food transport", false);
        this.addToggleProductButtonText(group, "Steel", RoadSegment.class, "Steel transport", false);
        this.addToggleProductButtonText(group, "Brick", RoadSegment.class, "Brick transport", false);

        hideAnimationClass(DistrictCentroid.class);
        hideAnimationClass(DistrictDemandAnimation.DistrictProducts.class);
        hideAnimationClass(Port.class);
        hideAnimationClass(LandPort.class);

        insertScrollPane();

        // this.panel.addToggleGISButtonText("buildings", "Buildings", this.gisWaterwayImport.getGisMap(),
        // "Turn GIS building map layer on or off");
        // this.panel.hideGISLayer("buildings");
    }

    /**
     * Insert a scrollpane with a vertical scrollbar between the animation panel and the toggle panel. Use reflection to get
     * access...
     */
    public final void insertScrollPane()
    {
        // togglePanel is unfortunately private. Get access to it anyhow...
        try
        {
            java.lang.reflect.Field togglePanelField = OTSAnimationPanel.class.getDeclaredField("togglePanel");
            togglePanelField.setAccessible(true);
            JPanel togglePanel = (JPanel) togglePanelField.get(this.panel);

            java.lang.reflect.Field borderPanelField = OTSAnimationPanel.class.getDeclaredField("borderPanel");
            borderPanelField.setAccessible(true);
            JPanel borderPanel = (JPanel) borderPanelField.get(this.panel);

            borderPanel.remove(togglePanel);

            JScrollPane scrollPane = new JScrollPane(togglePanel);
            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.add(scrollPane, BorderLayout.CENTER);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

            borderPanel.add(wrapper, BorderLayout.WEST);
            togglePanelField.set(this.panel, wrapper);

            this.panel.revalidate();
        }
        catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException exception)
        {
            exception.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Add a button for toggling an product class on or off.
     * @param group the button group
     * @param name the name of the button
     * @param locatableClass the class for which the button holds (e.g., GTU.class)
     * @param toolTipText the tool tip text to show when hovering over the button
     * @param initiallyVisible whether the class is initially shown or not
     */
    public final void addToggleProductButtonText(final ButtonGroup group, final String name,
            final Class<? extends Locatable> locatableClass, final String toolTipText, final boolean initiallyVisible)
    {
        JPanel togglePanel = null;
        // togglePanel is unfortunately private. Get access to it anyhow...
        try
        {
            java.lang.reflect.Field togglePanelField = OTSAnimationPanel.class.getDeclaredField("togglePanel");
            togglePanelField.setAccessible(true);
            togglePanel = (JPanel) togglePanelField.get(this.panel);
        }
        catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException exception)
        {
            exception.printStackTrace();
            System.exit(-1);
        }
        final JPanel finalTogglePanel = togglePanel;

        JRadioButton button = new JRadioButton(name);
        button.setName(name);
        if (name.equals("None"))
            button.setSelected(true);
        button.setActionCommand(name);
        button.setToolTipText(toolTipText);
        button.addActionListener(new ActionListener()
        {
            @SuppressWarnings("synthetic-access")
            @Override
            public void actionPerformed(ActionEvent e)
            {
                CentroidRoutesApp.this.selectedProduct = name;
                if (name.equals("None"))
                {
                    // turn on the roads
                    showAnimationClass(SegmentN.class);
                    showAnimationClass(SegmentR.class);
                    showAnimationClass(SegmentZ.class);
                    showAnimationClass(WaterwayLink.class);
                    showGISLayer("WFP-water");
                    hideAnimationClass(DistrictCentroid.class);
                    hideAnimationClass(DistrictDemandAnimation.DistrictProducts.class);
                    hideAnimationClass(Port.class);
                    hideAnimationClass(LandPort.class);
                    hideAnimationClass(WaterwayTerminal.class);
                }
                else
                {
                    // turn off the roads
                    hideAnimationClass(SegmentN.class);
                    hideAnimationClass(SegmentR.class);
                    hideAnimationClass(SegmentZ.class);
                    hideAnimationClass(WaterwayLink.class);
                    hideGISLayer("WFP-water");
                    showAnimationClass(DistrictCentroid.class);
                    showAnimationClass(DistrictDemandAnimation.DistrictProducts.class);
                    showAnimationClass(Port.class);
                    showAnimationClass(LandPort.class);
                    showAnimationClass(WaterwayTerminal.class);
                }

                // change the checkboxes
                for (Component c : finalTogglePanel.getComponents())
                {
                    if (c instanceof JPanel)
                    {
                        for (Component b : ((JPanel) c).getComponents())
                        {
                            if (b instanceof JCheckBox && b.getName().endsWith("-Roads"))
                            {
                                ((JCheckBox) b).setSelected(name.equals("None"));
                            }
                            if (b instanceof JCheckBox && b.getName().equals("WwTerm"))
                            {
                                ((JCheckBox) b).setSelected(name.equals("None"));
                            }
                            if (b instanceof JCheckBox && b.getName().equals("Waterways"))
                            {
                                ((JCheckBox) b).setSelected(name.equals("None"));
                            }
                            if (b instanceof JCheckBox && b.getName().equals("WFP-water"))
                            {
                                ((JCheckBox) b).setSelected(name.equals("None"));
                            }
                        }
                    }
                }

                finalTogglePanel.repaint();
                CentroidRoutesApp.this.panel.repaint();
            }
        });
        group.add(button);

        JPanel radioBox = new JPanel();
        radioBox.setLayout(new BoxLayout(radioBox, BoxLayout.X_AXIS));
        radioBox.add(button);

        togglePanel.add(radioBox);
        radioBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    /**
     * @return selectedProduct
     */
    public final String getSelectedProduct()
    {
        return this.selectedProduct;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "CentroidRoutes []";
    }

    /** */
    static class RoadGISLayer extends GISLayer
    {
        /** */
        private static final long serialVersionUID = 1L;

        @SuppressWarnings("javadoc")
        public RoadGISLayer(String filename, OTSSimulatorInterface simulator, double z, Color outlineColor, float lineWidth)
                throws IOException
        {
            super(filename, simulator, z, outlineColor, lineWidth);
        }
    }

    /** */
    static class RailGISLayer extends GISLayer
    {
        /** */
        private static final long serialVersionUID = 1L;

        @SuppressWarnings("javadoc")
        public RailGISLayer(String filename, OTSSimulatorInterface simulator, double z, Color outlineColor, float lineWidth)
                throws IOException
        {
            super(filename, simulator, z, outlineColor, lineWidth);
        }
    }

    /** */
    static class WaterGISLayer extends GISLayer
    {
        /** */
        private static final long serialVersionUID = 1L;

        @SuppressWarnings("javadoc")
        public WaterGISLayer(String filename, OTSSimulatorInterface simulator, double z, Color outlineColor, float lineWidth)
                throws IOException
        {
            super(filename, simulator, z, outlineColor, lineWidth);
        }
    }

    //
    //
    //

    /** The dialog for the parameters. */
    private static class ParameterDialog extends JDialog implements ActionListener
    {
        /** */
        private static final long serialVersionUID = 1L;

        /** the fields with the parameters. */
        private List<Field> fields = new ArrayList<>();

        /** the map of parameters, mapping the upper-case name to the double / list / integer parameter. */
        private Map<String, Object> parameters;

        /** information from the parameters.tsv file. */
        Map<String, String[]> uiParams;

        /**
         * Construct a parameter dialog.
         * @param parameters the map of parameters, mapping the upper-case name to the double / list / integer parameter
         * @param uiParams information from the parameters.tsv file
         */
        public ParameterDialog(final Map<String, Object> parameters, final Map<String, String[]> uiParams)
        {
            super(null, "Worldbank Infrastructure application", Dialog.ModalityType.DOCUMENT_MODAL);
            this.uiParams = uiParams;
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            for (WindowListener wl : this.getWindowListeners())
                this.removeWindowListener(wl);
            this.addWindowListener(new WindowAdapter()
            {
                @Override
                public void windowClosing(WindowEvent e)
                {
                    System.exit(0);
                }
            });
            setPreferredSize(new Dimension(1024, 600));

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            this.parameters = parameters;
            JTabbedPane tabbedPane = new JTabbedPane();
            panel.add(tabbedPane);

            JPanel socioeconomicPanel = new JPanel();
            JPanel socioeconomicWrapper = new JPanel(new BorderLayout());
            socioeconomicWrapper.add(socioeconomicPanel, BorderLayout.NORTH);
            socioeconomicWrapper.add(Box.createGlue(), BorderLayout.CENTER);
            tabbedPane.addTab("Socio-economic", socioeconomicWrapper);
            GridLayout seGridLayout = new GridLayout(20, 3);
            socioeconomicPanel.setLayout(seGridLayout);

            this.fields.add(new DoubleField(socioeconomicPanel, "Textile_production"));
            this.fields.add(new DoubleField(socioeconomicPanel, "Textile_consumption"));
            this.fields.add(new DoubleField(socioeconomicPanel, "Textile_export"));
            this.fields.add(new DoubleField(socioeconomicPanel, "Textile_import"));
            this.fields.add(new DoubleField(socioeconomicPanel, "Garment_production"));
            this.fields.add(new DoubleField(socioeconomicPanel, "Garment_consumption"));
            this.fields.add(new DoubleField(socioeconomicPanel, "Garment_export"));
            this.fields.add(new DoubleField(socioeconomicPanel, "Garment_import"));
            this.fields.add(new DoubleField(socioeconomicPanel, "Steel_production"));
            this.fields.add(new DoubleField(socioeconomicPanel, "Steel_consumption"));
            this.fields.add(new DoubleField(socioeconomicPanel, "Steel_export"));
            this.fields.add(new DoubleField(socioeconomicPanel, "Steel_import"));
            this.fields.add(new DoubleField(socioeconomicPanel, "Brick_production"));
            this.fields.add(new DoubleField(socioeconomicPanel, "Brick_consumption"));
            this.fields.add(new DoubleField(socioeconomicPanel, "Brick_export"));
            this.fields.add(new DoubleField(socioeconomicPanel, "Brick_import"));
            this.fields.add(new DoubleField(socioeconomicPanel, "Food_production"));
            this.fields.add(new DoubleField(socioeconomicPanel, "Food_consumption"));
            this.fields.add(new DoubleField(socioeconomicPanel, "Food_export"));
            this.fields.add(new DoubleField(socioeconomicPanel, "Food_import"));

            JPanel transportModePanel = new JPanel();
            JPanel transportModeWrapper = new JPanel(new BorderLayout());
            transportModeWrapper.add(transportModePanel, BorderLayout.NORTH);
            transportModeWrapper.add(Box.createGlue(), BorderLayout.CENTER);
            tabbedPane.addTab("Transport Modes", transportModeWrapper);
            GridLayout tmGridLayout = new GridLayout(15, 3);
            transportModePanel.setLayout(tmGridLayout);

            this.fields.add(new DoubleField(transportModePanel, "Textile_road"));
            this.fields.add(new DoubleField(transportModePanel, "Textile_rail"));
            this.fields.add(new DoubleField(transportModePanel, "Textile_water"));
            this.fields.add(new DoubleField(transportModePanel, "Garment_road"));
            this.fields.add(new DoubleField(transportModePanel, "Garment_rail"));
            this.fields.add(new DoubleField(transportModePanel, "Garment_water"));
            this.fields.add(new DoubleField(transportModePanel, "Steel_road"));
            this.fields.add(new DoubleField(transportModePanel, "Steel_rail"));
            this.fields.add(new DoubleField(transportModePanel, "Steel_water"));
            this.fields.add(new DoubleField(transportModePanel, "Brick_road"));
            this.fields.add(new DoubleField(transportModePanel, "Brick_rail"));
            this.fields.add(new DoubleField(transportModePanel, "Brick_water"));
            this.fields.add(new DoubleField(transportModePanel, "Food_road"));
            this.fields.add(new DoubleField(transportModePanel, "Food_rail"));
            this.fields.add(new DoubleField(transportModePanel, "Food_water"));

            JPanel costPanel = new JPanel();
            JPanel costWrapper = new JPanel(new BorderLayout());
            costWrapper.add(costPanel, BorderLayout.NORTH);
            costWrapper.add(Box.createGlue(), BorderLayout.CENTER);
            tabbedPane.addTab("Cost", costWrapper);
            GridLayout costGridLayout = new GridLayout(3, 3);
            costPanel.setLayout(costGridLayout);

            this.fields.add(new DoubleField(costPanel, "Road_cost"));
            this.fields.add(new DoubleField(costPanel, "Water_cost"));
            this.fields.add(new DoubleField(costPanel, "Trs_cost"));

            JPanel damagePanel = new JPanel();
            JPanel damageWrapper = new JPanel(new BorderLayout());
            damageWrapper.add(damagePanel, BorderLayout.NORTH);
            damageWrapper.add(Box.createGlue(), BorderLayout.CENTER);
            tabbedPane.addTab("Damage", damageWrapper);
            GridLayout damageGridLayout = new GridLayout(18, 3);
            damagePanel.setLayout(damageGridLayout);

            this.fields.add(new SelectField(damagePanel, "Flood_area"));
            this.fields.add(new DoubleField(damagePanel, "Flood_duration"));
            this.fields.add(new DoubleField(damagePanel, "Damage_road_n"));
            this.fields.add(new DoubleField(damagePanel, "Damage_road_r"));
            this.fields.add(new DoubleField(damagePanel, "Damage_road_z"));
            this.fields.add(new DoubleField(damagePanel, "Damage_ferry"));
            this.fields.add(new DoubleField(damagePanel, "Damage_bridge_a"));
            this.fields.add(new DoubleField(damagePanel, "Damage_bridge_b"));
            this.fields.add(new DoubleField(damagePanel, "Damage_bridge_c"));
            this.fields.add(new DoubleField(damagePanel, "Damage_bridge_d"));
            this.fields.add(new DoubleField(damagePanel, "Damage_waterway_1"));
            this.fields.add(new DoubleField(damagePanel, "Damage_waterway_2"));
            this.fields.add(new DoubleField(damagePanel, "Damage_waterway_3"));
            this.fields.add(new DoubleField(damagePanel, "Damage_waterway_4"));
            this.fields.add(new DoubleField(damagePanel, "Damage_terminals"));
            this.fields.add(new DoubleField(damagePanel, "Damage_ports"));
            this.fields.add(new DoubleField(damagePanel, "Damage_railways"));
            this.fields.add(new DoubleField(damagePanel, "Damage_railstations"));

            JPanel constPanelTransport = new JPanel();
            JPanel constTransportWrapper = new JPanel(new BorderLayout());
            constTransportWrapper.add(constPanelTransport, BorderLayout.NORTH);
            constTransportWrapper.add(Box.createGlue(), BorderLayout.CENTER);
            tabbedPane.addTab("Const_Transport", constTransportWrapper);
            GridLayout constTransportGridLayout = new GridLayout(18, 3);
            constPanelTransport.setLayout(constTransportGridLayout);

            this.fields.add(new DoubleField(constPanelTransport, "Const_NFactor"));
            this.fields.add(new DoubleField(constPanelTransport, "Const_RFactor"));
            this.fields.add(new DoubleField(constPanelTransport, "Const_ZFactor"));
            this.fields.add(new DoubleField(constPanelTransport, "Const_AvgSpeed_Road_N"));
            this.fields.add(new DoubleField(constPanelTransport, "Const_AvgSpeed_Road_R"));
            this.fields.add(new DoubleField(constPanelTransport, "Const_AvgSpeed_Road_Z"));
            this.fields.add(new DoubleField(constPanelTransport, "Const_Ferry_Speed"));
            this.fields.add(new DoubleField(constPanelTransport, "Const_Ferry_Wait"));
            this.fields.add(new DoubleField(constPanelTransport, "Const_Barge_Speed_Cat1"));
            this.fields.add(new DoubleField(constPanelTransport, "Const_Barge_Speed_Cat2"));
            this.fields.add(new DoubleField(constPanelTransport, "Const_Barge_Speed_Cat3"));
            this.fields.add(new DoubleField(constPanelTransport, "Const_Barge_Speed_Cat4"));
            this.fields.add(new DoubleField(constPanelTransport, "Const_Barge_Tranship_Cat1"));
            this.fields.add(new DoubleField(constPanelTransport, "Const_Barge_Tranship_Cat2"));
            this.fields.add(new DoubleField(constPanelTransport, "Const_Barge_Tranship_Cat3"));
            this.fields.add(new DoubleField(constPanelTransport, "Const_Barge_Tranship_Cat4"));
            this.fields.add(new DoubleField(constPanelTransport, "Const_Rail_Speed"));
            this.fields.add(new DoubleField(constPanelTransport, "Const_Rail_Tranship"));

            JPanel constPanelOD = new JPanel();
            JPanel constODWrapper = new JPanel(new BorderLayout());
            constODWrapper.add(constPanelOD, BorderLayout.NORTH);
            constODWrapper.add(Box.createGlue(), BorderLayout.CENTER);
            tabbedPane.addTab("Const_OD", constODWrapper);
            GridLayout constODGridLayout = new GridLayout(10, 3);
            constPanelOD.setLayout(constODGridLayout);

            this.fields.add(new DoubleField(constPanelOD, "Const_Fraction_Local_Consumption_Textile"));
            this.fields.add(new DoubleField(constPanelOD, "Const_Fraction_Local_Consumption_Garment"));
            this.fields.add(new DoubleField(constPanelOD, "Const_Fraction_Local_Consumption_Brick"));
            this.fields.add(new DoubleField(constPanelOD, "Const_Fraction_Local_Consumption_Steel"));
            this.fields.add(new DoubleField(constPanelOD, "Const_Fraction_Local_Consumption_Food"));
            this.fields.add(new DoubleField(constPanelOD, "Const_Flooding_Production_Textile"));
            this.fields.add(new DoubleField(constPanelOD, "Const_Flooding_Production_Garment"));
            this.fields.add(new DoubleField(constPanelOD, "Const_Flooding_Production_Brick"));
            this.fields.add(new DoubleField(constPanelOD, "Const_Flooding_Production_Steel"));
            this.fields.add(new DoubleField(constPanelOD, "Const_Flooding_Production_Food"));

            JPanel treatmentPanel = new JPanel();
            JPanel treatmentWrapper = new JPanel(new BorderLayout());
            treatmentWrapper.add(treatmentPanel, BorderLayout.NORTH);
            treatmentWrapper.add(Box.createGlue(), BorderLayout.CENTER);
            tabbedPane.addTab("Treatment", treatmentWrapper);
            GridLayout treatmentGridLayout = new GridLayout(3, 3);
            treatmentPanel.setLayout(treatmentGridLayout);

            this.fields.add(new DoubleField(treatmentPanel, "Const_WarmupDays"));
            this.fields.add(new DoubleField(treatmentPanel, "Const_RunLength"));
            this.fields.add(new DoubleField(treatmentPanel, "Const_Flood_StartDay"));

            JPanel interventionPanel = new JPanel();
            JPanel interventionWrapper = new JPanel(new BorderLayout());
            interventionWrapper.add(interventionPanel, BorderLayout.NORTH);
            interventionWrapper.add(Box.createGlue(), BorderLayout.CENTER);
            tabbedPane.addTab("Interventions", interventionWrapper);
            GridLayout interventionGridLayout = new GridLayout(12, 3);
            interventionPanel.setLayout(interventionGridLayout);

            for (int i = 1; i <= 12; i++)
                this.fields.add(new BooleanField(interventionPanel, "Intervention_" + i));

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout());
            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            centerPanel.add(buttonPanel);
            panel.add(centerPanel);

            JButton startSimulationButton = new JButton("Start simulation model");
            startSimulationButton.addActionListener(this);
            buttonPanel.add(startSimulationButton);

            JButton cancelButton = new JButton("Cancel");
            buttonPanel.add(cancelButton);
            cancelButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    setVisible(false);
                    dispose();
                    System.exit(0);
                }
            });

            add(panel);

            pack();
            setVisible(true);
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            for (Field field : this.fields)
            {
                if (field instanceof DoubleField)
                {
                    this.parameters.put(field.getKey().toUpperCase(), ((DoubleField) field).getDoubleValue());
                }
                else if (field instanceof SelectField)
                {
                    this.parameters.put(field.getKey().toUpperCase(), ((SelectField) field).getValue());
                }
                else if (field instanceof StringField)
                {
                    this.parameters.put(field.getKey().toUpperCase(), ((StringField) field).getStringValue());
                }
                else if (field instanceof BooleanField)
                {
                    this.parameters.put(field.getKey().toUpperCase(), ((BooleanField) field).getValue());
                }
            }
            setVisible(false);
            dispose();
        }

        /** interface for fields. */
        private interface Field
        {
            /** @return the key of the field. */
            String getKey();
        }

        /** String field. */
        private class StringField implements Field
        {
            /** key for the field. */
            String key;

            /** field for the user interface. */
            JTextField textField;

            /**
             * Create a string field on the screen.
             * @param panel panel to add the field to
             * @param key key of the parameter
             */
            public StringField(JPanel panel, String key)
            {
                String[] ui = ParameterDialog.this.uiParams.get(key.toUpperCase());
                this.key = ui[0];
                JLabel label = new JLabel(ui[5]);
                this.textField = new JTextField(ui[2].equalsIgnoreCase("Double") ? 5 : 20);
                this.textField.setText(ui[1]);
                JLabel explanation = new JLabel(ui[6]);
                panel.add(label);
                panel.add(this.textField);
                panel.add(explanation);
            }

            /** {@inheritDoc} */
            @Override
            public final String getKey()
            {
                return this.key;
            }

            /** @return the string value of the field in the gui. */
            public String getStringValue()
            {
                return this.textField.getText();
            }
        }

        /** Double field. */
        private class DoubleField extends StringField
        {
            /**
             * Create a double field on the screen.
             * @param panel panel to add the field to
             * @param key key of the parameter
             */
            public DoubleField(JPanel panel, String key)
            {
                super(panel, key);
            }

            /** @return the double value of the field in the gui. */
            public double getDoubleValue()
            {
                return Double.valueOf(this.textField.getText());
            }
        }

        /** Selection from a list (combo box) field. */
        private class SelectField implements Field
        {
            /** key for the field. */
            String key;

            /** combo box foe the user interface. */
            JComboBox<String> selectField;

            /**
             * Create a selection field on the screen.
             * @param panel panel to add the field to
             * @param key key of the parameter
             */
            public SelectField(JPanel panel, String key)
            {
                String[] ui = ParameterDialog.this.uiParams.get(key.toUpperCase());
                this.key = ui[0];
                JLabel label = new JLabel(ui[5]);
                String[] selections = ui[3].split(",");
                this.selectField = new JComboBox<>(selections);
                for (int i = 0; i < selections.length; i++)
                {
                    if (ui[1].equals(selections[i]))
                    {
                        this.selectField.setSelectedIndex(i);
                        break;
                    }
                }
                JLabel explanation = new JLabel(ui[6]);
                panel.add(label);
                panel.add(this.selectField);
                panel.add(explanation);
            }

            /** {@inheritDoc} */
            @Override
            public final String getKey()
            {
                return this.key;
            }

            /** @return the string value of the selected field in the gui. */
            public String getValue()
            {
                return this.selectField.getSelectedItem().toString();
            }
        }

        /** Selection from a list (combo box) field. */
        private class BooleanField implements Field
        {
            /** key for the field. */
            String key;

            /** combo box foe the user interface. */
            JCheckBox checkField;

            /**
             * Create a checkbox field on the screen.
             * @param panel panel to add the field to
             * @param key key of the parameter
             */
            public BooleanField(JPanel panel, String key)
            {
                String[] ui = ParameterDialog.this.uiParams.get(key.toUpperCase());
                this.key = ui[0];
                JLabel label = new JLabel(ui[5]);
                this.checkField = new JCheckBox(key);
                JLabel explanation = new JLabel(ui[6]);
                panel.add(this.checkField);
                panel.add(label);
                panel.add(explanation);
            }

            /** {@inheritDoc} */
            @Override
            public final String getKey()
            {
                return this.key;
            }

            /** @return the string value of the selected field in the gui. */
            public boolean getValue()
            {
                return this.checkField.isSelected();
            }
        }

    }

}
