package Sakari.repository;

import Sakari.domain.MensajeContacto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeContactoRepository extends JpaRepository<MensajeContacto, Long> {
    
    List<MensajeContacto> findByLeidoFalseOrderByFechaEnvioDesc();
    
    List<MensajeContacto> findAllByOrderByFechaEnvioDesc();
    
    List<MensajeContacto> findByRespondidoFalseOrderByFechaEnvioDesc();
    
    long countByLeidoFalse();
    
    long countByRespondidoFalse();
}
