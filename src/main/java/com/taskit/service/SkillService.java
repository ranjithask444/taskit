package com.taskit.service;

import com.taskit.model.Skill;
import com.taskit.repository.SkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkillService {

    @Autowired
    private SkillRepository skillRepository;

    public List<Skill> getAvailableSkills(){
        return skillRepository.findAll();
    }

    public List<Skill> getSkillsById(List<Long> skillids){
        return skillRepository.findAllByIdIn(skillids);
    }
}
