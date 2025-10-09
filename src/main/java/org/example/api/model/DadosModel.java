package org.example.api.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class DadosModel {
    @Id
    private  int id;

    private String nome__peca;
    private String nome__responsavel;
    private int dados;
}
