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
    public ResponseEntity<List<RootCA>> getRootCAs(@RequestParam(required = false, name = "attestorId") List<Long> attestorIds) {
        if (attestorIds != null) {
            List<RootCA> rootCAS = this.rootCAService.listByAttestors(attestorIds);
            //List<Attestation> attestations = new ArrayList<>(rootCAS.get(0).getAttestations());
            return new ResponseEntity<>(rootCAS, HttpStatus.OK);
        }
        return new ResponseEntity<>(this.rootCAService.listAll(), HttpStatus.OK);
    }

    @GetMapping(
            value = "/root/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RootCA> getRootCA(@PathVariable Long id) {
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
    public ResponseEntity<RootCA> createRootCA(@RequestBody String rootCACert) {
        PEMParser pemParser = new PEMParser(new StringReader(rootCACert));
        try {
            X509CertificateHolder certificateHolder = (X509CertificateHolder) pemParser.readObject();
            pemParser.close();
            if (certificateHolder != null && certificateHolder.isValidOn(new Date()) && CryptoUtil.isSelfSigned(certificateHolder)) {
                RootCA rootCA = new RootCA();
                rootCA.setCertificate(rootCACert);
                X500Name x500Name = certificateHolder.getSubject();
                RDN cn = x500Name.getRDNs(BCStyle.CN)[0];
                String cnString = IETFUtils.valueToString(cn.getFirst().getValue());
                rootCA.setName(cnString);
                RootCA newRootCA = this.rootCAService.save(rootCA);
                return new ResponseEntity<>(newRootCA, HttpStatus.OK);
            }
        } catch (IOException e) {
            log.error("New root CA certificate could not be parsed");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
