package com.pipemasters.server.repository.impl;

import com.pipemasters.server.dto.UploadBatchFilter;
import com.pipemasters.server.entity.UploadBatch;
import com.pipemasters.server.repository.UploadBatchFilterRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class UploadBatchFilterRepositoryImpl implements UploadBatchFilterRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<UploadBatch> findFiltered(UploadBatchFilter filter, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UploadBatch> cq = cb.createQuery(UploadBatch.class);
        Root<UploadBatch> root = cq.from(UploadBatch.class);

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
            List<Predicate> keywordPredicates = new ArrayList<>();
            for (String keyword : filter.getKeywords()) {
                keywordPredicates.add(cb.isMember(keyword.toLowerCase(), root.get("keywords")));
            }
            predicates.add(cb.or(keywordPredicates.toArray(new Predicate[0])));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.desc(root.get("createdAt")));

        TypedQuery<UploadBatch> query = entityManager.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<UploadBatch> resultList = query.getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<UploadBatch> countRoot = countQuery.from(UploadBatch.class);
        countQuery.select(cb.count(countRoot)).where(predicates.toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(resultList, pageable, total);
    }
}