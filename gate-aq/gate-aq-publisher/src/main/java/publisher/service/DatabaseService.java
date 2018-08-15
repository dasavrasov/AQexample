package publisher.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.StoredProcedure;
import org.springframework.stereotype.Service;

import com.othersystemys.abs.model.response.Response;
import com.othersystemys.sbns.integration.StateResponse;

import publisher.config.CustomBasicDataSource;

@Service
public class DatabaseService {

	private static final Logger log = LoggerFactory.getLogger(DatabaseService.class);
	private static final String DOC_TYPE="FirstRequestIntegrationModel";
	public static final String REFUSED_BY_SYSTEM = "REFUSED_BY_SYSTEM";
	public static final Object DATA_HEADER = "---------";
	public static final Object DATA_FOOTER = "-----END----";

	@Autowired
	JdbcTemplate jdbcTemplate;
	
	public List<Response> getDataList() {

		String dbg="";
		
		String sql = "SELECT DATA_1 entity_data, docid request_id FROM DB_SCHEMA.DATA_REQUEST\r\n" +
				"WHERE PARAM>0";

		try {
			dbg="Запрос успешно обработанных  из DB";
		
			log.debug(dbg);
			List<Response> responses=jdbcTemplate.query(sql, new EntityRowMapper());								

			try {
				dbg="Количество записей с : "+responses.size();
				log.debug(dbg);
			} catch (Exception e) {
				dbg="Количество записей с : 0";
				log.debug(dbg);
			}
			
			if (responses.isEmpty())
				return responses;
			
			return responses;

		} catch (EmptyResultDataAccessException e) {
			dbg="Запрос к БД верул пустой список";
			log.debug(dbg);
			return new ArrayList<Response>();
		} catch (Exception e) {
			log.error("ОШИБКА:DatabaseService:getCertList:"+e.getMessage()); // write log
			return new ArrayList<Response>(); //пусто
		}

	}
	
	private static final class EntityRowMapper implements RowMapper<Response> {
	    @Override
	    public Response mapRow(ResultSet rs, int rowNum) throws SQLException {
	    	StringBuilder sb = new StringBuilder();
	    	Response response = new Response();
	    	
	    	Response.Certificate entityificate=new Response.Certificate();
	    	sb.append(DATA_HEADER);
	    	sb.append("\r\n"); //0D0A
	    	sb.append(rs.getString("entityificate_data"));
//	    	sb.append("\r\n"); //0D0A Непонятно откуда, но 0D0A в конце уже и так есть 
	    	sb.append(DATA_FOOTER);
	    	entityificate.setData(CustomBasicDataSource.base64Encode(sb.toString()));
	    	response.setParam1(entity);
	    	response.setParam2(getXmlCurrentDate());
	    	response.setParam3("1");
	    	response.setParam4("22");
	    	response.setParam5((byte) 1);
	        return response;
	    }
	}

	private static final class RefusedRowMapper implements RowMapper<StateResponse> {
	    @Override
	    public StateResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
	    	StateResponse response = new StateResponse();
	    	response.setDocType(DOC_TYPE);
	    	response.setDocId(rs.getString("docid"));
	    	response.setExtId(rs.getString("docid"));
	    	response.setCreateTime(getXmlCurrentDate());
	    	response.setState(REFUSED_BY_SYSTEM);
	        return response;	        
	    }
	}
	
	private static XMLGregorianCalendar getXmlCurrentDate() {
		Date date = new Date();
		XMLGregorianCalendar xmlDate = null;
		GregorianCalendar gc = new GregorianCalendar();

		gc.setTime(date);

		try {
			xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
			return xmlDate;
		} catch (Exception e) {
			log.error("Ошибка преобразования даты");
		}
		return xmlDate;
	}

	public void deleteExportFlag(String requestId) {

		MyStoredProcedure myStoredProcedure = new MyStoredProcedure(jdbcTemplate,
				"DB_SCHEMA.entity_exported_to_othersystem");

		String dbg = "deleteExportFlag:requestId:" + requestId + "\n";

		// Sql parameter mapping
		SqlParameter frequestId = new SqlParameter("request_Id", Types.VARCHAR);

		// SqlOutParameter fId = new SqlOutParameter("P_ID", Types.VARCHAR);
		SqlParameter[] paramArray = { frequestId };

		try {
			myStoredProcedure.setParameters(paramArray);
			myStoredProcedure.compile();

			// Call stored procedure
			myStoredProcedure.execute(requestId);

			dbg = "Вызов процедуры сброса флага обработки DB_SCHEMA.entity_exported_to_othersystem -> OK";
			log.debug(dbg);
			
		} catch (Exception e) {
			log.info("ERROR:DatabaseService:deleteExportFlag:Ошибка сброса флага обработки в DB" + e.getMessage());
		}

	}

	class MyStoredProcedure extends StoredProcedure {

		public MyStoredProcedure(JdbcTemplate jdbcTemplate, String name) {

			super(jdbcTemplate, name);
			setFunction(false);
		}

	}

	public List<StateResponse> getRefusedList() {

		String dbg="";
		
		String sql = "SELECT docid FROM OARA_TABLE\r\n" +
				"WHERE PARAM>0";

		try {
			dbg="Запрос отрицательных ответов на запрос из DB";
		
			log.debug(dbg);
			List<StateResponse> responses=jdbcTemplate.query(sql, new RefusedRowMapper());								

			try {
				dbg="Количество записей с отказами: "+responses.size();
				log.debug(dbg);
			} catch (Exception e) {
				dbg="Количество записей с отказами: 0";
				log.debug(dbg);
			}
			
			if (responses.isEmpty())
				return responses;
			
			return responses;

		} catch (EmptyResultDataAccessException e) {
			dbg="DatabaseService:getRefusedList:Запрос к БД вернул пустой список";
			log.debug(dbg);
			return new ArrayList<StateResponse>();
		} catch (Exception e) {
			log.error("ОШИБКА:DatabaseService:getRefusedList:"+e.getMessage()); // write log
			return new ArrayList<StateResponse>(); //пусто
		}

	}	
	
}
