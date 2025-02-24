# Order Service

O **Order Service** é uma aplicação Spring Boot que gerencia pedidos (orders) e autenticação de usuários, utilizando MongoDB como banco de dados, Redis para cache, e Spring Security para autenticação e autorização. Este projeto inclui serviços para gerenciar usuários (`UserService`), autenticação (`AuthService`), e integração com repositórios para persistência de dados.

## Descrição

Este projeto foi desenvolvido para simular um serviço de gestão de pedidos, com foco em autenticação segura usando JWT (JSON Web Tokens), integração com MongoDB para armazenar dados de usuários, e Redis para caching. Ele utiliza Spring Boot 3.4.3, Spring Data MongoDB, Spring Data Redis, Spring Security, e outras dependências modernas para criar uma API robusta e escalável.

## Requisitos

- **Java 17** ou superior
- **Maven 3.8.0+** (para build e dependências)
- **MongoDB** (local ou remoto, configurado via `application.properties`)
- **Redis** (local ou remoto, configurado via `application.properties`)
- **Docker** (opcional, para Testcontainers em testes)
- **IntelliJ IDEA** ou outro IDE Java (opcional, para desenvolvimento)

## Dependências

As principais dependências do projeto, listadas no `pom.xml`, incluem:

- **Spring Boot Starter Web** (3.4.3): Para criar APIs RESTful.
- **Spring Boot Starter Data MongoDB** (3.4.3): Para integração com MongoDB.
- **Spring Boot Starter Data Redis** (3.4.3): Para integração com Redis.
- **Spring Boot Starter Security** (3.4.3): Para autenticação e autorização com Spring Security.
- **JJWT** (0.11.5): Para gerar e validar JWTs.
- **Lombok** (1.18.36): Para reduzir boilerplate com anotações como `@Data` e `@Builder`.
- **JUnit 5** (5.11.4): Para testes unitários e de integração.
- **Mockito** (5.12.0): Para mocks em testes unitários.
