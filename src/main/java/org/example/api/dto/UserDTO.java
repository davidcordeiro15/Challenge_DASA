package org.example.api.dto;

import org.example.api.model.UserModel;
import org.example.api.repository.UserRepository;
import org.springframework.beans.BeanUtils;


public class UserDTO {
    private int id;

    private String email;
    private String senha;

    public UserDTO(UserRepository user) {
        BeanUtils.copyProperties(user, this);
    }
    public UserDTO() {}

    public UserDTO(UserModel userModel) {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
