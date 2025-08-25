package org.example;

import org.example.Model.Peca;

import java.util.List;

public class Laboratorio {
    private String nome;
    private String endereco;
    private int quantidadeDeFuncionarios;
    private List<Peca> estoqueDePecas;

    public Laboratorio(String nome, String endereco, int quantidadeDeFuncionarios) {
        this.nome = nome;
        this.endereco = endereco;
        this.quantidadeDeFuncionarios = quantidadeDeFuncionarios;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public int getQuantidadeDeFuncionarios() {
        return quantidadeDeFuncionarios;
    }

    public void setQuantidadeDeFuncionarios(int quantidadeDeFuncionarios) {
        this.quantidadeDeFuncionarios = quantidadeDeFuncionarios;
    }

    public List<Peca> getEstoqueDePecas() {
        return estoqueDePecas;
    }

    public void setEstoqueDePecas(List<Peca> estoqueDePecas) {
        this.estoqueDePecas = estoqueDePecas;
    }
}
