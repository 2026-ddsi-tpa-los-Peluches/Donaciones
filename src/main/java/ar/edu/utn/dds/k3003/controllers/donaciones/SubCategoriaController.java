package ar.edu.utn.dds.k3003.controllers.donaciones;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.IdentificadorDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.SubCategoriaDTO;
import ar.edu.utn.dds.k3003.exceptions.donaciones.IdentificadorNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/identificadores")
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


}
