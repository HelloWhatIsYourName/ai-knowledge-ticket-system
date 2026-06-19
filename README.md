# AI Knowledge Ticket System

第一阶段目标：搭建 Spring Boot、Oracle 23ai、Redis 基础环境，并验证 Oracle `VECTOR(1024, FLOAT32)` 与阿里百炼 `text-embedding-v3` 的向量写入和检索闭环。

## Local Services

```bash
docker compose up -d
```

## Backend

```bash
cd backend
mvn test
mvn spring-boot:run
```
