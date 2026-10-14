# AI Agent Constraints (Backend)

This document enforces strict coding rules and constraints for all AI agents generating or modifying code in this Java/Spring Boot backend environment.

## 1. General Spring Boot Rules
- **Version:** Use Java 21 and Spring Boot 3 (Spring 6).
- **Dependency Injection:** Use constructor injection (via `final` fields and `@RequiredArgsConstructor` using Lombok, if available, or explicit constructors). Avoid `@Autowired` on fields.

## 2. Spring Data JPA & Hibernate
- **Entities:** Map entities properly with `@Entity`. Avoid `@Data` from Lombok on `@Entity` classes due to performance issues with `hashCode()` and `equals()`; use `@Getter`, `@Setter`, and carefully implement `equals()`/`hashCode()` based on business keys.
- **Lazy Loading:** All associations (`@ManyToOne`, `@OneToMany`, `@ManyToMany`) MUST be configured to fetch lazily (`fetch = FetchType.LAZY`). 
- **N+1 Problem:** Always write optimized queries using `@EntityGraph` or explicit `JOIN FETCH` in Spring Data `@Query` definitions to prevent N+1 select issues when retrieving nested trees or lists of nodes.
- **Projections:** Use interface-based projections or DTOs for read-heavy operations instead of fetching entire entity graphs, especially for the "View trees of all other users" feature.

## 3. Database & MySQL Query Optimization
- **Tree Structures:** Given the max depth of 5 and max 10,000 nodes, use an appropriate pattern for tree storage in SQL (e.g., Adjacency List for simplicity, or Materialized Path/Nested Sets if query performance demands it, but keep constraints in mind).
- **Indexing:** Ensure all foreign keys and frequently queried fields (e.g., `owner_id`, `tree_id`, `labels`) have database indexes.
- **Soft Deletion:** Implement soft deletes using Hibernate's `@SQLDelete` and `@Where` (or `@SQLRestriction` in newer Hibernate versions) to transparently filter out deleted rows.
- **Batch Processing:** Enable JDBC batch inserts and updates for tree duplication or import operations to handle up to 10,000 nodes efficiently. Set `spring.jpa.properties.hibernate.jdbc.batch_size` appropriately.

## 4. Spring Security & OAuth 2.0
- **Authentication:** Must integrate strictly with Google OAuth 2.0. Do not implement local username/password authentication.
- **Statelessness:** If using JWTs or external session management, ensure the security context is correctly populated without relying on bulky server-side HTTP sessions where possible.
- **Authorization:** Implement method-level security (`@PreAuthorize`) to enforce ownership and edit access checks (e.g., verifying if the current user owns the tree or has been granted shared edit access).

## 5. Server-Sent Events (SSE)
- **Concurrency:** Implement non-blocking or properly managed thread pools for SSE emitters.
- **Resource Management:** Ensure emitters are properly completed or timed out and removed from the active pool when clients disconnect to prevent memory leaks.

## 6. Real-Time Collaboration Logic
- **Concurrency Control:** Implement optimistic locking (`@Version`) on entities to handle concurrent modifications to trees or nodes. 
- **Immutability of History:** Node edits must generate a new version/changeset rather than purely overwriting the old state, to support comments on specific versions and detailed history tracking.
