package Sakari.controller;

import Sakari.domain.Usuario;
import Sakari.security.CustomUserDetails;
import Sakari.service.UsuarioService;
import Sakari.service.PedidoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perfil")
@RequiredArgsConstructor
@Slf4j
public class PerfilController {

    private final UsuarioService usuarioService;
    private final PedidoService pedidoService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String verPerfil(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {
        
        Usuario usuario = usuarioService.buscarPorId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("pedidos", pedidoService.listarPedidosUsuario(usuario.getId()));
        
        return "usuario/perfil";
    }

    @GetMapping("/editar")
    public String mostrarFormularioEdicion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {
        
        Usuario usuario = usuarioService.buscarPorId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        model.addAttribute("usuario", usuario);
        return "usuario/editar-perfil";
    }

    @PostMapping("/editar")
    public String actualizarPerfil(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String direccion,
            RedirectAttributes redirectAttributes) {
        
        try {
            Usuario usuario = usuarioService.buscarPorId(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setTelefono(telefono);
            usuario.setDireccion(direccion);
            
            usuarioService.actualizarUsuario(usuario);
            
            redirectAttributes.addFlashAttribute("mensaje", "Perfil actualizado exitosamente");
            log.info("Perfil actualizado para usuario: {}", usuario.getEmail());
            
        } catch (Exception e) {
            log.error("Error al actualizar perfil", e);
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el perfil");
        }
        
        return "redirect:/perfil";
    }

    @GetMapping("/cambiar-password")
    public String mostrarFormularioCambioPassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {
        
        Usuario usuario = usuarioService.buscarPorId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        model.addAttribute("usuario", usuario);
        return "usuario/cambiar-password";
    }

    @PostMapping("/cambiar-password")
    public String cambiarPassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String passwordActual,
            @RequestParam String passwordNueva,
            @RequestParam String confirmarPassword,
            RedirectAttributes redirectAttributes) {
        
        try {
            Usuario usuario = usuarioService.buscarPorId(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            // Verificar contraseña actual
            if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "La contraseña actual es incorrecta");
                return "redirect:/perfil/cambiar-password";
            }
            
            // Verificar que las contraseñas nuevas coincidan
            if (!passwordNueva.equals(confirmarPassword)) {
                redirectAttributes.addFlashAttribute("error", "Las contraseñas nuevas no coinciden");
                return "redirect:/perfil/cambiar-password";
            }
            
            // Validar longitud mínima
            if (passwordNueva.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 6 caracteres");
                return "redirect:/perfil/cambiar-password";
            }
            
            // Cambiar contraseña
            usuarioService.cambiarPassword(usuario.getId(), passwordNueva);
            
            redirectAttributes.addFlashAttribute("mensaje", "Contraseña cambiada exitosamente");
            log.info("Contraseña cambiada para usuario: {}", usuario.getEmail());
            
        } catch (Exception e) {
            log.error("Error al cambiar contraseña", e);
            redirectAttributes.addFlashAttribute("error", "Error al cambiar la contraseña");
        }
        
        return "redirect:/perfil";
    }

    @GetMapping("/pedidos")
    public String verMisPedidos(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {
        
        Usuario usuario = usuarioService.buscarPorId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("pedidos", pedidoService.listarPedidosUsuario(userDetails.getId()));
        return "usuario/pedidos";
    }
}
