# System States & Dictionaries (ENUMS)

## Scope
This document lists the core enumerations, constants, and state dictionaries used across the Treeier application to ensure alignment between the Backend (Java) and Frontend (TypeScript) data models.

## Lifecycle
- **Entity State (`EntityStatus`):**
  - `ACTIVE`: The entity is visible and operational.
  - `DELETED`: The entity has been soft-deleted and should not be displayed in standard queries.

## Domain
- **Tree Node Type (`TreeNodeType`):**
  - `DEFAULT`: A standard node with only a title and description.
  - `FLAG`: A node carrying a boolean value.
  - `LINK`: A node carrying a web URL string.
- **Access Level (`AccessLevel`):**
  - `OWNER`: Implicit for the user who created the tree; full control.
  - `EDIT`: Granted to other users; allows modifying nodes and leaving comments.
  - `READ`: Implicit for all users by default; view only.

## Integration
- **OAuth Scopes:**
  - `openid`: Standard OpenID connect scope.
  - `profile`: User's basic profile information.
  - `email`: User's email address.
- **SSE Event Types (`SseEventType`):**
  - `TREE_UPDATED`: Sent when the structure of a tree changes.
  - `NODE_EDITED`: Sent when a specific node's content is updated.
  - `COMMENT_ADDED`: Sent when a new comment is posted on a node.

## Events
- **Domain Events (Internal Messaging):**
  - `TreeForkedEvent`: Triggered when a tree is duplicated to handle asynchronous tracking if necessary.
  - `TreeSoftDeletedEvent`: Triggered to asynchronously handle cleanup of child references (e.g., notifying forked children).

## Change checklist
When modifying or adding new enums/states:
- [ ] Ensure the enum is updated in both the Backend (Java enum class) and Frontend (TypeScript enum/type definition).
- [ ] Verify that database mappings (e.g., JPA `@Enumerated(EnumType.STRING)`) are correctly handling the new state.
- [ ] Check API validation (e.g., DTO parsing) to ensure the new enum value is accepted.
- [ ] Update this `ENUMS.md` file to reflect the change.
- [ ] If an SSE event type is added, ensure the frontend SSE listener map is updated accordingly.
