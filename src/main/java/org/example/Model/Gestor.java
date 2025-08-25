package org.example.Model;

import org.example.Laboratorio;

import java.util.List;

public class Gestor extends Funcionario {
    // Com essa lista ele tem acesso a todas as peças analisadas pelos funcionários citados
    private List<Analista> funcionarios; // Lista de funcionários que é responsável

    public Gestor(String setor, Laboratorio laboratorio) {
        super(setor, laboratorio);
    }
}
