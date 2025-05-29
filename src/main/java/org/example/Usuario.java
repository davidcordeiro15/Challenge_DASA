package org.example;
import java.util.*;

public class Usuario {
    private Long id;
    private String nome;
    private String login;
    private String senha;
    private Funcionario cargo;
    private String email;
    private List<Peca> pecas; // Peças que foram analizadas por esse funcionário

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
