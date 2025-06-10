package org.example;

import javax.swing.*;
import java.awt.*;

public class Login extends JFrame {

    private Usuario usuario = new Usuario();

    private CardLayout cardLayout;
    private JPanel painelPrincipal;

    public Login() {
        setTitle("Bem-vindo!");
        setSize(400, 500); // aumentada para comportar mais campos
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

    private JPanel criarTelaInicial() {
        JPanel painel = new JPanel(new BorderLayout());
        JLabel titulo = new JLabel("Bem-vindo!", JLabel.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 22));
        painel.add(titulo, BorderLayout.NORTH);

        JPanel botoes = new JPanel(new FlowLayout());

        JButton btnLogin = new JButton("Já tenho login");
        JButton btnCadastro = new JButton("Quero me cadastrar");

        btnLogin.addActionListener(e -> cardLayout.show(painelPrincipal, "login"));
        btnCadastro.addActionListener(e -> cardLayout.show(painelPrincipal, "cadastro"));

        botoes.add(btnLogin);
        botoes.add(btnCadastro);

        painel.add(botoes, BorderLayout.CENTER);
        return painel;
    }

    private JPanel criarPainelLogin() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JTextField campoEmail = new JTextField();
        JPasswordField campoSenha = new JPasswordField();

        JButton botaoLogin = new JButton("Entrar");
        JButton botaoVoltar = new JButton("Voltar");

        botaoLogin.addActionListener(e -> {
            usuario.setEmail(campoEmail.getText());
            usuario.setSenha(new String(campoSenha.getPassword()));

            JOptionPane.showMessageDialog(this, "Login com email: " + usuario.getEmail());

            FileSelector file = new FileSelector();
            file.showDisplay();
            dispose();
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

    private JPanel criarPainelCadastro() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JTextField campoNome = new JTextField();
        JTextField campoEmail = new JTextField();
        JPasswordField campoSenha = new JPasswordField();
        JTextField campoSetor = new JTextField();
        JTextField campoNomeLaboratorio = new JTextField();
        JTextField campoEnderecoLaboratorio = new JTextField();

        JButton botaoCadastrar = new JButton("Cadastrar");
        JButton botaoVoltar = new JButton("Voltar");

        botaoCadastrar.addActionListener(e -> {
            usuario.setNome(campoNome.getText());
            usuario.setEmail(campoEmail.getText());
            usuario.setSenha(new String(campoSenha.getPassword()));
            usuario.setCargo(campoSetor.getText(), campoNomeLaboratorio.getText(), campoEnderecoLaboratorio.getText());

            JOptionPane.showMessageDialog(this, "Usuário cadastrado: " + usuario.getNome());
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
