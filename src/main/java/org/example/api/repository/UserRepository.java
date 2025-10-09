package org.example.api.repository;


import org.example.Model.Usuario;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Usuario, Integer> {

    // 🔹 Busca um usuário com o e-mail e senha correspondentes
    Optional<Usuario> findByEmailAndSenha(String email, String senha);
}
