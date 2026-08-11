package com.amatuer3.demo.Repository;

import com.amatuer3.demo.model.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResultRepository extends JpaRepository<Result, Long> {
    
    // 1. Inapata matokeo yote ya mwanafunzi (Bila kujali ni mtihani gani)
    List<Result> findByUsername(String username);

    // 2. Inapata matokeo ya mwanafunzi kulingana na aina ya mtihani (MUHIMU)
    // Hii itatumika mwanafunzi akichagua "Midterm March" au "Annual" kwenye dropdown
    List<Result> findByUsernameAndExamType(String username, String examType);
}