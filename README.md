# wbtransport
Worldbank BGD multimodal transport model

The wbtransport folder contains the java code for the simulation model.

As the pom.xml file shows, it is dependent on:
- com.opencsv - opencsv 
- xml-apis - xml-apis
- org.djunits - djunits 
- dsol - dsol-swing 
- opentrafficsim - ots-core
- opentrafficsim - ots-kpi 
- opentrafficsim - ots-road 
- opentrafficsim - ots-rail 
- opentrafficsim - ots-water
- opentrafficsim - ots-parser-shape 
- org.jfree - jfreechart 
- org.geotools - gt-shapefile 
- org.geotools - gt-main 
- org.geotools - gt-api 
- org.geotools - gt-swing 
- org.geotools - gt-epsg-hsql 
- org.apache.poi - poi 
- org.apache.poi - poi-ooxml
- org.jgrapht - jgrapht-core 
- org.jgrapht - jgrapht-ext 
- com.bric - multislider
- org.ojalgo - ojalgo 
- org.openstreetmap.osmosis - osmosis-core 
- org.openstreetmap.osmosis - osmosis-xml 
- org.openstreetmap.osmosis - osmosis-pbf
- org.openstreetmap.osmosis - osmosis-osm-binary 

The main interactive program to run is `CentroidRoutesApp` for the version with or without animation. A special version to run from the EMA workbench is contained in `CentroidRoutesEMA`. The model with animation that is started from `CentroidRoutesApp` is `CentroidRoutesAnimationModel`. The model without animation is contained in `CentroidRoutesModel`. 
