#!/bin/bash
rm -f whisper.store
for filename in grapherex_prod_2021.cer; do
  keytool -importcert -v -trustcacerts -file "$filename" -alias "$filename" -keystore "whisper.store" -provider org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath "/Users/esseckers/Downloads/bcprov-jdk16-1.45.jar" -storetype BKS -storepass whisper -noprompt -v
done
