package com.amatuer3.demo.controller;

import com.amatuer3.demo.model.Result;
import com.amatuer3.demo.model.User;
import com.amatuer3.demo.Repository.ResultRepository;
import jakarta.servlet.http.HttpSession;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class ResultController {

    @Autowired
    private ResultRepository resultRepository;

    // ==========================================
    // 1. STUDENT: VIEW RESULTS
    // ==========================================
    @GetMapping("/student/results")
    public String viewMyResults(HttpSession session, 
                                @RequestParam(value = "examType", required = false) String examType,
                                Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || !"STUDENT".equalsIgnoreCase(user.getRole())) {
            return "redirect:/";
        }

        List<Result> myResults = (examType != null && !examType.isEmpty()) 
            ? resultRepository.findByUsernameAndExamType(user.getUsername(), examType) 
            : Collections.emptyList();

        int total = 0;
        List<Integer> pointsList = new ArrayList<>();
        for (Result r : myResults) {
            total += r.getMarks();
            pointsList.add(getGradePoints(r.getGrade()));
        }

        double avg = (myResults.size() > 0) ? (double) total / myResults.size() : 0.0;
        int totalPoints = calculateBest7(pointsList);

        model.addAttribute("currentUser", user); 
        model.addAttribute("results", myResults); 
        model.addAttribute("totalMarks", total);
        model.addAttribute("average", avg);
        model.addAttribute("totalPoints", totalPoints);
        model.addAttribute("division", getDivisionName(totalPoints, pointsList.size()));
        model.addAttribute("selectedExam", examType); 
        
        return "StudentResult"; 
    }

    // ==========================================
    // 2. ADMIN: SHOW UPLOAD PAGE (GET)
    // ==========================================
    @GetMapping("/admin/upload_results")
public String showUploadPage(HttpSession session) {
    User user = (User) session.getAttribute("loggedInUser");
    if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
        return "redirect:/";
    }
    // BADILISHA HAPA: Ilandane na jina la file lako halisi
    return "upload_results"; 
}

    // ==========================================
    // 3. ADMIN: PROCESS EXCEL UPLOAD (POST)
    // ==========================================
    @PostMapping("/admin/perform_upload")
    public String uploadExcel(@RequestParam("file") MultipartFile file, 
                              @RequestParam("examType") String examType) {
        if (file.isEmpty()) return "redirect:/admin/upload_results?error";

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            List<Result> resultsList = new ArrayList<>();
            
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row
                
                try {
                    Result r = new Result();
                    // Column 0: Username | Column 1: Subject | Column 2: Marks
                    r.setUsername(row.getCell(0).getStringCellValue().trim());
                    r.setCourseName(row.getCell(1).getStringCellValue().trim());
                    r.setMarks((int) row.getCell(2).getNumericCellValue());
                    r.setExamType(examType);
                    
                    calculateAndSetGrade(r);
                    resultsList.add(r);
                } catch (Exception e) { 
                    // Skip rows zenye makosa madogo madogo
                    continue; 
                }
            }
            resultRepository.saveAll(resultsList);
            return "redirect:/admin_dashboard?success";
            
        } catch (Exception e) { 
            e.printStackTrace();
            return "redirect:/admin/upload_results?error_processing"; 
        }
    }

    // ==========================================
    // HELPER METHODS (Logic ya Tanzania O-Level)
    // ==========================================
    private void calculateAndSetGrade(Result r) {
        int m = r.getMarks();
        if (m >= 75) r.setGrade("A");
        else if (m >= 65) r.setGrade("B");
        else if (m >= 45) r.setGrade("C");
        else if (m >= 30) r.setGrade("D");
        else r.setGrade("F");
    }

    private int getGradePoints(String g) {
        if (g == null) return 5;
        switch (g.toUpperCase()) {
            case "A": return 1; 
            case "B": return 2; 
            case "C": return 3; 
            case "D": return 4; 
            default: return 5;
        }
    }

    private int calculateBest7(List<Integer> points) {
        if (points.isEmpty()) return 0;
        Collections.sort(points);
        int sum = 0;
        int limit = Math.min(points.size(), 7);
        for (int i = 0; i < limit; i++) {
            sum += points.get(i);
        }
        return sum;
    }

    private String getDivisionName(int pts, int count) {
        if (count == 0) return "No Data";
        if (count < 7) return "Incomplete (Needs 7+ subjects)";
        
        if (pts <= 17) return "Division I";
        if (pts <= 21) return "Division II";
        if (pts <= 25) return "Division III";
        if (pts <= 33) return "Division IV";
        return "Division 0";
    }
}