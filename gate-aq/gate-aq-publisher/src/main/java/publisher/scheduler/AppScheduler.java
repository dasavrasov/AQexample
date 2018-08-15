package publisher.scheduler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import model.response.Response;
import integration.StateResponse;

import publisher.service.DatabaseService;

@Component
@EnableScheduling
public class AppScheduler {

	private static final Logger log = LoggerFactory.getLogger(AppScheduler.class);
	
	@Autowired
	private JmsTemplate jmsTemplate;	

	@Autowired
	DatabaseService databaseService;
	
	@Value("${outbound.endpoint}")
	private String destination;	
	
	@Scheduled(cron="${cron_expr}")
	public void process() {

		log.debug("Начало цикла обработки");
		//Получаем List<Response>
		List<Response> entityList = databaseService.getDataList();
		
		
		//Кладем сообщения в AQ в AQ_in
		for (Response entity : entityList) {
			try {
				jmsTemplate.convertAndSend(destination, entity);
				
				//отправили сообщение с 
				//надо сбросить флаг Other_TO_EXPORT в AQ
				databaseService.deleteExportFlag(entity.getRequestId());
				
				log.debug("Данные выгружен "+entity.getResponseId()+" в очередь "+destination);
			} catch (JmsException e) {
				log.info("Ошибка выгрузки сообщения с  в очередь "+destination+" "+e.getMessage());
			}
		}

		//Получаем List<Response>
		List<StateResponse> refusedList = databaseService.getRefusedList();
		
		
		//Кладем сообщения в AQ в AQ_in
		for (StateResponse response : refusedList) {
			try {
				jmsTemplate.convertAndSend(destination, response);
				
				//отправили сообщение с 
				//надо сбросить флаг Other_TO_EXPORT в AQ
				databaseService.deleteExportFlag(response.getDocId());
				
				log.debug("Отказ выгружен "+response.getDocId()+" в очередь "+destination);
			} catch (JmsException e) {
				log.info("Ошибка выгрузки сообщения об отказе в очередь "+destination+" "+e.getMessage());
			}
		}
		log.debug("Конец цикла обработки");		
		
	}
	
}
