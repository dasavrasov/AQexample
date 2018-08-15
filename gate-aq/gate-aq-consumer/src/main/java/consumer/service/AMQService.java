package consumer.service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.JmsException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MarshallingMessageConverter;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

import integration.FirstRequestIntegrationModel;
import integration.SecondRequestIntegrationModel;
import integration.StateResponse;

import consumer.config.CustomBasicDataSource;
import consumer.props.ApplicationProperties;

@Service
public class AMQService {

	private static final Logger log = LoggerFactory.getLogger(AMQService.class);

	@Autowired
	DatabaseService databaseService;

	@Autowired
	ApplicationProperties props;

	@Autowired
	JmsTemplate jmsTemplate;

	@Autowired
	MarshallingMessageConverter converter;

	@Value("${outbound.endpoint}")
	private String destinationOut;

	@JmsListener(destination = "${inbound.endpoint}")
	@SendTo("${outbound.endpoint}")
	public void process(Message mess) {

		String str = "";
		try {
			str = ((TextMessage) mess).getText().toString();

			//запрос на генерацию
			if (str.contains("FirstRequestIntegrationModel")) {
				try {
					FirstRequestIntegrationModel request = (FirstRequestIntegrationModel) converter
							.fromMessage(mess);

					processOne(request);
				} catch (MessageConversionException | JMSException e) {
					log.debug("Ошибка парсинга запроса типа FirstRequestIntegrationModel" + e.getMessage());
				} catch (Exception e) {
					log.debug("Ошибка парсинга запроса типа FirstRequestIntegrationModel" + e.getMessage());
				}

			} 
			else 
			//	запрос на перегенерацию
			if (str.contains("SecondRequestIntegrationModel")) {
				try {
					SecondRequestIntegrationModel request = (SecondRequestIntegrationModel) converter
							.fromMessage(mess);

					processOne(request);
				} catch (MessageConversionException | JMSException e) {
					log.debug("Ошибка парсинга запроса типа SecondRequestIntegrationModel" + e.getMessage());
				} catch (Exception e) {
					log.debug("Ошибка парсинга запроса типа SecondRequestIntegrationModel" + e.getMessage());
				}

			} else {
				log.debug("Получен запрос неизвестного формата ");
				log.debug(str);
			}

		} catch (Exception e1) {
			log.debug("Ошибка обрабюотки запроса" + e1.getMessage());
		}
	}

	private void processOne(Object requestObj) {
		if (requestObj instanceof SecondRequestIntegrationModel) {
			SecondRequestIntegrationModel request = ((SecondRequestIntegrationModel)requestObj);
			String docId=request.getDocId();
			String exampleData=request.getExampleRequestData();

			Long codeLiableFace = 0L;			
			codeLiableFace = request.getExtPersonID();
			if (codeLiableFace == 0L) {
				log.info("Ошибка в поле ExtPersonId");
			}		
			processTwo(param1,param2,param3);			
		}	
		else	
		if (requestObj instanceof FirstRequestIntegrationModel){
			FirstRequestIntegrationModel request = ((FirstRequestIntegrationModel)requestObj);
			String docId=request.getDocId();
			String exampleData=request.getExampleRequestData();

			Long codeLiableFace=0L;
			codeLiableFace = request.getExtPersonID();
			if (codeLiableFace == 0L) {
				log.info("Ошибка в поле ExtPersonId");
			}		

			processTwo(docId,exampleData,codeLiableFace);			
		}
		else
			log.debug("Ошибка парсинга. Неизвестный тип запроса");

	}

	private void processTwo(String docId, String exampleData, Long codeLiableFace) {
		log.debug("Получен запрос docId: " + docId);
		/**
		 * Save to AQ
		 */
		StateResponse stateResponse;

		// decode Base64
		String exampleRequestData = null;
		try {
			exampleRequestData = CustomBasicDataSource.base64Decode(exampleData);
			exampleRequestData = exampleRequestData.replace("----------", "");
		} catch (Exception e) {
			log.info("Ошибка в поле exampleRequestData");
		}
		Long var1 = null;
		try {
			var1 = Long.parseLong(props.getParam1());
		} catch (NumberFormatException e) {
			log.info("Ошибка в поле 1");
		}
		String var2 = null;
		try {
			var2 = props.getParam2();
		} catch (Exception e) {
			log.info("Ошибка в поле 2");
		}
		Long var3 = null;
		try {
			var3 = Long.parseLong(props.getParam3());
		} catch (NumberFormatException e) {
			log.info("Ошибка в поле 3");
		}
		Long var4 = null;
		try {
			var4 = Long.parseLong(props.getparam4());
		} catch (NumberFormatException e) {
			log.info("Ошибка в поле 4");
		}
		Long var5 = null;
		try {
			var5 = Long.parseLong(props.getParam5());
		} catch (NumberFormatException e) {
			log.info("Ошибка в поле 5");
		}
		
		stateResponse = databaseServicesaveCert(exampleRequestData, var1, var2, var3, var4,
				var5, var6, var7);

		/**
		 * Return Ticket to AQ
		 */
		// return MessageBuilder.withPayload(stateResponse).build();
		// Кладем ответ в AQ в AQ_in
		try {
			jmsTemplate.convertAndSend(destinationOut, stateResponse);
		} catch (JmsException e) {
			log.info("Ошибка передачи ответного сообщения " + e.getMessage());
		} catch (Exception e) {
			log.info("Ошибка передачи ответного сообщения " + e.getMessage());
		}
	}

}
