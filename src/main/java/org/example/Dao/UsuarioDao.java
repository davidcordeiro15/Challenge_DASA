package org.example.Dao;

import org.example.Config.DatabaseConnectionFactory;
import org.example.Model.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioDao {

    private static final String INSERT_SQL = "INSERT INTO usuarios (nome, email) VALUES (?, ?)";
    private static final String SELECT_ALL_SQL = "SELECT id, nome, email FROM usuarios ORDER BY nome";
    private static final String SELECT_BY_ID_SQL = "SELECT id, nome, email FROM usuarios WHERE id = ?";
    private static final String SELECT_BY_EMAIL_SQL = "SELECT id, nome, email FROM usuarios WHERE email = ?";
    private static final String SELECT_BY_CREDENTIALS_SQL = "SELECT id, nome, email FROM usuarios WHERE nome = ? AND email = ?";
    private static final String UPDATE_SQL = "UPDATE usuarios SET nome = ?, email = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM usuarios WHERE email = ?";
    private static final String EXISTS_BY_EMAIL_SQL = "SELECT 1 FROM usuarios WHERE email = ?";

    // 🔹 Criar novo usuário e retornar o ID
    public int salvar(Usuario usuario) throws SQLException {
        try (Connection conn = DatabaseConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Falha ao criar usuário, nenhuma linha afetada.");
            }

            Usuario usuarioCriado = buscarPorEmail(usuario.getEmail());
            try  {

                return usuarioCriado.getId();
            } catch (Exception ex) {
                throw new SQLException("Falha ao criar usuário, nenhum ID obtido.");
            }
        }
    }

    // 🔹 Listar todos os usuários
    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();

        try (Connection conn = DatabaseConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapearResultSetParaUsuario(rs));
            }
        }
        return usuarios;
    }

    // 🔹 Buscar usuário por email
    public Usuario buscarPorEmail(String email) throws SQLException {
        try (Connection conn = DatabaseConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_EMAIL_SQL)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSetParaUsuario(rs);
                }
                return null;
            }
        }
    }
    public Usuario buscarPorId(int id) throws SQLException {
        try (Connection conn = DatabaseConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSetParaUsuario(rs);
                }
                return null;
            }
        }
    }



    // 🔹 Verificar se usuário existe por credenciais
    public Usuario buscarPorCredenciais(String nome, String email) throws SQLException {
        try (Connection conn = DatabaseConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_CREDENTIALS_SQL)) {

            stmt.setString(1, nome);
            stmt.setString(2, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSetParaUsuario(rs);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Usuário não encontrado com nome: " + nome + " e email: " + email);
        }
        return null;
    }

    // 🔹 Verificar se email já existe
    public boolean existePorEmail(String email) throws SQLException {
        try (Connection conn = DatabaseConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(EXISTS_BY_EMAIL_SQL)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // 🔹 Atualizar usuário
    public boolean atualizar(Usuario usuario) throws SQLException {
        try (Connection conn = DatabaseConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());


            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // 🔹 Deletar usuário por ID
    public boolean deletar(String email) throws SQLException {
        try (Connection conn = DatabaseConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setString(1, email);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Método auxiliar para mapear ResultSet para Usuario
    private Usuario mapearResultSetParaUsuario(ResultSet rs) throws SQLException {
        System.out.println(rs);
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setNome(rs.getString("nome"));
        usuario.setEmail(rs.getString("email"));

        return usuario;
    }
}