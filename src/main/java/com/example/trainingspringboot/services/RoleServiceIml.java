package com.example.trainingspringboot.services;

import com.example.trainingspringboot.entities.Role;
import com.example.trainingspringboot.entities.User;
import com.example.trainingspringboot.model.request.RoleCreatingRequest;
import com.example.trainingspringboot.model.request.RoleUpdatingRequest;
import com.example.trainingspringboot.model.response.RoleResponse;
import com.example.trainingspringboot.repositories.PermissionRepository;
import com.example.trainingspringboot.repositories.RoleRepository;
import com.example.trainingspringboot.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Component
public class RoleServiceIml implements RoleService {
    @Autowired
    private RoleRepository repo;
    @Autowired
    private PermissionRepository perRepo;
    @Autowired
    private UserRepository userRepo;

    @Override
    public List<RoleResponse> getListRole() {
        return repo.findAllByOrderByIdAsc().stream().map(role-> new RoleResponse(role)).collect(Collectors.toList());
    }

    @Override
    public RoleResponse createRole(RoleCreatingRequest roleCreatingRequest) {
        Role newRole = new Role();
        if(repo.findByName(roleCreatingRequest.getName()).isPresent())
        {
            throw new DataIntegrityViolationException("Duplicate name");
        }
        newRole.setName(roleCreatingRequest.getName());
        if(roleCreatingRequest.getPermissions() != null)
        {
            newRole.setMappedPermission(roleCreatingRequest.getPermissions().stream().map(permission -> {
                if(perRepo.findById(permission).isPresent()){
                    return perRepo.findById(permission).get();
                } else {
                    throw new NoSuchElementException("Not found permission");
                }
            }).collect(Collectors.toList()));
        }
        repo.save(newRole);
        return new RoleResponse(newRole);
    }

    @Override
    public RoleResponse updateRole(RoleUpdatingRequest roleUpdatingRequest, Integer id) {
        Role newRole = repo.getById(id);

        if(roleUpdatingRequest.getName() != null && !roleUpdatingRequest.getName().equals(""))
        {
            if(repo.findByName(roleUpdatingRequest.getName()).isPresent() &&
                     (repo.getRoleByName(roleUpdatingRequest.getName()) != newRole))
            {
                throw new DataIntegrityViolationException("Duplicate role name");
            }
            newRole.setName(roleUpdatingRequest.getName());
        }
        if(roleUpdatingRequest.getPermissions() != null)
        {
            newRole.setMappedPermission(roleUpdatingRequest.getPermissions().stream().map(permission -> {
                if(perRepo.findById(permission).isPresent()){
                    return perRepo.findById(permission).get();
                } else {
                    throw new NoSuchElementException("Not found permission");
                }
            }).collect(Collectors.toList()));
        }
        newRole.setUpdatedDate(LocalDate.now(ZoneId.of("GMT+07:00")));
        repo.save(newRole);
        return new RoleResponse(newRole);
    }

    @Override
    public void deleteRole(Integer id) {
        if(repo.findById(id).isPresent()){
            if(repo.getById(id).getUsers().size() != 0){
                throw new DataIntegrityViolationException("Some user have this role");
            }
            repo.deleteById(id);
        } else {
            throw new NoSuchElementException("Not found role");
        }
    }
}
