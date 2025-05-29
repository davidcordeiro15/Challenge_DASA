package org.example;
import java.util.*;

public class Usuario {
    private Long Id; 
    private String Nome;
    private String Login;
    private String Senha;
    private Funcionario Cargo;
    private List<Peca> Pecas; // Peças que foram analizadas por esse funcionário

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
