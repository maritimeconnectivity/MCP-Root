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

package net.maritimeconnectivity.rootcalist.utils;

import net.maritimeconnectivity.rootcalist.model.EntityModel;
import org.bouncycastle.cert.CertException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ContentVerifierProviderBuilder;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.encoders.HexEncoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CryptoUtil {

    private CryptoUtil() {

    }

    public static X509CertificateHolder[] extractCertificates(String pemString) throws IOException {
        PEMParser pemParser = new PEMParser(new StringReader(pemString));
        List<X509CertificateHolder> certificateHolders = new ArrayList<>();
        while (true) {
            X509CertificateHolder certificateHolder = (X509CertificateHolder) pemParser.readObject();
            if (certificateHolder == null) {
                break;
            }
            certificateHolders.add(certificateHolder);
        }
        return certificateHolders.toArray(new X509CertificateHolder[0]);
    }

    public static void verifyChain(X509CertificateHolder[] certificateHolders) throws CertException, OperatorCreationException {
        JcaX509ContentVerifierProviderBuilder contentVerifierProviderBuilder = new JcaX509ContentVerifierProviderBuilder();
        contentVerifierProviderBuilder.setProvider("BC");
        for (int i = 0; i < certificateHolders.length - 1; i++) {
            X509CertificateHolder certificateHolder = certificateHolders[i];
            X509CertificateHolder issuer = certificateHolders[i + 1];

            Date today = new Date();
            if (!certificateHolder.isValidOn(today) || !issuer.isValidOn(today)) {
                throw new CertException("One or several certificates in chain have expired!");
            }
            if (!certificateHolder.isSignatureValid(contentVerifierProviderBuilder.build(issuer))) {
                throw new CertException("The chain could not be verified");
            }
        }
    }

    // checks if the given certificate is self signed
    public static boolean isSelfSigned(X509CertificateHolder certificateHolder) {
        if (certificateHolder.getSubject().equals(certificateHolder.getIssuer())) {
            JcaX509ContentVerifierProviderBuilder contentVerifierProviderBuilder = new JcaX509ContentVerifierProviderBuilder();
            contentVerifierProviderBuilder.setProvider("BC");
            try {
                return certificateHolder.isSignatureValid(contentVerifierProviderBuilder.build(certificateHolder));
            } catch (CertException | OperatorCreationException e) {
                return false;
            }
        }
        return false;
    }

    // checks if a signature is valid given the signer and the content that has been signed
    public static boolean isSignatureValid(String signatureString, String algorithmIdentifier, EntityModel signer, String original)
            throws IOException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException,
            InvalidKeyException, SignatureException {
        HexEncoder hexEncoder = new HexEncoder();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        hexEncoder.decode(signatureString, outputStream);
        byte[] rawSignature = outputStream.toByteArray();
        Signature signature = Signature.getInstance(algorithmIdentifier, "BC");
        PEMParser pemParser = new PEMParser(new StringReader(signer.getCertificate()));
        X509CertificateHolder certificateHolder = (X509CertificateHolder) pemParser.readObject();
        pemParser.close();
        X509Certificate certificate = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certificateHolder);
        signature.initVerify(certificate);
        byte[] rawOriginal = original.getBytes("UTF-8");
        signature.update(original.getBytes("UTF-8"));
        boolean isValid = signature.verify(rawSignature);
        return isValid;
    }
}
