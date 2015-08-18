package com.jackiezhuang.sgframework.utils.chiper;

import android.util.Base64;

import com.jackiezhuang.sgframework.utils.CommonUtil;
import com.jackiezhuang.sgframework.utils.SGConfig;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;

/**
 * DH非对称加密解密算法类
 * <p/>
 * Created by JackieZhuang on 2015/8/17.
 */
public class DH {
	/**
	 * 默认加密算法
	 */
	private static final String DEFAULT_ALGORITHM = "DH";
	/**
	 * 默认生成键值对的位数
	 */
	private static final int DEFAULT_INIT_SIZE = 1024;
	/**
	 * 默认DH下对称加密算法的加密密钥，可选其它加密方式
	 */
	private static final String DEFAULT_SECRET_ALGORITHM = "DES";

	/**
	 * 加解密实际操作方法
	 *
	 * @param data
	 * @param pubKey
	 * @param prvKey
	 * @param mode
	 * @return
	 */
	private static byte[] doMethod(byte[] data, byte[] pubKey, byte[] prvKey,
	                               int mode) {
		byte[] result = null;
		try {
			SecretKey sKey = getSecretKey(pubKey, prvKey);
			Cipher cipher = Cipher.getInstance(sKey.getAlgorithm());
			cipher.init(mode, sKey);
			result = cipher.doFinal(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 根据传入公私玥构建SecretKey对象
	 *
	 * @param pubKey
	 * @param prvKey
	 * @return
	 */
	private static SecretKey getSecretKey(byte[] pubKey, byte[] prvKey) {
		SecretKey result = null;
		try {
			KeyFactory keyFactory = KeyFactory.getInstance(DEFAULT_ALGORITHM);
			// 初始化公玥
			X509EncodedKeySpec pKeySpec = new X509EncodedKeySpec(pubKey);
			PublicKey pubK = keyFactory.generatePublic(pKeySpec);
			// 初始化私钥
			PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(prvKey);
			PrivateKey prvK = keyFactory.generatePrivate(pkcs8KeySpec);

			KeyAgreement kAgree = KeyAgreement.getInstance(keyFactory
					.getAlgorithm());
			kAgree.init(prvK);
			kAgree.doPhase(pubK, true);
			result = kAgree.generateSecret(DEFAULT_SECRET_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 生成指定大小的公私玥键值对(甲方)
	 *
	 * @param initSize 指定大小
	 * @return 字节二维数组，0下标为私钥字节数组，1下标为公玥字节数组
	 */
	public static byte[][] genAKeys(int initSize) {
		byte[][] result = null;
		try {
			KeyPairGenerator keyGenerator = KeyPairGenerator
					.getInstance(DEFAULT_ALGORITHM);
			keyGenerator.initialize(initSize);
			KeyPair keyPair = keyGenerator.generateKeyPair();
			result = new byte[][]{
					keyPair.getPrivate().getEncoded(),
					keyPair.getPublic().getEncoded()};
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 生成用Base64编码的指定大小的公私玥键值对(甲方)
	 *
	 * @param initSize
	 * @return 字符串数组，0下标为私钥，1下标为公玥
	 */
	public static String[] genAKeysInBase64(int initSize) {
		byte[][] key = genAKeys(initSize);
		return new String[]{Base64.encodeToString(key[0], Base64.DEFAULT), Base64.encodeToString(key[1], Base64
				.DEFAULT)};
	}

	/**
	 * 生成用Base64编码的默认大小的公私玥键值对(甲方)
	 *
	 * @return 字符串数组，0下标为私钥，1下标为公玥
	 */
	public static String[] genAKeysInBase64() {
		return genAKeysInBase64(DEFAULT_INIT_SIZE);
	}

	/**
	 * 生成进行了十六进制转换的指定大小的公私玥键值对(甲方)
	 *
	 * @param initSize
	 * @return 字符串数组，0下标为私钥，1下标为公玥
	 */
	public static String[] genAKeysInHex(int initSize) {
		byte[][] key = genAKeys(initSize);
		return new String[]{CommonUtil.bytes2Hex(key[0]),
				CommonUtil.bytes2Hex(key[1])};
	}

	/**
	 * 生成进行了十六进制转换的默认大小的公私玥键值对(甲方)
	 *
	 * @return 字符串数组，0下标为私钥，1下标为公玥
	 */
	public static String[] genAKeysInHex() {
		return genAKeysInHex(DEFAULT_INIT_SIZE);
	}

	/**
	 * 基于甲方的公玥生成乙方的公私玥键值对(乙方)
	 *
	 * @param ApubKey
	 * @return 字节二维数组，0下标为私钥字节数组，1下标为公玥字节数组
	 */
	public static byte[][] genBKeys(byte[] ApubKey) {
		byte[][] result = null;
		try {
			// 转换甲方的公玥
			X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(ApubKey);
			PublicKey Apub = KeyFactory.getInstance(DEFAULT_ALGORITHM)
					.generatePublic(x509KeySpec);

			// 构建乙方公私玥
			DHParameterSpec dhSpec = ((DHPublicKey) Apub).getParams();
			KeyPairGenerator kGenerator = KeyPairGenerator
					.getInstance(DEFAULT_ALGORITHM);
			kGenerator.initialize(dhSpec);
			KeyPair keyPair = kGenerator.generateKeyPair();
			// 注意点：以下Key用getEncoded方法转为byte数组之前需要先显示转换为DHKey类型才行,
			// 否则后面另一方以该公玥进行再构建会出现定义错误
			result = new byte[][]{
					((DHPrivateKey) keyPair.getPrivate()).getEncoded(),
					((DHPublicKey) keyPair.getPublic()).getEncoded()};
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 基于甲方的公玥生成乙方的公私玥键值对，结果用Base64编码(乙方)
	 *
	 * @param ApubKey
	 * @return 字符串数组，0下标为私钥，1下标为公玥
	 */
	public static String[] genBKeysInBase64(String ApubKey) {
		byte[][] keys = genBKeys(Base64.decode(ApubKey, Base64.DEFAULT));
		return new String[]{Base64.encodeToString(keys[0], Base64.DEFAULT), Base64.encodeToString(keys[1], Base64
				.DEFAULT)};
	}

	/**
	 * 基于甲方的公玥生成乙方的公私玥键值对，结果用Base64编码(乙方)
	 *
	 * @param ApubKey
	 * @return 字符串数组，0下标为私钥，1下标为公玥
	 */
	public static String[] genBKeysInBase64(byte[] ApubKey) {
		byte[][] keys = genBKeys(ApubKey);
		return new String[]{Base64.encodeToString(keys[0], Base64.DEFAULT), Base64.encodeToString(keys[1], Base64
				.DEFAULT)};
	}

	/**
	 * 基于甲方的公玥生成乙方的公私玥键值对，结果转换为十六进制字符串(乙方)
	 *
	 * @param ApubKey
	 * @return 字符串数组，0下标为私钥，1下标为公玥
	 */
	public static String[] genBKeysInHex(String ApubKey) {
		byte[][] keys = genBKeys(CommonUtil.hex2Bytes(ApubKey));
		return new String[]{CommonUtil.bytes2Hex(keys[0]),
				CommonUtil.bytes2Hex(keys[1])};
	}

	/**
	 * 基于甲方的公玥生成乙方的公私玥键值对，结果转换十六进制字符串(乙方)
	 *
	 * @param ApubKey
	 * @return 字符串数组，0下标为私钥，1下标为公玥
	 */
	public static String[] genBKeysInHex(byte[] ApubKey) {
		byte[][] keys = genBKeys(ApubKey);
		return new String[]{CommonUtil.bytes2Hex(keys[0]),
				CommonUtil.bytes2Hex(keys[1])};
	}

	/**
	 * 使用甲方公玥和乙方私钥进行加密
	 *
	 * @param data
	 * @param ApubKey 甲方公玥
	 * @param BprvKey 乙方私钥
	 * @return
	 */
	public static byte[] encrypt(byte[] data, byte[] ApubKey, byte[] BprvKey) {
		return doMethod(data, ApubKey, BprvKey, Cipher.ENCRYPT_MODE);
	}

	/**
	 * 使用甲方公玥和乙方私钥进行加密，并使用Base64进行解码和编码(乙方操作)
	 *
	 * @param data
	 * @param ApubKey 甲方公玥
	 * @param BprvKey 乙方私钥
	 * @param charset
	 * @return
	 */
	public static String encryptInBase64(String data, String ApubKey,
	                                     String BprvKey, String charset) {
		String result = null;
		try {
			result = Base64.encodeToString(encrypt(data.getBytes(charset),
					Base64.decode(ApubKey, Base64.DEFAULT), Base64.decode(BprvKey, Base64.DEFAULT)), Base64.DEFAULT);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 使用甲方公玥和乙方私钥进行加密，并使用Base64进行解码和编码(乙方操作)
	 *
	 * @param data    待加密数据
	 * @param ApubKey 甲方公玥
	 * @param BprvKey 乙方私钥
	 * @return
	 */
	public static String encryptInBase64(String data, String ApubKey,
	                                     String BprvKey) {
		return encryptInBase64(data, ApubKey, BprvKey, SGConfig.DEFAULT_SYS_CHARSET);
	}

	/**
	 * 使用甲方公玥和乙方私钥进行加密，并进行字节数组和十六进制字符串相互转换(乙方操作)
	 *
	 * @param data
	 * @param ApubKey 甲方公玥
	 * @param BprvKey 乙方私钥
	 * @param charset
	 * @return
	 */
	public static String encryptInHex(String data, String ApubKey,
	                                  String BprvKey, String charset) {
		String result = null;
		try {
			result = CommonUtil.bytes2Hex(encrypt(data.getBytes(charset),
					CommonUtil.hex2Bytes(ApubKey), CommonUtil.hex2Bytes(BprvKey)));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 使用甲方公玥和乙方私钥进行加密，并进行字节数组和十六进制字符串相互转换(乙方操作)
	 *
	 * @param data
	 * @param ApubKey 甲方公玥
	 * @param BprvKey 乙方私钥
	 * @return
	 */
	public static String encryptInHex(String data, String ApubKey,
	                                  String BprvKey) {
		return encryptInHex(data, ApubKey, BprvKey, SGConfig.DEFAULT_SYS_CHARSET);
	}

	/**
	 * 使用乙方公玥和甲方私钥进行解密（甲方操作）
	 *
	 * @param data
	 * @param BpubKey 乙方公钥
	 * @param AprvKey 甲方私钥
	 * @return
	 */
	public static byte[] decrypt(byte[] data, byte[] BpubKey, byte[] AprvKey) {
		return doMethod(data, BpubKey, AprvKey, Cipher.DECRYPT_MODE);
	}

	/**
	 * 使用乙方公玥和甲方私钥进行解密，并对字符串和字节数组进行Base64编码和解码(甲方操作)
	 *
	 * @param data
	 * @param BpubKey 乙方公钥
	 * @param AprvKey 甲方私钥
	 * @param charset
	 * @return
	 */
	public static String decryptInBase64(String data, String BpubKey,
	                                     String AprvKey, String charset) {
		String result = null;
		try {
			result = new String(decrypt(Base64.decode(data, Base64.DEFAULT),
					Base64.decode(BpubKey, Base64.DEFAULT), Base64.decode(AprvKey, Base64.DEFAULT)), charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 使用乙方公玥和甲方私钥进行解密，并对字符串和字节数组进行Base64编码和解码(甲方操作)
	 *
	 * @param data
	 * @param BpubKey 乙方公钥
	 * @param AprvKey 甲方私钥
	 * @return
	 */
	public static String decryptInBase64(String data, String BpubKey,
	                                     String AprvKey) {
		return decryptInBase64(data, BpubKey, AprvKey, SGConfig.DEFAULT_SYS_CHARSET);
	}

	/**
	 * 使用乙方公玥和甲方私钥进行解密，并进行字节数组和十六进制字符串相互转换(甲方操作)
	 *
	 * @param data
	 * @param BpubKey 乙方公钥
	 * @param AprvKey 甲方私钥
	 * @param charset
	 * @return
	 */
	public static String decryptInHex(String data, String BpubKey,
	                                  String AprvKey, String charset) {
		String result = null;
		try {
			result = new String(decrypt(CommonUtil.hex2Bytes(data),
					CommonUtil.hex2Bytes(BpubKey), CommonUtil.hex2Bytes(AprvKey)), charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 使用乙方公玥和甲方私钥进行解密，并进行字节数组和十六进制字符串相互转换(甲方操作)
	 *
	 * @param data
	 * @param BpubKey 乙方公钥
	 * @param AprvKey 甲方私钥
	 * @return
	 */
	public static String decryptInHex(String data, String BpubKey,
	                                  String AprvKey) {
		return decryptInHex(data, BpubKey, AprvKey, SGConfig.DEFAULT_SYS_CHARSET);
	}
}
