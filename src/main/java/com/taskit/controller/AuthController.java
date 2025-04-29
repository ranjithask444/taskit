package com.taskit.controller;

import com.taskit.model.Skill;
import com.taskit.model.User;
import com.taskit.repository.UserRepository;
import com.taskit.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SkillService skillService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("skills", skillService.getAvailableSkills());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, @RequestParam("skillIds") List<Long> skillIds,
                               @RequestParam("availableBandwidth") Integer availableBandwidth) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        List<Skill> selectedSkills = skillService.getSkillsById(skillIds);
        user.setSkills(new HashSet<>(selectedSkills));
        userRepository.save(user);
        return "redirect:/login";
    }

}
