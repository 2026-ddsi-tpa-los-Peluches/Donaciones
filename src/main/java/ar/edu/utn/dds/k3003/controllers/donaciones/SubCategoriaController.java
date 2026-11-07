package ar.edu.utn.dds.k3003.controllers.donaciones;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.IdentificadorDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.SubCategoriaDTO;
import ar.edu.utn.dds.k3003.exceptions.donaciones.IdentificadorNoEncontradoException;
import ar.edu.utn.dds.k3003.exceptions.donaciones.SubCategoriaNoEncontradaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/subcategorias")
public class SubCategoriaController {

    private Fachada fachada;

    public SubCategoriaController(Fachada fachada) {
        this.fachada = fachada;
    }

    @PostMapping
    public ResponseEntity<SubCategoriaDTO> agregarSubCategoria(@RequestBody SubCategoriaDTO subCategoriaDTO) {
        try {
            SubCategoriaDTO subCategoriaCreada = this.fachada.agregarSubCategoria(subCategoriaDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(subCategoriaCreada);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<SubCategoriaDTO>> obtenerSubCategorias() {
        List<SubCategoriaDTO> subCategorias = this.fachada.obtenerTodasLasSubCategorias();
        return ResponseEntity.ok(subCategorias);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> obtenerSubCategoria(@PathVariable String id) {
        try {
            SubCategoriaDTO subCategoria = this.fachada.buscarSubCategoriaPorID(id);
            return ResponseEntity.ok(subCategoria);
        } catch (SubCategoriaNoEncontradaException | NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error en la solicitud de la subcategoría.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SubCategoriaDTO> eliminarSubCategoria(@PathVariable String id) {
        try {
            this.fachada.eliminarSubCategoria(id);
            return ResponseEntity.noContent().build();
        } catch (SubCategoriaNoEncontradaException | NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
