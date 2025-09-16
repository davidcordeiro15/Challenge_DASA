package org.example;

import org.example.Model.Usuario;
import org.example.Service.UsuarioService;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UsuarioServiceTest {

    static UsuarioService usuarioService;

    @BeforeAll
    public static void setup() {
        usuarioService = new UsuarioService();
    }
    @Test
    @Order(1)
    public void testCriarUsuario() throws SQLException {
        // Criar um usuário de teste
        Usuario usuarioTeste = new Usuario();
        usuarioTeste.setNome("b26");
        usuarioTeste.setEmail("b26@exemplo.com");

        // Tentar adicionar o usuário
        Usuario usuarioCriado = usuarioService.adicionarUsuario(usuarioTeste);

        // Verificar se o usuário foi criado com sucesso
        assertEquals(usuarioTeste, usuarioCriado, "Usuário deveria ter sido criado com sucesso");

        // Verificar se o ID foi gerado
        Assertions.assertTrue(usuarioCriado.getId() > 0, "Usuário deveria ter um ID válido");

        // Verificar se os dados foram persistidos corretamente
        assertEquals("b26", usuarioCriado.getNome(), "Nome deveria ser igual");
        assertEquals("b26@exemplo.com", usuarioCriado.getEmail(), "Email deveria ser igual");

        // Verificar se o usuário aparece na listagem
        List<Usuario> listaUsuarios = usuarioService.listarUsuarios();
        Assertions.assertTrue(
                listaUsuarios.stream().anyMatch(u ->
                        u.getEmail().equals("b26@exemplo.com") &&
                                u.getNome().equals("b26")),
                "Usuário criado deveria aparecer na listagem"
        );


        boolean result = usuarioService.deletarUsuario(usuarioCriado.getEmail());
        Assertions.assertTrue(result, "Usuário deveria ter sido deletado");
    }
    @Test
    @Order(2)
    public void testListarUsuarios() throws SQLException {
        List<Usuario> lista = usuarioService.listarUsuarios();
        Assertions.assertNotNull(lista, "A lista de usuários não deveria ser nula");
        Assertions.assertFalse(lista.isEmpty(), "A lista de usuários não deveria estar vazia");
    }



    @Test
    @Order(3)
    public void testBuscarUsuarioPorEmail() throws SQLException {


        // Buscar o usuário pelo ID
        Usuario usuarioEncontrado = usuarioService.buscarUsuarioPorEmail("a@a.com");

        Assertions.assertFalse(usuarioEncontrado.getNome().isEmpty(), "Deveria encontrar o usuário pelo email");
        assertEquals("a", usuarioEncontrado.getNome());
        assertEquals("a@a.com", usuarioEncontrado.getEmail());

    }


}
