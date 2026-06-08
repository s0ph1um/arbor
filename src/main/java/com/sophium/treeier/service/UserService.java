package com.sophium.treeier.service;

import com.sophium.treeier.dto.UserDto;
import com.sophium.treeier.entity.Tree;
import com.sophium.treeier.entity.User;
import com.sophium.treeier.exception.NotFoundException;
import com.sophium.treeier.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.sophium.treeier.util.AuthUtil.getAuthenticatedUserEmail;
import static com.sophium.treeier.util.AuthUtil.getAuthenticatedUserName;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
            .map(this::toDto)
            .toList();
    }

    public List<UserDto> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllUsers();
        }
        return userRepository.searchByEmailOrName(query.trim()).stream()
            .map(this::toDto)
            .toList();
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    }

    private UserDto toDto(User entity) {
        return UserDto.builder()
            .id(entity.getId())
            .email(entity.getEmail())
            .name(entity.getName())
            .build();
    }

    public User getTreeOwner(Tree tree) {
        String userId = getAuthenticatedUserEmail();
        String userName = getAuthenticatedUserName();
        Optional<User> existingUserOpt = userRepository.findByEmail(userId);

        return existingUserOpt
            .orElseGet(() -> userRepository.save(User.builder()
                .email(userId)
                .name(userName)
                .createdAt(LocalDateTime.now())
                .editableTrees(Set.of(tree))
                .build()));
    }
}
