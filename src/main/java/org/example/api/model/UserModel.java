package org.example.api.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class UserModel {
    @Id
    private int id;

    private String email;
    private String senha;


}
