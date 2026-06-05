package org.example.orderservice.infrastructure.security.bootstrap;

import org.example.orderservice.domain.auth.enums.AuthRole;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@ConfigurationProperties(prefix = "security.auth.bootstrap")
public class AuthBootstrapProperties {

    private boolean enabled;
    private boolean updateExistingUsers;
    private String defaultPassword = "";
    private Set<BootstrapUser> users = new HashSet<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isUpdateExistingUsers() {
        return updateExistingUsers;
    }

    public void setUpdateExistingUsers(boolean updateExistingUsers) {
        this.updateExistingUsers = updateExistingUsers;
    }

    public String getDefaultPassword() {
        return defaultPassword;
    }

    public void setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword == null ? "" : defaultPassword;
    }

    public Set<BootstrapUser> getUsers() {
        return users;
    }

    public void setUsers(Set<BootstrapUser> users) {
        this.users = users == null ? new HashSet<>() : users;
    }

    public static class BootstrapUser {

        private String email = "";
        private String password = "";
        private String fullName = "";
        private Set<AuthRole> roles = new HashSet<>();

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email == null ? "" : email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password == null ? "" : password;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName == null ? "" : fullName;
        }

        public Set<AuthRole> getRoles() {
            return roles;
        }

        public void setRoles(Set<AuthRole> roles) {
            this.roles = roles == null ? new HashSet<>() : roles;
        }
    }
}
