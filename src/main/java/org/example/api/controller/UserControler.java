package org.example.api.controller;


import org.example.api.dto.UserDTO;
import org.example.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api")
public class UserControler {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> logarNaConta(@RequestBody UserDTO user) {
        String token = userService.loginComToken(user.getEmail(), user.getSenha());

        if (token != null) {
            return ResponseEntity.ok().body("{\"token\": \"" + token + "\"}");
        } else {
            return ResponseEntity.status(401).body("{\"erro\": \"Email ou senha incorretos.\"}");
        }
    }


}
