package com.amatuer3.demo.controller;

import java.util.Optional;
import java.util.UUID;
import com.amatuer3.demo.model.User;
import com.amatuer3.demo.Repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/")
    public String showLogin() {
        return "Login";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String username, 
                              @RequestParam String password, 
                              HttpSession session,
                              Model model) {
        
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            boolean isValid = false;

            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                isValid = user.getPassword().equals(password);
            } else {
                isValid = user.getMiddleName() != null && user.getMiddleName().equalsIgnoreCase(password);
            }

            if (isValid) {
                session.setAttribute("loggedInUser", user);
                if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    return "redirect:/admin_dashboard";
                } else {
                    return "redirect:/student_dashboard";
                }
            } else {
                model.addAttribute("error", "Incorrect Password or Grandfather's Name!");
            }
        } else {
            model.addAttribute("error", "Registration Number not found!");
        }
        return "Login";
    }

    @GetMapping("/admin_dashboard")
    public String showAdminDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            return "redirect:/"; 
        }
        model.addAttribute("currentUser", user);
        return "admin_dashboard";
    }

    @GetMapping("/student_dashboard")
    public String showStudentDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || !"STUDENT".equalsIgnoreCase(user.getRole())) {
            return "redirect:/"; 
        }
        model.addAttribute("currentUser", user); 
        return "student_dashboard";
    }

    // --- DISPLAY CHANGE PASSWORD PAGE ---
    @GetMapping("/change_password")
    public String showChangePasswordPage(HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/";
        }
        return "ResetPassword"; 
    }

    // --- HANDLE CHANGE PASSWORD ACTION ---
    @PostMapping("/change_password")
    public String handlePasswordChange(@RequestParam String oldPassword, 
                                       @RequestParam String newPassword, 
                                       HttpSession session, 
                                       Model model) {
        
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return "redirect:/";
        }

        if (!loggedInUser.getPassword().equals(oldPassword)) {
            model.addAttribute("error", "Password ya zamani siyo sahihi! Jaribu tena.");
            return "ResetPassword"; 
        }

        return userRepository.findById(loggedInUser.getId()).map(user -> {
            user.setPassword(newPassword);
            userRepository.save(user);
            session.setAttribute("loggedInUser", user);
            model.addAttribute("message", "Password imebadilishwa kikamilifu!");
            return "Login"; 
        }).orElse("redirect:/");
    }

    // --- FORGOT PASSWORD SECTION ---
    @PostMapping("/update_password")
    public String updatePassword(@RequestParam String token, @RequestParam String password, Model model) {
        Optional<User> userOpt = userRepository.findByResetToken(token);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(password); 
            user.setResetToken(null); 
            userRepository.save(user);
            model.addAttribute("message", "Password imebadilishwa! Ingia sasa.");
            return "Login";
        }
        model.addAttribute("error", "Token is invalid or expired.");
        return "Login";
    }

    @GetMapping("/forgot_password")
    public String showForgotPassword() { return "Forgot"; }

    @PostMapping("/forgot_password")
    public String processForgotPassword(@RequestParam String email, Model model) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            userRepository.save(user);
            String resetLink = "http://localhost:6467/reset_password?token=" + token; 
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(user.getEmail());
                message.setSubject("Password Reset Request");
                message.setText("Habari " + user.getFirstName() + ",\n\nBonyeza link hii: " + resetLink);
                mailSender.send(message);
                model.addAttribute("message", "Email imetumwa!");
            } catch (Exception e) {
                model.addAttribute("error", "Tatizo la Email.");
            }
        } else {
            model.addAttribute("error", "Email haipo.");
        }
        return "Forgot";
    }

    @GetMapping("/reset_password")
    public String showResetPasswordForm(@RequestParam String token, Model model) {
        if (userRepository.findByResetToken(token).isPresent()) {
            model.addAttribute("token", token);
            return "ResetPassword"; 
        }
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "Login";
    }
}