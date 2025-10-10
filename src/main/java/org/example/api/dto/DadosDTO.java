package org.example.api.dto;

public class DadosDTO {
    private  int id;

    private String nome__peca;
    private String nome__responsavel;
    private int dados;

    public String getNome__peca() {
        return nome__peca;
    }

    public void setNome__peca(String nome__peca) {
        this.nome__peca = nome__peca;
    }

    public String getNome__responsavel() {
        return nome__responsavel;
    }

    public void setNome__responsavel(String nome__responsavel) {
        this.nome__responsavel = nome__responsavel;
    }

    public int getDados() {
        return dados;
    }

    public void setDados(int dados) {
        this.dados = dados;
    }
}
