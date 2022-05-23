package org.matsim.analysis;

import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.gis.ShapeFileReader;

import java.sql.SQLOutput;
import java.util.stream.Collectors;

public class CountActivitiesMitte {

    public static void main(String[] args) {

        var shapeFileName = "C:\\Users\\Ardias\\Desktop\\MATSim\\seminar_5\\Bezirke_-_Berlin\\Berlin_Bezirke.shp";
        var plansFileName = "C:\\Users\\Ardias\\Desktop\\MATSim\\seminar_5\\berlin-v5.5.3-1pct.output_plans.xml.gz";
        var transformation = TransformationFactory.getCoordinateTransformation("EPSG:31468", "EPSG:3857");

        var features = ShapeFileReader.getAllFeatures(shapeFileName);

        var geometriesMitte = features.stream()
                .filter(simpleFeature -> simpleFeature.getAttribute("Gemeinde_s").equals("001"))
                .map(simpleFeature -> (Geometry) simpleFeature.getDefaultGeometry())
                .collect(Collectors.toList());
        var geometriesFhainxberg = features.stream()
                .filter(simpleFeature -> simpleFeature.getAttribute("Gemeinde_s").equals("002"))
                .map(simpleFeature -> (Geometry) simpleFeature.getDefaultGeometry())
                .collect(Collectors.toList());

        var mitteGeometry = geometriesMitte.get(0);
        var fhainxbergGeometry = geometriesFhainxberg.get(0);

        var population = PopulationUtils.readPopulation(plansFileName);

        var counterMitte = 0;
        var counterFhainxberg = 0;

        for (Person person : population.getPersons().values()) {

            var plan = person.getSelectedPlan();
            var activities = TripStructureUtils.getActivities(plan, TripStructureUtils.StageActivityHandling.ExcludeStageActivities);

            // for (Trip trip : trips) {
            //

            for (Activity activity : activities) {

                var coord = activity.getCoord();
                var transformedCoord = transformation.transform(coord);
                var geotoolsPoint = MGC.coord2Point(transformedCoord);
                // MGC = Matsim Geo Coordinate

                if (mitteGeometry.contains(geotoolsPoint)) {
                    counterMitte++;

                }
                if (fhainxbergGeometry.contains(geotoolsPoint)) {
                    counterFhainxberg++;

                }
            }
        }
        System.out.println(counterMitte + " activities in Mitte.");
        System.out.println(counterFhainxberg + " activities in Friedrichshain-Kreuzberg.");
    }
}
