package publisher.config;

import java.util.Base64;

import org.apache.commons.dbcp2.BasicDataSource;


public class CustomBasicDataSource extends BasicDataSource {
	
	public synchronized void setPassword(String encryptedPassword){	
		super.setPassword(base64Decode(encryptedPassword));		
   }
	
	/**
	 * @param token
	 * @return encoded
	 */
	public static String base64Encode(String token) {
		
		byte[] encodedBytes = Base64.getEncoder().encode(token.getBytes());
		return new String(encodedBytes);
	}

	/**
	 * @param token
	 * @return
	 */
	public static String base64Decode(String token) {
		byte[] decodedBytes = Base64.getDecoder().decode(token);
	    return new String(decodedBytes);
	}	 	
}
