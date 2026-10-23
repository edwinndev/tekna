package com.tekna.api.dto;

import com.tekna.api.entity.Role;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class UserOutput {

    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
