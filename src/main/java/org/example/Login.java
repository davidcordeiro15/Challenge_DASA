package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Login extends JFrame {

    private Usuario usuario = new Usuario();
    private List<Usuario> usuariosCadastrados = new ArrayList<>();

    private CardLayout cardLayout;
    private JPanel painelPrincipal;

    // Função para fazer as configurações iniciais das telas
    public Login() {
        setTitle("BioMeasure");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        painelPrincipal = new JPanel(cardLayout);

        painelPrincipal.add(criarTelaInicial(), "inicio");
        painelPrincipal.add(criarPainelLogin(), "login");
        painelPrincipal.add(criarPainelCadastro(), "cadastro");

        add(painelPrincipal);
        setVisible(true);
    }

    // Cria a primeira tela com os botões de cadastro ou login
    private JPanel criarTelaInicial() {
        JPanel painel = new JPanel(new BorderLayout());

        JPanel painelTitulo = new JPanel();
        painelTitulo.setLayout(new BoxLayout(painelTitulo, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Bem-vindo ao BioMeasure", JLabel.CENTER);
        JLabel texto = new JLabel("Aqui sua análise é mais precisa", JLabel.CENTER);

        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        texto.setAlignmentX(Component.CENTER_ALIGNMENT);

        titulo.setFont(new Font("Arial", Font.BOLD, 22));
        texto.setFont(new Font("Arial", Font.PLAIN, 16));

        painelTitulo.add(Box.createVerticalStrut(30));
        painelTitulo.add(titulo);
        painelTitulo.add(Box.createVerticalStrut(10));
        painelTitulo.add(texto);
        painelTitulo.add(Box.createVerticalStrut(30));

        painel.add(painelTitulo, BorderLayout.NORTH);

        JPanel botoes = new JPanel();
        botoes.setLayout(new BoxLayout(botoes, BoxLayout.Y_AXIS));

        JButton btnLogin = new JButton("Já tenho login");
        JButton btnCadastro = new JButton("Quero me cadastrar");

        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCadastro.setAlignmentX(Component.CENTER_ALIGNMENT);


        btnLogin.addActionListener(e -> cardLayout.show(painelPrincipal, "login"));
        btnCadastro.addActionListener(e -> cardLayout.show(painelPrincipal, "cadastro"));

        botoes.add(btnLogin);
        botoes.add(Box.createVerticalStrut(15));
        botoes.add(btnCadastro);

        painel.add(botoes, BorderLayout.CENTER);
        return painel;
    }

    // Cria a tela de login
    private JPanel criarPainelLogin() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JTextField campoEmail = new JTextField();
        JPasswordField campoSenha = new JPasswordField();

        Dimension campoTamanho = new Dimension(Integer.MAX_VALUE, 25);
        campoEmail.setMaximumSize(campoTamanho);
        campoSenha.setMaximumSize(campoTamanho);

        JButton botaoLogin = new JButton("Entrar");
        JButton botaoVoltar = new JButton("Voltar");

        // botão de logar na conta
        botaoLogin.addActionListener(e -> {
            String email = campoEmail.getText();
            String senha = new String(campoSenha.getPassword());

            if (email.isEmpty() || senha.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Preencha todos os campos!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Optional<Usuario> usuarioEncontrado = usuariosCadastrados.stream()
                    .filter(u -> u.getEmail().equalsIgnoreCase(email) && u.getSenha().equals(senha))
                    .findFirst();

            if (usuarioEncontrado.isPresent()) {
                usuario = usuarioEncontrado.get();
                JOptionPane.showMessageDialog(this, "Login com sucesso: " + usuario.getNome());

                FileSelector file = new FileSelector();
                file.showDisplay();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Email ou senha inválidos!", "Erro de Login", JOptionPane.ERROR_MESSAGE);
            }
        });

        botaoVoltar.addActionListener(e -> cardLayout.show(painelPrincipal, "inicio"));

        painel.add(new JLabel("Email:"));
        painel.add(campoEmail);
        painel.add(Box.createVerticalStrut(10));
        painel.add(new JLabel("Senha:"));
        painel.add(campoSenha);
        painel.add(Box.createVerticalStrut(15));
        painel.add(botaoLogin);
        painel.add(Box.createVerticalStrut(10));
        painel.add(botaoVoltar);

        return painel;
    }

    // Cria painel de cadastro
    private JPanel criarPainelCadastro() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JTextField campoNome = new JTextField();
        JTextField campoEmail = new JTextField();
        JTextField campoSenha = new JTextField();
        JTextField campoSetor = new JTextField();
        JTextField campoNomeLaboratorio = new JTextField();
        JTextField campoEnderecoLaboratorio = new JTextField();

        JButton botaoCadastrar = new JButton("Cadastrar");
        JButton botaoVoltar = new JButton("Voltar");

        // botão de cadastrar que verifica se o email já está em uso, evitando perfils duplicados
        botaoCadastrar.addActionListener(e -> {
            String nome = campoNome.getText();
            String email = campoEmail.getText();
            String senha = campoSenha.getText();
            String setor = campoSetor.getText();
            String nomeLab = campoNomeLaboratorio.getText();
            String enderecoLab = campoEnderecoLaboratorio.getText();

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()
                    || setor.isEmpty() || nomeLab.isEmpty() || enderecoLab.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Preencha todos os campos!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean jaExiste = usuariosCadastrados.stream()
                    .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));

            if (jaExiste) {
                JOptionPane.showMessageDialog(this, "Este email já está cadastrado!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Usuario novoUsuario = new Usuario();
            novoUsuario.setNome(nome);
            novoUsuario.setEmail(email);
            novoUsuario.setSenha(senha);
            novoUsuario.setCargo(setor, nomeLab, enderecoLab);

            usuariosCadastrados.add(novoUsuario);
            JOptionPane.showMessageDialog(this, "Usuário cadastrado: " + novoUsuario.getNome());

            campoNome.setText("");
            campoEmail.setText("");
            campoSenha.setText("");
            campoSetor.setText("");
            campoNomeLaboratorio.setText("");
            campoEnderecoLaboratorio.setText("");
        });

        botaoVoltar.addActionListener(e -> cardLayout.show(painelPrincipal, "inicio"));

        painel.add(new JLabel("Nome:"));
        painel.add(campoNome);
        painel.add(Box.createVerticalStrut(10));
        painel.add(new JLabel("Email:"));
        painel.add(campoEmail);
        painel.add(Box.createVerticalStrut(10));
        painel.add(new JLabel("Senha:"));
        painel.add(campoSenha);
        painel.add(Box.createVerticalStrut(10));
        painel.add(new JLabel("Setor:"));
        painel.add(campoSetor);
        painel.add(Box.createVerticalStrut(10));
        painel.add(new JLabel("Nome do Laboratório:"));
        painel.add(campoNomeLaboratorio);
        painel.add(Box.createVerticalStrut(10));
        painel.add(new JLabel("Endereço do Laboratório:"));
        painel.add(campoEnderecoLaboratorio);
        painel.add(Box.createVerticalStrut(15));
        painel.add(botaoCadastrar);
        painel.add(Box.createVerticalStrut(10));
        painel.add(botaoVoltar);

        return painel;
    }


}
