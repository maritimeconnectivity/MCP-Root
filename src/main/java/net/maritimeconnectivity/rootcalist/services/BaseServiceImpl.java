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

import lombok.extern.slf4j.Slf4j;
import net.maritimeconnectivity.rootcalist.model.TimestampModel;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Transactional(readOnly = true)
public abstract class BaseServiceImpl<T extends TimestampModel> implements BaseService<T> {

    @Override
    public T getById(Long id) {
        Optional<T> optionalT = getRepository().findById(id);
        return optionalT.orElse(null);
    }

    @Transactional
    @Override
    public T save(T entity) {
        log.debug("Just saved entity");
        return getRepository().save(entity);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        getRepository().deleteById(id);
    }

}
