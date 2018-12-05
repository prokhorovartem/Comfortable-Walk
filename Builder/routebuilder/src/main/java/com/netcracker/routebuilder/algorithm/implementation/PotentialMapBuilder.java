package com.netcracker.routebuilder.algorithm.implementation;

import com.netcracker.routebuilder.data.bean.GeoCoordinates;
import com.netcracker.routebuilder.properties.AlgorithmParameters;
import com.netcracker.routebuilder.util.enums.RouteProperty;
import com.netcracker.routebuilder.util.implementation.PlacesMap;
import com.netcracker.routebuilder.util.implementation.RouteMap;
import com.netcracker.routebuilder.util.implementation.WeatherMap;
import com.netcracker.routebuilder.util.implementation.ZeroMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

import static com.netcracker.routebuilder.util.implementation.Utils.*;

/**
 * Class to combine all potential maps into one, taking into account the necessary features of the route
 *
 * @author Kirill.Vakhrushev
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class PotentialMapBuilder {

    private final AlgorithmParameters params;
    private final ZeroMap zeroMap;
    private final WeatherMap weatherMap;
    private final PlacesMap placesMap;
    private final RouteMap routeMap;

    /**
     * method for assemble main potential map
     *
     * @param start              start point of the route
     * @param end                end point of the route
     * @param includedProperties necessary properties of the route
     * @return assembled potential map
     */
    public int[][] assemblePotentialMap(GeoCoordinates start, GeoCoordinates end, ArrayList<RouteProperty> includedProperties) {
        if (includedProperties.isEmpty()) {
            log.info("Route property list is empty, a zero potential map will be used");
            return zeroMap.getField();
        } else {
            int[][] field = initField(params.getScale());

            if (includedProperties.contains(RouteProperty.GOOD_WEATHER)) {
                int[][] weatherField = weatherMap.getField();
                fieldNormalization100(weatherField);
                combineFields(field, weatherField, params.getWeatherFieldFactor());
            }

            combineFields(field, placesMap.getField(includedProperties), params.getPlacesFieldFactor());

            int[][] routeField = routeMap.buildMap(start, end);
            fieldNormalization100(routeField);
            combineFields(field, routeField, params.getRouteFieldFactor());

            fieldNormalization100(field);
            return field;
        }
    }
}
