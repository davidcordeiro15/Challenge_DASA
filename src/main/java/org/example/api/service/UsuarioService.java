package org.example.api.service;

import org.example.api.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UserRepository userRepository;

    public UsuarioService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 🔹 Login simples — retorna true se existir usuário com essas credenciais
    public boolean Login(String email, String senha) {
        return userRepository.findByEmailAndSenha(email, senha).isPresent();
    }
}
