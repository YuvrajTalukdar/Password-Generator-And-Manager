package com.yuvraj.passwordgeneratorandmanager;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {
    private static final String AES_TRANSFORMATION_MODE="AES/CBC/PKCS7Padding";

    public String encrypt(String plaintext, String key_text)
    {
        try {
            //getting the sha256 hash for the key
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(key_text.getBytes(StandardCharsets.UTF_8));
            SecretKey key = new SecretKeySpec(hash, "AES");
            //initializing the cipher and encrypting the data
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION_MODE);
            SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), AES_TRANSFORMATION_MODE);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new SecureRandom());
            byte[] encrypted_bytes = cipher.doFinal(plaintext.getBytes("UTF-8"));
            //combining iv with encrypted text
            byte[] iv = cipher.getIV();
            byte[] combined_payload = new byte[iv.length + encrypted_bytes.length];
            System.arraycopy(iv, 0, combined_payload, 0, iv.length);
            System.arraycopy(encrypted_bytes, 0, combined_payload, iv.length, encrypted_bytes.length);
            //conversion for bytes to string
            String combined_payload_text = Base64.encodeToString(combined_payload, Base64.DEFAULT);

            return combined_payload_text;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.out.println("Encryption failed! cause= "+e.getCause());
            return null;
        }
    }

    public aes_data decrypt(String encrypted_text, String key_text)
    {
        try {
            //getting the sha256 hash for the key
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(key_text.getBytes(StandardCharsets.UTF_8));
            SecretKey key = new SecretKeySpec(hash, "AES");
            //converting encrypted_text to encrypted_bytes
            byte[] encryptedPayload = Base64.decode(encrypted_text, Base64.DEFAULT);
            //extracting the iv from encrypted_bytes:
            byte[] iv = new byte[16];
            System.arraycopy(encryptedPayload, 0, iv, 0, 16);
            //extracting data part of encrypted_bytes :
            byte[] encrypted_data = new byte[encryptedPayload.length - iv.length];
            System.arraycopy(encryptedPayload, iv.length, encrypted_data, 0, encrypted_data.length);
            //initializing the cipher and decrypting the data
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION_MODE);
            SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), AES_TRANSFORMATION_MODE);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
            byte[] decryptedText = cipher.doFinal(encrypted_data);
            System.out.println("Decryption success.");
            aes_data aes_data_obj=new aes_data(true,new String(decryptedText));
            return aes_data_obj;
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Decryption failed!"+e.getCause());
            aes_data aes_data_obj=new aes_data(false,null);
            return aes_data_obj;
        }
    }

    public static class aes_data {

        private boolean decryption_status;
        private String decrypted_data;

        public String get_decrypted_data() {
            return decrypted_data;
        }

        public boolean get_decryption_success_status() {
            return decryption_status;
        }

        public aes_data(boolean success_status, String data) {
            decryption_status = success_status;
            decrypted_data = data;
        }

        public aes_data()
        {}
    }
}
