package com.netcracker.commons.data.repository;

import com.netcracker.commons.data.model.Place;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Created by Grout on 28.10.2018.
 */
public interface PlaceRepository extends JpaRepository<Place, UUID> {

    List<Place> findAllByType(String type);
}