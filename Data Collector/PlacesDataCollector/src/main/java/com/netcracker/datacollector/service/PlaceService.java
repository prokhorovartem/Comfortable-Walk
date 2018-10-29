package com.netcracker.datacollector.service;

import com.netcracker.datacollector.data.model.Place;

import java.util.UUID;

/**
 * Created by Grout on 28.10.2018.
 */
public interface PlaceService {

    Place savePlace(Place place);
    void deletePlace(UUID id);

}
