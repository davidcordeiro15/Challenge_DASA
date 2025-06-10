package org.example;
import java.util.*;

public class Usuario {
    private Long id;
    private String nome;
    private String senha;
    private Funcionario cargo;
    private String email;


    public Usuario() {}
    public Usuario(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    // Getters e Setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
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

    public Funcionario getCargo() {
        return cargo;
    }

    public void setCargo(String setor, String nomeLaboratorio, String enderecoLaboratorio) {
        Laboratorio lab = new Laboratorio(nomeLaboratorio, enderecoLaboratorio, 20); // Para evitar muita complexidade no início do projeto optamos por colocar um número fixo de funcionários
        Funcionario funcionario = new Funcionario(setor, lab);
    }
    /* Teste
    * import com.jme3.app.SimpleApplication;

public class TesteJME extends SimpleApplication {
    public static void main(String[] args) {
        new TesteJME().start();
    }

    @Override
    public void simpleInitApp() {
        System.out.println("jMonkeyEngine funcionando!");
    }
}
*/
}
