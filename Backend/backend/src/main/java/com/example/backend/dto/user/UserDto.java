package com.example.backend.dto.user;

public class UserDto {
    private Long id;
    private String login;
    private String email;
    private boolean admin;

    public UserDto() {}

    public UserDto(Long id, String login, String email, boolean admin) {
        this.id = id;
        this.login = login;
        this.email = email;
        this.admin = admin;
    }

    public Long getId()              { return id; }
    public void setId(Long id)       { this.id = id; }

    public String getLogin()             { return login; }
    public void setLogin(String login)   { this.login = login; }

    public String getEmail()             { return email; }
    public void setEmail(String email)   { this.email = email; }

    public boolean isAdmin()             { return admin; }
    public void setAdmin(boolean admin)  { this.admin = admin; }
}
