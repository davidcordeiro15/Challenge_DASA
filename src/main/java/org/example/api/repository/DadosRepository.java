package org.example.api.repository;

import org.example.api.model.DadosModel;
import org.example.api.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface DadosRepository extends JpaRepository<DadosModel, Integer> {

}
