package org.example.storageservice.infrastructure.jpa.specification;

import jakarta.persistence.criteria.*;
import org.example.storageservice.application.dto.request.CarFilterRequest;
import org.example.storageservice.domain.car.model.Car;
import org.example.storageservice.domain.car.model.CarModel;
import org.example.storageservice.domain.part.enums.PartType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class CarSpecifications {

    private CarSpecifications() {
    }

    public static Specification<Car> byFilter(CarFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("removed")));

            if (filter == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            if (filter.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), BigDecimal.valueOf(filter.minPrice())));
            }
            if (filter.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), BigDecimal.valueOf(filter.maxPrice())));
            }
            if (filter.color() != null) {
                predicates.add(cb.equal(root.get("color"), filter.color()));
            }

            boolean requiresModelJoin = hasModelFilter(filter) || hasComponentFilter(filter);
            Join<Car, CarModel> modelJoin = null;
            if (requiresModelJoin) {
                modelJoin = root.join("model", JoinType.INNER);
            }

            if (hasModelFilter(filter)) {
                addModelPredicates(filter, predicates, modelJoin, cb);
            }
            if (hasComponentFilter(filter)) {
                addComponentPredicates(filter.componentIds(), predicates, modelJoin, query, cb);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addModelPredicates(CarFilterRequest filter, List<Predicate> predicates,
                                            Join<Car, CarModel> modelJoin, CriteriaBuilder cb) {
        if (filter.brand() != null) {
            predicates.add(cb.equal(modelJoin.get("brand"), filter.brand()));
        }

        if (filter.modelName() != null) {
            predicates.add(cb.equal(cb.lower(modelJoin.get("modelName")), filter.modelName().toLowerCase()));
        }

        if (filter.bodyType() != null) {
            predicates.add(cb.equal(modelJoin.get("bodyType"), filter.bodyType()));
        }

        if (filter.fuelType() != null) {
            predicates.add(cb.equal(modelJoin.get("fuelType"), filter.fuelType()));
        }

        if (filter.minEnginePower() != null) {
            predicates.add(cb.greaterThanOrEqualTo(modelJoin.get("enginePower"), filter.minEnginePower()));
        }

        if (filter.maxEnginePower() != null) {
            predicates.add(cb.lessThanOrEqualTo(modelJoin.get("enginePower"), filter.maxEnginePower()));
        }

        if (filter.minEngineVolume() != null) {
            predicates.add(cb.greaterThanOrEqualTo(modelJoin.get("engineVolumeLiters"), filter.minEngineVolume()));
        }

        if (filter.maxEngineVolume() != null) {
            predicates.add(cb.lessThanOrEqualTo(modelJoin.get("engineVolumeLiters"), filter.maxEngineVolume()));
        }

        if (filter.gearBoxType() != null) {
            predicates.add(cb.equal(modelJoin.get("gearBoxType"), filter.gearBoxType()));
        }

        if (filter.drivetrainType() != null) {
            predicates.add(cb.equal(modelJoin.get("drivetrainType"), filter.drivetrainType()));
        }
    }

    private static void addComponentPredicates(Set<UUID> componentIds, List<Predicate> predicates,
                                               Join<Car, CarModel> modelJoin, CriteriaQuery<?> query,
                                               CriteriaBuilder cb) {
        for (UUID componentId : componentIds) {
            Subquery<UUID> componentSubquery = query.subquery(UUID.class);
            Root<CarModel> subModel = componentSubquery.from(CarModel.class);
            MapJoin<CarModel, PartType, UUID> subBaseParts = subModel.joinMap("basePartIds", JoinType.INNER);

            componentSubquery.select(subModel.get("id"));
            componentSubquery.where(cb.equal(subModel.get("id"), modelJoin.get("id")),
                                    cb.equal(subBaseParts.value(), componentId));

            predicates.add(cb.exists(componentSubquery));
        }

        query.distinct(true);
    }

    private static boolean hasModelFilter(CarFilterRequest filter) {
        return filter.brand() != null
                || filter.modelName() != null
                || filter.bodyType() != null
                || filter.fuelType() != null
                || filter.minEnginePower() != null
                || filter.maxEnginePower() != null
                || filter.minEngineVolume() != null
                || filter.maxEngineVolume() != null
                || filter.gearBoxType() != null
                || filter.drivetrainType() != null;
    }

    private static boolean hasComponentFilter(CarFilterRequest filter) {
        return filter.componentIds() != null && !filter.componentIds().isEmpty();
    }
}