package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Honor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HonorRepository extends JpaRepository<Honor, Integer> {

    // 根据荣誉ID查询
    Optional<Honor> findByHonorId(Integer honorId);

    // 根据学生ID查询荣誉
    List<Honor> findByStudentPersonId(Integer personId);

    // 根据荣誉名称查询
    List<Honor> findByHonorName(String honorName);

    // 根据荣誉名称或颁发机构查询荣誉列表
    @Query("SELECT h FROM Honor h WHERE (:nameOrOrganization IS NULL OR :nameOrOrganization = '') " +
            "OR LOWER(h.honorName) LIKE LOWER(CONCAT('%', :nameOrOrganization, '%')) " +
            "OR LOWER(h.issuingOrganization) LIKE LOWER(CONCAT('%', :nameOrOrganization, '%'))")
    List<Honor> findHonorListByNameOrOrganization(@Param("nameOrOrganization") String nameOrOrganization);

    // 分页查询荣誉列表
    @Query("SELECT h FROM Honor h WHERE (:nameOrOrganization IS NULL OR :nameOrOrganization = '') " +
            "OR LOWER(h.honorName) LIKE LOWER(CONCAT('%', :nameOrOrganization, '%')) " +
            "OR LOWER(h.issuingOrganization) LIKE LOWER(CONCAT('%', :nameOrOrganization, '%'))")
    Page<Honor> findHonorPageByNameOrOrganization(@Param("nameOrOrganization") String nameOrOrganization, Pageable pageable);

    // 根据学生ID查询荣誉（分页）
    @Query("SELECT h FROM Honor h WHERE h.student.personId = :personId AND " +
            "((:nameOrOrganization IS NULL OR :nameOrOrganization = '') " +
            "OR LOWER(h.honorName) LIKE LOWER(CONCAT('%', :nameOrOrganization, '%')) " +
            "OR LOWER(h.issuingOrganization) LIKE LOWER(CONCAT('%', :nameOrOrganization, '%'))) ")
    Page<Honor> findHonorPageByStudentId(@Param("personId") Integer personId, @Param("nameOrOrganization") String nameOrOrganization, Pageable pageable);
}