package ar.edu.utn.dds.k3003.controllers.donaciones;

import ar.edu.utn.dds.k3003.Fachada;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.DonacionDTO;
import ar.edu.utn.dds.k3003.model.donaciones.Donacion.EstadoRequestDTO;
import ar.edu.utn.dds.k3003.model.donaciones.Donacion.QuejaRequestDTO;
import ar.edu.utn.dds.k3003.catedra.dtos.donaciones.EstadoDonacionEnum;
import ar.edu.utn.dds.k3003.catedra.dtos.donadoresYEntidades.QuejaDTO;
import ar.edu.utn.dds.k3003.exceptions.DonadorNoEncontradoException;
import ar.edu.utn.dds.k3003.exceptions.donaciones.CambioEstadoInvalidoException;
import ar.edu.utn.dds.k3003.exceptions.donaciones.DonacionInvalidaException;
import ar.edu.utn.dds.k3003.exceptions.donaciones.DonacionNoEncontradaException;
import ar.edu.utn.dds.k3003.exceptions.donaciones.DonadorNoAptoException;
import ar.edu.utn.dds.k3003.model.donaciones.Donacion;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/donaciones")
public class DonacionController {

    private Fachada fachada;

    public DonacionController(Fachada fachada) {
        this.fachada = fachada;
    }

    @PostMapping
    public ResponseEntity<DonacionDTO> registrarDonacion(@RequestBody DonacionDTO donacionDTO) {
        try {
            DonacionDTO donacionRegistrada = this.fachada.registrarDonacion(donacionDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(donacionRegistrada);
        } catch (DonadorNoAptoException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (DonadorNoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

    }

    @GetMapping
    public ResponseEntity<List<DonacionDTO>> obtenerDonaciones() {
        List<DonacionDTO> donaciones = this.fachada.obtenerTodasLasDonaciones();
        return ResponseEntity.ok(donaciones);

    }

    @GetMapping("/{id}")
    public ResponseEntity<DonacionDTO> obtenerDonacion(@PathVariable String id) {
        try {
            DonacionDTO donacionBuscada = this.fachada.buscarDonacionPorID(id);
            return ResponseEntity.ok(donacionBuscada);
        }  catch (DonacionNoEncontradaException | NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public  ResponseEntity<Void> eliminarDonacion(@PathVariable String id) {
        try {
            this.fachada.eliminarDonacion(id);
            return ResponseEntity.noContent().build();
        } catch (DonacionNoEncontradaException | NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<DonacionDTO>> buscarDonacionPorDonadorYFecha(@RequestParam String donadorID, @RequestParam String fechaInicio) {
        try {
            LocalDate fecha = LocalDate.parse(fechaInicio);
            List<DonacionDTO> donaciones = this.fachada.buscarPorDonadorYFechaInicio(donadorID, fecha);

            return ResponseEntity.ok(donaciones);
        } catch (DonadorNoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<DonacionDTO> ModificarEstadoDeDonacion(@PathVariable String id, @RequestBody EstadoDonacionEnum estado) {
        try {
            DonacionDTO donacionActualizada = this.fachada.cambiarEstadoDeDonacion(id, estado);
            return  ResponseEntity.ok(donacionActualizada);
        } catch (CambioEstadoInvalidoException e) {
         return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (DonacionNoEncontradaException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/{id}/queja")
    public ResponseEntity<DonacionDTO> registrarQuejaSobreDonacion(@PathVariable String id, @RequestBody QuejaRequestDTO descripcion) {
        try {
            String descripcionaux= descripcion.descripcion();
            DonacionDTO donacionActualizada = this.fachada.registrarQuejaEnDonacion(id,descripcionaux);
            return ResponseEntity.status(HttpStatus.CREATED).body(donacionActualizada);
        } catch (DonacionNoEncontradaException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
