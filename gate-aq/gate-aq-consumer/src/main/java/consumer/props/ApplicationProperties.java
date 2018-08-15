package consumer.props;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
public class ApplicationProperties {

	@Value("${param1}")
	private String param1;

	@Value("${param2}")
	private String param2;

	@Value("${param3}")
	private String param3;

	@Value("${param4}")
	private String param4;

	@Value("${param5}")
	private String param5;

	@Value("${param6}")
	private String param6;

}
