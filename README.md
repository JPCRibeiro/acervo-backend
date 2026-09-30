<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="src/assets/logo.png">
    <img src="src/assets/logo-black.png" alt="Acervo" width="180">
  </picture>
</p>

<p align="center">
  API de perguntas e respostas sobre documentos (RAG) com suporte a múltiplas organizações. O usuário envia PDFs e outros arquivos, o sistema indexa o conteúdo e responde perguntas citando de quais documentos cada resposta veio.
</p>

<br />

<p align="center">
  <img alt="CD" src="https://github.com/JPCRibeiro/acervo-backend/actions/workflows/cd.yml/badge.svg">
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?logo=springboot&logoColor=white">
  <img alt="Spring AI" src="https://img.shields.io/badge/Spring%20AI-2.0-6DB33F?logo=spring&logoColor=white">
  <img alt="PostgreSQL" src="https://img.shields.io/badge/PostgreSQL-pgvector-1082C3?logo=postgresql&logoColor=white">
</p>

## O que faz

- Autenticação por JWT (RS256), com refresh token em cookie HttpOnly.
- Múltiplas organizações por usuário, com papéis OWNER e MEMBER. Cada organização só enxerga os próprios documentos e conversas.
- Ingestão de arquivos (PDF, DOCX, PPTX, TXT, MD, HTML): upload para o S3, extração de texto, divisão em trechos e geração de embeddings.
- Chat com RAG e streaming (SSE): a resposta chega token a token e traz as fontes usadas.
- Conversas persistidas: histórico de mensagens por usuário/organização.

## Stack

- Java 21, Spring Boot 4
- Spring AI (OpenAI: `gpt-4o-mini` para chat, `text-embedding-3-small` para embeddings)
- PostgreSQL com a extensão pgvector (busca por similaridade)
- Flyway para migrations
- AWS S3 para armazenar os arquivos
- Maven

## Rodando localmente

Pré-requisitos: Java 21, Docker e uma chave da OpenAI.

1. Suba o Postgres com pgvector:
   ```bash
   docker compose up -d
   ```

2. Gere o par de chaves do JWT:
   ```bash
   openssl genpkey -algorithm RSA -out private.pem -pkeyopt rsa_keygen_bits:2048
   openssl rsa -pubout -in private.pem -out public.pem
   ```

3. Crie um `.env` (ou exporte as variáveis) com pelo menos `OPENAI_API_KEY` e as credenciais do S3. Banco e chaves já têm padrão para local (veja a tabela abaixo).

4. Rode:
   ```bash
   ./mvnw spring-boot:run
   ```

A API sobe em `http://localhost:8080`. O Flyway cria o schema e a extensão vector no primeiro start.

### Variáveis de ambiente

```env
PORT=8080
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/acervo
SPRING_DATASOURCE_USERNAME=seu_usuario
SPRING_DATASOURCE_PASSWORD=sua_senha
OPENAI_API_KEY=sk-sua-chave-openai
CORS_ALLOWED_ORIGINS=http://localhost:5173
JWT_PRIVATE_KEY=file:private.pem
JWT_PUBLIC_KEY=file:public.pem
S3_REGION=us-east-1
S3_ACCESS_KEY=sua_chave
S3_SECRET_KEY=seu_secret
S3_BUCKET_NAME=nome-do-bucket
```

Para rodar local com o `docker compose` deste repo, os valores padrão de banco (`acervo` / `dev`, banco `acervo`) já funcionam, então você pode omitir `SPRING_DATASOURCE_*`.

## API

Rotas protegidas usam o header `Authorization: Bearer <access_token>`. As de autenticação são públicas.

Principais grupos de endpoints:

- `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/join`
- `POST /api/auth/refresh`, `POST /api/auth/logout`, `POST /api/auth/switch-organization`
- `POST /api/documents` (upload), `GET /api/documents`, `GET /api/documents/{id}`, `GET /api/documents/{id}/url`
- `POST /api/chat/stream` (RAG via SSE)
- `GET /api/conversations`, `GET /api/conversations/{id}`
- `GET /api/organizations`, `GET /api/organizations/me`, `GET /api/organizations/invite-code`, `POST /api/organizations`, `POST /api/organizations/join`
- `GET /api/members`, `GET /api/users/me`

## Arquitetura

Monólito modular organizado por funcionalidade. Cada módulo tem suas próprias camadas (`controller`, `service`, `repository`, `domain`, `dto`, `exception`).

```
src/main/java/br/app/acervo/
├── auth/           # login, registro, JWT e refresh token
├── user/           # perfil do usuário
├── organization/   # organizações e códigos de convite
├── membership/     # papéis OWNER / MEMBER
├── document/       # metadados, status e presigned URL dos documentos
├── ingestion/      # upload, extração de texto, chunking e embeddings
├── retrieval/      # chat RAG com streaming (SSE) e citações
├── conversation/   # histórico de conversas e mensagens
├── config/         # Spring AI, segurança, CORS e beans
└── shared/         # segurança (JWT) e tratamento de erros comuns
```

O fluxo de RAG passa por dois módulos: `ingestion` prepara os documentos (texto → trechos → embeddings no pgvector) e `retrieval` responde às perguntas, buscando os trechos mais parecidos e mandando para o modelo junto com a pergunta.

## Deploy

Deploy contínuo via GitHub Actions a cada push na `master`:

1. O workflow builda a imagem Docker e publica no Docker Hub.
2. Conecta por SSH na EC2 e roda `docker compose pull` + `up -d`, subindo a nova versão.

Em produção:

- **EC2** roda o container da API atrás do **Caddy**, que faz o reverse proxy e cuida do HTTPS automático (Let's Encrypt).
- **Neon** (Postgres serverless com pgvector) como banco.
- **S3** para os arquivos.

As chaves do JWT e as credenciais ficam em variáveis de ambiente no servidor (`acervo.env`), fora do repositório.
