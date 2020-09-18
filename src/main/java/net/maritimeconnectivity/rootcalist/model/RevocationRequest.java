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

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RevocationRequest {

    @ApiModelProperty(value = "The ID of the attestor making the attestation", required = true)
    private Long attestorId;
    @ApiModelProperty(value = "The ID of the root CA being attested", required = true)
    private Long rootCAid;
    @ApiModelProperty(value = "The ID of the attestation that this being revoked", required = true)
    private Long attestationId;
    @ApiModelProperty(value = "The HEX encoded signature that is made by signing the certificate of the root CA" +
            "with the private key of the attestor", required = true)
    private String signature;
    @ApiModelProperty(value = "The algorithm identifier of the signature", required = true)
    private String algorithmIdentifier;
}
