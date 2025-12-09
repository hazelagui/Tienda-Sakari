package Sakari.service;

import Sakari.domain.MensajeContacto;
import Sakari.repository.MensajeContactoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ContactoService {

    private final MensajeContactoRepository mensajeRepository;

    public MensajeContacto enviarMensaje(MensajeContacto mensaje) {
        mensaje.setFechaEnvio(LocalDateTime.now());
        mensaje.setLeido(false);
        mensaje.setRespondido(false);
        
        MensajeContacto guardado = mensajeRepository.save(mensaje);
        log.info("Nuevo mensaje de contacto recibido de: {}", mensaje.getEmail());
        
        return guardado;
    }

    public List<MensajeContacto> listarTodos() {
        return mensajeRepository.findAllByOrderByFechaEnvioDesc();
    }

    public List<MensajeContacto> listarNoLeidos() {
        return mensajeRepository.findByLeidoFalseOrderByFechaEnvioDesc();
    }

    public List<MensajeContacto> listarPendientesRespuesta() {
        return mensajeRepository.findByRespondidoFalseOrderByFechaEnvioDesc();
    }

    public Optional<MensajeContacto> buscarPorId(Long id) {
        return mensajeRepository.findById(id);
    }

    public MensajeContacto marcarComoLeido(Long id) {
        MensajeContacto mensaje = mensajeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));
        
        mensaje.setLeido(true);
        mensaje.setFechaLectura(LocalDateTime.now());
        
        return mensajeRepository.save(mensaje);
    }

    public MensajeContacto marcarComoRespondido(Long id) {
        MensajeContacto mensaje = mensajeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));
        
        mensaje.setRespondido(true);
        if (!mensaje.getLeido()) {
            mensaje.setLeido(true);
            mensaje.setFechaLectura(LocalDateTime.now());
        }
        
        return mensajeRepository.save(mensaje);
    }

    public void eliminarMensaje(Long id) {
        mensajeRepository.deleteById(id);
        log.info("Mensaje de contacto eliminado: {}", id);
    }

    public long contarNoLeidos() {
        return mensajeRepository.countByLeidoFalse();
    }

    public long contarPendientesRespuesta() {
        return mensajeRepository.countByRespondidoFalse();
    }
}
