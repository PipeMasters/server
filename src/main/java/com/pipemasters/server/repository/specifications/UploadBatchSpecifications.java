package com.pipemasters.server.repository.specifications;

import com.pipemasters.server.dto.UploadBatchFilter;
import com.pipemasters.server.entity.UploadBatch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UploadBatchSpecifications {
    public static Specification<UploadBatch> withFilter(UploadBatchFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getSpecificDate() != null) {
                predicates.add(cb.equal(root.get("trainDeparted"), filter.getSpecificDate()));
            } else {
                if (filter.getDateFrom() != null)
                    predicates.add(cb.greaterThanOrEqualTo(root.get("trainDeparted"), filter.getDateFrom()));
                if (filter.getDateTo() != null)
                    predicates.add(cb.lessThanOrEqualTo(root.get("trainDeparted"), filter.getDateTo()));
            }

            if (filter.getTrainNumber() != null) {
                Join<Object, Object> trainJoin = root.join("train");
                predicates.add(cb.like(cb.lower(trainJoin.get("number")), "%" + filter.getTrainNumber().toLowerCase() + "%"));
            }

            if (filter.getChiefName() != null) {
                Join<Object, Object> trainJoin = root.join("train");
                predicates.add(cb.like(cb.lower(trainJoin.get("chiefFullName")), "%" + filter.getChiefName().toLowerCase() + "%"));
            }

            if (filter.getUploadedByName() != null) {
                Join<Object, Object> uploadedByJoin = root.join("uploadedBy");
                predicates.add(cb.like(cb.lower(uploadedByJoin.get("fullName")), "%" + filter.getUploadedByName().toLowerCase() + "%"));
            }

            if (filter.getKeywords() != null && !filter.getKeywords().isEmpty()) {
                Join<UploadBatch, String> keywordJoin = root.joinSet("keywords");
                predicates.add(keywordJoin.in(filter.getKeywords()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
