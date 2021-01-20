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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import net.maritimeconnectivity.rootcalist.model.database.Attestor;
import net.maritimeconnectivity.rootcalist.services.AttestorService;
import net.maritimeconnectivity.rootcalist.utils.CryptoUtil;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.CertException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
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

import java.io.IOException;
import java.io.StringWriter;
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
    @Operation(
            description = "Gets the list of all attestors."
    )
    public ResponseEntity<List<Attestor>> getAttestors() {
        List<Attestor> attestors = this.attestorService.listAll();
        return new ResponseEntity<>(attestors, HttpStatus.OK);
    }

    @GetMapping(
            value = "/attestor/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            description = "Gets a specific attestor based on the given ID."
    )
    public ResponseEntity<Attestor> getAttestor(@PathVariable @Parameter(description = "The ID of the attestor") Long id) {
        Attestor attestor = this.attestorService.getById(id);
        if (attestor != null) {
            return new ResponseEntity<>(attestor, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping(
            value = "/attestor",
            consumes = "application/pem-certificate-chain",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            description = "Creates a new attestor. The body of the request must contain a PEM certificate chain " +
                    "consisting of either only the certificate of the attestor or the same followed by the remaining " +
                    "trust chain of the certificate."
    )
    public ResponseEntity<Attestor> createAttestor(@RequestBody String certChain) {
        X509CertificateHolder[] certificateHolders;
        try {
            certificateHolders = CryptoUtil.extractCertificates(certChain);
            if (certificateHolders.length == 0) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            CryptoUtil.verifyChain(certificateHolders);
        } catch (IOException | CertException | OperatorCreationException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Attestor attestor = new Attestor();
        StringWriter stringWriter = new StringWriter();
        PemWriter pemWriter = new PemWriter(stringWriter);
        if (certificateHolders.length > 1) {
            try {
                pemWriter.writeObject(new PemObject("CERTIFICATE", certificateHolders[1].getEncoded()));
                pemWriter.flush();
                attestor.setIssuer(stringWriter.toString());
                stringWriter.flush();
            } catch (IOException e) {
                log.error("Could not verify certificate", e);
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        try {
            pemWriter.writeObject(new PemObject("CERTIFICATE", certificateHolders[0].getEncoded()));
            pemWriter.flush();
            attestor.setCertificate(stringWriter.toString());
            pemWriter.close();
            stringWriter.flush();
            X500Name x500Name = certificateHolders[0].getSubject();
            if (x500Name == null || x500Name.getRDNs(BCStyle.CN).length < 1) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            RDN cn = x500Name.getRDNs(BCStyle.CN)[0];
            String cnString = IETFUtils.valueToString(cn.getFirst().getValue());
            attestor.setName(cnString);
        } catch (IOException e) {
            log.error("Could not write certificate", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Attestor newAttestor = this.attestorService.save(attestor);
        return new ResponseEntity<>(newAttestor, HttpStatus.OK);
    }
}
