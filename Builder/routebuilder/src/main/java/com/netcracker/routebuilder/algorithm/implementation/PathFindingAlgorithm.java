package com.netcracker.routebuilder.algorithm.implementation;

import com.netcracker.routebuilder.data.bean.Cell;
import com.netcracker.routebuilder.data.bean.FieldCoordinates;
import com.netcracker.routebuilder.data.bean.GeoCoordinates;
import com.netcracker.routebuilder.properties.AlgorithmParameters;
import com.netcracker.routebuilder.util.implementation.*;
import com.netcracker.routebuilder.util.interfaces.AbstractPotentialMap;
import com.netcracker.routebuilder.util.enums.RouteProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class PathFindingAlgorithm {

    GoogleRouteBuilder googleRouteBuilder;
    AlgorithmParameters params;
    private final static double lat1KM = 0.00898; //1 км в градусах широты
    private final static double lon1KM = 0.01440; //1 км в градусах долготы

    public ArrayList<GeoCoordinates> buildRoute(GeoCoordinates startPoint, GeoCoordinates endPoint, ArrayList<RouteProperties> routeProperties) {
        log.info("--Start of the algorithm--");
        log.info("Distance between Starting and ending point: " + EuclideanDist(startPoint, endPoint));

        if (EuclideanDist(startPoint, endPoint) < params.getMinDistBetweenStartEnd()) {
            log.warn("Starting and ending point too close, give the standard Google route");
            return googleRouteBuilder.buildRoute(startPoint, endPoint);
        }

        //количество клеток 1x1 км по Ox и Oy  на нашей потенциальной карте
        final int defaultNumPointsX = 21;
        final int defaultNumPointsY = 20;

        final int numPointsX = recountWithNewScale(defaultNumPointsX);
        final int numPointsY = recountWithNewScale(defaultNumPointsY);

        log.info("New potential map size: " + numPointsX + ", " + numPointsY);

        final FieldCoordinates startCell = convertGeoToFieldCoordinates(startPoint);
        final FieldCoordinates endCell = convertGeoToFieldCoordinates(endPoint);

        log.info("Start cell: " + startCell.getX() + ", " + startCell.getY());
        log.info("End cell: " + endCell.getX() + ", " + endCell.getY());

        ArrayList<ArrayList<Cell>> potentialField = new ArrayList<>();
        potentialFieldInitialization(potentialField, numPointsX, numPointsY);
        fillPotentialField(potentialField, routeProperties);

        Cell startNode = potentialField.get(startCell.getX()).get(startCell.getY());
        Cell endNode = potentialField.get(endCell.getX()).get(endCell.getY());

        PriorityQueue<Cell> nodes = new PriorityQueue<>(Comparator.comparing(Cell::getFx));

        Cell firstPoint = potentialField.get(startCell.getX()).get(startCell.getY());

        //todo calcGlobalH
        Double firstPointH = calcDistToDestinationCell(startNode, endNode);
        firstPoint.updateParameters(null, 0d, firstPointH);

        nodes.add(firstPoint);

        if (1 == 1) {
            return null;
        }

        while (!nodes.isEmpty()) {
            Cell curNode = nodes.poll();
            if (curNode.getFieldCoordinates().equals(endNode.getFieldCoordinates())) {
                System.out.println("Нашли");

                ArrayList<GeoCoordinates> route = routeRestoration(curNode);
                log.info("The algorithm found the best way");
                return googleRouteBuilder.buildRoute(startPoint, route, endPoint);
            }

            int x = curNode.getFieldCoordinates().getX();
            int y = curNode.getFieldCoordinates().getY();
            potentialField.get(x).get(y).setClosed(true);

            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                for (int offsetY = -1; offsetY <= 1; offsetY++) {
                    int newX = x + offsetX;
                    int newY = y + offsetY;

                    if (newX >= 0 && newY >= 0 && newX < numPointsX && newY < numPointsY) {
                        Cell nextCell = potentialField.get(newX).get(newY);

                        Double tentativeScore = curNode.getGx() + calcGxBetweenPoints(potentialField, curNode, nextCell);

                        if (!nextCell.isClosed() || tentativeScore < nextCell.getGx()) {
                            nextCell.updateParameters(curNode,
                                    tentativeScore, calcDistToDestinationCell(curNode, endNode));
                            if (!nodes.contains(nextCell)) {
                                nodes.add(nextCell);
                            }
                        }
                    }
                }
            }
        }

        log.warn("The algorithm did not find the path, give the standard Google route");
        return googleRouteBuilder.buildRoute(startPoint, endPoint);
    }

    private static Double calcGxBetweenPoints(ArrayList<ArrayList<Cell>> potentialField, Cell from, Cell to) {
        return (double) potentialField.get(to.getFieldCoordinates().getX()).get(to.getFieldCoordinates().getY()).getValue();
    }

    private void fillPotentialField(ArrayList<ArrayList<Cell>> potentialField, ArrayList<RouteProperties> routeProperties) {
        log.info("start of filling a potential map");
        AbstractPotentialMap assembledMap = new PotentialMapBuilder(params.getScale()).assemblePotentialMap(routeProperties);

        for (int i = 0; i < potentialField.size(); i++) {
            for (int j = 0; j < potentialField.get(i).size(); j++) {
                potentialField.get(i).get(j).setValue(assembledMap.get(i, j));
            }
        }
        log.info("potential map filled");
    }

    private void potentialFieldInitialization(ArrayList<ArrayList<Cell>> potentialField, int x, int y) {

        double latStep = lat1KM / (double) params.getScale();
        double lonStep = lon1KM / (double) params.getScale();

        double curX = 30.18035;
        double curY = 60.02781;

        for (int i = 0; i < x; i++) {
            curX = 30.18035;
            potentialField.add(new ArrayList<>());
            for (int j = 0; j < y; j++) {
                potentialField.get(i).add(new Cell(0, new FieldCoordinates(i, j), new GeoCoordinates(curX, curY)));
                curX += lonStep;
            }
            curY -= latStep;
        }

        log.info("Potential map initialized");
    }

    private  Double calcDistToDestinationCell (Cell from, Cell to) {
        switch (params.getDistanceType()){
            case EUCLIDEAN:
                return EuclideanDist(from, to);
            case CHEBYSHEVA:
                return ChebyshevDist(from, to);
            case COMPOSIT:
                return 0d;
            default:
                log.error("Wrong distance type in the parameters");
                return 0d;
        }
    }

    private static Double ChebyshevDist(Cell from, Cell to) {
        //TODO переделать
        return (double) Math.max(Math.abs(from.getFieldCoordinates().getX() - to.getFieldCoordinates().getX()),
                Math.abs(from.getFieldCoordinates().getY() - to.getFieldCoordinates().getY()));
    }

    //ответ в метрах
    private static Double EuclideanDist(Cell from, Cell to) {
        return EuclideanDist(from.getGeoCoordinates(), to.getGeoCoordinates());
    }

    //ответ в метрах
    private static Double EuclideanDist(GeoCoordinates from, GeoCoordinates to) {
        return Math.sqrt(Math.pow(((from.getX() - to.getX()) / lon1KM) * 1000, 2) +
                Math.pow(((from.getY() - to.getY()) / lat1KM) * 1000, 2));
    }

    private Integer recountWithNewScale(int original) {
        return Utils.recountWithNewScale(original, params.getScale());
    }

    private FieldCoordinates convertGeoToFieldCoordinates(GeoCoordinates point) {
        return Utils.convertGeoToFieldCoordinates(point, params.getScale());
    }

    private ArrayList<GeoCoordinates> routeRestoration(Cell curNode) {
        ArrayList<GeoCoordinates> route = new ArrayList<>();

        //не включаем конечную точку
        //route.add(curNode.getGeoCoordinates());

        while (curNode.getParent() != null) {
            curNode = curNode.getParent();
            route.add(curNode.getGeoCoordinates());
        }

        //не включаем начальную точку
        route.remove(route.size() - 1);

        Collections.reverse(route);

        return route;
    }

}