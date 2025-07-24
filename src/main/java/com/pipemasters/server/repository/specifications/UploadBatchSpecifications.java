package com.pipemasters.server.repository.specifications;

import com.pipemasters.server.dto.UploadBatchFilter;
import com.pipemasters.server.entity.*;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UploadBatchSpecifications {

    public static Specification<UploadBatch> withFilter(UploadBatchFilter f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();

            if (f.getSpecificDate() != null) {
                p.add(cb.equal(root.get("trainDeparted"), f.getSpecificDate()));
            } else {
                if (f.getDepartureDateFrom() != null)
                    p.add(cb.greaterThanOrEqualTo(root.get("trainDeparted"), f.getDepartureDateFrom()));
                if (f.getDepartureDateTo() != null)
                    p.add(cb.lessThanOrEqualTo(root.get("trainDeparted"), f.getDepartureDateTo()));
            }

            if (f.getArrivalDateFrom() != null)
                p.add(cb.greaterThanOrEqualTo(root.get("trainArrived"), f.getArrivalDateFrom()));
            if (f.getArrivalDateTo() != null)
                p.add(cb.lessThanOrEqualTo(root.get("trainArrived"), f.getArrivalDateTo()));

            if (f.getCreatedFrom() != null)
                p.add(cb.greaterThanOrEqualTo(root.get("createdAt"), f.getCreatedFrom()));
            if (f.getCreatedTo() != null)
                p.add(cb.lessThanOrEqualTo(root.get("createdAt"), f.getCreatedTo()));

            if (f.getTrainId() != null) {
                Join<Object, Object> train = root.join("train");
                p.add(cb.equal(train.get("id"), f.getTrainId()));
            }

            if (f.getChiefId() != null) {
                Join<Object, Object> train = root.join("train");
                Join<Object, Object> chief = train.join("chief");
                p.add(cb.equal(chief.get("id"), f.getChiefId()));
            }

            if (f.getUploadedById() != null) {
                Join<Object, Object> up = root.join("uploadedBy");
                p.add(cb.equal(up.get("id"), f.getUploadedById()));
            }

            if (f.getUploadedByName() != null) {
                Join<Object, Object> up = root.join("uploadedBy");
                var full = cb.concat(
                        cb.concat(
                                cb.concat(up.get("surname"), " "),
                                cb.concat(up.get("name"), " ")
                        ),
                        up.get("patronymic")
                );
                p.add(cb.like(cb.lower(full), "%" + f.getUploadedByName().toLowerCase() + "%"));
            }

            if (f.getBranchId() != null) {
                Join<Object, Object> br = root.join("branch");
                p.add(cb.equal(br.get("id"), f.getBranchId()));
            }

            if (f.getTagIds() != null && !f.getTagIds().isEmpty()) {
                assert query != null;
                Join<UploadBatch, MediaFile> mediaFileJoin = root.join("files");
                Join<MediaFile, TagInstance> tagInstanceJoin = mediaFileJoin.join("tagInstances");
                Join<TagInstance, TagDefinition> tagDefinitionJoin = tagInstanceJoin.join("definition");

                p.add(tagDefinitionJoin.get("id").in(f.getTagIds()));

                query.groupBy(root.get("id"));
                query.having(cb.equal(cb.countDistinct(tagDefinitionJoin.get("id")), f.getTagIds().size()));
                query.distinct(true);
            }


            return cb.and(p.toArray(new Predicate[0]));
        };
    }
}
