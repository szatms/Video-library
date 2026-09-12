package io.github.szatms.videolibrary.model.usermodel;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "users")
public class User {
    @Id
    private String userId;

    @Field(name = "username")
    private String username;
    @Field(name = "password")
    private String passwordHash;

    private Role role;
    private LocalDateTime createdAt;
    private Boolean enabled;
}
