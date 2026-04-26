package edu.market.userservice.domain.model;

import edu.market.userservice.domain.exception.UserException;
import edu.market.userservice.domain.vo.SocialLoginDataVO;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class User {
    
    // Patrones de validación
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    
    private static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");

    private Long id;
    private String name;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phone;
    
    // Campos de estado
    private boolean enabled;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    
    // Roles del usuario
    private List<Role> roles;
    
    // Campos de auditoría
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    
    // Constructor por defecto
    public User() {
        this.roles = new ArrayList<>();
    }
    
    // Constructor completo
    public User(Long id, String name, String firstName, String lastName, String email, String password, String phone,
                boolean enabled, boolean accountNonExpired, boolean accountNonLocked, boolean credentialsNonExpired,
                List<Role> roles,
                LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime lastLoginAt) {
        this.id = id;
        this.name = name;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;        
        this.roles = roles != null ? roles : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLoginAt = lastLoginAt;
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }
    
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }
    
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }
    
    public List<Role> getRoles() {
        return roles;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
    
    // Setters
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public void setEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw UserException.invalidEmailFormat();
        }
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setPassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("La contraseña no puede ser nula");
        }
        this.password = password;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public void setAccountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }
    
    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }
    
    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }
    
    public void setRoles(List<Role> roles) {
        this.roles = roles != null ? roles : new ArrayList<>();
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
    
    
    /**
     * Cambia la contraseña del usuario y la codifica
     * 
     * @param rawPassword Contraseña sin codificar
     * @param passwordEncoder Codificador de contraseñas
     */
    public void changePassword(String rawPassword, PasswordEncoder passwordEncoder) {
        if (rawPassword == null || passwordEncoder == null) {
            throw new IllegalArgumentException("La contraseña y el codificador no pueden ser nulos");
        }
        if (!PASSWORD_PATTERN.matcher(rawPassword).matches()) {
            throw UserException.invalidPasswordFormat();
        }
        this.password = passwordEncoder.encode(rawPassword);
        this.credentialsNonExpired = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Actualiza la información básica del usuario
     * 
     * @param firstName Nombre
     * @param lastName Apellido
     * @param phone Teléfono
     */
    public void updateBasicInfo(String firstName, String lastName, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.name = firstName + " " + lastName;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Activa la cuenta del usuario
     */
    public void activate() {
        this.enabled = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Desactiva la cuenta del usuario
     */
    public void deactivate() {
        this.enabled = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Bloquea la cuenta del usuario
     */
    public void lock() {
        this.accountNonLocked = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Desbloquea la cuenta del usuario
     */
    public void unlock() {
        this.accountNonLocked = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Registra un inicio de sesión exitoso
     */
    public void registerLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }
    
    /**
     * Verifica si el usuario puede iniciar sesión
     * 
     * @return true si el usuario puede iniciar sesión
     */
    public boolean canLogin() {
        return this.enabled && this.accountNonExpired && this.accountNonLocked && this.credentialsNonExpired;
    }
    
    /**
     * Prepara un usuario para el registro inicial
     * 
     * @param passwordEncoder Codificador de contraseñas (opcional, puede ser null si la contraseña ya está codificada)
     */
    public void prepareForRegistration(PasswordEncoder passwordEncoder) {
        // Codificar contraseña si se proporciona un codificador
        if (passwordEncoder != null && this.password != null) {
            this.password = passwordEncoder.encode(this.password);
        }
        
        // Configurar campos de estado
        this.enabled = true;
        this.accountNonExpired = true;
        this.accountNonLocked = true;
        this.credentialsNonExpired = true;
        
        // Configurar campos de auditoría
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
    
    /**
     * Actualiza el usuario con datos de autenticación social
     * 
     * @param socialLoginData Datos de autenticación social
     */
    public void updateFromSocialLogin(SocialLoginDataVO socialLoginData) {
        if (socialLoginData == null) {
            throw new IllegalArgumentException("Los datos de login social no pueden ser nulos");
        }
        
        // Solo actualizamos si hay datos nuevos
        if (socialLoginData.getEmail() != null && !socialLoginData.getEmail().equals(this.email)) {
            this.email = socialLoginData.getEmail();
        }
        
        if (socialLoginData.getName() != null) {
            // Extraer nombre y apellido del nombre completo si es posible
            String[] nameParts = socialLoginData.getName().split(" ", 2);
            if (nameParts.length > 0) {
                this.firstName = nameParts[0];
                if (nameParts.length > 1) {
                    this.lastName = nameParts[1];
                }
                this.name = socialLoginData.getName();
            }
        }
        
        // Marcar como actualizado
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Verifica si el email es válido
     * 
     * @param email Email a validar
     * @return true si el email es válido
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Verifica si la contraseña es válida según el patrón definido
     * 
     * @param password Contraseña a validar
     * @return true si la contraseña es válida
     */
    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }
}
