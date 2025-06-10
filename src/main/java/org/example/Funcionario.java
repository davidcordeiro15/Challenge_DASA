package org.example;

public class Funcionario extends Usuario{
    private String setor;
    private Laboratorio laboratorio;

    public Funcionario (String setor, Laboratorio laboratorio){
        this.setor = setor;

    }
    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }


}
