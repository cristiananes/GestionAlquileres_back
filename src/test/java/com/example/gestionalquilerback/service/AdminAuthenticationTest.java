package com.example.gestionalquilerback.service;
import org.junit.jupiter.api.Test;
import com.example.gestionalquilerback.dto.LoginRequest;
import com.example.gestionalquilerback.model.entity.User;
import com.example.gestionalquilerback.model.enums.Role;
import com.example.gestionalquilerback.repository.UserRepository;
import com.example.gestionalquilerback.security.JwtTokenProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class AdminAuthenticationTest {
    @Test void weakAdministratorCredentialsDoNotProduceAToken() {
        var users = mock(UserRepository.class); var encoder = mock(PasswordEncoder.class); var tokens = mock(JwtTokenProvider.class);
        var user = User.builder().id(1L).email("admin@test.example").password("hash").role(Role.ADMIN).build();
        when(users.findByEmail(user.getEmail())).thenReturn(java.util.Optional.of(user));
        when(encoder.matches("short-password","hash")).thenReturn(true);
        var request = new LoginRequest(); request.setEmail(user.getEmail()); request.setPassword("short-password");
        assertThrows(BadCredentialsException.class,()->new AuthService(users,encoder,tokens).login(request));
        verifyNoInteractions(tokens);
    }
    @Test void ordinaryUserSessionsKeepTheirExistingLoginBehaviour() {
        var users = mock(UserRepository.class); var encoder = mock(PasswordEncoder.class); var tokens = mock(JwtTokenProvider.class);
        var user = User.builder().id(1L).email("normal@test.example").password("hash").role(Role.USER).build();
        when(users.findByEmail(user.getEmail())).thenReturn(java.util.Optional.of(user));
        when(encoder.matches("short-password","hash")).thenReturn(true);
        when(tokens.generateToken(user)).thenReturn("test-token");
        var request = new LoginRequest(); request.setEmail(user.getEmail()); request.setPassword("short-password");
        assertEquals("test-token",new AuthService(users,encoder,tokens).login(request).getToken());
    }
}
