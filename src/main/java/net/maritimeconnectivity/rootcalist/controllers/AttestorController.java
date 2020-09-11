/*
 * Copyright 2020 Maritime Connectivity Platform Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.maritimeconnectivity.rootcalist.controllers;

import lombok.extern.slf4j.Slf4j;
import net.maritimeconnectivity.rootcalist.model.Attestor;
import net.maritimeconnectivity.rootcalist.services.AttestorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class AttestorController {

    private AttestorService attestorService;

    @Autowired
    public void setAttestorService(AttestorService attestorService) {
        this.attestorService = attestorService;
    }

    @GetMapping(
            value = "/attestors",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<Attestor>> getAttestors() {
        List<Attestor> attestors = this.attestorService.listAll();
        return new ResponseEntity<>(attestors, HttpStatus.OK);
    }

    @GetMapping(
            value = "/attestor/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Attestor> getAttestor(@PathVariable Long id) {
        Attestor attestor = this.attestorService.getById(id);
        if (attestor != null) {
            return new ResponseEntity<>(attestor, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping(
            value = "/attestor",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Attestor> createAttestor(@RequestBody Attestor attestor) {
        Attestor newAttestor = this.attestorService.save(attestor);
        return new ResponseEntity<>(newAttestor, HttpStatus.OK);
    }
}
