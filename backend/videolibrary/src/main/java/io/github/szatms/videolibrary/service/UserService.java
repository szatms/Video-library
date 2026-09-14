package io.github.szatms.videolibrary.service;

import io.github.szatms.videolibrary.mapper.UserMapper;
import io.github.szatms.videolibrary.model.notemodel.Note;
import io.github.szatms.videolibrary.model.notemodel.NoteRepository;
import io.github.szatms.videolibrary.model.usermodel.Role;
import io.github.szatms.videolibrary.model.usermodel.User;
import io.github.szatms.videolibrary.model.usermodel.UserRepository;
import io.github.szatms.videolibrary.model.usermodel.dto.UserAdminUpdateDTO;
import io.github.szatms.videolibrary.model.usermodel.dto.UserResponseDTO;
import io.github.szatms.videolibrary.model.usermodel.dto.UserSelfUpdateDTO;
import io.github.szatms.videolibrary.model.userplaylistmodel.UserPlaylist;
import io.github.szatms.videolibrary.model.uservideomodel.UserVideo;
import io.github.szatms.videolibrary.model.uservideomodel.UserVideoRepository;
import io.github.szatms.videolibrary.model.userplaylistmodel.UserPlaylistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserVideoRepository userVideoRepository;
    private final UserPlaylistRepository userPlaylistRepository;
    private final NoteRepository noteRepository;
    private final UserVideoService userVideoService;
    private final UserPlaylistService userPlaylistService;

    //=========================
    // CURRENT USER
    //=========================
    public UserResponseDTO getCurrentUser() {
        return userMapper.toResponseDTO(getCurrentUserEntity());
    }

    public List<UserResponseDTO> getUsers(){
        return userRepository.findAll().stream()
                .map(userMapper::toResponseDTO)
                .toList();
    }

    //=========================
    // UPDATE
    //=========================
    public UserResponseDTO updateSelf(UserSelfUpdateDTO dto) {
        User user = getCurrentUserEntity();

        if (!isValidUsername(dto.getUsername()))
            throw new IllegalArgumentException("Username invalid or taken already");
        String newUsername = dto.getUsername().trim();

        user.setUsername(newUsername);

        if (dto.getPassword() != null) {
            if (dto.getPassword().isBlank())
                throw new IllegalArgumentException("Password cannot be empty");

            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        userRepository.save(user);
        return userMapper.toResponseDTO(user);
    }

    public UserResponseDTO adminUpdate(UserAdminUpdateDTO dto){
        if (dto.getPassword() == null || dto.getPassword().isEmpty() || dto.getPassword().isBlank())
            return adminUpdateWithoutPassword(dto);
        else
            return adminUpdateWithPassword(dto);
    }

    private UserResponseDTO adminUpdateWithPassword(UserAdminUpdateDTO dto){
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Cannot find user"));
        
        if (dto.getUsername() != null) {
            String newUsername = dto.getUsername().trim();
            if (!newUsername.equals(user.getUsername()) && !isValidUsername(newUsername)) {
                throw new IllegalArgumentException("Username invalid or taken already");
            }
        }
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        if (dto.getEnabled() != null)
            user.setEnabled(dto.getEnabled());

        if (dto.getRole() == Role.ADMIN || dto.getRole() == Role.USER)
            user.setRole(dto.getRole());

        userRepository.save(user);
        return userMapper.toResponseDTO(user);
    }

    private UserResponseDTO adminUpdateWithoutPassword(UserAdminUpdateDTO dto){
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Cannot find user"));

        if (dto.getUsername() != null) {
            String newUsername = dto.getUsername().trim();
            if (!newUsername.equals(user.getUsername()) && !isValidUsername(newUsername)) {
                throw new IllegalArgumentException("Username invalid or taken already");
            }
        }

        if (dto.getEnabled() != null)
            user.setEnabled(dto.getEnabled());

        if (dto.getRole() == Role.ADMIN || dto.getRole() == Role.USER)
            user.setRole(dto.getRole());

        userRepository.save(user);
        return userMapper.toResponseDTO(user);
    }

    //=========================
    // DELETE
    //=========================
    public void deleteSelf() {
        User user = getCurrentUserEntity();
        deleteUser(user.getUserId());
    }

    public void deleteByAdmin(List<String> userIds){
        for (String userId : userIds) {
            deleteUser(userId);
        }
    }

    //=========================
    // HELPERS
    //=========================
    private boolean isValidUsername(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }

        return !userRepository.existsByUsername(username);
    }

    private User getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null)
            throw new IllegalStateException("Not authenticated");

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof io.github.szatms.videolibrary.security.CustomUserDetails userDetails))
            throw new IllegalStateException("Not authenticated");

        String userId = userDetails.getUser().getUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    private void deleteUser(String userId){
        // Delete all UserVideos for this user
        List<UserVideo> userVideos = userVideoRepository.findAllByUserId(userId, Sort.unsorted());
        List<String> userVideoIds = userVideos.stream().map(UserVideo::getId).toList();

        if (!userVideoIds.isEmpty()) {
            userVideoService.deleteVideos(userVideoIds);
        }

        // Delete all UserPlaylists for this user
        List<UserPlaylist> userPlaylists = userPlaylistRepository.findAllByUserId(userId, Sort.unsorted());
        List<String> userPlaylistIds = userPlaylists.stream().map(UserPlaylist::getId).toList();

        for (String userPlaylistId : userPlaylistIds) {
            userPlaylistService.deletePlaylist(userId, userPlaylistId);
        }

        // Delete all notes created by this user
        List<Note> userNotes = noteRepository.findByUserId(userId);
        for (Note note : userNotes) {
            noteRepository.deleteById(note.getId());
        }

        // Finally, delete the user
        userRepository.deleteById(userId);
    }
}
