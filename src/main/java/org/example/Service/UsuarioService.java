package org.example.Service;

import org.example.Dao.UsuarioDao;
import org.example.Model.Usuario;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class UsuarioService {

    private UsuarioDao usuarioDAO = new UsuarioDao();

    // 🔹 Adicionar usuário com validação
    public Usuario adicionarUsuario(Usuario usuario) throws SQLException {
        // Verificar se email já existe

        try {
            if (!usuarioDAO.existePorEmail(usuario.getEmail())) {
                int id = usuarioDAO.salvar(usuario);
                usuario.setId(id);
                return usuario;
            }
            return null;

        } catch (SQLException ex) {
            System.err.println("Erro ao adicionar usuário: " + ex.getMessage());

            throw ex;

        }
    }

    // 🔹 Listar todos os usuários
    public List<Usuario> listarUsuarios() throws SQLException {
        return usuarioDAO.listarTodos();
    }


    // 🔹 Buscar usuário por email
    public Usuario buscarUsuarioPorEmail(String email) throws SQLException {
        return usuarioDAO.buscarPorEmail(email);
    }
    public Usuario buscarUsuarioPorId(int id) throws SQLException {
        return usuarioDAO.buscarPorId(id);
    }

    // 🔹 Autenticar usuário
    public Usuario autenticarUsuario(String senha, String email) throws SQLException {
        Usuario user = usuarioDAO.buscarPorCredenciais(senha, email);
        if (user == null) {
            Usuario usuarioInvalido = new Usuario();
            usuarioInvalido.setId(-1); // ID negativo indica usuário inválido
            usuarioInvalido.setNome("Usuário não encontrado");
            usuarioInvalido.setEmail("");
            return usuarioInvalido;
        }
        return user;
    }

    // 🔹 Verificar se usuário existe
    public boolean existeUsuario(String nome, String email) throws SQLException {
        Usuario usuario = usuarioDAO.buscarPorCredenciais(nome, email);
        if (usuario.getNome().equals(nome) && usuario.getEmail().equals(email)) {
            return true;
        }
        return false;
    }

    // 🔹 Atualizar usuário
    public boolean atualizarUsuario(Usuario usuario) throws SQLException {
        // Verificar se o novo email já pertence a outro usuário
        Usuario usuarioComEmail = usuarioDAO.buscarPorEmail(usuario.getEmail());

        if (usuarioComEmail.getEmail().equals(usuario.getEmail())) {
            return false; // Email já está em uso por outro usuário
        }

        return usuarioDAO.atualizar(usuario);
    }

    // 🔹 Deletar usuário por Email
    public boolean deletarUsuario(String email) throws SQLException {
        return usuarioDAO.deletar(email);
    }

    // 🔹 Verificar se email está disponível
    public boolean emailDisponivel(String email) throws SQLException {
        return !usuarioDAO.existePorEmail(email);
    }


}