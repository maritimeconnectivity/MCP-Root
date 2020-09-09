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

package net.maritimeconnectivity.rootcalist.services;

import net.maritimeconnectivity.rootcalist.model.RootCA;
import net.maritimeconnectivity.rootcalist.repositories.RootCARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RootCAServiceImpl extends BaseServiceImpl<RootCA> implements RootCAService {

    private RootCARepository rootCARepository;

    @Autowired
    private void setRootCARepository(RootCARepository rootCARepository) {
        this.rootCARepository = rootCARepository;
    }

    public List<RootCA> listAll() {
        return (List<RootCA>) this.rootCARepository.findAll();
    }

    @Override
    public List<RootCA> listByAttestors(List<Long> attestorIds) {
        return this.rootCARepository.findByAttestor(attestorIds);
    }

    @Override
    public RootCARepository getRepository() {
        return this.rootCARepository;
    }
}