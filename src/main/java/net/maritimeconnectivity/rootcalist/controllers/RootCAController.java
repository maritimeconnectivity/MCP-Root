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
import net.maritimeconnectivity.rootcalist.exception.BasicRestException;
import net.maritimeconnectivity.rootcalist.model.database.RootCA;
import net.maritimeconnectivity.rootcalist.services.RootCAService;
import net.maritimeconnectivity.rootcalist.utils.CryptoUtil;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.openssl.PEMParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/")
public class RootCAController {

    private RootCAService rootCAService;

    @Autowired
    public void setRootCAService(RootCAService rootCAService) {
        this.rootCAService = rootCAService;
    }

    @GetMapping(
            value = "/roots",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            description = "Gets the list of root CAs. Can also be used to get only root CAs that are attested by " +
                    "specific attestors using the attestorId query parameter."
    )
    public ResponseEntity<List<RootCA>> getRootCAs(@RequestParam(required = false, name = "attestorId") @Parameter(description = "The ID of an attestor") List<Long> attestorIds) {
        if (attestorIds != null) {
            List<RootCA> rootCAS = this.rootCAService.listByAttestors(attestorIds);
            return new ResponseEntity<>(rootCAS, HttpStatus.OK);
        }
        return new ResponseEntity<>(this.rootCAService.listAll(), HttpStatus.OK);
    }

    @GetMapping(
            value = "/root/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            description = "Gets a specific root CA based on its ID."
    )
    public ResponseEntity<RootCA> getRootCA(@PathVariable @Parameter(description = "The ID of the requested root CA") Long id) {
        RootCA rootCA = this.rootCAService.getById(id);
        if (rootCA != null) {
            return new ResponseEntity<>(rootCA, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping(
            value = "/root",
            consumes = "application/x-pem-file",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            description = "Creates a new root CA. The root CA certificate must be sent in PEM format in the body of " +
                    "the request."
    )
    public ResponseEntity<RootCA> createRootCA(HttpServletRequest request, @RequestBody String rootCACert) throws BasicRestException {
        PEMParser pemParser = new PEMParser(new StringReader(rootCACert));
        try {
            X509CertificateHolder certificateHolder = (X509CertificateHolder) pemParser.readObject();
            pemParser.close();
            if (certificateHolder != null && certificateHolder.isValidOn(new Date()) && CryptoUtil.isSelfSigned(certificateHolder)) {
                RootCA rootCA = new RootCA();
                rootCA.setCertificate(rootCACert);
                X500Name x500Name = certificateHolder.getSubject();
                if (x500Name != null && x500Name.getRDNs(BCStyle.CN).length > 0) {
                    RDN cn = x500Name.getRDNs(BCStyle.CN)[0];
                    String cnString = IETFUtils.valueToString(cn.getFirst().getValue());
                    rootCA.setName(cnString);
                    RootCA newRootCA = this.rootCAService.save(rootCA);
                    return new ResponseEntity<>(newRootCA, HttpStatus.OK);
                }
            }
        } catch (IOException e) {
            log.error("New root CA certificate could not be parsed");
            throw new BasicRestException(HttpStatus.BAD_REQUEST, "The certificate could not be verified", request.getServletPath());
        } catch (DataIntegrityViolationException e) {
            log.error("New root CA could not be persisted because it already exists", e);
            throw new BasicRestException(HttpStatus.BAD_REQUEST, "A root CA with the same certificate already exists", request.getServletPath());
        }
        throw new BasicRestException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong while creating new Root CA", request.getServletPath());
    }
}
