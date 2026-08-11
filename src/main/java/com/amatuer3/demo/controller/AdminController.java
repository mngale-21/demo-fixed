package com.amatuer3.demo.controller;

import com.amatuer3.demo.model.User;
import com.amatuer3.demo.Repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    // 1. NJIA YA KUFUNGUA UKURASA WA UPLOAD
    @GetMapping("/admin/upload_page")
    public String showNewStudentPage() {
        return "new"; // Inatafuta templates/new.html
    }

    // 2. NJIA YA KU-PROCESS EXCEL NA KUSAVE WANAFUNZI
    @PostMapping("/admin/upload_students")
    public String uploadStudents(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return "redirect:/admin/upload_page?error=no_file";

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            List<User> userList = new ArrayList<>();

            for (Row row : sheet) {
                // Ruka Header (Mstari wa kwanza)
                if (row.getRowNum() == 0) continue; 

                try {
                    // SAFETY: Ruka kama cell ya RegNo (0) au MiddleName (2) ni tupu kabisa
                    if (row.getCell(0) == null || row.getCell(2) == null) continue;

                    String regNo = row.getCell(0).getStringCellValue().trim();
                    String fName = (row.getCell(1) != null) ? row.getCell(1).getStringCellValue().trim() : "";
                    String mName = row.getCell(2).getStringCellValue().trim(); 
                    String sName = (row.getCell(3) != null) ? row.getCell(3).getStringCellValue().trim() : "";

                    // Muhimu: Kama jina la kati ni tupu, MySQL itakataa password (NOT NULL)
                    if (regNo.isEmpty() || mName.isEmpty()) continue;

                    // Angalia kama mwanafunzi tayari yupo kwenye database ili usirudie (Duplicate)
                    Optional<User> existingUser = userRepository.findByUsername(regNo);
                    
                    if (existingUser.isEmpty()) {
                        User user = new User();
                        user.setUsername(regNo);
                        user.setFirstName(fName);
                        user.setMiddleName(mName);
                        user.setLastName(sName);
                        
                        // HAPA NDIPO TUNASEMA PASSWORD = MIDDLE NAME
                        // Hii inazuia "password cannot be null" error
                        user.setPassword(mName); 
                        
                        user.setRole("STUDENT");
                        userList.add(user);
                    }
                } catch (Exception e) {
                    // Kama row moja ina shida (mfano format mbaya), ruka uende inayofuata
                    System.out.println("Error reading row " + row.getRowNum() + ": " + e.getMessage());
                    continue; 
                }
            }

            // Save wote kwa mpigo kuokoa muda (Performance)
            if (!userList.isEmpty()) {
                userRepository.saveAll(userList);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/upload_page?error=upload_failed";
        }
        
        return "redirect:/admin/upload_page?success=registered";
    }
}