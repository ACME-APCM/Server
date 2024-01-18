package it.unitn.APCM.ACME.DBManager;

import com.fasterxml.jackson.core.JsonProcessingException;

import it.unitn.APCM.ACME.ServerCommon.CryptographyPrimitive;
import it.unitn.APCM.ACME.ServerCommon.JSONToArray;
import it.unitn.APCM.ACME.ServerCommon.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

@RestController
@RequestMapping("/api/v1")
public class DB_RESTInterface {
	// Connection statically instantiated
	private final Connection conn = DB_Connection.getDbconn();
	private static final Logger log = LoggerFactory.getLogger(DB_RESTInterface.class);
	
	/**
	 * Endpoint for list all file for a specific owner
	 */
	@GetMapping("/files")
	public Map<String, String> get_files(@RequestParam(value = "owner") String owner) {
		HashMap<String, String> res = new HashMap<>();
		String selectQuery = "SELECT path_hash, path FROM Files WHERE owner = ?";
		PreparedStatement preparedStatement;
		try {
			preparedStatement = conn.prepareStatement(selectQuery);
			preparedStatement.setString(1, owner);
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {
				res.put("path_hash", rs.getString("path_hash"));
				res.put("path", rs.getString("path"));
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return res;
	}

	/**
	 * Endpoint for getting the password of the specified file if authorized
	 */
	@GetMapping("/decryption_key")
	public ResponseEntity<Response> get_key(@RequestParam(value = "path_hash") String path_hash,
			@RequestParam(value = "email") String email,
			@RequestParam(value = "user_groups") String user_group,
			@RequestParam(value = "admin") String admin,
			@RequestParam(value = "id") int id) {

		Response res = new Response();
		res.set_auth(false);
		res.set_w_mode(false);
		res.set_email(email);
		res.set_path_hash(path_hash);
		res.set_id(id);

		ArrayList<String> user_groups = new ArrayList<String>(Arrays.asList(user_group.split(",")));

		String getInfoQuery = "SELECT path_hash, owner, rw_groups, r_groups FROM Files WHERE path_hash = ?";
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement(getInfoQuery);
			ps.setString(1, path_hash);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				if (rs.isFirst()) {
					if (admin.equals("1")) {
						log.trace("User is an admin");
						res.set_auth(true);
						res.set_w_mode(true);
					} else if (rs.getString("owner").equals(email)) {
						log.trace("User is the owner for the file requested");
						res.set_auth(true);
						res.set_w_mode(true);
					} else {
						ArrayList<String> rw_groups = new JSONToArray(rs.getString("rw_groups"));
						ArrayList<String> r_groups = new JSONToArray(rs.getString("r_groups"));

						for (String g : user_groups) {
							if (rw_groups.contains(g)) {
								res.set_auth(true);
								res.set_w_mode(true);
								break;
							} else if (r_groups.contains(g)) {
								res.set_auth(true);
								res.set_w_mode(false);
								// no break because can be also present after another g in the rw_groups
							}
						}

					}
				} else {
					// more than one result
					// possible collision of hash
					log.error("Found more than one path_hash, possible collision or multiple row for one file");
					throw new ResponseStatusException(HttpStatus.CONFLICT, "HASH collision");
				}
			}
		} catch (SQLException | JsonProcessingException e) {
			throw new RuntimeException(e);
		}

		if (res.get_auth()) {
			// Check ok then return key
			String selectQuery = "SELECT path_hash, encryption_key FROM Files WHERE path_hash = ?";
			try {
				ps = conn.prepareStatement(selectQuery);
				ps.setString(1, path_hash);
				ResultSet rs = ps.executeQuery();

				String encryptionKey = null;

				while (rs.next()) {
					encryptionKey = (rs.getString("encryption_key"));
				}

				if (encryptionKey != "" && encryptionKey != null) {
					System.out.println("CHIAVE CIFRATA LETTURA: " + encryptionKey);
					String k = new String((new CryptographyPrimitive()).decrypt(encryptionKey.getBytes(), DB_RESTApp.masterKey));
					System.out.println("CHIAVE LETTA: " + k);
				 	res.set_key(k);
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		} else {
			res.set_key("noKey");
		}

		HttpHeaders headers = new HttpHeaders();

		ResponseEntity<Response> entity = new ResponseEntity<>(res, headers, HttpStatus.CREATED);

		return entity;
	}

	/**
	 * Endpoint for getting the password of the specified file if authorized
	 */
	@GetMapping("/newFile")
	public ResponseEntity<Response> new_File(@RequestParam(value = "path_hash") String path_hash,
			@RequestParam(value = "path") String path,
			@RequestParam(value = "email") String email,
			@RequestParam(value = "user_groups") String user_group,
			@RequestParam(value = "admin") String admin,
			@RequestParam(value = "id") int id) {

		Response res = new Response();

		boolean error = false;

		String getInfoQuery = "SELECT path_hash FROM Files WHERE path_hash = ?";
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement(getInfoQuery);
			ps.setString(1, path_hash);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				error = true;
				log.error("File already existing");
				throw new ResponseStatusException(HttpStatus.CONFLICT, "File already existing");
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		if(error== false){
			res.set_auth(true);
			res.set_w_mode(true);
			res.set_email(email);
			res.set_path_hash(path_hash);
			res.set_id(id);
			//generate new key 
			SecretKey sK = (new CryptographyPrimitive()).getSymmetricKey();
			System.out.println("CHIAVE CREATA: " + new String(sK.getEncoded()));
			String enc_key = new String((new CryptographyPrimitive()).encrypt(sK.getEncoded(), DB_RESTApp.masterKey));
			System.out.println("CHIAVE CIFRATA: " + enc_key);
			String dec = new String((new CryptographyPrimitive()).decrypt(enc_key.getBytes(), DB_RESTApp.masterKey));
			System.out.println("Decifrata: " + dec);
			res.set_key(new String(sK.getEncoded()));
			
			String insertQuery = "INSERT INTO Files(path_hash, path, owner, rw_groups, r_groups, encryption_key) VALUES (?,?,?,?,?,?)";
			try {
				PreparedStatement prepStatement = conn.prepareStatement(insertQuery);
				prepStatement.setString(1, path_hash);
				prepStatement.setString(2, path);
				prepStatement.setString(3, email);
				prepStatement.setString(4, user_group);
				prepStatement.setString(5, user_group);
				prepStatement.setString(6, enc_key);

				prepStatement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			res = null;
		}

		HttpHeaders headers = new HttpHeaders();

		ResponseEntity<Response> entity = new ResponseEntity<>(res, headers, HttpStatus.CREATED);

		return entity;
	}
}
