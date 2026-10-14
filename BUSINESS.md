# Treeier Business Logic

## Core Domain
Treeier is a system for efficiently storing and managing tree-like data structures in a concurrent environment. The core domain involves managing complex hierarchies (up to 5 levels deep, maximum 10,000 nodes per tree) and supporting highly concurrent, real-time collaboration.

### Key Entities
- **Tree**: The root container for a hierarchical structure, having a title, description, and up to 5 searchable key-value labels.
- **Tree Node**: An element within a tree. Nodes can be of type `Default`, `Flag` (boolean), or `Link` (URL).
- **Comment**: User-generated notes attached to specific historical versions of tree nodes.
- **User**: Authenticated via Google OAuth 2.0, owning trees and possessing various access levels (read, edit).
- **Changeset/History**: Immutable records tracking modifications to trees and nodes, capturing the author and delta.

## User Flows
1. **Authentication:** Users register/login exclusively using Google OAuth 2.0.
2. **Tree Creation & Duplication:** 
   - Users can create trees from scratch.
   - Users can deep-copy an existing tree (forking). The fork receives a new owner. The fork retains a reference to its parent, and the parent tracks its forks. History is *not* copied during a fork.
3. **Collaboration & Sharing:**
   - Trees are public (read-only) to all registered users by default.
   - Owners can grant explicit edit access to other users.
4. **Real-Time Editing:**
   - When users open a tree, they receive real-time updates via Server-Sent Events (SSE) if someone else edits it.
5. **Commenting:**
   - Users with edit access or ownership can leave comments on specific versions of nodes. Comments reflect the exact node state at the time of authoring.
6. **Deletion:**
   - Trees are soft-deleted. If a parent tree is deleted, its forked children will mark their parent reference as disabled.
7. **Import/Export:**
   - Users can import/export trees as JSON files.

## Business Rules & Constraints
- **Tree Size Constraints:** Maximum depth of 5; maximum 10,000 nodes total per tree.
- **Labels:** Maximum 5 labels per tree, key/value strings limited to 15 characters each.
- **Node Types:** 
  - `Default`: Standard textual node.
  - `Flag`: Carries a boolean state.
  - `Link`: Carries a valid URL.
- **Versioning:** Node history is preserved for tracking changesets. Comments attach strictly to point-in-time versions of nodes, not just the latest state.
- **Soft Deletes:** Deletions are logical (soft-deleted). Related data is retained for audit and integrity of forks.

## External Integrations & Core Service Logic
- **Google OAuth 2.0 Authorization Server:** Used as the sole identity provider.
- **Server-Sent Events (SSE):** Core service logic pushes real-time diffs or invalidation events to connected clients when concurrent edits occur.
