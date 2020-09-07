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

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import java.util.Set;

@MappedSuperclass
@Getter
@Setter
public abstract class EntityModel extends TimestampModel {

    @ApiModelProperty(value = "The name that identifies the entity", required = true)
    @Column(name = "name", nullable = false)
    protected String name;

    @ApiModelProperty(value = "The certificate of the entity", required = true)
    @Column(name = "certificate", nullable = false)
    protected String certificate;

    @OneToMany
    private Set<Signature> signatures;
}