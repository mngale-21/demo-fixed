package com.amatuer3.demo.controller;

import com.amatuer3.demo.model.Attendance;
import com.amatuer3.demo.model.User;
import com.amatuer3.demo.Repository.AttendanceRepository;
import jakarta.servlet.http.HttpSession;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    /**
     * 1. OPEN ADMIN ATTENDANCE PAGE
     * Inafungua Admin_attendance.html
     */
    @GetMapping("/admin/attendance_dashboard")
    public String showAttendanceDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        
        // Ulinzi wa Role
        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            return "redirect:/"; 
        }

        LocalDate today = LocalDate.now();
        List<Attendance> todaysLogs = attendanceRepository.findAll().stream()
                .filter(a -> a.getAttendanceDate() != null && a.getAttendanceDate().equals(today))
                .toList();

        model.addAttribute("currentUser", user); // Muhimu kwa Navbar
        model.addAttribute("recentAttendance", todaysLogs);
        
        return "Admin_attendance"; 
    }

    /**
     * 2. UPLOAD EXCEL LOGIC (ADMIN ONLY)
     */
    @PostMapping("/admin/upload_attendance")
    public String uploadAttendance(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "redirect:/admin/attendance_dashboard?error=no_file";
        }

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            List<Attendance> list = new ArrayList<>();
            LocalDate today = LocalDate.now();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; 

                if (row.getCell(0) == null || row.getCell(0).getCellType() == CellType.BLANK) continue;

                String regId = row.getCell(0).getStringCellValue().trim();
                String status = row.getCell(1).getStringCellValue().trim();

                List<Attendance> existing = attendanceRepository.findByUsernameAndAttendanceDate(regId, today);
                
                if (existing.isEmpty()) {
                    Attendance att = new Attendance();
                    att.setUsername(regId);
                    att.setAttendanceDate(today);
                    att.setStatus(status.toUpperCase());
                    list.add(att);
                }
            }
            
            if (!list.isEmpty()) {
                attendanceRepository.saveAll(list);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/attendance_dashboard?error=fail";
        }
        
        return "redirect:/admin/attendance_dashboard?attendance_success";
    }

    /**
     * 3. STUDENT VIEW (Zilizorekebishwa)
     * URL hii "/student/attendance" sasa inalingana na Sidebar yako
     */
    @GetMapping("/student/attendance")
    public String viewMyAttendance(HttpSession session, Model model) {
        // 1. Pata user kutoka session
        User user = (User) session.getAttribute("loggedInUser");
        
        // 2. Kagua usalama
        if (user == null || !"STUDENT".equalsIgnoreCase(user.getRole())) {
            return "redirect:/";
        }

        // 3. Vuta data za huyu mwanafunzi pekee kwa kutumia username yake
        List<Attendance> myAtt = attendanceRepository.findByUsername(user.getUsername());
        
        // 4. Piga mahesabu ya asilimia na Trend (Hii ni sehemu ya ripoti yako)
        long presentCount = myAtt.stream().filter(a -> "PRESENT".equalsIgnoreCase(a.getStatus())).count();
        long absentCount = myAtt.stream().filter(a -> "ABSENT".equalsIgnoreCase(a.getStatus())).count();
        double total = myAtt.size();
        double percentage = (total > 0) ? ((double) presentCount / total) * 100 : 0;

        // 5. Tuma kila kitu kwenye HTML
        model.addAttribute("currentUser", user); // Lazima ili jina lionekane kwenye Navbar
        model.addAttribute("attendanceList", myAtt);
        model.addAttribute("presentDays", presentCount);
        model.addAttribute("absentDays", absentCount);
        model.addAttribute("percent", String.format("%.1f", percentage));

        return "Student_attendance"; 
    }
}