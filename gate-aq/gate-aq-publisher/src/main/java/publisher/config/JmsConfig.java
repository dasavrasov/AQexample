package publisher.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MarshallingMessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.bssys.abs.model.response.Response;
import com.bssys.sbns.integration.StateResponse;

@Configuration
public class JmsConfig {
	
	@Bean
	public MarshallingMessageConverter createMarshallingMessageConverter(final Jaxb2Marshaller jaxb2Marshaller) {
		return new MarshallingMessageConverter(jaxb2Marshaller);
	}


	@Bean
	public Jaxb2Marshaller getMarshaller() {
		Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
		jaxb2Marshaller.setClassesToBeBound(Response.class,StateResponse.class);

		final Map<String,Object> map = new HashMap<>();
		map.put("jaxb.formatted.output", true);

		jaxb2Marshaller.setMarshallerProperties(map);
		return jaxb2Marshaller;
	}	

}
 