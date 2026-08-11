package com.amatuer3.demo.Repository;

import com.amatuer3.demo.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    // Inapata mahudhurio yote ya mwanafunzi mmoja
    List<Attendance> findByUsername(String username);
    
    // Inasaidia kuona kama mwanafunzi tayari ameshawekewa mahudhurio ya leo (Kuzuia Double Entry)
    List<Attendance> findByUsernameAndAttendanceDate(String username, java.time.LocalDate date);
}