# Entity-Relationship & Database Constraints (Backend)

## Scope
This document outlines the database schema, entity relationships, and structural constraints for the core domain of the Treeier application, focusing on MySQL usage with Spring Data JPA.

## Entity-relationship
- **User to Tree:** One-to-Many. A user can own multiple trees.
- **Tree to Tree (Forking):** One-to-Many (Self-referencing). A tree can be forked multiple times, but a fork has only one direct parent. If a parent is deleted, the fork's reference to it is disabled (set to null or marked invalid).
- **Tree to Tree Access (Sharing):** Many-to-Many via a join table. Trees can be shared with multiple users for edit access.
- **Tree to Labels:** One-to-Many. A tree can have up to 5 labels (key-value pairs).
- **Tree to Tree Node:** One-to-Many. A tree contains up to 10,000 nodes.
- **Tree Node to Tree Node:** One-to-Many (Self-referencing Adjacency List). Defines the hierarchy within the tree, up to a max depth of 5.
- **Tree Node to Node History:** One-to-Many. A node has a history of changes (changesets) capturing author and state.
- **Tree Node to Comment:** One-to-Many. Comments are attached to specific nodes. However, comments must reference a specific *version* of the node to fulfill the requirement that comments display only on the version they were created for.

## Table catalog
*Note: Primary keys are assumed to be UUIDs or auto-incrementing BIGINTs.*

- `users`: `id`, `email`, `name`, `google_id` (Indexed, Unique)
- `trees`: `id`, `owner_id` (FK), `forked_from_id` (FK, Nullable), `title` (VARCHAR 255), `description` (VARCHAR 1000), `is_deleted` (BOOLEAN), `created_at`, `updated_at`
- `tree_access`: `tree_id` (FK), `user_id` (FK), `access_level` (VARCHAR)
- `tree_labels`: `id`, `tree_id` (FK), `label_key` (VARCHAR 15), `label_value` (VARCHAR 15)
- `tree_nodes`: `id`, `tree_id` (FK), `parent_node_id` (FK, Nullable), `title` (VARCHAR 255), `description` (VARCHAR 1000), `node_type` (VARCHAR), `type_value` (VARCHAR), `version` (INT, for optimistic locking), `is_deleted` (BOOLEAN)
- `node_history`: `id`, `node_id` (FK), `author_id` (FK), `title`, `description`, `type_value`, `version` (INT), `changed_at` (TIMESTAMP)
- `comments`: `id`, `node_id` (FK), `node_version` (INT), `author_id` (FK), `text` (VARCHAR 1000), `created_at` (TIMESTAMP)

## Database-owned behavior
- **Foreign Keys:** All relationships must enforce referential integrity at the database level using foreign keys.
- **Cascades:** 
  - Deleting a tree physically (if ever needed) should cascade delete its labels, nodes, and access grants.
  - Due to soft-delete requirements, application-level `@SQLDelete` will be used instead of database-level `ON DELETE CASCADE`.
- **Soft Deletes:** `is_deleted` flags on `trees` and `tree_nodes`. Soft deletion of a tree does *not* automatically cascade soft deletion to its forked children, but the application must handle the parent reference logic.
- **Constraints:** 
  - Tree label keys and values max length 15.
  - Max 5 labels per tree (enforced at application level).
  - Node hierarchy max depth 5 (enforced at application level).

## Change checklist
Before applying any schema changes or migrations, ensure:
- [ ] The migration script (e.g., Flyway or Liquibase) has been thoroughly reviewed.
- [ ] New foreign keys have corresponding indexes to prevent table locks during cascading updates or deletes.
- [ ] Application-level limits (max depth, max nodes) are not negatively impacted by query structures.
- [ ] Soft delete flags are properly accounted for in any new unique constraints.
- [ ] Entity models in the Java codebase accurately reflect the updated table catalog.
- [ ] Existing data integrity is preserved during migration (write a rollback plan).
