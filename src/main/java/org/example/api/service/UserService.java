package org.example.api.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.example.api.dto.UserDTO;
import org.example.api.model.UserModel;
import org.example.api.repository.UserRepository;
import org.example.api.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;



    public List<UserDTO> listarUsuarios() {
        List<UserModel> users = userRepository.findAll();
        return users.stream().map(UserDTO::new).toList();
    }

    @Transactional
    public String loginComToken(String email, String senha) {
        boolean autenticado = userRepository.findByEmailAndSenha(email, senha).toString().isEmpty();

        if (autenticado) {
            return JwtUtil.generateToken(email);
        } else {
            return null;
        }
    }

}
