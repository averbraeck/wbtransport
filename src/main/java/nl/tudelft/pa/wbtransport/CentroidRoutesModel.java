package nl.tudelft.pa.wbtransport;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import javax.naming.NamingException;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkEdge;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;

import com.opencsv.CSVReader;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

import nl.tudelft.pa.wbtransport.bridge.BridgeBGD;
import nl.tudelft.pa.wbtransport.bridge.animation.BridgeAnimation;
import nl.tudelft.pa.wbtransport.district.District;
import nl.tudelft.pa.wbtransport.district.DistrictCentroid;
import nl.tudelft.pa.wbtransport.district.DistrictReader;
import nl.tudelft.pa.wbtransport.district.animation.DistrictDemandAnimation;
import nl.tudelft.pa.wbtransport.network.ProductNetwork;
import nl.tudelft.pa.wbtransport.port.ImportExportLocation;
import nl.tudelft.pa.wbtransport.port.LandPort;
import nl.tudelft.pa.wbtransport.port.Port;
import nl.tudelft.pa.wbtransport.road.CompleteRouteRoad;
import nl.tudelft.pa.wbtransport.road.Gap;
import nl.tudelft.pa.wbtransport.road.GapPoint;
import nl.tudelft.pa.wbtransport.road.LRP;
import nl.tudelft.pa.wbtransport.road.ODNode;
import nl.tudelft.pa.wbtransport.road.Road;
import nl.tudelft.pa.wbtransport.road.RoadLink;
import nl.tudelft.pa.wbtransport.road.RoadNode;
import nl.tudelft.pa.wbtransport.road.RoadSegment;
import nl.tudelft.pa.wbtransport.road.SegmentN;
import nl.tudelft.pa.wbtransport.road.SegmentR;
import nl.tudelft.pa.wbtransport.road.SegmentZ;
import nl.tudelft.pa.wbtransport.road.animation.LRPAnimation;
import nl.tudelft.pa.wbtransport.road.animation.RoadNAnimation;
import nl.tudelft.pa.wbtransport.road.animation.RoadRAnimation;
import nl.tudelft.pa.wbtransport.road.animation.RoadSegmentAnimation;
import nl.tudelft.pa.wbtransport.road.animation.RoadZAnimation;
import nl.tudelft.pa.wbtransport.util.ExcelUtil;
import nl.tudelft.pa.wbtransport.util.NumberUtil;
import nl.tudelft.pa.wbtransport.water.CompleteRouteWaterway;
import nl.tudelft.pa.wbtransport.water.Waterway;
import nl.tudelft.pa.wbtransport.water.WaterwayLink;
import nl.tudelft.pa.wbtransport.water.WaterwayNode;
import nl.tudelft.pa.wbtransport.water.WaterwayTerminal;
import nl.tudelft.pa.wbtransport.water.animation.WaterwayAnimation;
import nl.tudelft.pa.wbtransport.water.animation.WaterwayProductAnimation;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;
import nl.tudelft.simulation.language.io.URLResource;

/**
 * CentroidRoutes model for the TU Delft Policy Analysis Worldbank Bangladesh project.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
 * All rights reserved. BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2016-08-26 16:34:41 +0200 (Fri, 26 Aug 2016) $, @version $Revision: 2150 $, by $Author: gtamminga $,
 * initial version un 27, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class CentroidRoutesModel implements OTSModelInterface
{
    /** */
    private static final long serialVersionUID = 20141121L;

    /** The simulator. */
    private OTSDEVSSimulatorInterface simulator;

    /** The overall road / bridge network. */
    private final ProductNetwork roadNetwork = new ProductNetwork("Road network with product volumes");

    /** the simplified road network. */
    private final OTSNetwork simplifiedRoadNetwork = new OTSNetwork("Simplified road network");

    /** the simplified waterway network. */
    private final ProductNetwork waterwayNetwork = new ProductNetwork("Waterway network with product volumes");

    /** the districts by code. */
    private SortedMap<String, District> districtCodeMap = new TreeMap<>();

    /** the districts by name. */
    private SortedMap<String, District> districtNameMap = new TreeMap<>();

    /** the districts by 2-letter code. */
    private SortedMap<String, District> districtCode2Map = new TreeMap<>();

    /** ports. */
    private SortedMap<String, Port> ports = new TreeMap<>();

    /** land ports. */
    private SortedMap<String, LandPort> landPorts = new TreeMap<>();

    /** waterway terminals. */
    private SortedMap<String, WaterwayTerminal> waterwayTerminals = new TreeMap<>();

    /** waterway terminals by number. */
    private SortedMap<Integer, WaterwayTerminal> waterwayTerminalIndexMap = new TreeMap<>();

    /** Map from district code to Waterway Terminal in that district. */
    private SortedMap<String, WaterwayTerminal> districtWaterwayTerminalMap = new TreeMap<>();

    /** Map from terminal to the fastest route to/from the centroid in the same district as the terminal, regular mode. */
    private SortedMap<WaterwayTerminal, CompleteRouteRoad> waterwayTerminalCentroidRouteRegular = new TreeMap<>();

    /** Map from terminal to the fastest route to/from the centroid in the same district as the terminal, disturbed mode. */
    private SortedMap<WaterwayTerminal, CompleteRouteRoad> waterwayTerminalCentroidRouteDisturbed = new TreeMap<>();

    /** Matrix with waterway routes from terminal to terminal, regular mode. */
    private CompleteRouteWaterway[][] waterwayRegularMatrix;

    /** Matrix with waterway routes from terminal to terminal, disturbed mode. */
    private CompleteRouteWaterway[][] waterwayDisturbedMatrix;

    /** cached regular route times (in hours) by waterway between district centroids. */
    private double[][] waterwayRegularTimeMatrix;

    /** cached disturbed route times (in hours) by waterway between district centroids. */
    private double[][] waterwayDisturbedTimeMatrix;

    /** the waterways. */
    private SortedMap<String, Waterway> waterwayMap = new TreeMap<>();

    /** lrps. */
    private SortedMap<String, LRP> lrpMap = new TreeMap<>();

    /** lrps per district. */
    private SortedMap<District, List<LRP>> districtLRPMap = new TreeMap<>();

    /** coordinates. */
    private Map<OTSPoint3D, LRP> pointLRPMap = new HashMap<>();

    /** the bridges. */
    private SortedMap<String, List<BridgeBGD>> bridgeMap = new TreeMap<>();

    /** the roads. */
    private SortedMap<String, Road> roadMap = new TreeMap<>();

    /** regular road graph. */
    SimpleDirectedWeightedGraph<RoadNode, LinkEdge<RoadLink>> roadRegularGraph;

    /** disturbed road graph. */
    SimpleDirectedWeightedGraph<RoadNode, LinkEdge<RoadLink>> roadDisturbedGraph;

    /** regular waterway graph to determine terminal-terminal connections. */
    SimpleDirectedWeightedGraph<WaterwayNode, LinkEdge<WaterwayLink>> waterwayRegularGraph;

    /** disturbed waterway graph to determine terminal-terminal connections. */
    SimpleDirectedWeightedGraph<WaterwayNode, LinkEdge<WaterwayLink>> waterwayDisturbedGraph;

    /** cached regular routes by road between districts, including (land) ports. */
    private CompleteRouteRoad[][] roadRegularMatrix;

    /** cached disturbed routes by road between districts, including (land) ports. */
    private CompleteRouteRoad[][] roadDisturbedMatrix;

    /** cached regular route times (in hours) by road between districts, including (land) ports. */
    private double[][] roadRegularTimeMatrix;

    /** cached disturbed route times (in hours) by road between districts, including (land) ports. */
    private double[][] roadDisturbedTimeMatrix;

    /** flooded districts. */
    private Set<String> floodSet = new HashSet<>();

    /** products. */
    private List<String> productList = new ArrayList<>();

    /** the number of O/D pairs. */
    private int numberOD;

    /** the OD codes, first the districts, then the ports, then the land ports, each group alphabetical. */
    private String[] odCodes;

    /** the OD nodes, districts, ports, and land ports. */
    private ODNode[] odNodes;

    /** lookup map from code to index. */
    private Map<String, Integer> odLookupMap = new HashMap<>();

    /** the annual production per good per district by OD code in metric ton per year. */
    private SortedMap<String, Double[]> productProdMatrix = new TreeMap<>();

    /** the annual consumption per good per district by OD code in metric ton per year. */
    private SortedMap<String, Double[]> productConsMatrix = new TreeMap<>();

    /** the production - consumption volumes in metric ton per year (from/to OD codes). */
    private SortedMap<String, Double[][]> pcMatrix = new TreeMap<>();

    /** the model parameters. */
    private Map<String, Object> parameters = new HashMap<>();

    /** the results. */
    private Map<String, Double> outputTransportCost = new HashMap<>();

    /** the results. */
    private Map<String, Double> outputTravelTime = new HashMap<>();

    /** the results. */
    private Map<String, Double> outputUnsatisfiedDemand = new HashMap<>();

    /** the output statistics. */
    private Map<String, Object> statistics;

    /** the file folder as the root for all files. */
    private String fileFolder;

    /** animation or not. */
    private boolean animation = false;

    /** currently flooded? */
    private boolean flooded = false;

    /** the calling program that might be used to retrieve animation settings. can be null. */
    private final CentroidRoutesApp app;

    /** spur color. */
    private static final Color SPUR_COLOR = new Color(63, 0, 0);

    /**
     * @param fileFolder the location of the static input files of the simulation
     * @param parameters the parameters to run the simulation with
     * @param statistics the location where the statistics will be written
     * @param app the calling program that might be used to retrieve animation settings. can be null
     */
    public CentroidRoutesModel(final String fileFolder, final Map<String, Object> parameters,
            final Map<String, Object> statistics, final CentroidRoutesApp app)
    {
        this.fileFolder = fileFolder;
        this.parameters = parameters;
        this.statistics = statistics;
        this.app = app;

        // Initialize statistics
        this.statistics.clear();
        this.statistics.put("TransportCost", this.outputTransportCost);
        this.statistics.put("TravelTime", this.outputTravelTime);
        this.statistics.put("UnsatisfiedDemand", this.outputUnsatisfiedDemand);
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel(final SimulatorInterface<Time, Duration, OTSSimTimeDouble> pSimulator) throws SimRuntimeException
    {
        this.simulator = (OTSDEVSSimulatorInterface) pSimulator;

        try
        {
            readProductList(this.fileFolder + "/infrastructure/products.txt");
            readFloodSet(this.parameters.get("FLOOD_AREA").toString(), this.fileFolder + "/infrastructure/flood_locations.csv");
            readDistricts(this.fileFolder + "/gis/gadm/BGD_adm2.shp");

            // make sure the flooded districts calculate their disturbed routes as well
            setFlooded(true, false);

            // set the intervention constants early -- they might influence the building of objects.
            addInterventionsConstants();

            // economic data per district
            URL district7 = URLResource.getResource(this.fileFolder + "/economics/District_level_data_v7.csv");
            URL district6 = URLResource.getResource(this.fileFolder + "/economics/District_level_data_v6.csv");
            if (district7 != null && new File(district7.getPath()).canRead())
                readDistrictData(this.fileFolder + "/economics/District_level_data_v7.csv");
            else if (district6 != null && new File(district6.getPath()).canRead())
                readDistrictData(this.fileFolder + "/economics/District_level_data_v6.csv");

            // waterways
            getWaterways(this.fileFolder
                    + "/infrastructure/water/WaterwaysSailable/waterways_53routes_routable_final_processed.shp");
            readWaterwayOverrides(this.fileFolder + "/infrastructure/waterway_overrides.csv");

            // roads
            URL roadCsv5 = URLResource.getResource(this.fileFolder + "/infrastructure/_roads5.csv");
            URL roadOverride5 = URLResource.getResource(this.fileFolder + "/infrastructure/roads5_override.csv");
            URL roadCsv4 = URLResource.getResource(this.fileFolder + "/infrastructure/_roads4.csv");
            if (roadCsv5 != null && new File(roadCsv5.getPath()).canRead())
            {
                readLRPsCsv5(this.fileFolder + "/infrastructure/_lrps5.csv");
                readRoadsCsv5(this.fileFolder + "/infrastructure/_roads5.csv");
                if (roadOverride5 != null && new File(roadOverride5.getPath()).canRead())
                    readRoadsCsv5(this.fileFolder + "/infrastructure/roads5_override.csv");
            }
            else if (roadCsv4 != null && new File(roadCsv4.getPath()).canRead())
            {
                readRoadsCsv34(this.fileFolder + "/infrastructure/_roads4.csv");
                repairSmallGapsCrossings();
                writeLRPsCsv5(this.fileFolder + "/infrastructure/_lrps5.csv");
                writeRoadsCsv5(this.fileFolder + "/infrastructure/_roads5.csv");
            }
            else
            {
                System.err.println("Cannot read input file with LRPs");
                System.exit(-1);
            }

            // bridges
            readBridgesWorldBank(this.fileFolder + "/infrastructure/Bridges.xlsx");
            readBMMS(this.fileFolder + "/infrastructure/BMMS_overview.xlsx");
            addBridgesToRoadSegments();

            // add infrastructure interventions
            addInterventionsRoadBridgeWaterway();

            // read or make ports, land ports, and waterway terminals
            findDistrictCentroids();
            readPorts(this.fileFolder + "/infrastructure/Ports.csv");
            readLandPorts(this.fileFolder + "/infrastructure/LandPorts.csv");
            determineWaterwayTerminals(this.fileFolder + "/infrastructure/_waterway_terminals.csv");

            // add the port interventions
            addInterventionsPorts();

            // make the road network
            makeSimplifiedRoadNetwork();
            odMatrixInit();
            calculateODDistancesByRoad();

            // the model starts not-flooded
            setFlooded(false, false);

            makeWaterwayTerminalCentroidRouteRegular();
            makeWaterwayTerminalCentroidRouteDisturbed();
            calculateWaterwayODMatrix();

            makeODMatrix(this.fileFolder + "/economics/District_level_data_v7.csv");
            printOD();

            // check the fractions per product
            for (String product : this.productList)
            {
                double fractionRoad = (double) this.parameters.get(product.toUpperCase() + "_ROAD");
                double fractionWater = (double) this.parameters.get(product.toUpperCase() + "_WATER");
                double fractionRail = (double) this.parameters.get(product.toUpperCase() + "_RAIL");
                double tot = fractionRoad + fractionWater + fractionRail;
                if (tot == 0.0)
                {
                    System.err.println(
                            "Infractructure fractions for product " + product + " add to zero. Road transport assumed");
                }
            }

            initializeStatistics();
            scheduleDailyTransportEvents();

            Duration floodStart =
                    new Duration(Double.parseDouble(this.parameters.get("CONST_FLOOD_STARTDAY").toString()), DurationUnit.DAY);
            this.simulator.scheduleEventRel(floodStart, this, this, "scheduledFloodStart", new Object[] {});
            Duration floodEnd = floodStart
                    .plus(new Duration(Double.parseDouble(this.parameters.get("FLOOD_DURATION").toString()), DurationUnit.DAY));
            this.simulator.scheduleEventRel(floodEnd, this, this, "scheduledFloodEnd", new Object[] {});
            this.simulator.scheduleEventRel(this.simulator.getReplication().getTreatment().getWarmupPeriod(), this, this,
                    "initializeStatistics", new Object[] {});
        }
        catch (Exception nwe)
        {
            nwe.printStackTrace();
        }
    }

    /**
     * Read the product list.
     * @param filename the filename
     * @throws Exception on I/O error
     */
    private void readProductList(final String filename) throws Exception
    {
        System.out.println("Read " + filename);
        FileInputStream fis;
        if (new File(filename).canRead())
            fis = new FileInputStream(filename);
        else
            fis = new FileInputStream(CentroidRoutesModel.class.getResource(filename).getFile());
        try (CSVReader reader = new CSVReader(new InputStreamReader(fis), ',', '"', 1))
        {
            String[] parts;
            while ((parts = reader.readNext()) != null)
            {
                String product = parts[0].trim();
                if (product.length() > 0)
                {
                    this.productList.add(product);
                    this.outputTransportCost.put(product, 0.0);
                    this.outputTravelTime.put(product, 0.0);
                    this.outputUnsatisfiedDemand.put(product, 0.0);
                    System.out.println("Added product: " + product);
                }
            }
        }
        this.roadNetwork.setProducts(this.productList);
    }

    private void readDistrictData(final String filename)
    {
        try
        {
            FileInputStream fis;
            if (new File(filename).canRead())
                fis = new FileInputStream(filename);
            else
                fis = new FileInputStream(CentroidRoutesModel.class.getResource(filename).getFile());
            try (CSVReader reader = new CSVReader(new InputStreamReader(fis), ',', '"', 0))
            {
                String[] header = reader.readNext();
                String[] parts;
                while ((parts = reader.readNext()) != null)
                {
                    District district = this.districtCode2Map.get(parts[3].substring(6));
                    for (int i = 0; i < header.length; i++)
                    {
                        if (parts[i].length() > 0 && NumberUtil.isNumber(parts[i]))
                        {
                            try
                            {
                                district.addData(header[i], Double.valueOf(parts[i]));
                            }
                            catch (NumberFormatException nfe)
                            {
                                System.err.println("NFE: " + header[i] + " => '" + parts[i] + "'");
                            }
                        }
                    }
                }
            }
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Initialize the O/D matrix.
     */
    private void odMatrixInit()
    {
        this.numberOD = this.districtCode2Map.size() + this.ports.size() + this.landPorts.size();
        this.odCodes = new String[this.numberOD];
        this.odNodes = new ODNode[this.numberOD];
        int index = 0;
        for (District district : this.districtCode2Map.values())
        {
            this.odCodes[index] = district.getCode2();
            this.odNodes[index] = district.getDistrictCentroid();
            this.odLookupMap.put(district.getCode2(), index);
            index++;
        }
        for (Port port : this.ports.values())
        {
            this.odCodes[index] = port.getId();
            this.odNodes[index] = port;
            this.odLookupMap.put(port.getId(), index);
            index++;
        }
        for (LandPort landPort : this.landPorts.values())
        {
            this.odCodes[index] = landPort.getId();
            this.odNodes[index] = landPort;
            this.odLookupMap.put(landPort.getId(), index);
            index++;
        }
        for (String product : this.productList)
        {
            this.productProdMatrix.put(product, new Double[this.numberOD]);
            this.productConsMatrix.put(product, new Double[this.numberOD]);
            this.pcMatrix.put(product, new Double[this.numberOD][this.numberOD]);
            for (int i = 0; i < this.numberOD; i++)
                this.pcMatrix.get(product)[i] = new Double[this.numberOD];
        }
    }

    /**
     * Fill the O/D matrix for the districts. The OD matrix is based on the production in the District_level_data file that has
     * been read by the readDistrictData method. The data in this file indicates the production, import and export volumes per
     * district. It is assumed that the consumption is divided according to the population in districts, and that the sourcing
     * takes place according to distance to another district. <br>
     * So first, the total_production is calculated, which equals: Sum(prod_per_district) + import.<br>
     * The total_locally_consumed_production (total_loc_prod) equals total_production - export. <br>
     * Then, the consumption per district is calculated: total_loc_prod * population_per_district / tot_population.<br>
     * Total_consumption then equals Sum(consumption_per_district) + export. This should equal total_production.<br>
     * Finally, the flows between districts are calculated such that total travel time is minimized. We use a simple algorithm
     * in an iterative manner.
     * @param filename the filename of the district level data
     * @throws IOException in case of I/O error on district file
     * @throws Exception in case of total consumption of a product below zero
     */
    private void makeODMatrix(final String filename) throws IOException, Exception
    {
        // fill the O/D matrix for the districts
        Map<String, Double> prodFactor = new HashMap<>();
        Map<String, Double> consFactor = new HashMap<>(); // will be ignored => result of prod + imp - exp...
        Map<String, Double> impFactor = new HashMap<>();
        Map<String, Double> expFactor = new HashMap<>();
        Map<String, Double> totProduction = new HashMap<>();
        Map<String, Double> totConsumption = new HashMap<>();
        Map<String, Double> totImport = new HashMap<>();
        Map<String, Double> totExport = new HashMap<>();

        for (String product : this.productList)
        {
            prodFactor.put(product, (double) this.parameters.get(product.toUpperCase() + "_PRODUCTION"));
            consFactor.put(product, (double) this.parameters.get(product.toUpperCase() + "_CONSUMPTION"));
            impFactor.put(product, (double) this.parameters.get(product.toUpperCase() + "_IMPORT"));
            expFactor.put(product, (double) this.parameters.get(product.toUpperCase() + "_EXPORT"));
            totConsumption.put(product, 0.0);
            totProduction.put(product, 0.0);
            totImport.put(product, 0.0);
            totExport.put(product, 0.0);
            for (int i = 0; i < this.numberOD; i++)
            {
                this.productProdMatrix.get(product)[i] = 0.0;
                this.productConsMatrix.get(product)[i] = 0.0;
                for (int j = 0; j < this.numberOD; j++)
                {
                    this.pcMatrix.get(product)[i][j] = 0.0;
                }
            }
        }

        double totPopulation = 0.0;

        // determine production volumes
        FileInputStream fis;
        if (new File(filename).canRead())
            fis = new FileInputStream(filename);
        else
            fis = new FileInputStream(CentroidRoutesModel.class.getResource(filename).getFile());
        try (CSVReader reader = new CSVReader(new InputStreamReader(fis), ',', '"', 0))
        {
            String[] header = reader.readNext();
            List<String> headerList = Arrays.asList(header); // to be able to do an indexOf()
            String[] parts;
            while ((parts = reader.readNext()) != null)
            {
                String districtCode = parts[headerList.indexOf("Code")].substring(6);
                District district = this.districtCode2Map.get(districtCode);
                int odNr = this.odLookupMap.get(districtCode);

                // production

                double bricks = Double.parseDouble(parts[headerList.indexOf("Bricks_ton")]) * prodFactor.get("Brick");
                this.productProdMatrix.get("Brick")[odNr] = bricks;
                totProduction.put("Brick", totProduction.get("Brick") + bricks);

                double food = (Double.parseDouble(parts[headerList.indexOf("Fruits_ton")])
                        + Double.parseDouble(parts[headerList.indexOf("Potatoes_ton")])
                        + Double.parseDouble(parts[headerList.indexOf("Rice_ton")])
                        + Double.parseDouble(parts[headerList.indexOf("Sugar_ton")])
                        + Double.parseDouble(parts[headerList.indexOf("Wheat_ton")])) * prodFactor.get("Food");
                this.productProdMatrix.get("Food")[odNr] = food;
                totProduction.put("Food", totProduction.get("Food") + food);

                double steel = Double.parseDouble(parts[headerList.indexOf("Steel_ton")]) * prodFactor.get("Steel");
                this.productProdMatrix.get("Steel")[odNr] = steel;
                totProduction.put("Steel", totProduction.get("Steel") + steel);

                double garment = Double.parseDouble(parts[headerList.indexOf("Garment_ton")]) * prodFactor.get("Garment");
                this.productProdMatrix.get("Garment")[odNr] = garment;
                totProduction.put("Garment", totProduction.get("Garment") + garment);

                double textile = Double.parseDouble(parts[headerList.indexOf("Jute_ton")]) * prodFactor.get("Textile");
                this.productProdMatrix.get("Textile")[odNr] = textile;
                totProduction.put("Textile", totProduction.get("Textile") + textile);

                // store population for weights
                double population = Double.parseDouble(parts[headerList.indexOf("Population")]);
                district.setPopulation(population);
                totPopulation += population;
            }
        }

        // determine import and exports by port
        for (Port port : this.ports.values())
        {
            for (String product : this.productList)
            {
                double exportTon = port.getExports().get(product);
                totExport.put(product, totExport.get(product) + exportTon);
                this.productConsMatrix.get(product)[this.odLookupMap.get(port.getId())] = exportTon;
                double importTon = port.getImports().get(product);
                totImport.put(product, totImport.get(product) + importTon);
                this.productProdMatrix.get(product)[this.odLookupMap.get(port.getId())] = importTon;
            }
        }

        // determine import and exports by land port; assume 75% is these products, and equal tonnage for each product...
        for (LandPort landPort : this.landPorts.values())
        {
            for (String product : this.productList)
            {
                double exportTon = 0.75 * landPort.getExports().get(product);
                totExport.put(product, totExport.get(product) + exportTon);
                this.productConsMatrix.get(product)[this.odLookupMap.get(landPort.getId())] = exportTon;
                double importTon = 0.75 * landPort.getImports().get(product);
                totImport.put(product, totImport.get(product) + importTon);
                this.productProdMatrix.get(product)[this.odLookupMap.get(landPort.getId())] = importTon;
            }
        }

        System.out.println("\nPRODUCTION AND CONSUMPTION");
        System.out.println(
                String.format("%-10s %10s %10s %10s %10s", "PRODUCT", "PRODUCTION", "CONSUMPTION", "IMPORT", "EXPORT"));
        for (String product : this.productList)
        {
            System.out.println(String.format("%-10s %10.0f %10.0f %10.0f %10.0f", product, totProduction.get(product),
                    totConsumption.get(product), totImport.get(product), totExport.get(product)));
        }

        // consumption = production + import - export, divided by population fraction over the districts
        for (String product : this.productList)
        {
            double totCons = totProduction.get(product) + totImport.get(product) - totExport.get(product);
            totConsumption.put(product, totCons);
            if (totCons < 0)
            {
                System.err.println("CONSUMPTION FOR PRODUCT " + product + " LESS THAN ZERO -- "
                        + "\nTOO HIGH EXPORT COMBINED WITH TOO LOW PRODUCTION OR IMPORT");
                throw new Exception("Consumption for product " + product + " less than zero");
            }
            for (District district : this.districtCode2Map.values())
            {
                int odNr = this.odLookupMap.get(district.getCode2());
                this.productConsMatrix.get(product)[odNr] = totCons * district.getPopulation() / totPopulation;
            }
        }

        System.out.println("\nPRODUCTION AND CONSUMPTION");
        System.out.println(
                String.format("%-10s %10s %10s %10s %10s", "PRODUCT", "PRODUCTION", "CONSUMPTION", "IMPORT", "EXPORT"));
        for (String product : this.productList)
        {
            System.out.println(String.format("%-10s %10.0f %10.0f %10.0f %10.0f", product, totProduction.get(product),
                    totConsumption.get(product), totImport.get(product), totExport.get(product)));
        }

        // unsatisfied production / demand
        SortedMap<String, Double[]> restProdMatrix = new TreeMap<>();
        SortedMap<String, Double[]> restConsMatrix = new TreeMap<>();
        for (String product : this.productList)
        {
            restProdMatrix.put(product, new Double[this.numberOD]);
            restConsMatrix.put(product, new Double[this.numberOD]);
            for (int i = 0; i < this.numberOD; i++)
            {
                restProdMatrix.get(product)[i] = this.productProdMatrix.get(product)[i];
                restConsMatrix.get(product)[i] = this.productConsMatrix.get(product)[i];
            }
        }

        // set local consumption to e.g., 80% of the local production.
        // this does NOT include import and export!
        for (int i = 0; i < this.districtCode2Map.size(); i++)
        {
            for (String product : this.productList)
            {
                double fractionLocalConsumption = Double.parseDouble(
                        this.parameters.get(("Const_Fraction_Local_Consumption_" + product).toUpperCase()).toString());
                double volume = fractionLocalConsumption
                        * Math.min(this.productProdMatrix.get(product)[i], this.productConsMatrix.get(product)[i]);
                this.pcMatrix.get(product)[i][i] = volume;
                restProdMatrix.get(product)[i] -= volume;
                restConsMatrix.get(product)[i] -= volume;
            }
        }

        // fill the table, based on shortest travel time by road; allocate 50% at a time
        // based on REGULAR times and distances!
        StreamInterface stream = new MersenneTwister(1234L); // reproducible!
        SortedMap<Double, Long> sortedTrips = new TreeMap<>();
        for (int o = 0; o < this.numberOD; o++)
        {
            for (int d = 0; d < this.numberOD; d++)
            {
                double regularTime = this.roadRegularTimeMatrix[o][d] + stream.nextDouble() / 100.0; // avoid duplicates...
                if (o == d || this.roadRegularTimeMatrix[o][d] == 0.0)
                    regularTime = 1000.0 + stream.nextDouble() / 100.0; // don't allocate own district or import with export.
                long index = o * 1000 + d;
                sortedTrips.put(regularTime, index);
            }
        }

        // allocate 50% of what is needed, this includes import and export!
        for (String product : this.productList)
        {
            Double[] restProd = restProdMatrix.get(product);
            Double[] restCons = restConsMatrix.get(product);
            for (long trip : sortedTrips.values())
            {
                int o = (int) (trip / 1000);
                int d = (int) (trip % 1000);
                if (o != d && restProd[o] > 0.0 && restCons[d] > 0.0)
                {
                    double volume = 0.5 * Math.min(restProd[o], restCons[d]);
                    this.pcMatrix.get(product)[o][d] += volume;
                    restProd[o] -= volume;
                    restCons[d] -= volume;
                }
            }
        }

        // allocate the remaining volumes...
        for (String product : this.productList)
        {
            Double[] restProd = restProdMatrix.get(product);
            Double[] restCons = restConsMatrix.get(product);
            for (long trip : sortedTrips.values())
            {
                int o = (int) (trip / 1000);
                int d = (int) (trip % 1000);
                if (o != d && restProd[o] > 0.0 && restCons[d] > 0.0)
                {
                    double volume = Math.min(restProd[o], restCons[d]);
                    this.pcMatrix.get(product)[o][d] += volume;
                    restProd[o] -= volume;
                    restCons[d] -= volume;
                }
            }
        }
    }

    private void printOD()
    {
        for (String product : this.productList)
        {
            System.out.println("\n\n\nO/D MATRIX FOR PRODUCT: " + product);
            System.out.print("         ");
            for (int j = 0; j < this.numberOD; j++)
                System.out.print(String.format("%-8s ", this.odCodes[j]));
            System.out.println();

            System.out.print("TOTPROD ");
            for (int j = 0; j < this.numberOD; j++)
                System.out.print((String.format("%8.0f ", this.productProdMatrix.get(product)[j])));
            System.out.println();

            System.out.print("TOTCONS ");
            for (int j = 0; j < this.numberOD; j++)
                System.out.print((String.format("%8.0f ", this.productConsMatrix.get(product)[j])));
            System.out.println();

            for (int i = 0; i < this.numberOD; i++)
            {
                System.out.print(String.format("%-8s ", this.odCodes[i]));
                for (int j = 0; j < this.numberOD; j++)
                {
                    System.out.print((String.format("%8.0f ", this.pcMatrix.get(product)[i][j])));
                }
                System.out.println();
            }
        }
        System.out.println("\n\n\n");
    }

    /**
     * (Re)initialize the statistics at a warmup event, or at the start of the run.
     */
    public void initializeStatistics()
    {
        for (String product : this.productList)
        {
            this.outputTransportCost.put(product, 0.0);
            this.outputTravelTime.put(product, 0.0);
            this.outputUnsatisfiedDemand.put(product, 0.0);

            for (int odNr = 0; odNr < this.numberOD; odNr++)
            {
                ODNode odNode = this.odNodes[odNr];
                odNode.initializeDemand(product);
            }
        }
        // keep the road traffic -- that's why we have a warmup period!
    }

    /**
     * Start / stop the flooding.
     * @param newFlooded new flood status
     * @param report report or not
     */
    public void setFlooded(final boolean newFlooded, final boolean report)
    {
        this.flooded = newFlooded;
        for (District district : this.districtCode2Map.values())
        {
            if (this.floodSet.contains(district.getCode2()))
            {
                district.setFlooded(newFlooded);
                if (report)
                    System.out.println("District " + district.getCode() + ", " + district.getName() + ", " + district.getCode2()
                            + " set to " + (newFlooded ? "flooded" : "dry"));
            }
        }
    }

    /**
     * Start the flooding.
     */
    public void scheduledFloodStart()
    {
        setFlooded(true, true);
    }

    /**
     * End the flooding.
     */
    public void scheduledFloodEnd()
    {
        setFlooded(false, true);
    }

    /**
     * @return flooded
     */
    public final boolean isFlooded()
    {
        return this.flooded;
    }

    public void scheduleDailyTransportEvents()
    {
        for (String product : this.productList)
        {
            for (int i = 0; i < this.numberOD; i++)
            {
                // add the needed consumption per day; arriving goods will be subtracted from this number
                double demand = this.productConsMatrix.get(product)[i] / 365.0;
                this.outputUnsatisfiedDemand.put(product, this.outputUnsatisfiedDemand.get(product) + demand);
                ODNode node = this.odNodes[i];
                node.addDailyDemand(product, demand, demand);
            }
            for (int o = 0; o < this.numberOD; o++)
            {
                for (int d = 0; d < this.numberOD; d++)
                {
                    if (o == d)
                    {
                        // local consumption and production can be matched; assume no transport time...
                        // production can be hurt in a flooded region...
                        District district = this.districtCode2Map.get(this.odCodes[o]);
                        if (district != null) // don't match supply and demand in a port...
                        {
                            double tonnes = this.pcMatrix.get(product)[o][o] / 365.0;
                            if (district.isFlooded())
                            {
                                // reduce: potentially less production as a result of flooding
                                tonnes = tonnes
                                        * (double) this.parameters.get("CONST_FLOODING_PRODUCTION_" + product.toUpperCase());
                            }
                            // assume instant arrival within the district
                            this.outputUnsatisfiedDemand.put(product, this.outputUnsatisfiedDemand.get(product) - tonnes);
                            district.getDistrictCentroid().addDailyDemand(product, 0, -tonnes);
                        }
                    }
                    else
                    {
                        double tonnes = this.pcMatrix.get(product)[o][d] / 365.0;
                        District fromDistrict = this.districtCode2Map.get(this.odCodes[o]);
                        if (fromDistrict != null && fromDistrict.isFlooded()) // 'district' could be (land) port
                        {
                            // reduce: potentially less production as a result of flooding
                            tonnes = tonnes
                                    * (double) this.parameters.get("CONST_FLOODING_PRODUCTION_" + product.toUpperCase());
                        }
                        if (tonnes > 0.0)
                        {
                            double fractionRoad = (double) this.parameters.get(product.toUpperCase() + "_ROAD");
                            double fractionWater = (double) this.parameters.get(product.toUpperCase() + "_WATER");
                            double fractionRail = (double) this.parameters.get(product.toUpperCase() + "_RAIL");
                            double tot = fractionRoad + fractionWater + fractionRail;
                            if (tot == 0.0)
                            {
                                fractionRoad = 1.0;
                            }
                            else
                            {
                                fractionRoad = fractionRoad / tot;
                                fractionWater = fractionWater / tot;
                                fractionRail = fractionRail / tot;
                            }

                            double tonnesRoad = tonnes * fractionRoad;
                            double tonnesWater = tonnes * fractionWater;
                            double tonnesRail = tonnes * fractionRail;

                            // check if we can transport (in an undisturbed situation) by water
                            if (tonnesWater > 0.0 && o < this.districtCode2Map.size() && d < this.districtCode2Map.size())
                            {
                                WaterwayTerminal oTerminal = this.districtWaterwayTerminalMap.get(this.odCodes[o]);
                                WaterwayTerminal dTerminal = this.districtWaterwayTerminalMap.get(this.odCodes[d]);
                                boolean waterOk = oTerminal != null && dTerminal != null;
                                if (waterOk)
                                {
                                    CompleteRouteWaterway route = this.isFlooded()
                                            ? this.waterwayDisturbedMatrix[oTerminal.getIndex()][dTerminal.getIndex()]
                                            : this.waterwayRegularMatrix[oTerminal.getIndex()][dTerminal.getIndex()];
                                    if (route != null)
                                        scheduleTransportWaterway(product, oTerminal, dTerminal, route, tonnesWater);
                                    else
                                        waterOk = false;
                                }
                                if (!waterOk)
                                    tonnesRoad += tonnesWater;
                            }

                            if (tonnesRoad > 0.0)
                            {
                                scheduleTransportRoad(product, o, d, tonnesRoad);
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Day = " + this.simulator.getSimulatorTime().getTime().si / (3600.0 * 24.0));
        for (String product : this.productList)
        {
            System.out.println("TransportCost " + product + " = " + this.outputTransportCost.get(product) + " tk");
            System.out.println("TravelTime " + product + " = " + this.outputTravelTime.get(product) + " ton-hrs");
            System.out.println("Unsatisfied demand " + product + " = " + this.outputUnsatisfiedDemand.get(product) + " ton");
        }

        try
        {
            this.simulator.scheduleEventRel(new Duration(1.0, DurationUnit.DAY), this, this, "scheduleDailyTransportEvents",
                    null);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    protected void scheduleTransportRoad(String product, int o, int d, double tonnes)
    {
        try
        {
            // see if we have the resources
            District districtO = this.districtCode2Map.get(this.odCodes[o]);
            if (districtO != null)
            {
                // we don't schedule trucks for import and export -- they come/go across the border
                if (districtO.getTruckCapacityTonnes() < tonnes)
                {
                    // schedule the next day
                    this.simulator.scheduleEventRel(new Duration(1.0, DurationUnit.DAY), this, this, "scheduleTransportRoad",
                            new Object[] { product, o, d, tonnes });
                    return;
                }
            }

            // schedule the arrival
            // Regular till flood event, disturbed after
            CompleteRouteRoad route = isFlooded() ? this.roadDisturbedMatrix[o][d] : this.roadRegularMatrix[o][d];

            Duration duration;
            if (isFlooded())
                duration = route.getAvgDisturbedTruckDuration();
            else
                duration = route.getAvgRegularTruckDuration();
            if (districtO == null)
                this.simulator.scheduleEventRel(duration, this, this, "transportRoadArrival",
                        new Object[] { route, product, tonnes, duration, this.odNodes[d] });
            else
                this.simulator.scheduleEventRel(duration, this, this, "transportRoadArrival",
                        new Object[] { route, product, tonnes, duration, districtO, this.odNodes[d] });

            // System.out.println("Road transport from " + this.odCodes[o] + " to " + this.odCodes[d] + " -- " + tonnes + " t "
            // + product + " (" + duration.getInUnit(DurationUnit.HOUR) + ")");

            if (districtO != null)
                districtO.addTruckCapacityTonnes(-tonnes);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    protected void transportRoadArrival(final CompleteRouteRoad route, final String product, final double tonnes,
            final Duration duration, final ODNode destination)
    {
        transportRoadArrival(route, product, tonnes, duration, null, destination);
    }

    protected void transportRoadArrival(final CompleteRouteRoad route, final String product, final double tonnes,
            final Duration duration, final District districtO, final ODNode destination)
    {
        if (districtO != null)
            districtO.addTruckCapacityTonnes(tonnes);
        double roadCost = (double) this.parameters.get("ROAD_COST");
        double distanceKm = route.getLength().getInUnit(LengthUnit.KILOMETER);
        this.outputTransportCost.put(product, this.outputTransportCost.get(product) + tonnes * distanceKm * roadCost);
        this.outputTravelTime.put(product, this.outputTravelTime.get(product) + tonnes * duration.getInUnit(DurationUnit.HOUR));
        this.outputUnsatisfiedDemand.put(product, this.outputUnsatisfiedDemand.get(product) - tonnes);
        destination.addDailyDemand(product, 0, -tonnes);

        // add usage to each road segment
        for (RoadLink link : route.getLinks())
        {
            for (RoadSegment segment : link.getSegments())
            {
                segment.addTransport(product, tonnes);
                this.roadNetwork.setMaxProductTransport(product, segment.getTransport().get(product));
            }
        }
    }

    protected void scheduleTransportWaterway(String product, WaterwayTerminal oTerminal, WaterwayTerminal dTerminal,
            CompleteRouteWaterway route, double tonnes)
    {
        try
        {
            // we assume there are always some trucks in the district, but see if we have the barge capacity
            if (oTerminal.getDistrict().getWaterwayCapacityTonnes() < tonnes)
            {
                // schedule the next day
                this.simulator.scheduleEventRel(new Duration(1.0, DurationUnit.DAY), this, this, "scheduleTransportWaterway",
                        new Object[] { product, oTerminal, dTerminal, route, tonnes });
            }
            else
            {
                double durationSI = 0.0;

                // Transport in origin district; regular till flood event, disturbed after
                CompleteRouteRoad oRoute =
                        oTerminal.getDistrict().isFlooded() ? this.waterwayTerminalCentroidRouteDisturbed.get(oTerminal)
                                : this.waterwayTerminalCentroidRouteRegular.get(oTerminal);
                durationSI += oTerminal.getDistrict().isFlooded() ? oRoute.getAvgDisturbedTruckDuration().si
                        : oRoute.getAvgRegularTruckDuration().si;

                // Loading time -- takes int account the local situation
                durationSI += oTerminal.getLoadingDuration().si;

                // Waterway transport; regular till flood event, disturbed after
                if (isFlooded())
                    durationSI += route.getAvgDisturbedBargeDuration().si;
                else
                    durationSI += route.getAvgRegularBargeDuration().si;

                // Unloading time -- takes int account the local situation
                durationSI += dTerminal.getLoadingDuration().si;

                // Transport in destination district; regular till flood event, disturbed after
                CompleteRouteRoad dRoute =
                        dTerminal.getDistrict().isFlooded() ? this.waterwayTerminalCentroidRouteDisturbed.get(dTerminal)
                                : this.waterwayTerminalCentroidRouteRegular.get(dTerminal);
                durationSI += dTerminal.getDistrict().isFlooded() ? dRoute.getAvgDisturbedTruckDuration().si
                        : dRoute.getAvgRegularTruckDuration().si;

                // schedule the arrival
                Duration duration = new Duration(durationSI, DurationUnit.SI);
                this.simulator.scheduleEventRel(duration, this, this, "transportWaterwayArrival",
                        new Object[] { oTerminal, dTerminal, oRoute, dRoute, route, product, tonnes, duration });

                // System.out.println("Waterway transport from " + oTerminal.getDistrict().getCode2() + " to "
                // + dTerminal.getDistrict().getCode2() + " -- " + tonnes + " t " + product + " ("
                // + duration.getInUnit(DurationUnit.HOUR) + ")");

                oTerminal.getDistrict().addWaterwayCapacityTonnes(-tonnes);
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    protected void transportWaterwayArrival(final WaterwayTerminal oTerminal, final WaterwayTerminal dTerminal,
            final CompleteRouteRoad oRoute, final CompleteRouteRoad dRoute, final CompleteRouteWaterway route,
            final String product, final double tonnes, final Duration duration)
    {
        oTerminal.getDistrict().addWaterwayCapacityTonnes(tonnes);

        // System.out.println("ARRIVAL waterway transport from " + oTerminal.getDistrict().getCode2() + " to "
        // + dTerminal.getDistrict().getCode2() + " -- " + tonnes + " t " + product + " ("
        // + duration.getInUnit(DurationUnit.HOUR) + ")");

        double roadCost = (double) this.parameters.get("ROAD_COST");
        double distanceRoadKm =
                oRoute.getLength().getInUnit(LengthUnit.KILOMETER) + dRoute.getLength().getInUnit(LengthUnit.KILOMETER);

        double waterwayCost = (double) this.parameters.get("WATER_COST");
        double distanceWaterwayKm = route.getLength().getInUnit(LengthUnit.KILOMETER);

        double transhipmentCost = 2 * (double) this.parameters.get("TRS_COST");

        this.outputTransportCost.put(product, this.outputTransportCost.get(product) + tonnes * distanceRoadKm * roadCost
                + tonnes * distanceWaterwayKm * waterwayCost + tonnes * transhipmentCost);
        this.outputTravelTime.put(product, this.outputTravelTime.get(product) + tonnes * duration.getInUnit(DurationUnit.HOUR));
        this.outputUnsatisfiedDemand.put(product, this.outputUnsatisfiedDemand.get(product) - tonnes);

        ODNode destinationNode = this.odNodes[this.odLookupMap.get(dTerminal.getDistrict().getCode2())];
        destinationNode.addDailyDemand(product, 0, -tonnes);

        // add usage to each road segment in the origin district
        for (RoadLink link : oRoute.getLinks())
        {
            for (RoadSegment segment : link.getSegments())
            {
                segment.addTransport(product, tonnes);
                this.roadNetwork.setMaxProductTransport(product, segment.getTransport().get(product));
            }
        }

        // add usage to each road segment in the destination district
        for (WaterwayLink link : route.getLinks())
        {
            link.addTransport(product, tonnes);
            this.waterwayNetwork.setMaxProductTransport(product, link.getTransport().get(product));
        }

        // add usage to each waterway link
        for (RoadLink link : dRoute.getLinks())
        {
            for (RoadSegment segment : link.getSegments())
            {
                segment.addTransport(product, tonnes);
                this.roadNetwork.setMaxProductTransport(product, segment.getTransport().get(product));
            }
        }
    }

    /**
     * Import a list of waterway (link) elements from a shape file
     * @param filename file name
     * @throws NetworkException on read error
     * @throws OTSGeometryException when design line not proper
     * @throws IOException on i/o error
     */
    private void getWaterways(String filename) throws NetworkException, OTSGeometryException, IOException
    {
        System.out.println("Read 53 waterways");

        URL url;
        if (new File(filename).canRead())
            url = new File(filename).toURI().toURL();
        else
            url = DistrictReader.class.getResource(filename);
        FileDataStore dataStoreLink = FileDataStoreFinder.getDataStore(url);

        // iterate over the features
        SimpleFeatureSource featureSource = dataStoreLink.getFeatureSource();
        SimpleFeatureCollection featureCollection = featureSource.getFeatures();
        SimpleFeatureIterator featureIterator = featureCollection.features();
        int nodeNr = 0;
        Map<String, Integer> wwLinkNrs = new HashMap<>();
        Map<String, List<Waterway>> waterwaySegments = new HashMap<>();
        while (featureIterator.hasNext())
        {
            SimpleFeature feature = featureIterator.next();
            try
            {
                Geometry theGeom = (Geometry) feature.getDefaultGeometryProperty().getValue();
                Coordinate[] coordinates = theGeom.getCoordinates();

                // {osm_id=6, null=0, fix_id=2, length=4, name=5, width=8, Class=1, id=3, type=7, the_geom=0, Linestring=9}

                Property property = feature.getProperty("name");
                String name = property.getValue().toString();
                name = name.length() == 0 ? UUID.randomUUID().toString() : name;
                property = feature.getProperty("Class");
                int wwClass = (int) Math.round(parseDouble(property));

                if (!waterwaySegments.containsKey(name))
                {
                    waterwaySegments.put(name, new ArrayList<>());
                    wwLinkNrs.put(name, 1);
                }
                else
                {
                    wwLinkNrs.put(name, wwLinkNrs.get(name) + 1);
                }

                WaterwayNode startNode = null;
                OTSPoint3D ptBegin = new OTSPoint3D(coordinates[0]);
                for (Node node : this.waterwayNetwork.getNodeMap().values())
                {
                    if (node.getPoint().equals(ptBegin))
                    {
                        ptBegin = node.getPoint();
                        startNode = (WaterwayNode) node;
                        break;
                    }
                }
                if (startNode == null)
                {
                    startNode = new WaterwayNode(this.waterwayNetwork, "WW." + (++nodeNr), ptBegin, name, wwClass);
                }

                WaterwayNode endNode = null;
                OTSPoint3D ptEnd = new OTSPoint3D(coordinates[coordinates.length - 1]);
                for (Node node : this.waterwayNetwork.getNodeMap().values())
                {
                    if (node.getPoint().equals(ptEnd))
                    {
                        ptEnd = node.getPoint();
                        endNode = (WaterwayNode) node;
                        break;
                    }
                }
                if (endNode == null)
                {
                    endNode = new WaterwayNode(this.waterwayNetwork, "WW." + (++nodeNr), ptEnd, name, wwClass);
                }

                OTSLine3D designLine = new OTSLine3D(coordinates);

                Waterway waterway = this.waterwayMap.get(name);
                if (waterway == null)
                {
                    waterway = new Waterway(this.waterwayNetwork, name, name);
                    this.waterwayMap.put(name, waterway);
                }

                WaterwayLink ww = new WaterwayLink(this.waterwayNetwork, name + "." + (wwLinkNrs.get(name)), startNode, endNode,
                        waterway, wwClass, designLine, this.simulator, this.parameters);
                try
                {
                    if (this.animation)
                    {
                        new WaterwayAnimation(ww, this.simulator, (float) ((5.0 - wwClass) * 0.002));
                        if (this.app != null)
                        {
                            ww.setProductAnimation(
                                    new WaterwayProductAnimation(ww, this.simulator, this.productList, this.app));
                        }
                    }
                }
                catch (RemoteException | NamingException exception)
                {
                    exception.printStackTrace();
                }
            }
            catch (OTSGeometryException ge)
            {
                ge.printStackTrace();
            }
        }
        featureIterator.close();
        dataStoreLink.dispose();
    }

    private void readWaterwayOverrides(String filename) throws IOException
    {
        // 0: waterway, 1: startnode, 2: endnode, 3: class
        FileInputStream fis;
        if (new File(filename).canRead())
        {
            System.out.println("\nProcessing waterway override file " + filename);
            fis = new FileInputStream(filename);
            try (CSVReader reader = new CSVReader(new InputStreamReader(fis), ',', '"', 1))
            {
                String[] parts;
                while ((parts = reader.readNext()) != null)
                {
                    String name = parts[0].trim();
                    String sNodeId = parts[1].trim();
                    String eNodeId = parts[2].trim();
                    int wwClass = Integer.parseInt(parts[3].trim());

                    Waterway waterway = this.waterwayMap.get(name);
                    if (waterway == null)
                    {
                        waterway = new Waterway(this.waterwayNetwork, name, name);
                        this.waterwayMap.put(name, waterway);
                    }

                    WaterwayNode startNode = null;
                    for (Node node : this.waterwayNetwork.getNodeMap().values())
                    {
                        if (node.getId().equals(sNodeId))
                        {
                            startNode = (WaterwayNode) node;
                            break;
                        }
                    }
                    if (startNode == null)
                    {
                        System.err.println("Override WW-node " + name + " - start node " + sNodeId + " not found");
                        continue;
                    }

                    WaterwayNode endNode = null;
                    for (Node node : this.waterwayNetwork.getNodeMap().values())
                    {
                        if (node.getId().equals(eNodeId))
                        {
                            endNode = (WaterwayNode) node;
                            break;
                        }
                    }
                    if (endNode == null)
                    {
                        System.err.println("Override WW-node " + name + " - end node " + eNodeId + " not found");
                        continue;
                    }

                    try
                    {
                        OTSLine3D designLine = new OTSLine3D(startNode.getPoint(), endNode.getPoint());

                        WaterwayLink ww = new WaterwayLink(this.waterwayNetwork, name, startNode, endNode, waterway, wwClass,
                                designLine, this.simulator, this.parameters);
                        System.out.println("Added waterway " + ww);
                        if (this.animation)
                        {
                            new WaterwayAnimation(ww, this.simulator, (float) ((5.0 - wwClass) * 0.002));
                            if (this.app != null)
                            {
                                ww.setProductAnimation(
                                        new WaterwayProductAnimation(ww, this.simulator, this.productList, this.app));
                            }
                        }
                    }
                    catch (RemoteException | NamingException | OTSGeometryException | NetworkException exception)
                    {
                        exception.printStackTrace();
                    }

                }
            }
            System.out.println();

        }
    }

    /**
     * @param initialDir initial directory
     * @param fileName file name without .shp
     * @return shape file data store
     * @throws IOException on read error
     */
    private FileDataStore newDatastore(String initialDir, final String fileName) throws IOException
    {
        try
        {
            URL url = CentroidRoutesModel.class.getResource("/");
            File file = new File(url.getFile() + initialDir);
            String fn = file.getCanonicalPath();
            fn = fn.replace('\\', '/');
            File iniDir = new File(fn);
            file = new File(iniDir, fileName + ".shp");

            FileDataStore dataStoreLink = FileDataStoreFinder.getDataStore(file);
            return dataStoreLink;

        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
        return null;

    }

    /**
     * Return an iterator.
     * @param dataStore the shape file store
     * @return iterator
     */
    private SimpleFeatureIterator getFeatureIterator(FileDataStore dataStore)
    {
        try
        {
            String[] typeNameLink = dataStore.getTypeNames();
            SimpleFeatureSource sourceLink;
            sourceLink = dataStore.getFeatureSource(typeNameLink[0]);
            SimpleFeatureCollection featuresLink = sourceLink.getFeatures();
            return featuresLink.features();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param property the property
     * @return a double
     */
    private Double parseDouble(Property property)
    {
        if (property.getValue() != null)
        {
            if (property.getValue().toString() != null && property.getValue().toString().length() > 0)
            {
                return Double.parseDouble(property.getValue().toString());
            }
        }
        return Double.NaN;
    }

    private void readBridgesWorldBank(final String filename) throws Exception
    {
        FileInputStream fis;
        if (new File(filename).canRead())
            fis = new FileInputStream(filename);
        else
            fis = new FileInputStream(CentroidRoutesModel.class.getResource(filename).getFile());
        XSSFWorkbook wbNuts = new XSSFWorkbook(fis);

        XSSFSheet sheet1 = wbNuts.getSheet("Bridge types and attributes");
        boolean firstRow = true;
        for (Row row : sheet1)
        {
            if (!firstRow)
            {
                try
                {
                    String roadNo = ExcelUtil.cellValue(row, "M");
                    double chainage = ExcelUtil.cellValueDoubleNull(row, "R");
                    String condition = ExcelUtil.cellValue(row, "D");
                    if (condition.length() > 0)
                    {
                        String type = ExcelUtil.cellValue(row, "C");
                        double width = ExcelUtil.cellValueDouble(row, "E");
                        double length = ExcelUtil.cellValueDouble(row, "F");
                        int constructionYear = ExcelUtil.cellValueInt(row, "G");
                        int numberOfSpans = ExcelUtil.cellValueInt(row, "H");

                        String name = ExcelUtil.cellValue(row, "B");
                        double latH = ExcelUtil.cellValueDouble(row, "S");
                        double latM = ExcelUtil.cellValueDouble(row, "T");
                        double latS = ExcelUtil.cellValueDouble(row, "U");
                        double lat = latH + latM / 60.0 + latS / 3600.0;
                        double lonH = ExcelUtil.cellValueDouble(row, "V");
                        double lonM = ExcelUtil.cellValueDouble(row, "W");
                        double lonS = ExcelUtil.cellValueDouble(row, "X");
                        double lon = lonH + lonM / 60.0 + lonS / 3600.0;

                        BridgeBGD b = new BridgeBGD(name, new DirectedPoint(lon, lat, 5.0), roadNo, chainage, condition, type,
                                width, length, constructionYear, numberOfSpans);

                        if (!this.bridgeMap.containsKey(roadNo))
                            this.bridgeMap.put(roadNo, new ArrayList<>());
                        this.bridgeMap.get(roadNo).add(b);

                        if (this.animation)
                        {
                            new BridgeAnimation(b, this.simulator);
                        }
                    }
                }
                catch (Exception e)
                {
                    System.err.println(e.getMessage());
                }
            }
            firstRow = false;
        }
    }

    private void readBMMS(final String filename) throws Exception
    {
        FileInputStream fis;
        if (new File(filename).canRead())
            fis = new FileInputStream(filename);
        else
            fis = new FileInputStream(CentroidRoutesModel.class.getResource(filename).getFile());
        XSSFWorkbook wbNuts = new XSSFWorkbook(fis);

        XSSFSheet sheet1 = wbNuts.getSheet("BMMS_overview");
        boolean firstRow = true;
        for (Row row : sheet1)
        {
            if (!firstRow)
            {
                try
                {
                    String roadId = ExcelUtil.cellValue(row, "A");
                    double chainage = ExcelUtil.cellValueDoubleNull(row, "B");
                    String condition = ExcelUtil.cellValue(row, "G");
                    if (condition.length() > 0)
                    {
                        String type = ExcelUtil.cellValue(row, "C");
                        double width = ExcelUtil.cellValueDoubleNull(row, "K");
                        double length = ExcelUtil.cellValueDoubleNull(row, "F");
                        int constructionYear = ExcelUtil.cellValueIntNull(row, "L");
                        int numberOfSpans = ExcelUtil.cellValueIntNull(row, "M");
                        String name = ExcelUtil.cellValue(row, "E");
                        double lat = ExcelUtil.cellValueDoubleNull(row, "R");
                        double lon = ExcelUtil.cellValueDoubleNull(row, "S");

                        if (lat > 0 && lon > 0)
                        {
                            BridgeBGD b = new BridgeBGD(name, new DirectedPoint(lon, lat, 5.0), roadId, chainage, condition,
                                    type, width, length, constructionYear, numberOfSpans);

                            if (!this.bridgeMap.containsKey(roadId))
                                this.bridgeMap.put(roadId, new ArrayList<>());
                            this.bridgeMap.get(roadId).add(b);

                            if (this.animation)
                            {
                                new BridgeAnimation(b, this.simulator);
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    System.err.println(e.getMessage());
                }
            }
            firstRow = false;
        }
    }

    /**
     * Add all the bridges to the road segments.
     */
    private void addBridgesToRoadSegments()
    {
        for (Road road : this.roadMap.values())
        {
            for (RoadSegment segment : road.getSegments())
            {
                segment.addBridges(this.bridgeMap.get(road.getId()));
            }
        }
    }

    /**
     * Read the Districts
     * @param filename filename
     * @throws Exception on I/O error
     */
    private void readDistricts(final String filename) throws Exception
    {
        System.out.println("read districts");
        URL url;
        if (new File(filename).canRead())
            url = new File(filename).toURI().toURL();
        else
            url = DistrictReader.class.getResource(filename);
        FileDataStore storeDistricts = FileDataStoreFinder.getDataStore(url);

        // CoordinateReferenceSystem worldCRS = CRS.decode("EPSG:4326");

        // iterate over the features
        SimpleFeatureSource featureSourceAdm2 = storeDistricts.getFeatureSource();
        SimpleFeatureCollection featureCollectionAdm2 = featureSourceAdm2.getFeatures();
        SimpleFeatureIterator iterator = featureCollectionAdm2.features();
        try
        {
            while (iterator.hasNext())
            {
                SimpleFeature feature = iterator.next();
                MultiPolygon polygon = (MultiPolygon) feature.getAttribute("the_geom");
                String code = feature.getAttribute("ID_2").toString();
                String name = feature.getAttribute("NAME_2").toString();
                String code2 = feature.getAttribute("HASC_2").toString().substring(6, 8);
                District district = new District(this.simulator, code, name, code2, polygon);
                new DistrictDemandAnimation(district, this.simulator, this.app);
                this.districtCodeMap.put(code, district);
                this.districtNameMap.put(name, district);
                this.districtCode2Map.put(code2, district);
                this.districtLRPMap.put(district, new ArrayList<>());
            }
        }
        catch (Exception problem)
        {
            problem.printStackTrace();
        }
        finally
        {
            iterator.close();
        }
        storeDistricts.dispose();
    }

    /**
     * Read the roads with LRP coordinates and make it part of a network for which we can calculate shortest path.
     * @param filename filename
     * @throws Exception on I/O error
     */
    private void readRoadsCsv34(final String filename) throws Exception
    {
        System.out.println("Read " + filename);
        District lastDistrict = null;
        GeometryFactory geometryFactory = new GeometryFactory();
        FileInputStream fis;
        if (new File(filename).canRead())
            fis = new FileInputStream(filename);
        else
            fis = new FileInputStream(CentroidRoutesModel.class.getResource(filename).getFile());
        try (CSVReader reader = new CSVReader(new InputStreamReader(fis), ',', '"', 1))
        {
            String[] parts;
            LRP lastLRP = null;
            String lastRoad = "";
            while ((parts = reader.readNext()) != null)
            {
                String roadId = parts[0];
                boolean newRoad = !roadId.equals(lastRoad);
                if (newRoad)
                {
                    lastLRP = null;
                }
                LRP lrp = null;
                String lrps = roadId + "_" + parts[2];
                String chas = parts[1];
                String lats = parts[3];
                String lons = parts[4];
                double lat = Double.parseDouble(lats);
                double lon = Double.parseDouble(lons);
                double chainage = Double.parseDouble(chas);
                String bf = parts[5];
                String type = parts[6];
                String name = parts[7];
                if (lastLRP == null || lon != lastLRP.getPoint().x || lat != lastLRP.getPoint().y) // no degenerate
                {
                    while (this.roadNetwork.containsNode(lrps)) // name clash
                    {
                        lrps += "+";
                    }
                    GapPoint gapPoint = GapPoint.getInstance(bf);

                    // see what district.
                    District district = null;
                    Point p = geometryFactory.createPoint(new Coordinate(lon, lat));
                    if (lastDistrict != null)
                    {
                        if (lastDistrict.getPolygon().contains(p))
                        {
                            district = lastDistrict;
                        }
                    }
                    if (district == null)
                    {
                        for (District d : this.districtCodeMap.values())
                        {
                            if (d.getPolygon().contains(p))
                            {
                                district = d;
                                lastDistrict = d;
                                break;
                            }
                        }
                    }
                    if (district == null)
                    {
                        System.out.print(
                                "cannot find district of LRP " + lrps + " at (" + lat + "," + lon + "). Searching boxes... ");
                        for (District d : this.districtCodeMap.values())
                        {
                            if (d.getPolygon().getEnvelope().contains(p))
                            {
                                district = d;
                                System.out.println("Found " + d.getCode2() + ": " + d.getName());
                                lastDistrict = null;
                                break;
                            }
                        }
                    }
                    if (district == null)
                    {
                        System.out.println("Assumed Dhaka...");
                        district = this.districtCode2Map.get("DH");
                    }

                    if (!this.roadMap.containsKey(roadId))
                    {
                        Speed avgTruckSpeed = new Speed(40, SpeedUnit.KM_PER_HOUR);
                        if (roadId.startsWith("N"))
                            avgTruckSpeed =
                                    new Speed((double) this.parameters.get("CONST_AVGSPEED_ROAD_N"), SpeedUnit.KM_PER_HOUR);
                        else if (roadId.startsWith("R"))
                            avgTruckSpeed =
                                    new Speed((double) this.parameters.get("CONST_AVGSPEED_ROAD_R"), SpeedUnit.KM_PER_HOUR);
                        else if (roadId.startsWith("Z"))
                            avgTruckSpeed =
                                    new Speed((double) this.parameters.get("CONST_AVGSPEED_ROAD_Z"), SpeedUnit.KM_PER_HOUR);
                        this.roadMap.put(roadId, new Road(roadId, avgTruckSpeed));
                    }
                    Road road = this.roadMap.get(roadId);
                    if (!this.bridgeMap.containsKey(roadId))
                        this.bridgeMap.put(roadId, new ArrayList<>());

                    OTSPoint3D coordinate = new OTSPoint3D(lon, lat, 0.0);
                    if (this.pointLRPMap.containsKey(coordinate))
                    {
                        // use the existing LRP
                        lrp = this.pointLRPMap.get(coordinate);
                        lrp.addRoad(road);
                    }
                    else
                    {
                        lrp = new LRP(this.roadNetwork, lrps, coordinate, road, chainage, type, name, gapPoint, district);
                        this.pointLRPMap.put(coordinate, lrp);
                        this.lrpMap.put(lrp.getId(), lrp);
                        if (district != null)
                        {
                            this.districtLRPMap.get(district).add(lrp);
                        }
                        if (this.animation)
                        {
                            new LRPAnimation(lrp, this.simulator, Color.BLUE);
                        }
                    }

                    if (lastLRP != null) // 2 points needed for a line
                    {
                        String linkName = lastLRP.getId() + "-" + lrp.getId();
                        if (!this.roadNetwork.containsLink(linkName))
                        {
                            Gap gap = Gap.ROAD;
                            if (gapPoint.isBridgeEnd() && lastLRP.getGapPoint().isBridgeStart())
                            {
                                gap = Gap.BRIDGE;
                            }
                            else if (gapPoint.isGapEnd() && lastLRP.getGapPoint().isGapStart())
                            {
                                gap = Gap.GAP;
                            }
                            else if (gapPoint.isFerryEnd() && lastLRP.getGapPoint().isFerryStart())
                            {
                                gap = Gap.FERRY;
                            }

                            OTSLine3D designLine = new OTSLine3D(lastLRP.getPoint(), lrp.getPoint());
                            if (roadId.startsWith("N"))
                            {
                                SegmentN r = new SegmentN(this.roadNetwork, linkName, road, lastLRP, lrp, LinkType.ALL,
                                        designLine, this.simulator, LongitudinalDirectionality.DIR_BOTH, gap, this.parameters);
                                if (this.animation)
                                {
                                    r.setAnimation(
                                            new RoadNAnimation(r, roadId, this.simulator, (float) (5.0 * 0.0005), Color.BLACK));
                                    addRoadSegmentAnimation(r);
                                }
                                road.addSegment(r);
                            }
                            if (roadId.startsWith("R"))
                            {
                                SegmentR r = new SegmentR(this.roadNetwork, linkName, road, lastLRP, lrp, LinkType.ALL,
                                        designLine, this.simulator, LongitudinalDirectionality.DIR_BOTH, gap, this.parameters);
                                if (this.animation)
                                {
                                    r.setAnimation(
                                            new RoadRAnimation(r, roadId, this.simulator, (float) (3.0 * 0.0005), Color.BLACK));
                                    addRoadSegmentAnimation(r);
                                }
                                road.addSegment(r);
                            }
                            if (roadId.startsWith("Z"))
                            {
                                SegmentZ r = new SegmentZ(this.roadNetwork, linkName, road, lastLRP, lrp, LinkType.ALL,
                                        designLine, this.simulator, LongitudinalDirectionality.DIR_BOTH, gap, this.parameters);
                                if (this.animation)
                                {
                                    r.setAnimation(
                                            new RoadZAnimation(r, roadId, this.simulator, (float) (1.0 * 0.0005), Color.BLACK));
                                    addRoadSegmentAnimation(r);
                                }
                                road.addSegment(r);
                            }
                        }
                    }
                    lastLRP = lrp;
                }
                lastRoad = roadId;
            }
        }
    }

    private void addRoadSegmentAnimation(final RoadSegment segment) throws RemoteException, NamingException
    {
        if (this.app != null)
        {
            segment.setSegmentAnimation(new RoadSegmentAnimation(segment, this.simulator, this.productList, this.app));
        }
    }

    /**
     * Write the LRP coordinates as well as other time consuming calculated information.
     * @param filename filename
     * @throws Exception on I/O error
     */
    private void writeLRPsCsv5(final String filename) throws Exception
    {
        System.out.println("Write " + filename);
        try (PrintWriter writer = new PrintWriter(new File(filename)))
        {
            writer.println("\"lrp\",\"lat\",\"lon\",\"type\",\"name\",\"district\",\"roadId\",\"chainage\",\"gapPoint\"");
            for (Node node : this.roadNetwork.getNodeMap().values())
            {
                if (node instanceof LRP)
                {
                    LRP lrp = (LRP) node;
                    writer.println("\"" + lrp.getId() + "\"," + lrp.getLocation().y + "," + lrp.getLocation().x + ",\""
                            + lrp.getType() + "\",\"" + lrp.getName() + "\",\"" + lrp.getDistrict().getCode2() + "\",\""
                            + lrp.getRoad() + "\"," + lrp.getChainage() + ",\"" + lrp.getGapPoint().toString() + "\"");
                }
            }
        }
    }

    /**
     * Write the roads with LRP references as well as other time consuming calculated information.
     * @param filename filename
     * @throws Exception on I/O error
     */
    private void writeRoadsCsv5(final String filename) throws Exception
    {
        System.out.println("Write " + filename);
        try (PrintWriter writer = new PrintWriter(new File(filename)))
        {
            writer.println("\"road\",\"lrp1\",\"lrp2\",\"name\",\"gap\"");
            for (Road road : this.roadMap.values())
            {
                for (RoadSegment segment : road.getSegments())
                {
                    writer.println("\"" + road.getId() + "\",\"" + segment.getStartLRP().getId() + "\",\""
                            + segment.getEndLRP().getId() + "\",\"" + segment.getId() + "\",\"" + segment.getGap().toString()
                            + "\"");
                }
            }
        }
    }

    /**
     * Read the LRPs and make it part of a network for which we can calculate shortest path.
     * @param filename filename
     * @throws Exception on I/O error
     */
    private void readLRPsCsv5(final String filename) throws Exception
    {
        System.out.println("Read " + filename);
        FileInputStream fis;
        if (new File(filename).canRead())
            fis = new FileInputStream(filename);
        else
            fis = new FileInputStream(CentroidRoutesModel.class.getResource(filename).getFile());
        try (CSVReader reader = new CSVReader(new InputStreamReader(fis), ',', '"', 1))
        {
            String[] parts;
            while ((parts = reader.readNext()) != null)
            {
                // writer.println("\"lrp\",\"lat\",\"lon\",\"type\",\"name\",\"district\",\"roadId\",\"chainage\",\"gapPoint\"");
                // 0:lrp, 1:lat, 2:lon, 3:type, 4:name, 5:district, 6:road, 7:chainage, 8:gapPoint
                LRP lrp = null;
                String lrps = parts[0].trim();
                String lats = parts[1].trim();
                String lons = parts[2].trim();
                double lat = Double.parseDouble(lats);
                double lon = Double.parseDouble(lons);
                String type = parts[3].trim();
                String name = parts[4].trim();
                String districtStr = parts[5].trim();
                String roadId = parts[6].trim();
                double chainage = Double.parseDouble(parts[7].trim());
                String gapType = parts[8].trim();

                GapPoint gapPoint = GapPoint.ROAD;
                switch (gapType)
                {
                    case "ROAD":
                        gapPoint = GapPoint.ROAD;
                        break;

                    case "BRIDGE_START":
                        gapPoint = GapPoint.BRIDGE_START;
                        break;

                    case "BRIDGE_END":
                        gapPoint = GapPoint.BRIDGE_END;
                        break;

                    case "FERRY_START":
                        gapPoint = GapPoint.FERRY_START;
                        break;

                    case "FERRY_END":
                        gapPoint = GapPoint.FERRY_END;
                        break;

                    case "GAP_START":
                        gapPoint = GapPoint.GAP_START;
                        break;

                    case "GAP_END":
                        gapPoint = GapPoint.GAP_END;
                        break;

                    default:
                        break;
                }

                Road road = this.roadMap.get(roadId);
                if (road == null)
                {
                    Speed avgTruckSpeed = new Speed(40, SpeedUnit.KM_PER_HOUR);
                    if (roadId.startsWith("N"))
                        avgTruckSpeed = new Speed((double) this.parameters.get("CONST_AVGSPEED_ROAD_N"), SpeedUnit.KM_PER_HOUR);
                    else if (roadId.startsWith("R"))
                        avgTruckSpeed = new Speed((double) this.parameters.get("CONST_AVGSPEED_ROAD_R"), SpeedUnit.KM_PER_HOUR);
                    else if (roadId.startsWith("Z"))
                        avgTruckSpeed = new Speed((double) this.parameters.get("CONST_AVGSPEED_ROAD_Z"), SpeedUnit.KM_PER_HOUR);
                    road = new Road(roadId, avgTruckSpeed);
                    this.roadMap.put(roadId, road);
                }
                // see what district.
                District district = this.districtCode2Map.get(districtStr);
                OTSPoint3D coordinate = new OTSPoint3D(lon, lat, 0.0);
                lrp = new LRP(this.roadNetwork, lrps, coordinate, road, chainage, type, name, gapPoint, district);
                this.pointLRPMap.put(coordinate, lrp);
                this.lrpMap.put(lrp.getId(), lrp);
                if (district != null)
                {
                    this.districtLRPMap.get(district).add(lrp);
                }
                if (this.animation)
                {
                    Color color = lrp.getId().contains("LRPX") ? Color.GREEN : Color.BLUE;
                    new LRPAnimation(lrp, this.simulator, color);
                }
            }
        }
    }

    /**
     * Read the floodset
     * @param scenarioName scenario name
     * @param filename filename
     * @throws Exception on I/O error
     */
    private void readFloodSet(final String scenarioName, final String filename) throws Exception
    {
        System.out.println("Read " + filename);
        System.out.println("Flood scenario: " + scenarioName);
        FileInputStream fis;
        if (new File(filename).canRead())
            fis = new FileInputStream(filename);
        else
            fis = new FileInputStream(CentroidRoutesModel.class.getResource(filename).getFile());
        try (CSVReader reader = new CSVReader(new InputStreamReader(fis), ';', '"', 1))
        {
            boolean ok = false;
            String[] parts;
            while ((parts = reader.readNext()) != null)
            {
                String scen = parts[0];
                if (scen.equals(scenarioName))
                {
                    ok = true;
                    for (int i = 1; i < parts.length; i++)
                    {
                        if (parts[i].length() > 0)
                        {
                            this.floodSet.add(parts[i]);
                        }
                    }
                    System.out.println("Flooded districts: " + this.floodSet);
                }
            }
            if (!ok)
            {
                System.err.println("ERROR: WAS NOT ABLE TO READ FLOOD SCENARIO: " + scenarioName + "from " + filename);
            }
        }
    }

    private void addInterventionsConstants() throws Exception
    {
        Map<String, Object> paramCopy = new HashMap<>(this.parameters);
        for (String param : paramCopy.keySet())
        {
            if (param.startsWith("INTERVENTION_"))
            {
                boolean active = false;
                if (paramCopy.get(param) instanceof Boolean && ((Boolean) paramCopy.get(param)))
                    active = true;
                if (paramCopy.get(param) instanceof Number && ((Number) paramCopy.get(param)).doubleValue() != 0)
                    active = true;
                if (paramCopy.get(param) instanceof String && paramCopy.get(param).toString().toUpperCase().startsWith("T"))
                    active = true;
                if (active)
                {
                    System.out.println("\n" + param + " ACTIVATED");

                    // CONSTANT CHANGES
                    String filenameConstant = this.fileFolder + "/interventions/" + param.toLowerCase() + "_constants.csv";
                    URL constantCsv = URLResource.getResource(filenameConstant);
                    if (constantCsv != null && new File(constantCsv.getPath()).canRead())
                    {
                        System.out.println("Constants intervention file: " + filenameConstant);
                        FileInputStream fis = new FileInputStream(filenameConstant);
                        try (CSVReader reader = new CSVReader(new InputStreamReader(fis), ',', '"', 1))
                        {
                            // 0: constant, 1: newvalue, 2: type
                            String[] parts;
                            while ((parts = reader.readNext()) != null)
                            {
                                String constant = parts[0].trim().toUpperCase();
                                String value = parts[1].trim();
                                String type = parts[2].trim();
                                if (!constant.startsWith("CONST_"))
                                {
                                    System.err.println("Constant name " + constant
                                            + " does not start with 'CONST_' (but still processed) in interventions file"
                                            + filenameConstant);
                                }
                                if (type.equalsIgnoreCase("Double"))
                                    this.parameters.put(constant, Double.parseDouble(value));
                                else if (type.equalsIgnoreCase("Integer"))
                                    this.parameters.put(constant, Integer.parseInt(value));
                                else if (type.equalsIgnoreCase("Boolean"))
                                    this.parameters.put(constant, Boolean.parseBoolean(value));
                                else if (type.equalsIgnoreCase("Float"))
                                    this.parameters.put(constant, Float.parseFloat(value));
                                else if (type.equalsIgnoreCase("Long"))
                                    this.parameters.put(constant, Long.parseLong(value));
                                else if (type.equalsIgnoreCase("Short"))
                                    this.parameters.put(constant, Short.parseShort(value));
                                else
                                    this.parameters.put(constant, value);
                            }
                        }
                    }
                }
            }
        }
    }

    private void addInterventionsRoadBridgeWaterway() throws Exception
    {
        for (String param : this.parameters.keySet())
        {
            if (param.startsWith("INTERVENTION_"))
            {
                boolean active = false;
                if (this.parameters.get(param) instanceof Boolean && ((Boolean) this.parameters.get(param)))
                    active = true;
                if (this.parameters.get(param) instanceof Number && ((Number) this.parameters.get(param)).doubleValue() != 0)
                    active = true;
                if (this.parameters.get(param) instanceof String
                        && this.parameters.get(param).toString().toUpperCase().startsWith("T"))
                    active = true;
                if (active)
                {
                    System.out.println("\n" + param + " ACTIVATED");

                    // ROAD CHANGES
                    String filenameRoad = this.fileFolder + "/interventions/" + param.toLowerCase() + "_roads.csv";
                    URL roadCsv = URLResource.getResource(filenameRoad);
                    if (roadCsv != null && new File(roadCsv.getPath()).canRead())
                    {
                        System.out.println("Road intervention file: " + filenameRoad);
                        readRoadsCsv5(filenameRoad);
                    }

                    // BRIDGE CHANGES
                    String filenameBridge = this.fileFolder + "/interventions/" + param.toLowerCase() + "_bridges.csv";
                    URL bridgeCsv = URLResource.getResource(filenameBridge);
                    if (bridgeCsv != null && new File(bridgeCsv.getPath()).canRead())
                    {
                        System.out.println("Bridge intervention file: " + filenameBridge);
                        readBridgeInterventionsCsv(filenameBridge);
                    }

                    // WATERWAY CHANGES
                    /*-
                    String filenameWw = this.fileFolder + "/interventions/" + param.toLowerCase() + "_waterways.csv";
                    URL waterwaysCsv = URLResource.getResource(filenameWw);
                    if (waterwaysCsv != null && new File(waterwaysCsv.getPath()).canRead())
                    {
                        System.out.println("Waterway intervention file: " + filenameWw);
                        readWaterwayInterventionsCsv(filenameWw);
                    }
                    */
                }
            }
        }
    }

    private void readBridgeInterventionsCsv(final String filenameBridge) throws IOException, NamingException
    {
        FileInputStream fis = new FileInputStream(filenameBridge);
        try (CSVReader reader = new CSVReader(new InputStreamReader(fis), ',', '"', 1))
        {
            // 0: road, 1: chainage, 2: newCat A-D, 3: new (NEW / blank), 4 (optional): oldcat, 5 (optional): comment / new name
            String[] parts;
            while ((parts = reader.readNext()) != null)
            {
                String roadId = parts[0].trim();
                if (roadId.length() > 0)
                {
                    String km = parts[1].trim();
                    String newCat = parts[2].trim();
                    String newBridge = parts[3].trim();
                    String newName = parts[5].trim();

                    Road road = this.roadMap.get(roadId);
                    if (road == null)
                    {
                        System.err.println("Cannot find road " + roadId + " from bridge interventions file " + filenameBridge);
                        continue;
                    }

                    RoadSegment bridgeSegment = null;
                    BridgeBGD updatedBridge = null;

                    double chainage = -1;
                    if (km.length() > 0)
                    {
                        chainage = Double.parseDouble(km);
                    }

                    DirectedPoint location = null;

                    for (RoadSegment segment : road.getSegments())
                    {
                        double c1 = segment.getStartLRP().getChainage();
                        double c2 = segment.getEndLRP().getChainage();
                        if (chainage >= Math.min(c1, c2) && chainage <= Math.max(c1, c2))
                        {
                            location = segment.getStartLRP().getLocation();
                            bridgeSegment = segment;
                            break;
                        }
                    }
                    if (bridgeSegment == null)
                    {
                        System.err.println("Could not find road segment for road " + roadId + " at km " + chainage
                                + " in intervention file " + filenameBridge);
                        continue;
                    }

                    if (newBridge.length() > 0 && newBridge.equalsIgnoreCase("new"))
                    {
                        // new bridge -- just add
                        updatedBridge =
                                new BridgeBGD(newName, location, roadId, chainage, newCat, "unknown", 5.0, 20.0, 2020, 1);
                        this.bridgeMap.get(roadId).add(updatedBridge);

                        if (this.animation)
                        {
                            new BridgeAnimation(updatedBridge, this.simulator);
                        }
                    }

                    else

                    {
                        // existing bridge -- find the bridge
                        BridgeBGD bestBridge = null;
                        double bestDistance = Double.MAX_VALUE;
                        for (RoadSegment segment : road.getSegments())
                        {
                            for (BridgeBGD bridge : segment.getBridges())
                            {
                                if (bestBridge == null || Math.abs(bridge.getChainage() - chainage) < bestDistance)
                                {
                                    bestDistance = Math.abs(bridge.getChainage() - chainage);
                                    bestBridge = bridge;
                                }
                            }
                        }

                        if (bestDistance > 0.1)
                        {
                            System.err.println("Cannot find bridge on road " + roadId + " at km " + chainage
                                    + " from interventions file " + filenameBridge);
                            continue;
                        }
                        bestBridge.setCondition(newCat);
                    }

                    // in all cases, reset the bridges and recalculate the travel times
                    bridgeSegment.addBridges(this.bridgeMap.get(roadId));
                }
            }
        }
    }

    private void readWaterwayInterventionsCsv(final String filenameWaterway) throws IOException
    {
        FileInputStream fis = new FileInputStream(filenameWaterway);
        try (CSVReader reader = new CSVReader(new InputStreamReader(fis), ',', '"', 1))
        {
            // 0: waterway, 1: fromKm, 2: toKm, 3: newCat 1-4 where 1 is best
            String[] parts;
            while ((parts = reader.readNext()) != null)
            {
                String waterwayId = parts[0].trim();
                double fromKm = Double.parseDouble(parts[1].trim());
                double toKm = Double.parseDouble(parts[2].trim());
                int newCat = Integer.parseInt(parts[2].trim());

                // find the waterway
                Waterway waterway = this.waterwayMap.get(waterwayId);
                if (waterway == null)
                {
                    System.err.println("Cannot find waterway " + waterwayId + " from interventions file " + filenameWaterway);
                    continue;
                }
                WaterwayNode node1 = findWaterwayNodeNear(waterway, fromKm, 1.0);
                WaterwayNode node2 = findWaterwayNodeNear(waterway, toKm, 1.0);
                if (node1 == null)
                {
                    System.err.println("Cannot find waterway " + waterwayId + ", node near km " + fromKm
                            + " from interventions file " + filenameWaterway);
                    continue;
                }
                if (node2 == null)
                {
                    System.err.println("Cannot find waterway " + waterwayId + ", node near km " + toKm
                            + " from interventions file " + filenameWaterway);
                    continue;
                }
                if (node1 != null && node2 != null && node1 != node2)
                {
                    // TODO: iterate from fromNode to toNode
                    // TODO: upgrade/downgrade the ww-class
                }
            }
        }
    }

    private WaterwayNode findWaterwayNodeNear(final Waterway waterway, final double km, final double threshold)
    {
        double bestDistance = Double.MAX_VALUE;
        WaterwayNode bestNode = null;
        for (Node node : waterwayNetwork.getNodeMap().values())
        {
            WaterwayNode wwNode = (WaterwayNode) node;
            if (wwNode.getLinks().size() > 0
                    && ((WaterwayLink) wwNode.getLinks().iterator().next()).getWaterway().equals(waterway))
            {
                // TODO: create the chainage for the waterways
                // if (bestNode == null || Math.abs(wwNode.getKm()) < bestDistance)
                // {
                // bestDistance = Math.abs(wwNode.getKm() - bestDistance);
                // bestNode = wwNode;
                // }
            }
        }
        if (bestDistance > threshold)
            return null;
        return bestNode;
    }

    private void addInterventionsPorts() throws Exception
    {
        Map<String, Object> paramCopy = new HashMap<>(this.parameters);
        for (String param : paramCopy.keySet())
        {
            if (param.startsWith("INTERVENTION_"))
            {
                boolean active = false;
                if (paramCopy.get(param) instanceof Boolean && ((Boolean) paramCopy.get(param)))
                    active = true;
                if (paramCopy.get(param) instanceof Number && ((Number) paramCopy.get(param)).doubleValue() != 0)
                    active = true;
                if (paramCopy.get(param) instanceof String && paramCopy.get(param).toString().toUpperCase().startsWith("T"))
                    active = true;
                if (active)
                {
                    System.out.println("\n" + param + " ACTIVATED");

                    // PORT CHANGES
                    String filenamePort = this.fileFolder + "/interventions/" + param.toLowerCase() + "_ports.csv";
                    URL portCsv = URLResource.getResource(filenamePort);
                    if (portCsv != null && new File(portCsv.getPath()).canRead())
                    {
                        System.out.println("Port intervention file: " + filenamePort);
                        FileInputStream fis = new FileInputStream(filenamePort);
                        try (CSVReader reader = new CSVReader(new InputStreamReader(fis), ',', '"', 1))
                        {
                            // 0: portname, 1: productname, 2: importfactor, 3: importdelta, 4: exportfactor, 5: exportdelta
                            String[] parts;
                            while ((parts = reader.readNext()) != null)
                            {
                                String portName = parts[0].trim();
                                String product = parts[1].trim();
                                Double importFactor = parts[2].trim().length() > 0 ? Double.parseDouble(parts[2].trim()) : null;
                                Double importDelta = parts[3].trim().length() > 0 ? Double.parseDouble(parts[3].trim()) : null;
                                Double exportFactor = parts[4].trim().length() > 0 ? Double.parseDouble(parts[4].trim()) : null;
                                Double exportDelta = parts[5].trim().length() > 0 ? Double.parseDouble(parts[5].trim()) : null;

                                // find the (land)port
                                ImportExportLocation ieLocation = null;
                                if (this.ports.containsKey(portName))
                                    ieLocation = this.ports.get(portName);
                                else if (this.landPorts.containsKey(portName))
                                    ieLocation = this.landPorts.get(portName);
                                else
                                {
                                    System.err.println(
                                            "Cannot find (land) port " + portName + " in interventions file " + filenamePort);
                                    continue;
                                }

                                // adapt the volumes
                                if (importFactor != null)
                                {
                                    double oldVolume = ieLocation.getImports().get(product);
                                    ieLocation.getImports().put(product, oldVolume * importFactor);
                                    System.out.println("Port " + portName + ", product " + product + ", import volume "
                                            + oldVolume + " -> " + ieLocation.getImports().get(product));
                                }
                                if (importDelta != null)
                                {
                                    double oldVolume = ieLocation.getImports().get(product);
                                    ieLocation.getImports().put(product, oldVolume + importDelta);
                                    System.out.println("Port " + portName + ", product " + product + ", import volume "
                                            + oldVolume + " -> " + ieLocation.getImports().get(product));
                                }
                                if (exportFactor != null)
                                {
                                    double oldVolume = ieLocation.getExports().get(product);
                                    ieLocation.getExports().put(product, oldVolume * exportFactor);
                                    System.out.println("Port " + portName + ", product " + product + ", export volume "
                                            + oldVolume + " -> " + ieLocation.getExports().get(product));
                                }
                                if (exportDelta != null)
                                {
                                    double oldVolume = ieLocation.getExports().get(product);
                                    ieLocation.getExports().put(product, oldVolume + exportDelta);
                                    System.out.println("Port " + portName + ", product " + product + ", export volume "
                                            + oldVolume + " -> " + ieLocation.getExports().get(product));
                                }
                            }
                        }
                        System.out.println();
                    }
                }
            }
        }
    }

    /**
     * Read the roads with LRP coordinates and make it part of a network for which we can calculate shortest path.
     * @param filename filename
     * @throws Exception on I/O error
     */
    private void readRoadsCsv5(final String filename) throws Exception
    {
        System.out.println("Read " + filename);
        FileInputStream fis;
        if (new File(filename).canRead())
            fis = new FileInputStream(filename);
        else
            fis = new FileInputStream(CentroidRoutesModel.class.getResource(filename).getFile());
        try (CSVReader reader = new CSVReader(new InputStreamReader(fis), ',', '"', 1))
        {
            // writer.println("\"road\",\"lrp1\",\"lrp2\",\"name\",\"gap\"");
            // note: the intervention file contains a new speed as parts[5]
            String[] parts;
            String lastRoad = "";
            while ((parts = reader.readNext()) != null)
            {
                String roadId = parts[0];
                String lrps1 = parts[1];
                String lrps2 = parts[2];
                String linkName = parts[3];
                String gaps = parts[4];
                String newSpeedString = parts.length > 5 ? parts[5].trim() : null;
                String newRoadClass = parts.length > 6 ? parts[6].trim() : null;

                LRP lrp1 = (LRP) this.roadNetwork.getNode(lrps1);
                if (lrp1 == null)
                {
                    System.err.println("cannot find " + lrps1);
                }
                LRP lrp2 = (LRP) this.roadNetwork.getNode(lrps2);
                if (lrp1 == null)
                {
                    System.err.println("cannot find " + lrps2);
                }

                int index = -1;
                if (this.roadNetwork.containsLink(linkName))
                {
                    // handle an override
                    Link link = this.roadNetwork.getLink(linkName);
                    index = removeLink(link);
                }

                Gap gap = Gap.valueOf(gaps);
                if (!this.roadMap.containsKey(roadId))
                {
                    Speed avgTruckSpeed = new Speed(40, SpeedUnit.KM_PER_HOUR);
                    if (roadId.startsWith("N"))
                        avgTruckSpeed = new Speed((double) this.parameters.get("CONST_AVGSPEED_ROAD_N"), SpeedUnit.KM_PER_HOUR);
                    else if (roadId.startsWith("R"))
                        avgTruckSpeed = new Speed((double) this.parameters.get("CONST_AVGSPEED_ROAD_R"), SpeedUnit.KM_PER_HOUR);
                    else if (roadId.startsWith("Z"))
                        avgTruckSpeed = new Speed((double) this.parameters.get("CONST_AVGSPEED_ROAD_Z"), SpeedUnit.KM_PER_HOUR);
                    this.roadMap.put(roadId, new Road(roadId, avgTruckSpeed));
                }
                Road road = this.roadMap.get(roadId);
                if (!this.bridgeMap.containsKey(roadId))
                    this.bridgeMap.put(roadId, new ArrayList<>());
                lrp1.addRoad(road);
                lrp2.addRoad(road);

                OTSLine3D designLine = new OTSLine3D(lrp1.getPoint(), lrp2.getPoint());
                Color color = linkName.contains("SPUR") ? SPUR_COLOR : lrp2.getId().contains("LRPX") ? Color.RED : Color.BLACK;
                if (roadId.startsWith("N"))
                {
                    SegmentN r = new SegmentN(this.roadNetwork, linkName, road, lrp1, lrp2, LinkType.ALL, designLine,
                            this.simulator, LongitudinalDirectionality.DIR_BOTH, gap, this.parameters);
                    if (this.animation)
                    {
                        r.setAnimation(new RoadNAnimation(r, roadId, this.simulator, (float) (5.0 * 0.0005), color));
                        addRoadSegmentAnimation(r);
                    }
                    if (index >= 0)
                        road.addSegment(index, r); // replacement
                    else
                        road.addSegment(r);
                    if (newSpeedString != null && newSpeedString.length() > 0)
                    {
                        Speed newSpeed = new Speed(Double.parseDouble(newSpeedString), SpeedUnit.KM_PER_HOUR);
                        r.setAvgTruckSpeed(newSpeed);
                    }
                    if (newRoadClass != null && newRoadClass.length() > 0)
                    {
                        r.setRoadClass(newRoadClass);
                    }
                    if (index >= 0)
                        r.addBridges(this.bridgeMap.get(road.getId()));
                }
                if (roadId.startsWith("R"))
                {
                    SegmentR r = new SegmentR(this.roadNetwork, linkName, road, lrp1, lrp2, LinkType.ALL, designLine,
                            this.simulator, LongitudinalDirectionality.DIR_BOTH, gap, this.parameters);
                    if (this.animation)
                    {
                        r.setAnimation(new RoadRAnimation(r, roadId, this.simulator, (float) (3.0 * 0.0005), color));
                        addRoadSegmentAnimation(r);
                    }
                    if (index >= 0)
                        road.addSegment(index, r); // replacement
                    else
                        road.addSegment(r);
                    if (newSpeedString != null && newSpeedString.length() > 0)
                    {
                        Speed newSpeed = new Speed(Double.parseDouble(newSpeedString), SpeedUnit.KM_PER_HOUR);
                        r.setAvgTruckSpeed(newSpeed);
                    }
                    if (newRoadClass != null && newRoadClass.length() > 0)
                    {
                        r.setRoadClass(newRoadClass);
                    }
                    if (index >= 0)
                        r.addBridges(this.bridgeMap.get(road.getId()));
                }
                if (roadId.startsWith("Z"))
                {
                    SegmentZ r = new SegmentZ(this.roadNetwork, linkName, road, lrp1, lrp2, LinkType.ALL, designLine,
                            this.simulator, LongitudinalDirectionality.DIR_BOTH, gap, this.parameters);
                    if (this.animation)
                    {
                        r.setAnimation(new RoadZAnimation(r, roadId, this.simulator, (float) (1.0 * 0.0005), color));
                        addRoadSegmentAnimation(r);
                    }
                    if (index >= 0)
                        road.addSegment(index, r); // replacement
                    else
                        road.addSegment(r);
                    if (newSpeedString != null && newSpeedString.length() > 0)
                    {
                        Speed newSpeed = new Speed(Double.parseDouble(newSpeedString), SpeedUnit.KM_PER_HOUR);
                        r.setAvgTruckSpeed(newSpeed);
                    }
                    if (newRoadClass != null && newRoadClass.length() > 0)
                    {
                        r.setRoadClass(newRoadClass);
                    }
                    if (index >= 0)
                        r.addBridges(this.bridgeMap.get(road.getId()));
                }

            }
        }
    }

    private int removeLink(Link link) throws NamingException, NetworkException
    {
        this.roadNetwork.removeLink(link);

        // remove the link from the road
        RoadSegment rs = (RoadSegment) link;
        Road r = rs.getRoad();
        int index = r.getSegments().indexOf(rs);
        r.getSegments().remove(rs);
        if (index != -1)
            System.out.println("Duplicate segment removed due to override: " + rs);

        // correct the network from the LRPs
        link.getStartNode().removeLink(link);
        link.getEndNode().removeLink(link);

        // remove the associated animation if present
        if (rs.getAnimation() != null)
        {
            rs.getAnimation().destroy();
            rs.setAnimation(null);
        }
        if (rs.getSegmentAnimation() != null)
        {
            rs.getSegmentAnimation().destroy();
            rs.setSegmentAnimation(null);
        }

        return index;
    }

    /** distance in m to connect a start or end point. */
    private final static double THRESHOLD_SPUR = 250;

    /** distance in m to connect two points on different roads (e.g., a crossing). */
    private final static double THRESHOLD_POINTS = 50;

    private double distance(Locatable point1, Locatable point2)
    {
        try
        {
            double lat1 = point1.getLocation().y;
            double lon1 = point1.getLocation().x;
            double lat2 = point2.getLocation().y;
            double lon2 = point2.getLocation().x;
            double p = 0.017453292519943295; // Math.PI / 180
            double a = 0.5 - Math.cos((lat2 - lat1) * p) / 2.0
                    + Math.cos(lat1 * p) * Math.cos(lat2 * p) * (1.0 - Math.cos((lon2 - lon1) * p)) / 2.0;
            return 12742000.0 * Math.asin(Math.sqrt(a)); // 2 * R; R = 6371 km, distance in m.
        }
        catch (RemoteException re)
        {
            System.err.println("RemoteException on determining distance between " + point1 + " and " + point2);
            return Double.NaN;
        }
    }

    /** record of 2 links. */
    private class Crossing
    {
        /** road segment 1. */
        public RoadSegment rs1;

        /** road segment 2 */
        public RoadSegment rs2;

        /**
         * @param rs1 road segment 1
         * @param rs2 road segment 2
         */
        public Crossing(RoadSegment rs1, RoadSegment rs2)
        {
            super();
            this.rs1 = rs1;
            this.rs2 = rs2;
        }
    }

    private void repairSmallGapsCrossings()
    {
        // test if start or end point of a road is very close to an LRP of another road.
        // find the road endpoints -- should be at least 2 per road (road could fork)
        System.out.println("\nLink start and end points of roads to roads close by");
        SortedMap<String, Road> endpoints = new TreeMap<>();
        for (Road road : this.roadMap.values())
        {
            if (road.getLRPs().size() > 1)
            {
                LRP lrp1 = road.getLRPs().get(0);
                endpoints.put(lrp1.getId(), road);
                LRP lrpn = road.getLRPs().get(road.getLRPs().size() - 1);
                endpoints.put(lrpn.getId(), road);
                for (LRP lrp : road.getLRPs())
                {
                    if (lrp.getName().equals("LRPS") || lrp.getName().equals("LRPE") || lrp.getCardinality() == 1)
                        endpoints.put(lrp.getId(), road);
                }
            }
        }

        // connect the start and end points to everything in a threshold range
        Set<LRP> originalLRPSet = new HashSet<>(this.lrpMap.values());
        for (String lrpId : endpoints.keySet())
        {
            final LRP lrp = this.lrpMap.get(lrpId);
            final Road road = endpoints.get(lrpId);
            originalLRPSet.stream().forEach(lrp2 -> {
                if ((Math.abs(lrp.getLocation().x - lrp2.getLocation().x) < 0.1
                        && Math.abs(lrp.getLocation().y - lrp2.getLocation().y) < 0.1))
                    testDistance(lrp, lrp2, road, THRESHOLD_SPUR, SPUR_COLOR);
            });
        }

        System.out.println("\nTest very close points on different roads");
        for (Road road1 : this.roadMap.values())
        {
            for (LRP lrp1 : road1.getLRPs())
            {
                for (Road road2 : this.roadMap.values())
                {
                    if (!road1.equals(road2) && road1.getId().compareTo(road2.getId()) < 0)
                    {
                        for (LRP lrp2 : road2.getLRPs())
                        {
                            if (Math.abs(lrp1.getLocation().x - lrp2.getLocation().x) < 0.01
                                    && Math.abs(lrp1.getLocation().y - lrp2.getLocation().y) < 0.01)
                            {
                                if (!lrp1.getId().substring(0, lrp1.getId().indexOf('_'))
                                        .equals(lrp2.getId().substring(0, lrp2.getId().indexOf('_'))))
                                {
                                    testDistance(lrp1, lrp2, road1, THRESHOLD_POINTS, SPUR_COLOR);
                                }
                            }
                        }
                    }
                }
            }
        }

        /*-
        // test crossings of roads without an LRP in the middle
        final List<Crossing> crossings = Collections.synchronizedList(new ArrayList<>());
        final SortedSet<Link> links = new TreeSet<Link>(this.bgdNetwork.getLinkMap().values().toCollection());
        this.roadMap.values().parallelStream().forEach(road -> {
            System.out.println("Testing road " + road.getId() + " for crossings");
            for (RoadSegment rs1 : road.getSegments())
            {
                for (Link link2 : links)
                {
                    if (link2 instanceof RoadSegment && link2 != null)
                    {
                        RoadSegment rs2 = (RoadSegment) link2;
                        if (rs1.getRoad().getId().compareTo(rs2.getRoad().getId()) < 0) // triangular matrix
                        {
                            if (!rs1.getId().contains(rs2.getRoad().getId()) && !rs2.getId().contains(rs1.getRoad().getId()))
                            {
                                if (boundsOverlap(rs1, rs2))
                                {
                                    if (testCrossing(rs1, rs2))
                                    {
                                        crossings.add(new Crossing(rs1, rs2));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
        
        System.out.println();
        
        for (Crossing crossing : crossings)
        {
            if (this.bgdNetwork.containsLink(crossing.rs1) && this.bgdNetwork.containsLink(crossing.rs2))
                makeCrossing(crossing.rs1, crossing.rs2);
        }
        */
    }

    private void testDistance(LRP lrp1, LRP lrp2, Road road, double threshold, final Color color)
    {
        try
        {
            Set<Road> roads12 = lrp1.getRoads();
            roads12.retainAll(lrp2.getRoads());
            if (!lrp1.equals(lrp2) && roads12.isEmpty())
            {
                double d = distance(lrp1, lrp2);
                if (d < threshold) // includes distance 0!
                {
                    // make a connection between lrp1 and lrp2
                    // returns null if already existing (in either direction)
                    RoadSegment rs = makeLink(lrp1, lrp2, road, "SPUR ", color);
                    if (rs != null)
                        System.out.println("RoadSegment added: " + rs + ", distance = " + d);
                }
            }
        }
        catch (NetworkException | RemoteException | NamingException | OTSGeometryException exception)
        {
            exception.printStackTrace();
        }
    }

    private boolean boundsOverlap(Link link1, Link link2)
    {
        OTSPoint3D ps = link1.getStartNode().getPoint();
        OTSPoint3D pe = link1.getEndNode().getPoint();
        OTSPoint3D qs = link2.getStartNode().getPoint();
        OTSPoint3D qe = link2.getEndNode().getPoint();
        double x1min = ps.x <= pe.x ? ps.x : pe.x;
        double x1max = ps.x >= pe.x ? ps.x : pe.x;
        double x2min = qs.x <= qe.x ? qs.x : qe.x;
        double x2max = qs.x >= qe.x ? qs.x : qe.x;
        double y1min = ps.y <= pe.y ? ps.y : pe.y;
        double y1max = ps.y >= pe.y ? ps.y : pe.y;
        double y2min = qs.y <= qe.y ? qs.y : qe.y;
        double y2max = qs.y >= qe.y ? qs.y : qe.y;
        if (x1max < x2min || y1max < y2min || x2max < x1min || y2max < y1min)
        {
            return false;
        }
        return true;
    }

    private static int lrpNumber = 100;

    private boolean testCrossing(RoadSegment rs1, RoadSegment rs2)
    {
        if (rs1.equals(rs2) || rs1.getRoad().equals(rs2.getRoad()) || !rs1.getGap().isRoad() || !rs2.getGap().isRoad())
        {
            return false;
        }
        return linesIntersect(rs1, rs2);
    }

    private void makeCrossing(RoadSegment rs1, RoadSegment rs2)
    {
        try
        {
            // split both lines at the intersection point.
            OTSPoint3D mid = getLineLineIntersection(rs1, rs2);
            if (mid != null)
            {
                System.out.println("Intersection " + rs1.getRoad() + " - " + rs2.getRoad());

                // unregister
                this.roadNetwork.removeLink(rs1);
                this.roadNetwork.removeLink(rs2);

                // correct the network from the LRPs
                rs1.getStartNode().removeLink(rs1);
                rs1.getEndNode().removeLink(rs1);
                rs2.getStartNode().removeLink(rs2);
                rs2.getEndNode().removeLink(rs2);

                // remove the associated animation if present
                if (rs1.getAnimation() != null)
                {
                    rs1.getAnimation().destroy();
                    rs1.setAnimation(null);
                }
                if (rs1.getSegmentAnimation() != null)
                {
                    rs1.getSegmentAnimation().destroy();
                    rs1.setSegmentAnimation(null);
                }
                if (rs2.getAnimation() != null)
                {
                    rs2.getAnimation().destroy();
                    rs2.setAnimation(null);
                }
                if (rs2.getSegmentAnimation() != null)
                {
                    rs2.getSegmentAnimation().destroy();
                    rs2.setSegmentAnimation(null);
                }

                // remove in roads and store indexes
                int index1 = rs1.getRoad().getSegments().indexOf(rs1);
                if (index1 < 0)
                {
                    System.err.println("Cannot find segment1 " + rs1 + " in road " + rs1.getRoad() + ", segments = "
                            + rs1.getRoad().getSegments());
                }
                rs1.getRoad().removeSegment(index1);
                int index2 = rs2.getRoad().getSegments().indexOf(rs2);
                if (index2 < 0)
                {
                    System.err.println("Cannot find segment2 " + rs2 + " in road " + rs2.getRoad() + ", segments = "
                            + rs2.getRoad().getSegments());
                }
                rs2.getRoad().removeSegment(index2);

                // register new
                double chainage = (rs1.getStartLRP().getChainage() + rs1.getEndLRP().getChainage()) / 2.0;
                String type = rs1.getStartLRP().getType();
                String name = rs1.getStartLRP().getName();
                LRP midLRP =
                        new LRP(this.roadNetwork, rs1.getRoad().getId() + "x" + rs2.getRoad().getId() + "_LRPX" + (lrpNumber++),
                                mid, rs1.getRoad(), chainage, type, name, GapPoint.ROAD, rs1.getStartLRP().getDistrict());
                midLRP.addRoad(rs2.getRoad());
                RoadSegment road11 = makeLink(rs1.getStartLRP(), midLRP, rs1.getRoad(), "", Color.RED);
                RoadSegment road12 = makeLink(midLRP, rs1.getEndLRP(), rs1.getRoad(), "", Color.RED);
                RoadSegment road21 = makeLink(rs2.getStartLRP(), midLRP, rs2.getRoad(), "", Color.RED);
                RoadSegment road22 = makeLink(midLRP, rs2.getEndLRP(), rs2.getRoad(), "", Color.RED);

                // register in the roads
                if (road11 != null)
                    rs1.getRoad().addSegment(index1, road11);
                if (road12 != null)
                    rs1.getRoad().addSegment(index1 + 1, road12);
                if (road21 != null)
                    rs2.getRoad().addSegment(index2, road21);
                if (road22 != null)
                    rs2.getRoad().addSegment(index2 + 1, road22);
            }
        }
        catch (NetworkException | RemoteException | NamingException | OTSGeometryException exception)
        {
            exception.printStackTrace();
        }
    }

    private RoadSegment makeLink(LRP lrp1, LRP lrp2, Road road, String prefix, Color color)
            throws NetworkException, RemoteException, NamingException, OTSGeometryException
    {
        String linkName = prefix + lrp1.getId() + "-" + lrp2.getId();
        String link2 = prefix + lrp2.getId() + "-" + lrp1.getId();
        String link3 = lrp1.getId() + "-" + lrp2.getId();
        String link4 = lrp2.getId() + "-" + lrp1.getId();
        if (!this.roadNetwork.containsLink(linkName) && !this.roadNetwork.containsLink(link2)
                && !this.roadNetwork.containsLink(link3) && !this.roadNetwork.containsLink(link4))
        {
            OTSLine3D designLine = new OTSLine3D(new OTSPoint3D[] { lrp1.getPoint(), lrp2.getPoint() });
            char roadType = road.getId().charAt(0);
            switch (roadType)
            {
                case 'N':
                {
                    SegmentN r = new SegmentN(this.roadNetwork, linkName, road, lrp1, lrp2, LinkType.ALL, designLine,
                            this.simulator, LongitudinalDirectionality.DIR_BOTH, Gap.ROAD, this.parameters);
                    if (this.animation)
                    {
                        r.setAnimation(new RoadNAnimation(r, road.getId(), this.simulator, (float) (5.0 * 0.0005), color));
                        addRoadSegmentAnimation(r);
                    }
                    road.addSegment(r);
                    return r;
                }

                case 'R':
                {
                    SegmentR r = new SegmentR(this.roadNetwork, linkName, road, lrp1, lrp2, LinkType.ALL, designLine,
                            this.simulator, LongitudinalDirectionality.DIR_BOTH, Gap.ROAD, this.parameters);
                    if (this.animation)
                    {
                        r.setAnimation(new RoadRAnimation(r, road.getId(), this.simulator, (float) (3.0 * 0.0005), color));
                        addRoadSegmentAnimation(r);
                    }
                    road.addSegment(r);
                    return r;
                }

                case 'Z':
                {
                    SegmentZ r = new SegmentZ(this.roadNetwork, linkName, road, lrp1, lrp2, LinkType.ALL, designLine,
                            this.simulator, LongitudinalDirectionality.DIR_BOTH, Gap.ROAD, this.parameters);
                    if (this.animation)
                    {
                        r.setAnimation(new RoadZAnimation(r, road.getId(), this.simulator, (float) (1.0 * 0.0005), color));
                        addRoadSegmentAnimation(r);
                    }
                    road.addSegment(r);
                    return r;
                }

                default:
                    System.err.println("Road not N, R or Z but " + roadType + " based on road " + road.getId());
                    break;
            }
        }
        return null;
    }

    private void findDistrictCentroids()
    {
        System.out.println("Find district centroids");
        for (District district : this.districtCode2Map.values())
        {
            OTSPoint3D c = district.getCentroid();
            double bestDist = Double.MAX_VALUE;
            LRP bestLRP = null;
            for (LRP lrp : this.districtLRPMap.get(district))
            {
                double distance = c.distanceSI(lrp.getPoint());
                distance = lrp.getId().charAt(0) == 'R' ? (double) this.parameters.get("CONST_RFACTOR") * distance
                        : lrp.getId().charAt(0) == 'N' ? (double) this.parameters.get("CONST_NFACTOR") * distance
                                : (double) this.parameters.get("CONST_ZFACTOR") * distance;
                if (distance < bestDist)
                {
                    bestDist = distance;
                    bestLRP = lrp;
                }
            }
            bestLRP.setCentroid(true);
            DistrictCentroid centroid = new DistrictCentroid(bestLRP, district, this.simulator);
            district.setDistrictCentroid(centroid);
        }
    }

    private void readPorts(final String filename) throws Exception
    {
        // 0: Id, 1: Name, 2: Lat, 3: Lon, 4: Road, 5: LRP, 6: Product, 7: ImportTon, 8: ExportTon
        System.out.println("Read Port-file: " + filename);
        FileInputStream fis;
        if (new File(filename).canRead())
            fis = new FileInputStream(filename);
        else
            fis = new FileInputStream(CentroidRoutesModel.class.getResource(filename).getFile());
        try (CSVReader reader = new CSVReader(new InputStreamReader(fis), ',', '"', 1))
        {
            String[] parts;
            String portId = "";
            Port port = null;
            while ((parts = reader.readNext()) != null)
            {
                if (parts[0].length() > 0) // port ID should be filled in every row
                {
                    if (!portId.equals(parts[0]))
                    {
                        portId = parts[0];
                        LRP lrp = this.lrpMap.get(parts[5]);
                        if (lrp == null)
                        {
                            System.err.println("Cannot find Port LRP " + parts[5] + " in LRP map");
                            System.exit(-1);
                        }
                        port = new Port(portId, parts[1], lrp, this.simulator);
                        this.ports.put(portId, port);
                        for (String product : this.productList)
                        {
                            port.getImports().put(product, 0.0);
                            port.getExports().put(product, 0.0);
                        }
                        lrp.setPort(true);
                    }
                    String product = parts[6];
                    double importTon = parts[7].length() > 0 ? (double) Double.parseDouble(parts[7]) : 0.0;
                    port.getImports().put(product, importTon);
                    double exportTon = parts[8].length() > 0 ? (double) Double.parseDouble(parts[8]) : 0.0;
                    port.getExports().put(product, exportTon);
                }
            }
        }
    }

    private void readLandPorts(final String filename) throws Exception
    {
        // 0: Id, 1: Name, 2: Lat, 3: Lon, 4: Road, 5: LRP, 6: Product, 7: ImportMTon, 8: ExportMTon
        System.out.println("Read LandPort-file: " + filename);
        FileInputStream fis;
        if (new File(filename).canRead())
            fis = new FileInputStream(filename);
        else
            fis = new FileInputStream(CentroidRoutesModel.class.getResource(filename).getFile());
        try (CSVReader reader = new CSVReader(new InputStreamReader(fis), ',', '"', 1))
        {
            String[] parts;
            String landPortId = "";
            LandPort landPort = null;
            while ((parts = reader.readNext()) != null)
            {
                if (parts[0].length() > 0) // landport ID should be filled in every row
                {
                    if (!landPortId.equals(parts[0]))
                    {
                        landPortId = parts[0];
                        LRP lrp = this.lrpMap.get(parts[5]);
                        if (lrp == null)
                        {
                            System.err.println("Cannot find LandPort LRP " + parts[5] + " in LRP map");
                            System.exit(-1);
                        }
                        landPort = new LandPort(landPortId, parts[1], lrp, this.simulator);
                        this.landPorts.put(landPortId, landPort);
                        for (String product : this.productList)
                        {
                            landPort.getImports().put(product, 0.0);
                            landPort.getExports().put(product, 0.0);
                        }
                        lrp.setLandPort(true);
                    }
                    String product = parts[6];
                    double importTon = parts[7].length() > 0 ? (double) Double.parseDouble(parts[7]) : 0.0;
                    landPort.getImports().put(product, importTon);
                    double exportTon = parts[8].length() > 0 ? (double) Double.parseDouble(parts[8]) : 0.0;
                    landPort.getExports().put(product, exportTon);
                }
            }
        }
    }

    private void makeSimplifiedRoadNetwork() throws NetworkException, OTSGeometryException
    {
        for (Road road : this.roadMap.values())
        {
            List<OTSPoint3D> points = new ArrayList<>();
            List<RoadSegment> segmentList = new ArrayList<>();
            List<RoadSegment> segments = road.getSegments();
            LRP startLRP = segments.get(0).getStartLRP();
            for (int i = 0; i < segments.size(); i++)
            {
                RoadSegment segment = segments.get(i);
                LRP endLRP = segment.getEndLRP();
                points.addAll(Arrays.asList(segment.getDesignLine().getPoints()));
                segmentList.add(segment);
                if (endLRP.getCardinality() == 1 || endLRP.getCardinality() >= 3 || endLRP.isCentroid() || endLRP.isLandPort()
                        || endLRP.isPort() || endLRP.isTerminalLink() || startLRP.getId().endsWith("LRPS")
                        || startLRP.getId().endsWith("LRPE") || endLRP.getId().endsWith("LRPS")
                        || endLRP.getId().endsWith("LRPE") || i == segments.size() - 1 || i == 0)
                {
                    RoadNode startNode = (RoadNode) this.simplifiedRoadNetwork.getNode(startLRP.getId());
                    if (startNode == null)
                        startNode = new RoadNode(this.simplifiedRoadNetwork, startLRP);
                    RoadNode endNode = (RoadNode) this.simplifiedRoadNetwork.getNode(endLRP.getId());
                    if (endNode == null)
                        endNode = new RoadNode(this.simplifiedRoadNetwork, endLRP);
                    String linkId = startNode.getId() + "-" + endNode.getId();
                    if (!this.simplifiedRoadNetwork.containsLink(linkId))
                    {
                        OTSLine3D line = OTSLine3D.createAndCleanOTSLine3D(points);
                        RoadLink link = new RoadLink(this.simplifiedRoadNetwork, linkId, startNode, endNode, road, line,
                                this.simulator);
                        link.setSegments(segmentList);
                    }
                    segmentList = new ArrayList<>();
                    if (i < segments.size() - 1)
                        startLRP = segments.get(i + 1).getStartLRP();
                    points = new ArrayList<>();
                }
            }
        }
    }

    /**
     * Build a graph of the regular road network.
     */
    public final void buildRegularRoadGraph()
    {
        System.out.println("\nBuild regular (undisturbed) road graph");
        @SuppressWarnings("unchecked")
        Class<? extends LinkEdge<RoadLink>> linkEdgeClass = (Class<? extends LinkEdge<RoadLink>>) LinkEdge.class;
        this.roadRegularGraph = new SimpleDirectedWeightedGraph<RoadNode, LinkEdge<RoadLink>>(linkEdgeClass);
        for (Node node : this.simplifiedRoadNetwork.getNodeMap().values())
        {
            this.roadRegularGraph.addVertex((RoadNode) node);
        }
        for (Link link : this.simplifiedRoadNetwork.getLinkMap().values())
        {
            if (!link.getStartNode().equals(link.getEndNode()))
            {
                if (link instanceof RoadLink)
                {
                    RoadLink roadLink = (RoadLink) link;
                    double weight = 0.0;
                    for (RoadSegment segment : roadLink.getSegments())
                    {
                        weight += segment.getAvgRegularTruckDuration().si;
                    }
                    LinkEdge<RoadLink> linkEdge1 = new LinkEdge<>(roadLink);
                    this.roadRegularGraph.addEdge((RoadNode) roadLink.getStartNode(), (RoadNode) roadLink.getEndNode(),
                            linkEdge1);
                    this.roadRegularGraph.setEdgeWeight(linkEdge1, weight);
                    LinkEdge<RoadLink> linkEdge2 = new LinkEdge<>(roadLink);
                    this.roadRegularGraph.addEdge((RoadNode) roadLink.getEndNode(), (RoadNode) roadLink.getStartNode(),
                            linkEdge2);
                    this.roadRegularGraph.setEdgeWeight(linkEdge2, weight);
                }
            }
        }
    }

    /**
     * Build a graph of the disturbed road network.
     */
    public final void buildDisturbedRoadGraph()
    {
        System.out.println("\nBuild disturbed road graph");
        @SuppressWarnings("unchecked")
        Class<? extends LinkEdge<RoadLink>> linkEdgeClass = (Class<? extends LinkEdge<RoadLink>>) LinkEdge.class;
        this.roadDisturbedGraph = new SimpleDirectedWeightedGraph<RoadNode, LinkEdge<RoadLink>>(linkEdgeClass);
        for (Node node : this.simplifiedRoadNetwork.getNodeMap().values())
        {
            this.roadDisturbedGraph.addVertex((RoadNode) node);
        }
        for (Link link : this.simplifiedRoadNetwork.getLinkMap().values())
        {
            if (!link.getStartNode().equals(link.getEndNode()))
            {
                if (link instanceof RoadLink)
                {
                    RoadLink roadLink = (RoadLink) link;
                    double weight = 0.0;
                    for (RoadSegment segment : roadLink.getSegments())
                    {
                        weight += segment.getAvgDisturbedTruckDuration().si;
                    }
                    LinkEdge<RoadLink> linkEdge1 = new LinkEdge<>(roadLink);
                    this.roadDisturbedGraph.addEdge((RoadNode) roadLink.getStartNode(), (RoadNode) roadLink.getEndNode(),
                            linkEdge1);
                    this.roadDisturbedGraph.setEdgeWeight(linkEdge1, weight);
                    LinkEdge<RoadLink> linkEdge2 = new LinkEdge<>(roadLink);
                    this.roadDisturbedGraph.addEdge((RoadNode) roadLink.getEndNode(), (RoadNode) roadLink.getStartNode(),
                            linkEdge2);
                    this.roadDisturbedGraph.setEdgeWeight(linkEdge2, weight);
                }
            }
        }
    }

    /**
     * @param roadGraph the graph (disturbed or regular) to use
     * @param nodeFrom should be a RoadNode
     * @param nodeTo should be a RoadNode
     * @return the route in the simplified network
     * @throws NetworkException when links cannot be connected
     */
    public final CompleteRouteRoad getShortestRoadBetween(
            final SimpleDirectedWeightedGraph<RoadNode, LinkEdge<RoadLink>> roadGraph, final RoadNode nodeFrom,
            final RoadNode nodeTo) throws NetworkException
    {
        CompleteRouteRoad route = new CompleteRouteRoad("Route from " + nodeFrom + "to " + nodeTo);
        DijkstraShortestPath<RoadNode, LinkEdge<RoadLink>> path =
                new DijkstraShortestPath<RoadNode, LinkEdge<RoadLink>>(roadGraph, nodeFrom, nodeTo);
        if (path.getPath() == null)
        {
            return null;
        }
        route.addNode(nodeFrom);
        for (LinkEdge<RoadLink> link : path.getPathEdgeList())
        {
            if (!link.getLink().getEndNode().equals(route.destinationNode()))
            {
                route.addNode(link.getLink().getEndNode());
            }
            else if (!link.getLink().getStartNode().equals(route.destinationNode()))
            {
                route.addNode(link.getLink().getStartNode());
            }
            else
            {
                throw new NetworkException("Cannot connect two links when calculating shortest route");
            }
        }
        return route;
    }

    private void calculateODDistancesByRoad() throws NetworkException
    {
        // init
        this.numberOD = this.districtCode2Map.size() + this.ports.size() + this.landPorts.size();
        this.roadRegularMatrix = new CompleteRouteRoad[this.numberOD][this.numberOD];
        this.roadDisturbedMatrix = new CompleteRouteRoad[this.numberOD][this.numberOD];
        this.roadRegularTimeMatrix = new double[this.numberOD][this.numberOD];
        this.roadDisturbedTimeMatrix = new double[this.numberOD][this.numberOD];
        for (int i = 0; i < this.numberOD; i++)
        {
            this.roadRegularMatrix[i] = new CompleteRouteRoad[this.numberOD];
            this.roadDisturbedMatrix[i] = new CompleteRouteRoad[this.numberOD];
            this.roadRegularTimeMatrix[i] = new double[this.numberOD];
            this.roadDisturbedTimeMatrix[i] = new double[this.numberOD];
        }

        // make the graphs
        buildRegularRoadGraph();
        buildDisturbedRoadGraph();

        System.out.println("\nCalculate shortest routes between districts by road");

        // calculate the undisturbed and disturbed routes
        for (int i = 0; i < this.numberOD; i++)
        {
            ODNode od1 = this.odNodes[i];
            RoadNode roadNode1 = (RoadNode) this.simplifiedRoadNetwork.getNode(od1.getLRP().getId());
            for (int j = 0; j < this.numberOD; j++)
            {
                ODNode od2 = this.odNodes[j];
                RoadNode roadNode2 = (RoadNode) this.simplifiedRoadNetwork.getNode(od2.getLRP().getId());
                if (i < j) // assume symmetry in travel times by road
                {
                    CompleteRouteRoad regularRoute = getShortestRoadBetween(this.roadRegularGraph, roadNode1, roadNode2);
                    CompleteRouteRoad disturbedRoute = getShortestRoadBetween(this.roadDisturbedGraph, roadNode1, roadNode2);
                    if (regularRoute == null)
                    {
                        System.err.println("WARNING: NO REGULAR ROUTE From " + od1.getId() + " to " + od2.getId() + " ("
                                + od1.getLRP().getId() + " - " + od2.getLRP().getId() + ")");
                    }
                    if (disturbedRoute == null)
                    {
                        System.err.println("WARNING: NO DISTURBED ROUTE From " + od1.getId() + " to " + od2.getId() + " ("
                                + od1.getLRP().getId() + " - " + od2.getLRP().getId() + ")");
                    }
                    else
                    {
                        this.roadRegularMatrix[i][j] = regularRoute;
                        this.roadRegularMatrix[j][i] = regularRoute;
                        this.roadDisturbedMatrix[i][j] = disturbedRoute;
                        this.roadDisturbedMatrix[j][i] = disturbedRoute;
                        this.roadRegularTimeMatrix[i][j] = regularRoute.getAvgRegularTruckDuration().si / 3600.0;
                        this.roadRegularTimeMatrix[j][i] = regularRoute.getAvgRegularTruckDuration().si / 3600.0;
                        this.roadDisturbedTimeMatrix[i][j] = disturbedRoute.getAvgDisturbedTruckDuration().si / 3600.0;
                        this.roadDisturbedTimeMatrix[j][i] = disturbedRoute.getAvgDisturbedTruckDuration().si / 3600.0;

                        System.out.println("From " + od1.getId() + " to " + od2.getId() + " (" + od1.getLRP().getId() + " - "
                                + od2.getLRP().getId() + ") REGULAR:" + regularRoute.routeRoadNames() + ", distance = "
                                + regularRoute.getLength() + ", time=" + regularRoute.getAvgRegularTruckDuration());
                        System.out.println("From " + od1.getId() + " to " + od2.getId() + " (" + od1.getLRP().getId() + " - "
                                + od2.getLRP().getId() + ") DISTURB:" + disturbedRoute.routeRoadNames() + ", distance = "
                                + disturbedRoute.getLength() + ", time=" + disturbedRoute.getAvgDisturbedTruckDuration());
                    }
                }
            }
        }
    }

    private void determineWaterwayTerminals(final String filename) throws Exception
    {
        FileInputStream fis;
        if (new File(filename).canRead())
            fis = new FileInputStream(filename);
        else
        {
            if (CentroidRoutesModel.class.getResource(filename) != null
                    && new File(CentroidRoutesModel.class.getResource(filename).getFile()).canRead())
                fis = new FileInputStream(CentroidRoutesModel.class.getResource(filename).getFile());
            else
                fis = null;
        }

        if (fis != null)
        {
            System.out.println("Read WaterwayTerminal-file: " + filename);
            readWaterwayTerminals(fis);
        }
        else
        {
            System.out.println("Build WaterwayTerminals");
            makeWaterwayTerminals();
            System.out.println("Write WaterwayTerminal-file: " + filename);
            writeWaterwayTerminals(filename);
        }
    }

    /**
     * Return the district of point (lat, lon)
     * @param id the id of the point for warnings
     * @param lat latitude
     * @param lon longitude
     * @param lastDistrict the last district we searched to quickly find the next (can be null)
     * @return the district in which this point lies, or null when not found
     */
    private District getDistrict(String id, double lat, double lon, District lastDistrict)
    {
        GeometryFactory geometryFactory = new GeometryFactory();
        District district = null;
        Point p = geometryFactory.createPoint(new Coordinate(lon, lat));
        if (lastDistrict != null)
        {
            if (lastDistrict.getPolygon().contains(p))
            {
                district = lastDistrict;
            }
        }
        if (district == null)
        {
            for (District d : this.districtCodeMap.values())
            {
                if (d.getPolygon().contains(p))
                {
                    district = d;
                    lastDistrict = d;
                    break;
                }
            }
        }
        if (district == null)
        {
            System.out.print("cannot find district of point " + id + " at (" + lat + "," + lon + "). Searching boxes... ");
            for (District d : this.districtCodeMap.values())
            {
                if (d.getPolygon().getEnvelope().contains(p))
                {
                    district = d;
                    System.out.println("Found " + d.getCode2() + ": " + d.getName());
                    lastDistrict = null;
                    break;
                }
            }
        }

        return district;
    }

    /** the maximum distance between an LRP and a waterway-node to link it to a terminal. */
    private static final double DISTANCE_ROAD_WWTERMINAL = 2000;

    /**
     * Make terminals connecting waterways and roads based on distance between (major) waterways and (major) roads. Starts and
     * ends of roads are preferred over midpoints of roads, as the starts and ends that are close to waterways often are
     * terminal locations. For now, only one terminal per district is identified.
     * @throws RemoteException on distance error
     */
    private void makeWaterwayTerminals() throws RemoteException
    {
        // make a list of waterway nodes per district
        Map<District, List<Node>> districtWwNodeMap = new HashMap<>();
        for (District district : this.districtCode2Map.values())
            districtWwNodeMap.put(district, new ArrayList<>());

        District lastDistrict = null;
        for (Node node : this.waterwayNetwork.getNodeMap().values())
        {
            District district = getDistrict(node.getId(), node.getLocation().y, node.getLocation().x, lastDistrict);
            if (district != null)
            {
                districtWwNodeMap.get(district).add(node);
                lastDistrict = district;
            }
            else
                System.out.println("Could not find district for waterway-point " + node.getId());
        }

        Map<District, SortedSet<NodeLRP>> candidateNodes = new HashMap<>();
        int terminalIndex = 0;
        for (District district : this.districtCode2Map.values())
        {
            candidateNodes.put(district, new TreeSet<>());
            for (Node node : districtWwNodeMap.get(district))
            {
                WaterwayNode wwNode = (WaterwayNode) node;
                for (LRP lrp : this.districtLRPMap.get(district))
                {
                    double dist = distance(wwNode, lrp);
                    if (dist < DISTANCE_ROAD_WWTERMINAL)
                    {
                        // see if the same road is already there; if so take nearest point
                        NodeLRP sameRoadFartherNodeLRP = null;
                        NodeLRP sameRoadCloserNodeLRP = null;
                        for (NodeLRP nodeLRP : candidateNodes.get(district))
                        {
                            if (lrp.getRoad().equals(nodeLRP.getLRP().getRoad()))
                            {
                                if (dist > nodeLRP.getDistance())
                                    sameRoadCloserNodeLRP = nodeLRP;
                                else
                                    sameRoadFartherNodeLRP = nodeLRP;
                                break;
                            }
                        }
                        if (sameRoadFartherNodeLRP != null)
                            candidateNodes.get(district).remove(sameRoadFartherNodeLRP);
                        if (sameRoadCloserNodeLRP == null)
                            candidateNodes.get(district).add(new NodeLRP(wwNode, lrp, dist));
                    }
                }
            }

            // report candidates
            System.out.println("\nCandidate nodes for district " + district.getCode2());
            for (NodeLRP nodeLRP : candidateNodes.get(district))
            {
                System.out.println(nodeLRP);
            }
            if (!candidateNodes.get(district).isEmpty())
            {
                NodeLRP bestNode = candidateNodes.get(district).iterator().next();
                System.out.println("Best terminal location: " + bestNode);
                String id = district.getCode2() + ":" + bestNode.getWaterwayNode().getId();
                String name = district.getCode2() + ":" + bestNode.getWaterwayNode().getWaterwayName() + " ("
                        + bestNode.getWaterwayNode().getId() + ")";
                WaterwayTerminal waterwayTerminal = new WaterwayTerminal(id, name, terminalIndex, bestNode.getWaterwayNode(),
                        bestNode.getLRP(), district, this.simulator, this.parameters);
                this.waterwayTerminalIndexMap.put(terminalIndex, waterwayTerminal);
                this.districtWaterwayTerminalMap.put(waterwayTerminal.getDistrict().getCode2(), waterwayTerminal);
                terminalIndex++;
                this.waterwayTerminals.put(waterwayTerminal.getId(), waterwayTerminal);
                bestNode.getLRP().setTerminalLink(true);
            }
        }
    }

    /** */
    private class NodeLRP implements Comparable<NodeLRP>
    {
        private final WaterwayNode waterwayNode;

        private final LRP lrp;

        private final double distance;

        public NodeLRP(WaterwayNode waterwayNode, LRP lrp, double distance)
        {
            super();
            this.waterwayNode = waterwayNode;
            this.lrp = lrp;
            this.distance = distance;
        }

        public WaterwayNode getWaterwayNode()
        {
            return this.waterwayNode;
        }

        public LRP getLRP()
        {
            return this.lrp;
        }

        public double getDistance()
        {
            return this.distance;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "NodeLRP [node=" + this.waterwayNode + ", lrp=" + this.lrp + ", distance=" + this.distance + "]";
        }

        /**
         * {@inheritDoc}
         * <p>
         * Sorting criteria: <br>
         * - lower waterway class (Class-I carries the largest vessels) gets priority (to do)<br>
         * - higher road (N &gt; R &gt; Z) gets priority<br>
         * - LRP with an S or E in the name gets priority<br>
         * - shorter distance gets priority<br>
         */
        @Override
        public int compareTo(NodeLRP o)
        {
            if (this.equals(o))
                return 0;
            if (this.waterwayNode.getWaterwayClass() < o.waterwayNode.getWaterwayClass())
                return -1;
            if (this.waterwayNode.getWaterwayClass() > o.waterwayNode.getWaterwayClass())
                return 1;
            String id1 = getLRP().getId();
            String id2 = o.getLRP().getId();
            if (id1.charAt(0) < id2.charAt(0))
                return -1;
            if (id1.charAt(0) > id2.charAt(0))
                return 1;
            if (id1.contains("LRPS") && !id2.contains("LRPS"))
                return -1;
            if (!id1.contains("LRPS") && id2.contains("LRPS"))
                return 1;
            if (getDistance() < o.getDistance())
                return -1;
            if (getDistance() > o.getDistance())
                return 1;
            return toString().compareTo(o.toString());
        }
    }

    /**
     * The file contains: "terminalId", "terminalName", "waterwayNodeId", "lrpId", "districtCode2"
     * @param fis file stream
     * @throws IOException on error
     */
    private void readWaterwayTerminals(final FileInputStream fis) throws IOException
    {
        try (CSVReader reader = new CSVReader(new InputStreamReader(fis), ',', '"', 1))
        {
            String[] parts;
            while ((parts = reader.readNext()) != null)
            {
                String terminalId = parts[0];
                WaterwayNode waterwayNode = (WaterwayNode) this.waterwayNetwork.getNode(parts[2]);
                if (waterwayNode == null)
                {
                    System.err.println("Cannot find Terminal Node " + parts[0] + " in waterway network map");
                    System.exit(-1);
                }
                LRP lrp = this.lrpMap.get(parts[3]);
                if (lrp == null)
                {
                    System.err.println("Cannot find LandPort LRP " + parts[1] + " in LRP map");
                    System.exit(-1);
                }
                District district = this.districtCode2Map.get(parts[4]);
                int terminalIndex = Integer.parseInt(parts[5].trim());
                WaterwayTerminal terminal = new WaterwayTerminal(terminalId, parts[1], terminalIndex, waterwayNode, lrp,
                        district, this.simulator, this.parameters);
                this.waterwayTerminals.put(terminalId, terminal);
                this.waterwayTerminalIndexMap.put(terminalIndex, terminal);
                this.districtWaterwayTerminalMap.put(terminal.getDistrict().getCode2(), terminal);
                terminal.getLRP().setTerminalLink(true);
            }
        }
    }

    /**
     * The file contains: "terminalId", "terminalName", "waterwayNodeId", "lrpId", "districtCode2"
     * @param filename file name to write
     * @throws IOException on error
     */
    private void writeWaterwayTerminals(final String filename) throws IOException
    {
        try (PrintWriter writer = new PrintWriter(new File(filename)))
        {
            writer.println("\"terminalId\", \"terminalName\", \"waterwayNodeId\", \"lrpId\", \"districtCode2\", \"index\"");
            for (WaterwayTerminal waterwayTerminal : this.waterwayTerminals.values())
            {
                writer.println("\"" + waterwayTerminal.getId() + "\", \"" + waterwayTerminal.getName() + "\", \""
                        + waterwayTerminal.getWaterwayNode().getId() + "\", \"" + waterwayTerminal.getLRP().getId() + "\", \""
                        + waterwayTerminal.getDistrict().getCode2() + "\", " + waterwayTerminal.getIndex());
            }
        }
    }

    /**
     * Calculate the fastest route from a waterway terminal to the centroid in the district of the terminal.
     * @throws NetworkException on network error for calculating shortest path
     */
    private void makeWaterwayTerminalCentroidRouteRegular() throws NetworkException
    {
        for (WaterwayTerminal waterwayTerminal : this.waterwayTerminals.values())
        {
            District district = waterwayTerminal.getDistrict();
            LRP centroidLRP = district.getDistrictCentroid().getLRP();
            LRP terminalLRP = waterwayTerminal.getLRP();
            RoadNode roadNode1 = (RoadNode) this.simplifiedRoadNetwork.getNode(centroidLRP.getId());
            RoadNode roadNode2 = (RoadNode) this.simplifiedRoadNetwork.getNode(terminalLRP.getId());
            CompleteRouteRoad route = getShortestRoadBetween(this.roadRegularGraph, roadNode1, roadNode2);
            this.waterwayTerminalCentroidRouteRegular.put(waterwayTerminal, route);
        }
    }

    /**
     * Calculate the fastest route from a waterway terminal to the centroid in the district of the terminal.
     * @throws NetworkException on network error for calculating shortest path
     */
    private void makeWaterwayTerminalCentroidRouteDisturbed() throws NetworkException
    {
        for (WaterwayTerminal waterwayTerminal : this.waterwayTerminals.values())
        {
            District district = waterwayTerminal.getDistrict();
            LRP centroidLRP = district.getDistrictCentroid().getLRP();
            LRP terminalLRP = waterwayTerminal.getLRP();
            RoadNode roadNode1 = (RoadNode) this.simplifiedRoadNetwork.getNode(centroidLRP.getId());
            RoadNode roadNode2 = (RoadNode) this.simplifiedRoadNetwork.getNode(terminalLRP.getId());
            CompleteRouteRoad route = getShortestRoadBetween(this.roadDisturbedGraph, roadNode1, roadNode2);
            this.waterwayTerminalCentroidRouteDisturbed.put(waterwayTerminal, route);
        }
    }

    /**
     * Calculate the fastest route between terminals, .
     * @throws NetworkException on network error for calculating shortest path
     */
    private void calculateWaterwayODMatrix() throws NetworkException
    {
        /*
         * completeRouteWater[][] waterwayDisturbedMatrix; private double[][] waterwayRegularTimeMatrix
         */
        // init
        int nrTerminals = this.waterwayTerminals.size();
        this.waterwayRegularMatrix = new CompleteRouteWaterway[nrTerminals][nrTerminals];
        this.waterwayDisturbedMatrix = new CompleteRouteWaterway[nrTerminals][nrTerminals];
        this.waterwayRegularTimeMatrix = new double[nrTerminals][nrTerminals];
        this.waterwayDisturbedTimeMatrix = new double[nrTerminals][nrTerminals];
        for (int i = 0; i < nrTerminals; i++)
        {
            this.waterwayRegularMatrix[i] = new CompleteRouteWaterway[nrTerminals];
            this.waterwayDisturbedMatrix[i] = new CompleteRouteWaterway[nrTerminals];
            this.waterwayRegularTimeMatrix[i] = new double[nrTerminals];
            this.waterwayDisturbedTimeMatrix[i] = new double[nrTerminals];
        }

        // make the graphs
        buildRegularWaterwayGraph();
        buildDisturbedWaterwayGraph();

        System.out.println("\nCalculate shortest routes between districts by waterway");

        // calculate the undisturbed and disturbed routes
        for (int i = 0; i < nrTerminals; i++)
        {
            WaterwayTerminal terminal1 = this.waterwayTerminalIndexMap.get(i);
            WaterwayNode waterwayNode1 = terminal1.getWaterwayNode();
            for (int j = 0; j < nrTerminals; j++)
            {
                WaterwayTerminal terminal2 = this.waterwayTerminalIndexMap.get(j);
                WaterwayNode waterwayNode2 = terminal2.getWaterwayNode();
                if (i < j) // assume symmetry in travel times by waterway -- current can be added if necessary
                {
                    CompleteRouteWaterway regularRoute =
                            getShortestWaterwayBetween(this.waterwayRegularGraph, waterwayNode1, waterwayNode2);
                    CompleteRouteWaterway disturbedRoute =
                            getShortestWaterwayBetween(this.waterwayDisturbedGraph, waterwayNode1, waterwayNode2);
                    if (regularRoute == null)
                    {
                        System.err.println("WARNING: NO REGULAR ROUTE From " + terminal1.getId() + " to " + terminal2.getId()
                                + " (" + terminal1.getWaterwayNode().getId() + " - " + terminal2.getWaterwayNode().getId()
                                + ")");
                    }
                    if (disturbedRoute == null)
                    {
                        System.err.println("WARNING: NO DISTURBED ROUTE From " + terminal1.getId() + " to " + terminal2.getId()
                                + " (" + terminal1.getWaterwayNode().getId() + " - " + terminal2.getWaterwayNode().getId()
                                + ")");
                    }
                    else
                    {
                        this.waterwayRegularMatrix[i][j] = regularRoute;
                        this.waterwayRegularMatrix[j][i] = regularRoute;
                        this.waterwayDisturbedMatrix[i][j] = disturbedRoute;
                        this.waterwayDisturbedMatrix[j][i] = disturbedRoute;
                        this.waterwayRegularTimeMatrix[i][j] = regularRoute.getAvgRegularBargeDuration().si / 3600.0;
                        this.waterwayRegularTimeMatrix[j][i] = regularRoute.getAvgRegularBargeDuration().si / 3600.0;
                        this.waterwayDisturbedTimeMatrix[i][j] = disturbedRoute.getAvgDisturbedBargeDuration().si / 3600.0;
                        this.waterwayDisturbedTimeMatrix[j][i] = disturbedRoute.getAvgDisturbedBargeDuration().si / 3600.0;

                        System.out.println("From " + terminal1.getId() + " to " + terminal2.getId() + " ("
                                + terminal1.getWaterwayNode().getId() + " - " + terminal2.getWaterwayNode().getId()
                                + ") REGULAR:" + regularRoute.routeWaterwayNames() + ", distance = " + regularRoute.getLength()
                                + ", time=" + regularRoute.getAvgRegularBargeDuration());
                        System.out.println("From " + terminal1.getId() + " to " + terminal2.getId() + " ("
                                + terminal1.getWaterwayNode().getId() + " - " + terminal2.getWaterwayNode().getId()
                                + ") DISTURB:" + disturbedRoute.routeWaterwayNames() + ", distance = "
                                + disturbedRoute.getLength() + ", time=" + disturbedRoute.getAvgDisturbedBargeDuration());
                    }
                }
            }
        }
    }

    /**
     * Build a graph of the regular waterway network.
     */
    public final void buildRegularWaterwayGraph()
    {
        System.out.println("\nBuild regular (undisturbed) waterway graph");
        @SuppressWarnings("unchecked")
        Class<? extends LinkEdge<WaterwayLink>> linkEdgeClass = (Class<? extends LinkEdge<WaterwayLink>>) LinkEdge.class;
        this.waterwayRegularGraph = new SimpleDirectedWeightedGraph<WaterwayNode, LinkEdge<WaterwayLink>>(linkEdgeClass);
        for (Node node : this.waterwayNetwork.getNodeMap().values())
        {
            this.waterwayRegularGraph.addVertex((WaterwayNode) node);
        }
        for (Link link : this.waterwayNetwork.getLinkMap().values())
        {
            if (!link.getStartNode().equals(link.getEndNode()))
            {
                if (link instanceof WaterwayLink)
                {
                    WaterwayLink waterwayLink = (WaterwayLink) link;
                    double weight = waterwayLink.getAvgRegularBargeDuration().si;
                    LinkEdge<WaterwayLink> linkEdge1 = new LinkEdge<>(waterwayLink);
                    this.waterwayRegularGraph.addEdge((WaterwayNode) waterwayLink.getStartNode(),
                            (WaterwayNode) waterwayLink.getEndNode(), linkEdge1);
                    this.waterwayRegularGraph.setEdgeWeight(linkEdge1, weight);
                    LinkEdge<WaterwayLink> linkEdge2 = new LinkEdge<>(waterwayLink);
                    this.waterwayRegularGraph.addEdge((WaterwayNode) waterwayLink.getEndNode(),
                            (WaterwayNode) waterwayLink.getStartNode(), linkEdge2);
                    this.waterwayRegularGraph.setEdgeWeight(linkEdge2, weight);
                }
            }
        }
    }

    /**
     * Build a graph of the disturbed waterway network.
     */
    public final void buildDisturbedWaterwayGraph()
    {
        System.out.println("\nBuild disturbed waterway graph");
        @SuppressWarnings("unchecked")
        Class<? extends LinkEdge<WaterwayLink>> linkEdgeClass = (Class<? extends LinkEdge<WaterwayLink>>) LinkEdge.class;
        this.waterwayDisturbedGraph = new SimpleDirectedWeightedGraph<WaterwayNode, LinkEdge<WaterwayLink>>(linkEdgeClass);
        for (Node node : this.waterwayNetwork.getNodeMap().values())
        {
            this.waterwayDisturbedGraph.addVertex((WaterwayNode) node);
        }
        for (Link link : this.waterwayNetwork.getLinkMap().values())
        {
            if (!link.getStartNode().equals(link.getEndNode()))
            {
                if (link instanceof WaterwayLink)
                {
                    WaterwayLink waterwayLink = (WaterwayLink) link;
                    double weight = waterwayLink.getAvgDisturbedBargeDuration().si;
                    LinkEdge<WaterwayLink> linkEdge1 = new LinkEdge<>(waterwayLink);
                    this.waterwayDisturbedGraph.addEdge((WaterwayNode) waterwayLink.getStartNode(),
                            (WaterwayNode) waterwayLink.getEndNode(), linkEdge1);
                    this.waterwayDisturbedGraph.setEdgeWeight(linkEdge1, weight);
                    LinkEdge<WaterwayLink> linkEdge2 = new LinkEdge<>(waterwayLink);
                    this.waterwayDisturbedGraph.addEdge((WaterwayNode) waterwayLink.getEndNode(),
                            (WaterwayNode) waterwayLink.getStartNode(), linkEdge2);
                    this.waterwayDisturbedGraph.setEdgeWeight(linkEdge2, weight);
                }
            }
        }
    }

    /**
     * @param waterwayGraph the graph (disturbed or regular) to use
     * @param nodeFrom should be a WaterwayNode
     * @param nodeTo should be a WaterwayNode
     * @return the route in the simplified network
     * @throws NetworkException when links cannot be connected
     */
    public final CompleteRouteWaterway getShortestWaterwayBetween(
            final SimpleDirectedWeightedGraph<WaterwayNode, LinkEdge<WaterwayLink>> waterwayGraph, final WaterwayNode nodeFrom,
            final WaterwayNode nodeTo) throws NetworkException
    {
        CompleteRouteWaterway route = new CompleteRouteWaterway("Route from " + nodeFrom + "to " + nodeTo);
        DijkstraShortestPath<WaterwayNode, LinkEdge<WaterwayLink>> path =
                new DijkstraShortestPath<WaterwayNode, LinkEdge<WaterwayLink>>(waterwayGraph, nodeFrom, nodeTo);
        if (path.getPath() == null)
        {
            return null;
        }
        route.addNode(nodeFrom);
        for (LinkEdge<WaterwayLink> link : path.getPathEdgeList())
        {
            if (!link.getLink().getEndNode().equals(route.destinationNode()))
            {
                route.addNode(link.getLink().getEndNode());
            }
            else if (!link.getLink().getStartNode().equals(route.destinationNode()))
            {
                route.addNode(link.getLink().getStartNode());
            }
            else
            {
                throw new NetworkException("Cannot connect two links when calculating shortest route");
            }
        }
        return route;
    }

    /** {@inheritDoc} */
    @Override
    public SimulatorInterface<Time, Duration, OTSSimTimeDouble> getSimulator()
    {
        return this.simulator;
    }

    /** {@inheritDoc} */
    @Override
    public OTSNetwork getNetwork()
    {
        return this.roadNetwork;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "GisWaterwayImport [simulator=" + this.simulator + "]";
    }

    public static boolean linesIntersect(Link l1, Link l2)
    {
        try
        {
            return linesIntersect(l1.getStartNode().getLocation(), l1.getEndNode().getLocation(),
                    l2.getStartNode().getLocation(), l2.getEndNode().getLocation());
        }
        catch (RemoteException exception)
        {
            exception.printStackTrace();
            return false;
        }
    }

    /* from http://www.java-gaming.org/index.php?topic=22590.0. */
    public static boolean linesIntersect(DirectedPoint p1, DirectedPoint p2, DirectedPoint q1, DirectedPoint q2)
    {
        double x1 = p1.x;
        double y1 = p1.y;
        double x2 = p2.x;
        double y2 = p2.y;
        double x3 = q1.x;
        double y3 = q1.y;
        double x4 = q2.x;
        double y4 = q2.y;
        // Return false if either of the lines have zero length
        if (x1 == x2 && y1 == y2 || x3 == x4 && y3 == y4)
        {
            return false;
        }
        // Fastest method, based on Franklin Antonio's "Faster Line Segment Intersection" topic "in Graphics Gems III" book
        // (http://www.graphicsgems.org/)
        double ax = x2 - x1;
        double ay = y2 - y1;
        double bx = x3 - x4;
        double by = y3 - y4;
        double cx = x1 - x3;
        double cy = y1 - y3;

        double alphaNumerator = by * cx - bx * cy;
        double commonDenominator = ay * bx - ax * by;
        if (commonDenominator > 0)
        {
            if (alphaNumerator < 0 || alphaNumerator > commonDenominator)
            {
                return false;
            }
        }
        else if (commonDenominator < 0)
        {
            if (alphaNumerator > 0 || alphaNumerator < commonDenominator)
            {
                return false;
            }
        }
        double betaNumerator = ax * cy - ay * cx;
        if (commonDenominator > 0)
        {
            if (betaNumerator < 0 || betaNumerator > commonDenominator)
            {
                return false;
            }
        }
        else if (commonDenominator < 0)
        {
            if (betaNumerator > 0 || betaNumerator < commonDenominator)
            {
                return false;
            }
        }
        if (commonDenominator == 0)
        {
            // This code wasn't in Franklin Antonio's method. It was added by Keith Woodward.
            // The lines are parallel.
            // Check if they're collinear.
            double y3LessY1 = y3 - y1;
            double collinearityTestForP3 = x1 * (y2 - y3) + x2 * (y3LessY1) + x3 * (y1 - y2); // see
                                                                                              // http://mathworld.wolfram.com/Collinear.html
            // If p3 is collinear with p1 and p2 then p4 will also be collinear, since p1-p2 is parallel with p3-p4
            if (collinearityTestForP3 == 0)
            {
                // The lines are collinear. Now check if they overlap.
                if (x1 >= x3 && x1 <= x4 || x1 <= x3 && x1 >= x4 || x2 >= x3 && x2 <= x4 || x2 <= x3 && x2 >= x4
                        || x3 >= x1 && x3 <= x2 || x3 <= x1 && x3 >= x2)
                {
                    if (y1 >= y3 && y1 <= y4 || y1 <= y3 && y1 >= y4 || y2 >= y3 && y2 <= y4 || y2 <= y3 && y2 >= y4
                            || y3 >= y1 && y3 <= y2 || y3 <= y1 && y3 >= y2)
                    {
                        return true;
                    }
                }
            }
            return false;
        }
        return true;
    }

    public static OTSPoint3D getLineLineIntersection(Link l1, Link l2)
    {
        try
        {
            return getLineLineIntersection(l1.getStartNode().getLocation(), l1.getEndNode().getLocation(),
                    l2.getStartNode().getLocation(), l2.getEndNode().getLocation());
        }
        catch (RemoteException exception)
        {
            exception.printStackTrace();
            return null;
        }
    }

    /* from http://www.java-gaming.org/index.php?topic=22590.0. */
    public static OTSPoint3D getLineLineIntersection(DirectedPoint p1, DirectedPoint p2, DirectedPoint q1, DirectedPoint q2)
    {
        double x1 = p1.x;
        double y1 = p1.y;
        double x2 = p2.x;
        double y2 = p2.y;
        double x3 = q1.x;
        double y3 = q1.y;
        double x4 = q2.x;
        double y4 = q2.y;

        double det1And2 = det(x1, y1, x2, y2);
        double det3And4 = det(x3, y3, x4, y4);
        double x1LessX2 = x1 - x2;
        double y1LessY2 = y1 - y2;
        double x3LessX4 = x3 - x4;
        double y3LessY4 = y3 - y4;
        double det1Less2And3Less4 = det(x1LessX2, y1LessY2, x3LessX4, y3LessY4);
        if (det1Less2And3Less4 == 0)
        {
            // the denominator is zero so the lines are parallel and there's either no solution (or multiple solutions if the
            // lines overlap) so return null.
            return null;
        }
        double x = (det(det1And2, x1LessX2, det3And4, x3LessX4) / det1Less2And3Less4);
        double y = (det(det1And2, y1LessY2, det3And4, y3LessY4) / det1Less2And3Less4);
        return new OTSPoint3D(x, y);
    }

    protected static double det(double a, double b, double c, double d)
    {
        return a * d - b * c;
    }

    /**
     * @return animation
     */
    protected final boolean isAnimation()
    {
        return this.animation;
    }

    /**
     * @param animation set animation
     */
    protected final void setAnimation(final boolean animation)
    {
        this.animation = animation;
    }

}
