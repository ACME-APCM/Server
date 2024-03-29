package it.unitn.APCM.ACME.DBManager;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.junit.runner.RunWith;

import it.unitn.APCM.ACME.ServerCommon.CryptographyPrimitive;
import it.unitn.APCM.ACME.ServerCommon.Response;
import it.unitn.APCM.ACME.ServerCommon.SecureRestTemplateConfig;

/**
 * The type Db manager testing.
 */
@RunWith(SpringRunner.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
public class DBManagerTesting {
	/**
	 * The Fixed url for the DBManager in testing mode.
	 */
	String fixedUrl = "https://localhost:50880/api/v1/";
	/**
	 * The Rest.
	 */
	RestTemplate rest = (new SecureRestTemplateConfig("Guard_keystore.jks", "GuardC_truststore.jks")).secureRestTemplate();
	/**
	 * The Path.
	 */
	String path, /**
	 * The Email.
	 */
	email, /**
	 * The R groups.
	 */
	r_groups, /**
	 * The Rw groups.
	 */
	rw_groups, /**
	 * The User groups.
	 */
	user_groups, /**
	 * The Path hash.
	 */
	path_hash, /**
	 * The File hash.
	 */
	file_hash, /**
	 * The Url.
	 */
	url = "";

	/**
	 * Test create first file.
	 *
	 * @throws Exception the exception
	 */
	@Test
    @Order(1)
    public void testCreateFirstFile() throws Exception  {
        path = "test1.txt";
        email = "user@acme.local";
        r_groups = "hr,students";
        rw_groups = "hr";
        path_hash = (new CryptographyPrimitive()).getHash(path.getBytes());
        url = fixedUrl + "newFile?" +
            "path_hash=" + path_hash +
            "&path=" + path +
            "&email=" + email +
            "&r_groups=" + r_groups +
			"&rw_groups=" + rw_groups;

        ResponseEntity<String> res = null;
        
        try{
            res = rest.postForEntity(url, null, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e){
            res = new ResponseEntity<>(e.getStatusCode());
        }
            
        Assertions.assertEquals(201, res.getStatusCode().value());
    }

	/**
	 * Test create second file.
	 *
	 * @throws Exception the exception
	 */
	@Test
    @Order(2)
    public void testCreateSecondFile() throws Exception  {
        path = "test2.txt";
        email = "user@acme.local";
        r_groups = "hr,students";
        rw_groups = "hr";
        path_hash = (new CryptographyPrimitive()).getHash(path.getBytes());
        url = fixedUrl + "newFile?" +
            "path_hash=" + path_hash +
            "&path=" + path +
            "&email=" + email +
            "&r_groups=" + r_groups +
			"&rw_groups=" + rw_groups;

        ResponseEntity<String> res = null;
        
        try{
            res = rest.postForEntity(url, null, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e){
            res = new ResponseEntity<>(e.getStatusCode());
        }
            
        Assertions.assertEquals(201, res.getStatusCode().value());
    }

	/**
	 * Test create file already existing.
	 *
	 * @throws Exception the exception
	 */
	@Test
    @Order(3)
    public void testCreateFileAlreadyExisting() throws Exception  {
        path = "test1.txt";
        email = "user@acme.local";
        r_groups = "hr,students";
        rw_groups = "hr";
        path_hash = (new CryptographyPrimitive()).getHash(path.getBytes());
        url = fixedUrl + "newFile?" +
                "path_hash=" + path_hash +
            "&path=" + path +
            "&email=" + email +
            "&r_groups=" + r_groups +
			"&rw_groups=" + rw_groups;

        ResponseEntity<String> res = null;
        
        try{
            res = rest.postForEntity(url, null, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e){
            res = new ResponseEntity<>(e.getStatusCode());
        }
    
        Assertions.assertEquals(409, res.getStatusCode().value());
    }

	/**
	 * Test create file bad request.
	 *
	 * @throws Exception the exception
	 */
	@Test
    @Order(4)
    public void testCreateFileBadRequest() throws Exception  {
        url = fixedUrl + "newFile?";

        ResponseEntity<String> res = null;

        try{ 
            res = rest.postForEntity(url, null, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e){
            res = new ResponseEntity<>(e.getStatusCode());
        }

        Assertions.assertEquals(400, res.getStatusCode().value());
    }

	/**
	 * Test get decryption key using admin.
	 *
	 * @throws Exception the exception
	 */
	@Test
    @Order(5)
    public void testGetDecryptionKeyAdmin() throws Exception  {
        path = "test1.txt";
        file_hash = "";
        email = "teacher@acme.local";
        user_groups = "teacher";
        path_hash = (new CryptographyPrimitive()).getHash(path.getBytes());
        url = fixedUrl + "decryption_key?" +
            "path_hash=" + path_hash +
            "&file_hash=" + file_hash +
            "&email=" + email +
            "&user_groups=" + user_groups +
			"&admin=" + 1;

        ResponseEntity<Response> res = null;
        
        try{
            res = rest.getForEntity(url, Response.class);
        } catch (HttpClientErrorException | HttpServerErrorException e){
            res = new ResponseEntity<>(e.getStatusCode());
        }
            
        Assertions.assertEquals(200, res.getStatusCode().value());
        //Check if he can read
		Assertions.assertTrue(res.getBody().get_auth());
        //Check if he can write
		Assertions.assertTrue(res.getBody().get_w_mode());
    }

	/**
	 * Test get decryption key using owner.
	 *
	 * @throws Exception the exception
	 */
	@Test
    @Order(6)
    public void testGetDecryptionKeyOwner() throws Exception  {
        path = "test1.txt";
        file_hash = "";
        email = "user@acme.local";
        user_groups = "user";
        path_hash = (new CryptographyPrimitive()).getHash(path.getBytes());
        url = fixedUrl + "decryption_key?" +
            "path_hash=" + path_hash +
            "&file_hash=" + file_hash +
            "&email=" + email +
            "&user_groups=" + user_groups +
			"&admin=" + 0;

        ResponseEntity<Response> res = null;
        
        try{
            res = rest.getForEntity(url, Response.class);
        } catch (HttpClientErrorException | HttpServerErrorException e){
            res = new ResponseEntity<>(e.getStatusCode());
        }
            
        Assertions.assertEquals(200, res.getStatusCode().value());
        //Check if he can read
		Assertions.assertTrue(res.getBody().get_auth());
        //Check if he can write
		Assertions.assertTrue(res.getBody().get_w_mode());
    }

	/**
	 * Test get decryption key using authorized write user.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Order(7)
    public void testGetDecryptionKeyAuthorizedWriteUser() throws Exception  {
        path = "test1.txt";
        file_hash = "";
        email = "student@acme.local";
        user_groups = "hr";
        path_hash = (new CryptographyPrimitive()).getHash(path.getBytes());
        url = fixedUrl + "decryption_key?" +
            "path_hash=" + path_hash +
            "&file_hash=" + file_hash +
            "&email=" + email +
            "&user_groups=" + user_groups +
			"&admin=" + 0;

        ResponseEntity<Response> res = null;

        try{
            res = rest.getForEntity(url, Response.class);
        } catch (HttpClientErrorException | HttpServerErrorException e){
            res = new ResponseEntity<>(e.getStatusCode());
        }

        Assertions.assertEquals(200, res.getStatusCode().value());
        //Check if he can read
        Assertions.assertEquals(true, res.getBody().get_auth());
        //Check if he can write
        Assertions.assertEquals(true, res.getBody().get_w_mode());
    }

	/**
	 * Test get decryption key using authorized read user.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Order(8)
    public void testGetDecryptionKeyAuthorizedReadUser() throws Exception  {
        path = "test1.txt";
        file_hash = "";
        email = "student@acme.local";
        user_groups = "students";
        path_hash = (new CryptographyPrimitive()).getHash(path.getBytes());
        url = fixedUrl + "decryption_key?" +
            "path_hash=" + path_hash +
            "&file_hash=" + file_hash +
            "&email=" + email +
            "&user_groups=" + user_groups +
			"&admin=" + 0;

        ResponseEntity<Response> res = null;

        try{
            res = rest.getForEntity(url, Response.class);
        } catch (HttpClientErrorException | HttpServerErrorException e){
            res = new ResponseEntity<>(e.getStatusCode());
        }

        Assertions.assertEquals(200, res.getStatusCode().value());
        //Check if he can read
		Assertions.assertTrue(res.getBody().get_auth());
        //Check if he cannot write
		Assertions.assertFalse(res.getBody().get_w_mode());
    }

	/**
	 * Test get decryption key using unauthorized user.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Order(9)
    public void testGetDecryptionKeyUnauthorizedUser() throws Exception  {
        path = "test1.txt";
        file_hash = "";
        email = "teacher@acme.local";
        user_groups = "teacher";
        path_hash = (new CryptographyPrimitive()).getHash(path.getBytes());
        url = fixedUrl + "decryption_key?" +
            "path_hash=" + path_hash +
            "&file_hash=" + file_hash +
            "&email=" + email +
            "&user_groups=" + user_groups +
			"&admin=" + 0;

        ResponseEntity<Response> res = null;

        try{
            res = rest.getForEntity(url, Response.class);
        } catch (HttpClientErrorException | HttpServerErrorException e){
            res = new ResponseEntity<>(e.getStatusCode());
        }

        Assertions.assertEquals(401, res.getStatusCode().value());
    }

	/**
	 * Test get decryption key using corrupted file.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Order(10)
    public void testGetDecryptionKeyCorruptedFile() throws Exception  {
        path = "test1.txt";
        file_hash = "WrongHash";
        email = "teacher@acme.local";
        user_groups = "teacher";
        path_hash = (new CryptographyPrimitive()).getHash(path.getBytes());
        url = fixedUrl + "decryption_key?" +
            "path_hash=" + path_hash +
            "&file_hash=" + file_hash +
            "&email=" + email +
            "&user_groups=" + user_groups +
			"&admin=" + 1;

        ResponseEntity<Response> res = null;

        try{
            res = rest.getForEntity(url, Response.class);
        } catch (HttpClientErrorException | HttpServerErrorException e){
            res = new ResponseEntity<>(e.getStatusCode());
        }

        Assertions.assertEquals(412, res.getStatusCode().value());
    }

	/**
	 * Test get decryption key using wrong path_hash.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Order(11)
    public void testGetDecryptionKeyWrongPathHash() throws Exception  {
        path = "NotExistingPath.txt";
        file_hash = "";
        email = "teacher@acme.local";
        user_groups = "teacher";
        path_hash = (new CryptographyPrimitive()).getHash(path.getBytes());
        url = fixedUrl + "decryption_key?" +
            "path_hash=" + path_hash +
            "&file_hash=" + file_hash +
            "&email=" + email +
            "&user_groups=" + user_groups +
			"&admin=" + 1;

        ResponseEntity<Response> res = null;

        try{
            res = rest.getForEntity(url, Response.class);
        } catch (HttpClientErrorException | HttpServerErrorException e){
            res = new ResponseEntity<>(e.getStatusCode());
        }

        Assertions.assertEquals(500, res.getStatusCode().value());
    }

	/**
	 * Test get decryption key bad request.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Order(12)
    public void testGetDecryptionKeyBadRequest() throws Exception  {
        url = fixedUrl + "decryption_key";

        ResponseEntity<Response> res = null;

        try{
            res = rest.getForEntity(url, Response.class);
        } catch (HttpClientErrorException | HttpServerErrorException e){
            res = new ResponseEntity<>(e.getStatusCode());
        }

        Assertions.assertEquals(400, res.getStatusCode().value());
    }

	/**
	 * Test save file using owner.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Order(13)
    public void testSaveFileOwner() throws Exception  {
        path = "test1.txt";
        file_hash = "newFileHash";
        email = "user@acme.local";
        user_groups = "teacher";
        path_hash = (new CryptographyPrimitive()).getHash(path.getBytes());
        url = fixedUrl + "saveFile?" +
            "path_hash=" + path_hash +
            "&file_hash=" + file_hash +
            "&email=" + email +
            "&user_groups=" + user_groups +
			"&admin=" + 0;
            
        ResponseEntity<String> res = null;

        try{
            res = rest.postForEntity(url, null, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e){
            res = new ResponseEntity<>(e.getStatusCode());
        }

        Assertions.assertEquals(200, res.getStatusCode().value());
    }

	/**
	 * Test save file using admin.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Order(14)
    public void testSaveFileAdmin() throws Exception  {
        path = "test1.txt";
        file_hash = "newFileHash";
        email = "admin@acme.local";
        user_groups = "teacher";
        path_hash = (new CryptographyPrimitive()).getHash(path.getBytes());
        url = fixedUrl + "saveFile?" +
            "path_hash=" + path_hash +
            "&file_hash=" + file_hash +
            "&email=" + email +
            "&user_groups=" + user_groups +
			"&admin=" + 1;
            
        ResponseEntity<String> res = null;

        try{
            res = rest.postForEntity(url, null, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e){
            res = new ResponseEntity<>(e.getStatusCode());
        }

        Assertions.assertEquals(200, res.getStatusCode().value());
    }

	/**
	 * Test save using authorized user.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Order(15)
    public void testSaveAuthorizedUser() throws Exception  {
        path = "test1.txt";
        file_hash = "newFileHash";
        email = "user2@acme.local";
        user_groups = "hr";
        path_hash = (new CryptographyPrimitive()).getHash(path.getBytes());
        url = fixedUrl + "saveFile?" +
            "path_hash=" + path_hash +
            "&file_hash=" + file_hash +
            "&email=" + email +
            "&user_groups=" + user_groups +
			"&admin=" + 0;
            
        ResponseEntity<String> res = null;

        try{
            res = rest.postForEntity(url, null, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e){
            res = new ResponseEntity<>(e.getStatusCode());
        }

        Assertions.assertEquals(200, res.getStatusCode().value());
    }

	/**
	 * Test save using unauthorized user.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Order(15)
    public void testSaveUnAuthorizedUser() throws Exception  {
        path = "test1.txt";
        file_hash = "newFileHash";
        email = "user3@acme.local";
        user_groups = "students";
        path_hash = (new CryptographyPrimitive()).getHash(path.getBytes());
        url = fixedUrl + "saveFile?" +
            "path_hash=" + path_hash +
            "&file_hash=" + file_hash +
            "&email=" + email +
            "&user_groups=" + user_groups +
			"&admin=" + 0;
            
        ResponseEntity<String> res = null;

        try{
            res = rest.postForEntity(url, null, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e){
            res = new ResponseEntity<>(e.getStatusCode());
        }

        Assertions.assertEquals(401, res.getStatusCode().value());
    }

	/**
	 * Test save file using wrong path_hash.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Order(16)
    public void testSaveFileWrongPathHash() throws Exception  {
        path = "ThisIsNotAnExistingPath.txt";
        file_hash = "newFileHash";
        email = "user@acme.local";
        user_groups = "students";
        path_hash = (new CryptographyPrimitive()).getHash(path.getBytes());
        url = fixedUrl + "saveFile?" +
            "path_hash=" + path_hash +
            "&file_hash=" + file_hash +
            "&email=" + email +
            "&user_groups=" + user_groups +
			"&admin=" + 0;

        ResponseEntity<String> res = null;

        try{
            res = rest.postForEntity(url, null, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e){
            res = new ResponseEntity<>(e.getStatusCode());
        }

        Assertions.assertEquals(401, res.getStatusCode().value());
    }

	/**
	 * Test save file bad request.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Order(15)
    public void testSaveFileBadRequest() throws Exception  {
        url = fixedUrl + "saveFile";

        ResponseEntity<String> res = null;

        try{
            res = rest.postForEntity(url, null, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e){
            res = new ResponseEntity<>(e.getStatusCode());
        }

        Assertions.assertEquals(400, res.getStatusCode().value());
    }

	/**
	 * Test delete file owner.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Order(16)
    public void testDeleteFileOwner() throws Exception  {
        path = "test3.txt";
        email = "user@acme.local";
        r_groups = "hr,students";
        rw_groups = "hr";
        path_hash = (new CryptographyPrimitive()).getHash(path.getBytes());
        url = fixedUrl + "newFile?" +
            "path_hash=" + path_hash +
            "&path=" + path +
            "&email=" + email +
            "&r_groups=" + r_groups +
			"&rw_groups=" + rw_groups;

        ResponseEntity<String> res = null;

        try{
            res = rest.postForEntity(url, null, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e){
            res = new ResponseEntity<>(e.getStatusCode());
        }

        url = fixedUrl + "deleteFile?" +
            "path_hash=" + path_hash +
            "&email=" + email +
            "&user_groups=hr" +
			"&admin=" + 0;

        res = null;

        try{
            res = rest.exchange(url, HttpMethod.DELETE, null, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e){
            res = new ResponseEntity<>(e.getStatusCode());
        }

        Assertions.assertEquals(200, res.getStatusCode().value());
    }

	/**
	 * Test delete not existing file.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Order(17)
    public void testDeleteNotExistingFile() throws Exception  {
        path = "test4.txt";
        email = "user@acme.local";
        user_groups = "hr";
        path_hash = (new CryptographyPrimitive()).getHash(path.getBytes());
        url = fixedUrl + "deleteFile?" +
            "path_hash=" + path_hash +
            "&email=" + email +
            "&user_groups=" + user_groups +
			"&admin=" + 1;

        ResponseEntity<String> res = null;


        try{
            res = rest.exchange(url, HttpMethod.DELETE, null, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e){
            res = new ResponseEntity<>(e.getStatusCode());
        }

        Assertions.assertEquals(401, res.getStatusCode().value());
    }

	/**
	 * Test delete file bad request.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Order(18)
    public void testDeleteFileBadRequest() throws Exception  {
        url = fixedUrl + "deleteFile";

        ResponseEntity<String> res = null;

        try{
            res = rest.exchange(url, HttpMethod.DELETE, null, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e){
            res = new ResponseEntity<>(e.getStatusCode());
        }

        Assertions.assertEquals(400, res.getStatusCode().value());
    }

	/**
	 * Delete all file created during the test.
	 *
	 * @throws Exception the exception
	 */
	@AfterAll
    public static void deleteAllFile()throws Exception  {
        String fixedUrl = "https://localhost:50880/api/v1/";
        RestTemplate rest = (new SecureRestTemplateConfig("Guard_keystore.jks", "GuardC_truststore.jks")).secureRestTemplate();
        String path = "test1.txt";
        String email = "user@acme.local";
        String user_groups = "hr";
        String path_hash = (new CryptographyPrimitive()).getHash(path.getBytes());
        String url = fixedUrl + "deleteFile?" +
            "path_hash=" + path_hash +
            "&email=" + email +
            "&user_groups=" + user_groups +
			"&admin=" + 0;

        try{
            rest.exchange(url, HttpMethod.DELETE, null, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException ignored){
        }

        path = "test2.txt";
        path_hash = (new CryptographyPrimitive()).getHash(path.getBytes());
        url = fixedUrl + "deleteFile?" +
            "path_hash=" + path_hash +
            "&email=" + email +
            "&user_groups=" + user_groups +
			"&admin=" + 0;

        try{
            rest.exchange(url, HttpMethod.DELETE, null, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e){
        }

    }
}


