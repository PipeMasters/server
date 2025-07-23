package com.pipemasters.server.repository.specifications;

import com.pipemasters.server.dto.UploadBatchFilter;
import com.pipemasters.server.entity.MediaFile;
import com.pipemasters.server.entity.Tag;
import com.pipemasters.server.entity.TranscriptFragment;
import com.pipemasters.server.entity.UploadBatch;
import com.pipemasters.server.service.impl.ImotioServiceImpl;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UploadBatchSpecifications {

    private final static Logger log = LoggerFactory.getLogger(UploadBatchSpecifications.class);

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

//            if (f.getKeywords() != null && !f.getKeywords().isEmpty()) {
//                Join<UploadBatch, String> kw = root.joinSet("keywords");
//                p.add(kw.in(f.getKeywords()));
//            }

            if (f.getTags() != null && !f.getTags().isEmpty()) {
                assert query != null;
                query.distinct(true);

                for (String tag : f.getTags()) {
                    Subquery<Long> tagSubquery = query.subquery(Long.class);
                    Root<UploadBatch> subRoot = tagSubquery.from(UploadBatch.class);

                    Join<UploadBatch, MediaFile> subMediaFileJoin = subRoot.join("files");
                    Join<MediaFile, TranscriptFragment> subFragmentJoin = subMediaFileJoin.join("transcriptFragments");
                    Join<TranscriptFragment, Tag> subTagJoin = subFragmentJoin.join("tags");

                    tagSubquery.select(subRoot.get("id"))
                            .where(cb.and(
                                    cb.equal(subRoot, root),
                                    cb.equal(cb.lower(subTagJoin.get("name")), tag.toLowerCase())
                            ));
                    p.add(cb.exists(tagSubquery));
                }
            }

            return cb.and(p.toArray(new Predicate[0]));
        };
    }
}
