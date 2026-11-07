package com.talentiq.config;

import com.talentiq.entity.Role;
import com.talentiq.entity.User;
import com.talentiq.entity.VerificationStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final String fullName;
    private final Role role;
    private final boolean emailVerified;
    private final VerificationStatus verificationStatus;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.fullName = user.getFullName();
        this.role = user.getRole();
        this.emailVerified = user.isEmailVerified();
        this.verificationStatus = user.getVerificationStatus();

        // Enforce role-based authority:
        // A recruiter only possesses ROLE_RECRUITER authority if their verificationStatus is VERIFIED.
        // Otherwise, they are assigned ROLE_RECRUITER_PENDING so recruiter-only endpoints block them.
        String authorityName;
        if (user.getRole() == Role.ROLE_RECRUITER) {
            if (user.getVerificationStatus() == VerificationStatus.VERIFIED) {
                authorityName = Role.ROLE_RECRUITER.name();
            } else {
                authorityName = "ROLE_RECRUITER_PENDING";
            }
        } else {
            authorityName = user.getRole().name();
        }

        this.authorities = Collections.singletonList(new SimpleGrantedAuthority(authorityName));
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
