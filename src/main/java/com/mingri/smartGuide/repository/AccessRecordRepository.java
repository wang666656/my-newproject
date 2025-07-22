package com.mingri.smartGuide.repository;

import com.mingri.smartGuide.entity.AccessRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AccessRecordRepository extends JpaRepository<AccessRecord, Long>, JpaSpecificationExecutor<AccessRecord> {

    List<AccessRecord> findByOutpatientIDOrderByAccessTimeDesc(@Param("outpatientID") String outpatientID);

    @Query("SELECT COUNT(ar) FROM AccessRecord ar WHERE ar.outpatientID = :outpatientID")
    Long countByOutpatientID(@Param("outpatientID") String outpatientID);

    @Query("SELECT COUNT(ar) FROM AccessRecord ar")
    Long countTotalAccess();
}