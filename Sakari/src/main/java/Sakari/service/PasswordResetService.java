package Sakari.service;

import Sakari.domain.TokenRecuperacion;
import Sakari.domain.Usuario;
import Sakari.repository.TokenRecuperacionRepository;
import Sakari.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PasswordResetService {

    private final TokenRecuperacionRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<String> crearTokenRecuperacion(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        
        if (usuarioOpt.isEmpty()) {
            log.warn("Intento de recuperación para email no registrado: {}", email);
            return Optional.empty();
        }
        
        Usuario usuario = usuarioOpt.get();
        
        // Invalidar tokens anteriores
        tokenRepository.invalidarTokensAnteriores(usuario);
        
        // Crear nuevo token
        TokenRecuperacion token = new TokenRecuperacion();
        token.setUsuario(usuario);
        token.setToken(UUID.randomUUID().toString());
        token.setFechaExpiracion(LocalDateTime.now().plusHours(24));
        token.setUsado(false);
        
        tokenRepository.save(token);
        
        log.info("Token de recuperación creado para usuario: {}", email);
        return Optional.of(token.getToken());
    }

    public boolean validarToken(String token) {
        Optional<TokenRecuperacion> tokenOpt = tokenRepository.findValidToken(token, LocalDateTime.now());
        return tokenOpt.isPresent() && tokenOpt.get().isValido();
    }

    public boolean resetearPassword(String token, String nuevaPassword) {
        Optional<TokenRecuperacion> tokenOpt = tokenRepository.findValidToken(token, LocalDateTime.now());
        
        if (tokenOpt.isEmpty()) {
            log.warn("Intento de reset con token inválido o expirado");
            return false;
        }
        
        TokenRecuperacion tokenRecuperacion = tokenOpt.get();
        
        if (!tokenRecuperacion.isValido()) {
            log.warn("Token ya usado o expirado");
            return false;
        }
        
        // Actualizar contraseña
        Usuario usuario = tokenRecuperacion.getUsuario();
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
        
        // Marcar token como usado
        tokenRecuperacion.setUsado(true);
        tokenRepository.save(tokenRecuperacion);
        
        log.info("Contraseña reseteada exitosamente para usuario: {}", usuario.getEmail());
        return true;
    }

    public void limpiarTokensExpirados() {
        tokenRepository.eliminarTokensExpirados(LocalDateTime.now());
        log.info("Tokens expirados eliminados");
    }

    public Optional<Usuario> obtenerUsuarioPorToken(String token) {
        return tokenRepository.findValidToken(token, LocalDateTime.now())
                .map(TokenRecuperacion::getUsuario);
    }
}
