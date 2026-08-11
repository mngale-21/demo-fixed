package com.amatuer3.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username; // Registration Number ya mwanafunzi

    @Column(name = "attendance_date")
    private LocalDate attendanceDate;

    private String status; // Mfano: PRESENT, ABSENT, PERMISSION

    // Constructor inayojiwekea tarehe ya leo moja kwa moja
    public Attendance() {
        this.attendanceDate = LocalDate.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}