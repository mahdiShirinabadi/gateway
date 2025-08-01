package com.eureka.acl.service;

import com.eureka.acl.entity.*;
import com.eureka.acl.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProjectRegistrationService {
    
    private final ProjectRepository projectRepository;
    private final ProjectApiRepository projectApiRepository;
    private final PermissionRepository permissionRepository;
    
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
            return projectRepository.save(project);
        }
        
        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setBaseUrl(baseUrl);
        project.setVersion(version);
        
        Project savedProject = projectRepository.save(project);
        log.info("Project {} registered successfully", name);
        return savedProject;
    }
    
    @Transactional
    public void registerProjectApis(String projectName, List<ApiRegistration> apis) {
        log.info("Registering {} APIs for project: {}", apis.size(), projectName);
        
        Optional<Project> projectOpt = projectRepository.findByName(projectName);
        if (projectOpt.isEmpty()) {
            log.error("Project {} not found", projectName);
            throw new RuntimeException("Project not found: " + projectName);
        }
        
        Project project = projectOpt.get();
        
        for (ApiRegistration api : apis) {
            // Check if permission exists, if not create it
            Optional<Permission> permissionOpt = permissionRepository.findByName(api.getPermissionName());
            if (permissionOpt.isEmpty()) {
                log.info("Creating permission: {} for project: {}", api.getPermissionName(), projectName);
                Permission permission = new Permission();
                permission.setName(api.getPermissionName());
                permission.setProjectName(projectName);
                permission.setCritical(api.isCritical());
                permission.setPersianName(api.getPersianName());
                permissionRepository.save(permission);
            }
            
            // Check if API already exists
            Optional<ProjectApi> existingApi = projectApiRepository.findByProjectNameAndApiPathAndMethod(
                    projectName, api.getApiPath(), api.getHttpMethod());
            
            if (existingApi.isPresent()) {
                log.info("API {} {} already exists for project {}, updating...", 
                        api.getHttpMethod(), api.getApiPath(), projectName);
                ProjectApi projectApi = existingApi.get();
                projectApi.setDescription(api.getDescription());
                projectApi.setPermissionName(api.getPermissionName());
                projectApi.setPublic(api.isPublic());
                projectApiRepository.save(projectApi);
            } else {
                log.info("Creating new API: {} {} for project: {}", 
                        api.getHttpMethod(), api.getApiPath(), projectName);
                ProjectApi projectApi = new ProjectApi();
                projectApi.setProject(project);
                projectApi.setApiPath(api.getApiPath());
                projectApi.setHttpMethod(api.getHttpMethod());
                projectApi.setPermissionName(api.getPermissionName());
                projectApi.setDescription(api.getDescription());
                projectApi.setPublic(api.isPublic());
                projectApiRepository.save(projectApi);
            }
        }
        
        log.info("Successfully registered {} APIs for project: {}", apis.size(), projectName);
    }
    
    public List<ProjectApi> getProjectApis(String projectName) {
        log.info("Getting APIs for project: {}", projectName);
        return projectApiRepository.findByProjectName(projectName);
    }
    
    public List<Project> getAllProjects() {
        log.info("Getting all projects");
        return projectRepository.findAll();
    }
    
    public static class ApiRegistration {
        private String apiPath;
        private String httpMethod;
        private String permissionName;
        private String description;
        private boolean isPublic;
        private boolean isCritical;
        private String persianName;
        
        // Getters and Setters
        public String getApiPath() { return apiPath; }
        public void setApiPath(String apiPath) { this.apiPath = apiPath; }
        
        public String getHttpMethod() { return httpMethod; }
        public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
        
        public String getPermissionName() { return permissionName; }
        public void setPermissionName(String permissionName) { this.permissionName = permissionName; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public boolean isPublic() { return isPublic; }
        public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
        
        public boolean isCritical() { return isCritical; }
        public void setCritical(boolean isCritical) { this.isCritical = isCritical; }
        
        public String getPersianName() { return persianName; }
        public void setPersianName(String persianName) { this.persianName = persianName; }
    }
} 