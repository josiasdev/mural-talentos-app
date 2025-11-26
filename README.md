# Mural de Talentos - App de Vagas (Quixadá)

<p align="center">
  Solução mobile para centralizar e modernizar o processo de contratação na cidade de Quixadá, conectando empresas locais e profissionais da região.
</p>

<p align="center">
    <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin"/>
    <img src="https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose"/>
    <img src="https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black" alt="Firebase"/>
    <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android"/>
    <img src="https://img.shields.io/badge/Material_Design_3-7B57C3?style=for-the-badge&logo=materialdesign&logoColor=white" alt="Material Design 3"/>
</p>

## 🎯 Sobre o Projeto

O **Mural de Talentos** é uma solução mobile projetada para modernizar e centralizar o processo de contratação na cidade de Quixadá. Atualmente, a busca e divulgação de vagas na região dependem de canais informais e descentralizados, como grupos de WhatsApp e redes sociais, resultando em desorganização, baixa visibilidade e dificuldade na conexão entre oferta e demanda de trabalho.

O projeto resolve este problema oferecendo um aplicativo Android de fácil uso, que serve como um ponto de encontro digital entre empresas locais e profissionais da região. O público-alvo são as empresas de Quixadá que buscam um canal eficiente para anunciar suas vagas e os candidatos locais que precisam de um espaço confiável para cadastrar suas informações e encontrar oportunidades.

Ao contrário de grandes plataformas como LinkedIn e InfoJobs, que têm baixa penetração em cidades menores, nossa solução foca exclusivamente no ecossistema de emprego regional. O Mural de Talentos visa fortalecer a economia local, otimizar o tempo de recrutamento e aumentar a empregabilidade, tudo através de uma interface simples, acessível e intuitiva.

---

## ✨ Funcionalidades Principais

O sistema oferece módulos distintos para atender às necessidades de empresas e candidatos:

#### Para Empresas (Recrutadores)
* **Gestão de Vagas:** Cadastrar, editar, pausar e publicar vagas de emprego.
* **Busca de Talentos:** Pesquisar perfis de candidatos cadastrados na plataforma.
* **Gerenciamento de Candidaturas:** Visualizar, aprovar ou rejeitar as candidaturas recebidas.
* **Agendamento:** Marcar e registrar entrevistas com os candidatos selecionados.

#### Para Candidatos (Profissionais)
* **Perfil Profissional:** Criar e gerenciar um perfil com informações pessoais, experiências e habilidades.
* **Busca de Vagas:** Visualizar e se candidatar às vagas publicadas pelas empresas.
* **Filtros Avançados:** Filtrar vagas por categoria, tipo de emprego ou setor.
* **Geolocalização:** Visualizar vagas próximas à sua localização em um mapa (via Google Maps).
* **Notificações:** Receber notificações em tempo real sobre o status de suas candidaturas.

#### Gerais
* **Autenticação:** Sistema de login e cadastro simplificado, incluindo opções com redes sociais.
* **Temas:** Suporte aos modos de visualização claro (Light Mode) e escuro (Dark Mode).

---

## 📱 Tecnologias Utilizadas

A arquitetura do projeto é baseada em um aplicativo nativo Android consumindo serviços de Backend as a Service (BaaS) do Firebase.

| Tecnologia | Descrição |
| --- | --- |
| **Kotlin** | Linguagem principal utilizada para o desenvolvimento mobile nativo Android. |
| **Jetpack Compose** | Framework moderno para construção de interfaces reativas e declarativas. |
| **Firebase Authentication** | Utilizado para autenticação segura de usuários (Email, Google, etc.). |
| **Firebase Firestore** | Banco de dados NoSQL em tempo real para armazenamento e sincronização de dados (vagas, perfis, candidaturas). |
| **Firebase Cloud Messaging** | Para o envio de notificações push sobre o status das candidaturas. |
| **Room Database** | Solução local de persistência de dados, garantindo operação offline (cache de vagas, perfil). |
| **ViewModel (Jetpack)** | Gerenciamento de estado e do ciclo de vida da UI, seguindo a arquitetura recomendada pelo Google. |
| **Kotlin Flows (StateFlow)** | Utilizado para fluxos de dados assíncronos e reativos entre o ViewModel e a UI. |
| **Google Maps API** | Integração para a funcionalidade de visualização de vagas no mapa. |
| **Material Design 3** | Interface moderna e consistente com as diretrizes do Google. |

---

## ⚙️ Como Executar o Projeto

### Pré-requisitos

* [Android Studio](https://developer.android.com/studio) (Versão Iguana ou superior)
* JDK 17 ou superior
* Um dispositivo Android (Físico ou Emulador)
* Conta no [Firebase](https://firebase.google.com/)

### Passos para Execução

1.  **Clone o repositório:**
    ```bash
    git clone git@github.com:josiasdev/mural-talentos-app.git
    cd mural-talentos-app
    ```

2.  **Configure o Firebase:**
    * Acesse o console do Firebase e crie um novo projeto.
    * Adicione um aplicativo Android ao seu projeto Firebase (o `package name` deve ser o mesmo do projeto clonado).
    * Baixe o arquivo `google-services.json` gerado pelo Firebase.
    * Mova o arquivo `google-services.json` para o diretório `app/` do seu projeto Android.
    * Ative os serviços necessários no console do Firebase: **Authentication**, **Firestore Database** e **Cloud Messaging**.

3.  **Abra no Android Studio:**
    * Inicie o Android Studio e selecione "Open an existing project".
    * Navegue até a pasta onde você clonou o repositório e abra-a.

4.  **Sincronize e Execute:**
    * Aguarde o Android Studio sincronizar as dependências do Gradle.
    * Selecione um dispositivo (emulador ou físico).
    * Clique no botão "Run" (▶️) para compilar e instalar o aplicativo.

---

## Padrões de Desenvolvimento

Para manter a organização e a qualidade do código, seguimos os seguintes padrões:

### Padrões de Branch
* **Main/Master:** Branch principal, contendo apenas código de produção estável.
* **Develop:** Branch de desenvolvimento principal. Todas as *features* são mescladas aqui.
* **Feature:** `feature/nome-da-feature` (Ex: `feature/login-empresa`)
* **Bugfix:** `bugfix/correcao-especifica` (Ex: `bugfix/ajuste-filtro-vagas`)
* **Refactor:** `refactor/melhoria-codigo` (Ex: `refactor/migracao-viewmodel-vagas`)

### Padrões de Commit
* Usar o padrão **Conventional Commits**.
    * `feat:` (Nova funcionalidade)
    * `fix:` (Correção de bug)
    * `docs:` (Alterações na documentação)
    * `style:` (Formatação, ponto e vírgula, etc.)
    * `refactor:` (Refatoração de código)
    * `test:` (Adição ou correção de testes)
    * `chore:` (Tarefas de build, config, etc.)
* **Exemplo:** `feat: adiciona tela de perfil do candidato`

### Pull Request (PR)
* Ao finalizar o desenvolvimento na branch de *feature*, abra um Pull Request para a branch `develop`.
* Descreva o que foi feito no PR.
* Aguarde a revisão de pelo menos um outro membro da equipe antes de mesclar.

---

## 👥 Equipe de Desenvolvimento

| Membro | Matrícula | GitHub |
| --- | --- | --- |
| Francisco Josias da Silva Batista | 542167 | [josiasdev](https://github.com/josiasdev) |
| Cristiano Mendes da Silva | 558382 | [CristianoMends](https://github.com/CristianoMends) |
| Paulo Victor Costa Ferreira | 557331 | [VictorFCos](https://github.com/VictorFCos)]* |
| Guilherme Damasceno Nobre | 511329 | *[Link-GitHub]* |
