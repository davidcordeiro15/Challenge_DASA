package org.example.api.controller;

import org.example.api.model.DadosModel;
import org.example.api.service.DadosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DadosController {

    @Autowired
    private DadosService dadosService;

    // Somente acessível com token válido
    @GetMapping("/dados")
    public ResponseEntity<List<DadosModel>> listarDados() {
        List<DadosModel> dados = dadosService.listarDados();
        return ResponseEntity.ok(dados);
    }
}
