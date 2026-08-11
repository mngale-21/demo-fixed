package com.amatuer3.demo.controller;

import com.amatuer3.demo.Repository.UserRepository;
import com.amatuer3.demo.model.User;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class Profile {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/complete_profile")
    public String showProfileForm() {
        return "Registration"; // Inafungua Registration.html
    }

    @PostMapping("/update_profile")
    public String updateProfile(@RequestParam String email,
                                @RequestParam String parentNumber,
                                @RequestParam String region,
                                @RequestParam String district,
                                @RequestParam String ward,
                                @RequestParam String street,
                                @RequestParam String houseNumber,
                                HttpSession session) {
        
        // 1. Pata mtumiaji aliyelogin kwenye session
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        // 2. Usalama: Hakikisha session haijaisha
        if (loggedInUser == null) {
            return "redirect:/"; 
        }

        // 3. Tafuta user kamili toka DB na fanya update
        return userRepository.findById(loggedInUser.getId()).map(user -> {
            
            user.setEmail(email);
            user.setParentNumber(parentNumber);
            user.setRegion(region);
            user.setDistrict(district);
            user.setWard(ward);
            user.setStreet(street);
            user.setHouseNumber(houseNumber);

            // 4. Hifadhi mabadiliko
            userRepository.save(user);

            // 5. MUHIMU: Update Session ili data mpya ionekane kwenye dashboard
            session.setAttribute("loggedInUser", user);

            return "redirect:/student_dashboard";

        }).orElse("redirect:/");
    }
}