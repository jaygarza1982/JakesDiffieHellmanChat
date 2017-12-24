//Purpose of class is to be a client to talk to a server using a diffie hellman key exchange/agreement

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

public class DHClient {
	//Socket to get streams
	private Socket socket;
	
	//Streams for communication
	private InputStream is;
	private OutputStream os;
	
	//Key to encrypt and decrypt messages with AES
	private byte aesKey[];
	
	public DHClient(String IP, int port) throws Exception {
		//Connect to server with IP and port number
		socket = new Socket(IP, port);
		
		//Set up streams for communication
		is = socket.getInputStream();
		os = socket.getOutputStream();
		
		//We will read the servers public key
		byte buffer[] = new byte[4096];
		int numberOfBytesRead = is.read(buffer);

		//Only copy what we got into new array TODO: might be a problem, check later
		byte serversPublicKey[] = Arrays.copyOfRange(buffer, 0, numberOfBytesRead);

		//Bob has received Alice's public key, he makes a DH key from this.
		KeyFactory bobKeyFac = KeyFactory.getInstance("DH");
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(serversPublicKey);

		PublicKey alicePubKey = bobKeyFac.generatePublic(x509KeySpec);

		//Bob creates his own DH key pair
		//Bob uses the same parameters to generate a key of his own
		DHParameterSpec dhParamFromAlicePubKey = ((DHPublicKey)alicePubKey).getParams();

		//Bob makes his own DK key pair
		KeyPairGenerator bobKpairGen = KeyPairGenerator.getInstance("DH");
		bobKpairGen.initialize(dhParamFromAlicePubKey);
		KeyPair bobKpair = bobKpairGen.generateKeyPair();

		// Bob creates and initializes his DH KeyAgreement object
		//System.out.println("BOB: Initialization ...");
		KeyAgreement bobKeyAgree = KeyAgreement.getInstance("DH");
		bobKeyAgree.init(bobKpair.getPrivate());

		// Bob encodes his public key, and sends it over to Alice.
		byte[] ourPublicKey = bobKpair.getPublic().getEncoded();

		//Send our public key to server
		os.write(ourPublicKey);
		os.flush();

		//Next phase of key agreement (last)
		bobKeyAgree.doPhase(alicePubKey, true);


		byte[] bobSharedSecret = new byte[4096];
		bobKeyAgree.generateSecret(bobSharedSecret, 0);

		//This is our key we will use for AES encryption
		aesKey = Arrays.copyOf(bobSharedSecret, 16);
		
		//byte[] bobsKey = Arrays.copyOf(bobSharedSecret, 16);
	}
	
	//Sends a message to the server
	public void send(String toSend) throws Exception {
		//Send encrypted message to the server
		os.write(encrypt(toSend.getBytes("UTF-8"), aesKey));
		os.flush();
	}
	
	//Receive a message from the server
	//This will block IO, you should run on a different thread
	public String receive() throws Exception {
		byte formServer[] = new byte[4096];
		int bytesRead = is.read(formServer);
		
		//Copy only the amount of bytes actually sent
		byte exactFromServer[] = Arrays.copyOf(formServer, bytesRead);
		return new String(decrypt(exactFromServer, aesKey));
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