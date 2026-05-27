package org.example.storageservice.application.repository;

import org.example.storageservice.application.dto.request.CarFilterRequest;
import org.example.storageservice.domain.car.model.Car;

import java.util.List;

public interface CarRepository extends Repository<Car> {

	List<Car> findAllByFilter(CarFilterRequest filter);

	List<Car> findAllForSale();
}