package com.simul_tech.netgenius.repositories;

import com.simul_tech.netgenius.models.StudentGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Collection;

public interface StudentGroupRepository extends JpaRepository<StudentGroup, Long> {
    List<StudentGroup> findByStudentId(Long studentId);
    List<StudentGroup> findByGroupId(Long groupId);
    boolean existsByStudentIdAndGroupId(Long studentId, Long groupId);
    long countByGroupId(Long groupId);
    void deleteByStudentIdAndGroupId(Long studentId, Long groupId);

    @Query("select s.groupId as groupId, count(s.id) as total from StudentGroup s "
            + "where s.groupId in :ids group by s.groupId")
    List<GroupCount> countForGroups(@Param("ids") Collection<Long> ids);

    interface GroupCount {
        Long getGroupId();
        long getTotal();
    }
}
