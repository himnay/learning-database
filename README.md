# Learning Database

<img src="image/spring-logo.png" alt="Spring" width="70"/> <img src="image/postgresql-logo.png" alt="PostgreSQL" width="70"/>

A multi-module **Spring Boot + PostgreSQL 19** learning project. One shared database (`learningdb`, started via Docker Compose), one module per topic — each module owns its own schema, Flyway migrations, and README.

![Architecture diagram](image/architecture-diagram.png)

## Table of Contents

1. 📦 [Modules](#1-modules)
2. 🚀 [Quick Start](#2-quick-start)
3. 🔌 [Database Connection](#3-database-connection)
4. 🏗️ [Repository Layout](#4-repository-layout)

---

<a id="1-modules"></a>
## 1. 📦 Modules

| Module                                     | Port | Schema   | What it covers                                                                                                                                                                                                                                                        |
|--------------------------------------------|------|----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [database-core](database-core/README.md)   | 8080 | `public` | SQL interview query problems (window functions, pivot, Nth-highest salary …) and a complete Spring Data JPA reference: relationships, cascades, inheritance strategies, projections, Specifications, auditing, soft delete, locking, `@Transactional`, JDBC, HikariCP |
| [database-graph](database-graph/README.md) | 8081 | `graph`  | PostgreSQL 19 **SQL/PGQ** property graphs: `CREATE PROPERTY GRAPH`, `GRAPH_TABLE` / `MATCH` pattern queries, heterogeneous graphs, multiple labels, edge properties, and recursive-CTE fallbacks for variable-length paths                                            |

<a id="2-quick-start"></a>
## 2. 🚀 Quick Start

```bash
# 1. Start PostgreSQL 19 (shared by all modules)
docker compose up -d

# 2. Build everything
mvn clean install

# 3. Run a module (Flyway migrates its schema automatically)
mvn -pl database-core  spring-boot:run   # http://localhost:8080
mvn -pl database-graph spring-boot:run   # http://localhost:8081
```

<a id="3-database-connection"></a>
## 3. 🔌 Database Connection

| Property | Value        |
|----------|--------------|
| Host     | `localhost`  |
| Port     | `5432`       |
| Database | `learningdb` |
| Username | `postgres`   |
| Password | `postgres`   |

<a id="4-repository-layout"></a>
## 4. 🏗️ Repository Layout

```
learning-database/
├── docker-compose.yml       ← PostgreSQL 19 container
├── interview-queries.sql    ← ready-to-run SQL for database-core
├── image/                   ← shared README assets
├── pom.xml                  ← parent aggregator POM
├── database-core/           ← SQL + Spring Data JPA deep dive
└── database-graph/          ← SQL/PGQ property graphs
```

Each module's README is the full documentation for its topic — start there.
