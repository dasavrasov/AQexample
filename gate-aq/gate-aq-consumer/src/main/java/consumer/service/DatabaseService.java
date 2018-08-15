package consumer.service;

import java.sql.Types;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.sql.DataSource;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.StoredProcedure;
import org.springframework.stereotype.Service;

import com.bssys.sbns.integration.StateResponse;

import consumer.props.ApplicationProperties;

@Service
public class DatabaseService {

	private static final Logger log = LoggerFactory.getLogger(DatabaseService.class);

	@Autowired
	DataSource dataSource;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	ApplicationProperties props;
	
	/**
	 * 		PROCEDURE create_request (params)
	 * 
	 */
	public StateResponse saveCert(String exampleRequestData, Long var1, String var2,
			Long var3, Long var4, Long var5, Long var6e, String var7) {
		String dbg = "";

		MyStoredProcedure myStoredProcedure = new MyStoredProcedure(jdbcTemplate,
				"ORA_PROC_NAME");

		dbg = var7+" " + "exampleRequestData:" + exampleRequestData + "\n";

		// Sql parameter mapping
		SqlParameter fvRequest = new SqlParameter("vRequest", Types.VARCHAR);
		SqlParameter fparam1 = new SqlParameter("param1", Types.BIGINT);
		SqlParameter fparam2 = new SqlParameter("param2", Types.VARCHAR);
		SqlParameter fparam3 = new SqlParameter("param3", Types.BIGINT);
		SqlParameter fparam4 = new SqlParameter("param4", Types.BIGINT);
		SqlParameter fparam5 = new SqlParameter("param5", Types.BIGINT);
		SqlParameter fparam6 = new SqlParameter("param6", Types.BIGINT);
		SqlParameter fparam7 = new SqlParameter("param7", Types.VARCHAR);

		SqlParameter[] paramArray = { fvRequest, fvar1, fvar2, fvar3, fvar4, fvar5,
				fvar6e, fvar7 };

		dbg = "Вызов процедуры записи запроса в БД ORA_PROC_NAME" + "\n";
		log.debug(dbg);

		try {
			myStoredProcedure.setParameters(paramArray);
			myStoredProcedure.compile();

			// Call stored procedure
			myStoredProcedure.execute(exampleRequestData, var1, var2, var3, var4, var5,
					var6e, var7);

			dbg = "Выполнена процедура сохранения данных запроса в AQ: "+var7+": ACCEPTED_BY_SYSTEM" + "\n";
			log.debug(dbg);

			StateResponse stateResponse = new StateResponse();
			stateResponse.setDocType(props.getDocType());
			stateResponse.setDocId(var7);
			stateResponse.setExtId(var7);
			stateResponse.setCreateTime(getXmlCurrentDate());
			stateResponse.setState("ACCEPTED_BY_SYSTEM");
			return stateResponse;
			
		} catch (Exception e) {
			log.info("ERROR:DatabaseService:saveCert:" + e.getMessage());
			log.info("DatabaseService:saveCert:Вызов ORA_PROC_NAME:ExtPersonId:"+var6e+" "+var7+": DECLINED_BY_SYSTEM" + "\n");
			StateResponse stateResponse = new StateResponse();
			stateResponse.setDocType(props.getDocType());
			stateResponse.setDocId(var7);
			stateResponse.setExtId(var7);
			stateResponse.setCreateTime(getXmlCurrentDate());
			stateResponse.setMessageOnlyForBank("DatabaseService:saveCert:" + e.getMessage());
			stateResponse.setState("DECLINED_BY_SYSTEM");
			return stateResponse;			
		}

	}

	class MyStoredProcedure extends StoredProcedure {

		public MyStoredProcedure(JdbcTemplate jdbcTemplate, String name) {

			super(jdbcTemplate, name);
			setFunction(false);
		}

	}

	private XMLGregorianCalendar getXmlCurrentDate() {
		Date date = new Date();
		XMLGregorianCalendar xmlDate = null;
		GregorianCalendar gc = new GregorianCalendar();

		gc.setTime(date);

		try {
			xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
			return xmlDate;
		} catch (Exception e) {
			log.error("ERROR:DatabaseService:getXmlCurrentDate:Error converting date");
		}
		return xmlDate;
	}
	
}
