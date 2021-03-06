package com.scapelog.client.loader.codec;

import com.scapelog.client.loader.CryptographyException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author Sean
 */
public final class AESCodec extends Codec<byte[]> {

	/**
	 * The key for the {@link SecretKeySpec}.
	 */
	private final ByteBuffer keyBuffer;

	/**
	 * The Iv for the {@link IvParameterSpec}.
	 */
	private final ByteBuffer ivBuffer;

	/**
	 * Creates a new {@link AESCodec}.
	 * @param keyBuffer The {@link SecretKeySpec} key.
	 * @param ivBuffer The {@link IvParameterSpec} iv.
	 */
	public AESCodec(ByteBuffer keyBuffer, ByteBuffer ivBuffer) {
		this.keyBuffer = keyBuffer;
		this.ivBuffer = ivBuffer;
	}

	@Override
	public byte[] read(InputStream is) throws IOException {
		try {
			return decode(readAndCloseInputStream(is));
		} catch (CryptographyException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void save(byte[] data, OutputStream out) throws IOException {
		try {
			try {
				out.write(encode(data));
			} catch (CryptographyException e) {
				e.printStackTrace();
			}
		} finally {
			out.close();
		}
	}

	/**
	 * Decodes the AES encryption.
	 * @param data The data to decode.
	 * @return The decoded data.
	 * @throws CryptographyException The exception thrown if a exception occurs with the AES cryptography.
	 */
	private byte[] decode(byte[] data) throws CryptographyException {
		try {
			Cipher cipherObject = Cipher.getInstance("AES/CBC/PKCS5Padding");
			SecretKeySpec secretKeySpec = new SecretKeySpec(keyBuffer.array(), "AES");
			cipherObject.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(ivBuffer.array()));
			return cipherObject.doFinal(data);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException e) {
			throw new CryptographyException(e);
		}
	}

	/**
	 * Encodes a byte array with AES encryption.
	 * @param data The data to encode.
	 * @return The decoded data.
	 * @throws CryptographyException The exception thrown if a exception occurs with the AES cryptography.
	 */
	private byte[] encode(byte[] data) throws CryptographyException {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			SecretKeySpec secretKeySpec = new SecretKeySpec(keyBuffer.array(), "AES");
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(ivBuffer.array()));
			return cipher.doFinal(data);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException e) {
			throw new CryptographyException(e);
		}
	}
}