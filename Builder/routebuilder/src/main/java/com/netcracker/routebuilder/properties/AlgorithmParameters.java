package com.netcracker.routebuilder.properties;

import com.netcracker.routebuilder.util.enums.DistanceTypes;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class AlgorithmParameters {

    @Value("${algorithm.maxAllowableIncrease}")
    private int maxAllowableIncrease; //Во сколько раз маршрут нашего алгоритма может превышать длину гугловского маршрута

    @Value("${algorithm.minDistBetweenStartEnd}")
    private int minDistBetweenStartEnd; //Если расстояние между точками начала и конца меньше, то сразу используется гугловский алгоритм

    @Value("${algorithm.scale}")
    private int scale; //Размер клетки потенциального поля на которой будет работать алгоритм

    @Value("${algorithm.distanceType}")
    private DistanceTypes distanceType; //какой алгоритм для расчета расстояния до цели (H) использовать

    @Value("${algorithm.normalFactorH}")
    private double normalFactorH; // нормализующий коэффициент для оценки расстояния до цели

    @Value("${algorithm.normalFactorG}")
    private double normalFactorG; //нормализующий коэффициент для стоимости пути от начальной вершины
}