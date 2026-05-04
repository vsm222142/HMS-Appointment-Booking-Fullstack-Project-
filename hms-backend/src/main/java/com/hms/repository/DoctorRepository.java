package com.hms.repository;

import com.hms.entity.Doctor;
import com.hms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUser(User user);

    @Query("SELECT d FROM Doctor d JOIN FETCH d.user LEFT JOIN FETCH d.department")
    List<Doctor> findAllWithUser();
}

