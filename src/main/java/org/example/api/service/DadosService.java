package org.example.api.service;

import org.example.api.model.DadosModel;
import org.example.api.repository.DadosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DadosService {

    @Autowired
    private DadosRepository dadosRepository;

    public List<DadosModel> listarDados() {
        return dadosRepository.findAll();
    }
}
