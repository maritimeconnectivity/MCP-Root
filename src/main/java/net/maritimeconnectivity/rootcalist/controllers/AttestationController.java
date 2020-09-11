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
import net.maritimeconnectivity.rootcalist.model.Attestation;
import net.maritimeconnectivity.rootcalist.services.AttestationService;
import net.maritimeconnectivity.rootcalist.services.AttestorService;
import net.maritimeconnectivity.rootcalist.services.RootCAService;
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
@RequestMapping("/api")
@RestController
public class AttestationController {

    private AttestationService attestationService;
    private AttestorService attestorService;
    private RootCAService rootCAService;

    @Autowired
    public void setAttestationService(AttestationService attestationService) {
        this.attestationService = attestationService;
    }

    @Autowired
    public void setAttestorService(AttestorService attestorService) {
        this.attestorService = attestorService;
    }

    @Autowired
    public void setRootCAService(RootCAService rootCAService) {
        this.rootCAService = rootCAService;
    }

    @GetMapping(
            value = "/attestations",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<Attestation>> getAttestations() {
        List<Attestation> attestations = this.attestationService.listAll();
        return new ResponseEntity<>(attestations, HttpStatus.OK);
    }

    @GetMapping(
            value = "/attestation/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Attestation> getAttestation(@PathVariable Long id) {
        Attestation attestation = this.attestationService.getById(id);
        if (attestation != null) {
            return new ResponseEntity<>(attestation, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping(
            value = "/attestation",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Attestation> createAttestation(@RequestBody Attestation input) {
        if (input.getAttestor() != null && input.getRootCA() != null) {

        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
