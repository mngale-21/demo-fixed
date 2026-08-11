package com.amatuer3.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "results")
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;    // Registration Number ya mwanafunzi
    private String courseName;  // Jina la somo
    private int marks;          // Alama alizopata
    private String grade;       // A, B, C, D, au F
    private String examType;    // Mfano: "Terminal", "Annual", "Mock"

    // --- CONSTRUCTORS ---
    public Result() {}

    // --- GETTERS AND SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public int getMarks() { return marks; }
    public void setMarks(int marks) { this.marks = marks; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }
}