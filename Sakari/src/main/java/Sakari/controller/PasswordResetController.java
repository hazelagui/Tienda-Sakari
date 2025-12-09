package Sakari.controller;

import Sakari.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @GetMapping("/recuperar-password")
    public String mostrarFormularioRecuperacion() {
        return "auth/recuperar-password";
    }

    @PostMapping("/recuperar-password")
    public String procesarSolicitudRecuperacion(
            @RequestParam String email,
            RedirectAttributes redirectAttributes) {
        
        var tokenOpt = passwordResetService.crearTokenRecuperacion(email);
        
        // Siempre mostramos el mismo mensaje por seguridad
        // (no revelamos si el email existe o no)
        redirectAttributes.addFlashAttribute("mensaje", 
            "Si el correo está registrado, recibirás instrucciones para restablecer tu contraseña. " +
            "Por favor revisa tu bandeja de entrada y spam.");
        
        // En un entorno real, aquí enviaríamos el email con el enlace
        // Para desarrollo, mostramos el token en consola
        tokenOpt.ifPresent(token -> {
            log.info("========================================");
            log.info("TOKEN DE RECUPERACIÓN (solo desarrollo)");
            log.info("Email: {}", email);
            log.info("Token: {}", token);
            log.info("URL: /reset-password?token={}", token);
            log.info("========================================");
        });
        
        return "redirect:/recuperar-password";
    }

    @GetMapping("/reset-password")
    public String mostrarFormularioReset(
            @RequestParam String token,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (!passwordResetService.validarToken(token)) {
            redirectAttributes.addFlashAttribute("error", 
                "El enlace de recuperación es inválido o ha expirado. Por favor solicita uno nuevo.");
            return "redirect:/recuperar-password";
        }
        
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String procesarResetPassword(
            @RequestParam String token,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {
        
        // Validar que las contraseñas coincidan
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden");
            return "redirect:/reset-password?token=" + token;
        }
        
        // Validar longitud mínima
        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 6 caracteres");
            return "redirect:/reset-password?token=" + token;
        }
        
        // Intentar resetear la contraseña
        boolean exito = passwordResetService.resetearPassword(token, password);
        
        if (exito) {
            redirectAttributes.addFlashAttribute("mensaje", 
                "Tu contraseña ha sido restablecida exitosamente. Ya puedes iniciar sesión.");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("error", 
                "No se pudo restablecer la contraseña. El enlace puede haber expirado.");
            return "redirect:/recuperar-password";
        }
    }
}
