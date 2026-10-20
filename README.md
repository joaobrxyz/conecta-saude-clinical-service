# 🩺 Clinical Service - Conecta Saúde

Este repositório contém o **Clinical Service**, o microsserviço responsável pela gestão do Prontuário Eletrónico do Paciente (PEP). Ele atua de forma isolada e segura para manter todo o histórico clínico, laudos, diagnósticos e prescrições médicas da clínica[cite: 9].

## 🎯 Funcionalidades Principais

*   **Prontuário Eletrónico (PEP):** Registo e consulta detalhada de evoluções clínicas, notas privadas do médico e sinais vitais (peso, altura, pressão arterial).
*   **Gestão de Prescrições:** Armazenamento estruturado de receitas médicas, incluindo medicamentos, dosagens e instruções de uso.
*   **Upload de Exames:** Suporte para o envio (upload), armazenamento e listagem de ficheiros de exames médicos anexados aos prontuários dos pacientes[cite: 9].
*   **Segurança e Controlo de Acesso:** Políticas rigorosas de acesso para garantir que informações clínicas sensíveis apenas possam ser acedidas pelos médicos responsáveis ou pelos próprios pacientes[cite: 9].

## 🛠️ Tecnologias Utilizadas

*   **Java 21**[cite: 9]
*   **Spring Boot** (Core e Web)[cite: 9]
*   **Spring Data MongoDB** (Persistência NoSQL orientada a documentos)
*   **Maven** (Gestão de dependências)[cite: 9]
*   **Docker & Docker Compose** (Contentorização)[cite: 9]

## 📦 Base de Dados (NoSQL)

Devido à natureza não relacional e hierárquica dos prontuários médicos (onde um atendimento pode ter múltiplas prescrições e sinais vitais opcionais), este serviço utiliza uma base de dados NoSQL[cite: 9]:
*   **Motor:** MongoDB[cite: 9]
*   **Nome:** `clinical_db`
*   **Porta Local (Docker):** `27017`

## 🚀 Como Executar

### Pré-requisitos
*   É estritamente necessário ter o **MongoDB** em execução na máquina ou via contentor Docker.
*   Os serviços `identity-service` e `appointment-service` devem estar ativos para validar os dados dos utilizadores e das consultas associadas aos prontuários.

### Localmente (Dev Mode)
Este serviço corre por defeito na porta `8083`. Para o iniciar usando o *wrapper* do Maven[cite: 9], executa o seguinte comando na raiz do projeto:

```bash
./mvnw spring-boot:run

Via Docker
Se preferires executar todo o ecossistema integrado, utiliza o docker-compose.yml[cite: 9] presente no repositório principal do projeto.

🤝 Autor
Desenvolvido por João como componente de dados clínicos do sistema Conecta Saúde.