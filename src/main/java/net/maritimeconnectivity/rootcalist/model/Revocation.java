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

package net.maritimeconnectivity.rootcalist.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "revocation")
@Getter
@Setter
public class Revocation extends SignatureModel {

    @OneToOne
    @JoinColumn(name = "id_attestation")
    private Attestation attestation;

    public Revocation() {
        // empty constructor
    }

    public Revocation(RevocationRequest revocationRequest) {
        this.signature = revocationRequest.getSignature();
        this.algorithmIdentifier = revocationRequest.getAlgorithmIdentifier();
    }
}
