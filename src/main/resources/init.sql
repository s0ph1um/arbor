-- Data cleanup
# DELETE FROM tree_editors;
# DELETE FROM tree_history;
# DELETE FROM tree_labels;
# DELETE FROM closure_entity;
# DELETE FROM nodes;
# DELETE FROM trees;
# DELETE FROM user;
# DELETE FROM comment;

-- Creating users
INSERT INTO user (id, name, email, created_at) VALUES
                                                   (1, 'admin', 'admin@example.com', NOW()),
                                                   (2, 'user1', 'user1@example.com', NOW()),
                                                   (3, 'user2', 'user2@example.com', NOW()),
                                                   (4, 'viewer', 'viewer@example.com', NOW());

-- Tree 1: Company organizational structure (owner: admin)
-- Root node
INSERT INTO nodes (id, parent, root, title, description, node_type) VALUES
    (1000, NULL, 1000, 'TechCorp Company', 'Head organization', 'DEFAULT');

INSERT INTO trees (id, title, description, root_node_id, owner_id, created_at, created_by, updated_at, node_count, max_depth) VALUES
    (1, 'Organizational Structure', 'TechCorp company structure', 1000, 1, NOW(), 'admin', NOW(), 7, 3);

-- Level 1
INSERT INTO nodes (id, parent, root, title, description, node_type) VALUES
                                                                        (1001, 1000, 1000, 'Development Department', 'IT department', 'DEFAULT'),
                                                                        (1002, 1000, 1000, 'Sales Department', 'Sales department', 'DEFAULT');

-- Level 2
INSERT INTO nodes (id, parent, root, title, description, node_type, flag_value) VALUES
                                                                                    (1003, 1001, 1000, 'Backend Team', 'Server-side development', 'FLAG', true),
                                                                                    (1004, 1001, 1000, 'Frontend Team', 'Interface development', 'FLAG', false),
                                                                                    (1005, 1002, 1000, 'B2B Sales', 'Corporate clients', 'DEFAULT', false);

-- Level 3
INSERT INTO nodes (id, parent, root, title, description, node_type, link_value) VALUES
    (1006, 1003, 1000, 'Java Developers', 'Spring Boot team', 'LINK', 'https://docs.spring.io');

-- Closure table for tree 1
INSERT INTO closure_entity (ancestor, descendant, depth, parent, root) VALUES
-- Self-references
(1000, 1000, 0, NULL, 1000),
(1001, 1001, 0, 1000, 1000),
(1002, 1002, 0, 1000, 1000),
(1003, 1003, 0, 1001, 1000),
(1004, 1004, 0, 1001, 1000),
(1005, 1005, 0, 1002, 1000),
(1006, 1006, 0, 1003, 1000),
-- Ancestor-descendant relationships
(1000, 1001, 1, 1000, 1000),
(1000, 1002, 1, 1000, 1000),
(1000, 1003, 2, 1001, 1000),
(1000, 1004, 2, 1001, 1000),
(1000, 1005, 2, 1002, 1000),
(1000, 1006, 3, 1003, 1000),
(1001, 1003, 1, 1001, 1000),
(1001, 1004, 1, 1001, 1000),
(1001, 1006, 2, 1003, 1000),
(1002, 1005, 1, 1002, 1000),
(1003, 1006, 1, 1003, 1000);

-- Labels for tree 1
INSERT INTO tree_labels (tree_id, label_key, label_value) VALUES
                                                              (1, 'type', 'org'),
                                                              (1, 'status', 'active'),
                                                              (1, 'size', 'medium');

-- Sharing tree 1 with user1
INSERT INTO tree_editors (tree_id, user_id) VALUES
    (1, 2);

-- Tree 2: Project hierarchy (owner: user1)
-- Root node
INSERT INTO nodes (id, parent, root, title, description, node_type) VALUES
    (2000, NULL, 2000, 'Project Alpha', 'Main project 2024', 'DEFAULT');

INSERT INTO trees (id, title, description, root_node_id, owner_id, created_at, created_by, updated_at, node_count, max_depth) VALUES
    (2, 'Project Alpha Plan', 'Project task structure', 2000, 2, NOW(), 'user1', NOW(), 5, 2);

-- Level 1
INSERT INTO nodes (id, parent, root, title, description, node_type) VALUES
                                                                        (2001, 2000, 2000, 'Phase 1: Analysis', 'Requirements research', 'DEFAULT'),
                                                                        (2002, 2000, 2000, 'Phase 2: Development', 'Feature implementation', 'DEFAULT');

-- Level 2
INSERT INTO nodes (id, parent, root, title, description, node_type, flag_value) VALUES
                                                                                    (2003, 2001, 2000, 'Client Interviews', 'Requirements gathering', 'FLAG', true),
                                                                                    (2004, 2002, 2000, 'MVP Version', 'Minimum viable product', 'FLAG', false);

-- Closure table for tree 2
INSERT INTO closure_entity (ancestor, descendant, depth, parent, root) VALUES
-- Self-references
(2000, 2000, 0, NULL, 2000),
(2001, 2001, 0, 2000, 2000),
(2002, 2002, 0, 2000, 2000),
(2003, 2003, 0, 2001, 2000),
(2004, 2004, 0, 2002, 2000),
-- Ancestor-descendant relationships
(2000, 2001, 1, 2000, 2000),
(2000, 2002, 1, 2000, 2000),
(2000, 2003, 2, 2001, 2000),
(2000, 2004, 2, 2002, 2000),
(2001, 2003, 1, 2001, 2000),
(2002, 2004, 1, 2002, 2000);

-- Labels for tree 2
INSERT INTO tree_labels (tree_id, label_key, label_value) VALUES
                                                              (2, 'type', 'project'),
                                                              (2, 'priority', 'high');

-- Tree 3: Fork of tree 2 (owner: user2)
-- Root node
INSERT INTO nodes (id, parent, root, title, description, node_type) VALUES
    (3000, NULL, 3000, 'Project Alpha', 'Main project 2024', 'DEFAULT');

INSERT INTO trees (id, title, description, root_node_id, owner_id, parent_tree_id, created_at, created_by, updated_at, node_count, max_depth) VALUES
    (3, 'Project Alpha Plan (Fork)', 'Modified task structure', 3000, 3, 2, NOW(), 'user2', NOW(), 6, 3);

-- Copy structure with changes
INSERT INTO nodes (id, parent, root, title, description, node_type) VALUES
                                                                        (3001, 3000, 3000, 'Phase 1: Analysis', 'Requirements research', 'DEFAULT'),
                                                                        (3002, 3000, 3000, 'Phase 2: Development', 'Feature implementation', 'DEFAULT'),
                                                                        (3003, 3001, 3000, 'Client Interviews', 'Requirements gathering', 'FLAG'),
                                                                        (3004, 3002, 3000, 'MVP Version', 'Minimum viable product', 'FLAG'),
-- New node in fork
                                                                        (3005, 3002, 3000, 'Phase 3: Testing', 'QA processes', 'DEFAULT');

-- Closure table for tree 3
INSERT INTO closure_entity (ancestor, descendant, depth, parent, root) VALUES
-- Self-references
(3000, 3000, 0, NULL, 3000),
(3001, 3001, 0, 3000, 3000),
(3002, 3002, 0, 3000, 3000),
(3003, 3003, 0, 3001, 3000),
(3004, 3004, 0, 3002, 3000),
(3005, 3005, 0, 3002, 3000),
-- Ancestor-descendant relationships
(3000, 3002, 1, 3000, 3000),
(3000, 3003, 2, 3001, 3000),
(3000, 3004, 2, 3002, 3000),
(3000, 3001, 1, 3000, 3000),
(3000, 3005, 2, 3002, 3000),
(3001, 3003, 1, 3001, 3000),
(3002, 3004, 1, 3002, 3000),
(3002, 3005, 1, 3002, 3000);

-- Tree 4: Simple tree for testing (owner: admin, marked as deleted)
INSERT INTO nodes (id, parent, root, title, description, node_type) VALUES
    (4000, NULL, 4000, 'Test Tree', 'For soft delete testing', 'DEFAULT');

INSERT INTO trees (id, title, description, root_node_id, owner_id, deleted_at, deleted_by, created_at, created_by, updated_at, node_count, max_depth) VALUES
    (4, 'Deleted Tree', 'This tree was deleted', 4000, 1, NOW(), 'admin', NOW(), 'admin', NOW(), 1, 0);

INSERT INTO closure_entity (ancestor, descendant, depth, parent, root) VALUES
    (4000, 4000, 0, NULL, 4000);


INSERT INTO comment(author_id, text, node_id, created_at)
VALUES (1, 'test comment', 1000, NOW());
