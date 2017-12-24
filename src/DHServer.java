//Purpose of class is to be a server to talk to a client using a diffie hellman key exchange/agreement

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

public class DHServer {
	//Socket to get streams
	private ServerSocket server;
	
	//Streams for communication
	private InputStream is;
	private OutputStream os;
	
	//Key to encrypt and decrypt messages with AES
	private byte aesKey[];
	
	public DHServer(int port) throws Exception {
		//Connect to server with IP and port number
		server = new ServerSocket(port);
		
		//Wait for client
		Socket socketFromServer = server.accept();
		
		//Set up streams for communication
		is = socketFromServer.getInputStream();
		os = socketFromServer.getOutputStream();
		//Alice creates her own DH key pair with 2048-bit key size
		//Make a key pair for Alice with the size of 2048 bits
		KeyPairGenerator aliceKpairGen = KeyPairGenerator.getInstance("DH");
		aliceKpairGen.initialize(2048);
		KeyPair aliceKpair = aliceKpairGen.generateKeyPair();

		// Alice creates and initializes her DH KeyAgreement object
		KeyAgreement aliceKeyAgree = KeyAgreement.getInstance("DH");
		aliceKeyAgree.init(aliceKpair.getPrivate());

		//Alice will send this encoded version of her public key to Bob
		byte[] alicePubKeyEnc = aliceKpair.getPublic().getEncoded();

		//Send public key to client
		os.write(alicePubKeyEnc);
		os.flush();

		//We will read the clients public key
		byte buffer[] = new byte[4096];
		int numberOfBytesRead = is.read(buffer);

		//Only copy what we got into new array TODO: might be a problem, check later
		byte clientsPublicKey[] = Arrays.copyOfRange(buffer, 0, numberOfBytesRead);

		//Use the clients public key
		KeyFactory aliceKeyFac = KeyFactory.getInstance("DH");
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(clientsPublicKey);
		PublicKey bobPubKey = aliceKeyFac.generatePublic(x509KeySpec);
		
		//Next phase of key agreement (last)
		aliceKeyAgree.doPhase(bobPubKey, true);

		byte[] aliceSharedSecret = new byte[4096];

		try {
			aliceSharedSecret = aliceKeyAgree.generateSecret();
		} catch (Exception e) {
			e.printStackTrace();
		}

		//This is the key will use for AES encryption
		aesKey = Arrays.copyOf(aliceSharedSecret, 16);
		

	}
	
	//Sends a message to the client
	public void send(String toSend) throws Exception {
		//Send encrypted message to the client
		os.write(encrypt(toSend.getBytes("UTF-8"), aesKey));
		os.flush();
	}
	
	//Receive a message from the client
	//This will block IO, you should run on a different thread
	public String receive() throws Exception {
		byte fromClient[] = new byte[4096];
		int bytesRead = is.read(fromClient);

		//Copy only the amount of bytes actually sent
		byte exactFromClient[] = Arrays.copyOf(fromClient, bytesRead);
		return new String(decrypt(exactFromClient, aesKey));
	}
	
	public static byte[] encrypt(byte bytesToEncrypt[], byte key[]) throws Exception {
		//Create secret key spec object that contains our passed in key
		Key secretKeySpec = new SecretKeySpec(key, "AES");
		//Get an AES cipher and let it use our secretKeySpec variable as a key
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

		//Return the encrypted bytes
		return cipher.doFinal(bytesToEncrypt);
	}

	public static byte[] decrypt(byte bytesToDecrypt[], byte key[]) throws Exception {
		//Create secret key spec object that contains our passed in key
		Key secretKeySpec = new SecretKeySpec(key, "AES");
		//Get an AES cipher and let it use our secretKeySpec variable as a key
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

		//Return the decrypted bytes
		return cipher.doFinal(bytesToDecrypt);
	}
}