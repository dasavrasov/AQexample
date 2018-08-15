package filescanner.service;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import filescanner.model.Credentials;

@Service
public class VsmsService {

	private static final Logger log = LoggerFactory.getLogger(VsmsService.class);
	private static String sql="INSERT INTO EXAMPLE_TABLE (ID, LOGINNAME, PASS) VALUES (?,?,?)";	
			
	@Autowired
	DataSource dataSource;

	@Autowired
	JdbcTemplate jdbcTemplate;

	public void insert(Credentials credentials) throws Exception {		
		try {
			jdbcTemplate.update(sql, credentials.getId(), credentials.getLoginname(), credentials.getPass());
			log.debug("Запись в EXAMPLE_TABLE -> OK. ID: "+credentials.getId());
		} catch (Exception e) {
			log.error(e.getMessage()+" Ошибка Записи в EXAMPLE_TABLE. ID: "+credentials.getId());
			throw new Exception(e.getMessage());
		}		
	}
}
