package Sakari.service;

import Sakari.domain.Categoria;
import Sakari.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    public List<Categoria> listarActivas() {
        return categoriaRepository.findByActivoTrue();
    }

    public Optional<Categoria> buscarPorId(Long id) {
        return categoriaRepository.findById(id);
    }

    public Optional<Categoria> buscarPorNombre(String nombre) {
        return categoriaRepository.findByNombre(nombre);
    }

    public Categoria crearCategoria(Categoria categoria) {
        if (categoriaRepository.existsByNombre(categoria.getNombre())) {
            throw new RuntimeException("Ya existe una categoría con ese nombre");
        }
        Categoria guardada = categoriaRepository.save(categoria);
        log.info("Categoría creada: {}", guardada.getNombre());
        return guardada;
    }

    public Categoria actualizarCategoria(Categoria categoria) {
        Categoria actualizada = categoriaRepository.save(categoria);
        log.info("Categoría actualizada: {}", actualizada.getNombre());
        return actualizada;
    }

    public void eliminarCategoria(Long id) {
        categoriaRepository.deleteById(id);
        log.info("Categoría eliminada: {}", id);
    }
}
