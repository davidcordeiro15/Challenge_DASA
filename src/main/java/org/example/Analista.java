package org.example;

import java.util.List;

public class Analista extends Funcionario{
    private List<Peca> pecas; // Peças que foram analizadas por esse funcionário


    public Analista(String setor, Laboratorio nomeLaboratorio) {
        super(setor, nomeLaboratorio);
    }

    public List<Peca> getPecas() {
        return pecas;
    }

    public void setPecas(List<Peca> pecas) {
        this.pecas = pecas;
    }
}
