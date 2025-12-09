package Sakari.repository;

import Sakari.domain.TokenRecuperacion;
import Sakari.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRecuperacionRepository extends JpaRepository<TokenRecuperacion, Long> {
    
    Optional<TokenRecuperacion> findByToken(String token);
    
    List<TokenRecuperacion> findByUsuarioAndUsadoFalse(Usuario usuario);
    
    @Query("SELECT t FROM TokenRecuperacion t WHERE t.token = :token AND t.usado = false AND t.fechaExpiracion > :ahora")
    Optional<TokenRecuperacion> findValidToken(String token, LocalDateTime ahora);
    
    @Modifying
    @Query("UPDATE TokenRecuperacion t SET t.usado = true WHERE t.usuario = :usuario")
    void invalidarTokensAnteriores(Usuario usuario);
    
    @Modifying
    @Query("DELETE FROM TokenRecuperacion t WHERE t.fechaExpiracion < :fecha")
    void eliminarTokensExpirados(LocalDateTime fecha);
}
