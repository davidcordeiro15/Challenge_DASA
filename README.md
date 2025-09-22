🧑‍💻 Sistema de Gestão de Usuários com Visualizador 3D
## Descrição do Projeto
Este projeto foi desenvolvido em Java 8 com JavaFX para a interface gráfica e JDBC para integração com banco de dados. Além das funcionalidades de cadastro e login de usuários, o sistema permite visualizar modelos 3D (.obj) dentro da aplicação, utilizando a biblioteca JFX3DModelImporters.

## 👥 Integrantes
RM 557538 - David Cordeiro

RM 555619 - Tiago Morais

RM 557065 - Vinicius Augusto

## 🚀 Funcionalidades
### 👤 Usuários
Cadastro de Usuário: cria novos usuários

Login: valida os dados e acessa o sistema

Atualização de Usuário: permite modificar dados existentes


### 🏢 Funcionários e Papéis
Modelos para Gestor, Funcionário, Analista e Laboratório

Representação em classes de domínio

### 🖼️ Visualização 3D
Carregamento de arquivos .obj via importador 3D

Interface para manipular e centralizar modelos

## 🗂 Estrutura do Projeto
    
    src/
    └── org.example/
        ├── Config/
        │   └── DatabaseConnectionFactory.java   # Conexão com o banco de dados
        │
        ├── Controller/
        │   ├── CadastroController.java          # Lógica da tela de cadastro
        │   ├── InicioController.java            # Tela inicial
        │   ├── LoginController.java             # Controle da tela de login
        │   ├── ViewerController.java            # Controle do visualizador de arquivos
        │   └── Visualizador3DController.java    # Controle do visualizador 3D
        │
        ├── Dao/
        │   ├── DatabaseSetupDao.java            # Criação de tabelas no BD
        │   └── UsuarioDao.java                  # Operações CRUD de usuários
        │
        ├── Model/
        │   ├── Analista.java
        │   ├── Funcionario.java
        │   ├── Gestor.java
        │   ├── Laboratorio.java
        │   ├── Peca.java
        │   └── Usuario.java                     # Classe modelo principal de usuários
        │
        ├── Service/
        │   └── UsuarioService.java              # Regras de negócio de usuários
        │
        ├── Ui/
        │   ├── FileSelector.java                # Utilitário de seleção de arquivos
        │   └── OBJFileViewer.java               # Viewer específico para arquivos 3D
        │
        ├── Util/
        │   ├── EmailValidator.java              # Validação de e-mails
        │   ├── OBJLoader.java                   # Loader para arquivos OBJ
        │   └── Run.java                         # Execução da aplicação
        │
        └── Main.java                            # Classe principal (entrypoint)
        └── test.org.example/
              └── UsuarioServiceTest.java          # Testes JUnit para Usuários 
    
    resources/
    ├── Cadastro.fxml          # Tela de cadastro
    ├── Login.fxml             # Tela de login
    ├── Tela3D.fxml            # Tela de visualização 3D
    ├── TelaInicial.fxml       # Tela inicial
    ├── Visualizador3D.fxml    # Estrutura da cena 3D
    └── style.css              # Estilos da aplicação


## ▶️ Como Executar
### Clone o repositório:
bash

    git clone https://github.com/davidcordeiro15/Challenge_DASA.git

### Configuração do Ambiente
Certifique-se de ter o Java 8+ e JavaFX configurados

No IntelliJ vá em Run > Edit Configurations > Application

Adicione o arquivo Main como ponto de entrada

Coloque no campo abaixo o comando: 
    
    --add-modules javafx.controls,javafx.fxml

### Execução da Aplicação

Clique em Run 

### Utilização do Sistema
Utilize as telas para login, cadastro e visualização 3D

## ✅ Tecnologias Utilizadas
Java 8

JavaFX (UI e FXML)

JDBC (persistência)

JFX3DModelImporters (visualização de modelos 3D .obj)

## 🧪 Testes
CRUD de Usuários

Validação de login

Verificação de carregamento de modelos .obj

## 📷 Interfaces
### 🔑 Tela de Login
Campos de usuário e senha

Acesso ao sistema com dados válidos

### 📝 Tela de Cadastro
Registro de novos usuários

### 🖼️ Tela 3D
Seleção e visualização de modelos .obj
