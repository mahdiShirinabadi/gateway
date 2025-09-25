package com.eureka.acl.service;

import com.eureka.acl.entity.*;
import com.eureka.acl.repository.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProjectRegistrationService {
    
    private final ProjectRepository projectRepository;

    @Transactional
    public Project registerProject(String name, String description, String baseUrl, String version) {
        log.info("Registering project: {} with base URL: {}", name, baseUrl);
        
        Optional<Project> existingProject = projectRepository.findByName(name);
        if (existingProject.isPresent()) {
            log.info("Project {} already exists, updating...", name);
            Project project = existingProject.get();
            project.setDescription(description);
            project.setBaseUrl(baseUrl);
            project.setVersion(version);
            project.setUpdateBy("System");
            project.setUpdateTime(LocalDateTime.now());
            return projectRepository.save(project);
        }
        
        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setBaseUrl(baseUrl);
        project.setVersion(version);
        project.setCreateBy("System");
        project.setCreateTime(LocalDateTime.now());
        
        Project savedProject = projectRepository.save(project);
        log.info("Project {} registered successfully", name);
        return savedProject;
    }
    
    public List<Project> getAllProjects() {
        log.info("Getting all projects");
        return projectRepository.findAll();
    }
    
    public Optional<Project> getProjectByName(String name) {
        log.info("Getting project by name: {}", name);
        return projectRepository.findByName(name);
    }

    @Getter
    @Setter
    public static class ApiRegistration {
        private String apiPath;
        private String httpMethod;
        private String permissionName;
        private String description;
        private boolean isPublic;
        private boolean isCritical;
        private String persianName;
    }
} 