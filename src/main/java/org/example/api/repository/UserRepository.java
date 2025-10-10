package org.example.api.repository;


import org.example.Model.Usuario;


import org.example.api.model.DadosModel;
import org.example.api.model.UserModel;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<UserModel, Integer> {

    UserModel findByEmailAndSenha(String email, String senha);
    default UserModel verificarCredenciais(String email, String senha) {
        return findByEmailAndSenha(email, senha);
    }

}
