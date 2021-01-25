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

CREATE TABLE `root_ca` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL,
    `created_at` DATETIME NOT NULL,
    `certificate` MEDIUMTEXT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE (`certificate`)
);

CREATE TABLE `attestor` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL,
    `created_at` DATETIME NOT NULL,
    `certificate` MEDIUMTEXT NOT NULL,
    `issuer` MEDIUMTEXT,
    PRIMARY KEY (`id`),
    UNIQUE (`certificate`)
);

CREATE TABLE `attestation` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `id_root_ca` INT NOT NULL,
    `id_attestor` INT NOT NULL,
    `signature` MEDIUMTEXT NOT NULL,
    `algorithm` VARCHAR(255),
    `created_at` DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`id_root_ca`) REFERENCES root_ca(`id`),
    FOREIGN KEY (`id_attestor`) REFERENCES attestor(`id`),
    CONSTRAINT attestor_root_ca UNIQUE (`id_attestor`, `id_root_ca`)
);

CREATE TABLE `revocation` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `id_root_ca` INT NOT NULL,
    `id_attestor` INT NOT NULL,
    `id_attestation` INT NOT NULL,
    `signature` MEDIUMTEXT NOT NULL,
    `algorithm` VARCHAR(255),
    `created_at` DATETIME NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`id_root_ca`) REFERENCES root_ca(`id`),
    FOREIGN KEY (`id_attestor`) REFERENCES attestor(`id`),
    FOREIGN KEY (`id_attestation`) REFERENCES attestation(`id`),
    CONSTRAINT root_ca_attestor_attestation UNIQUE (`id_root_ca`, `id_attestor`, `id_attestation`)
);
