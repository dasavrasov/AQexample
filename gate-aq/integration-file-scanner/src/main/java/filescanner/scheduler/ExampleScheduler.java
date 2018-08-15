package filescanner.scheduler;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import filescanner.model.Credentials;
import filescanner.service.VsmsService;

@Component
@EnableScheduling
public class ExampleScheduler {

	private static final Logger log = LoggerFactory.getLogger(ExampleScheduler.class);

	@Autowired
	DatabaseService databaseService;

	@Value("${main_path}")
	private String mainPath;

	@Value("${archive_path}")
	private String archivePath;

	@Value("${error_path}")
	private String errorPath;

	@Value("${delimiter}")
	private String delimiter;

	@Value("${name_pattern}")
	private String namePattern;

	@Scheduled(cron = "${cron_expr}")
	public void process() {

		log.debug("Начало цикла обработки");

		Path dir = Paths.get(mainPath);
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, namePattern)) {			
			
			for (Path p : stream) {
					try {
						processFile(p);
						try {
							Path moved=moveFile(p, archivePath);
							try {
								deletePasswords(moved);
							} catch (Exception e) {
								log.debug("Ошибка искажения данных в  файле "+moved.getFileName()+" . Файл с таким именем уже есть в архиве "+e.getMessage());								
							}
						} catch (FileAlreadyExistsException e) {							
							log.debug("Ошибка переноса файла "+p.getFileName()+" в архив. Файл с таким именем уже есть в архиве "+e.getMessage());	
						} catch (Exception e) {							
							log.debug("Ошибка переноса файла "+p.getFileName()+" в архив "+e.getMessage());
						}
					} catch (Exception e) {
						try {
							Path moved=moveFile(p, errorPath);
							try {
								deletePasswords(moved);
							} catch (Exception e1) {
								log.debug("Ошибка искажения данных в  файле "+moved.getFileName()+" . Файл с таким именем уже есть в архиве "+e1.getMessage());								
							}
						} catch (Exception e1) {
							log.debug("Ошибка переноса файла "+p.getFileName()+" в каталог ошибок "+e.getMessage());
						}
						log.debug("Ошибка обработки файла "+p.getFileName()+" "+e.getMessage());
					}				
			}
		} catch (IOException e) {
			log.debug("Ошибка чтения каталога "+dir+" "+e.getMessage());
		}		
		
		log.debug("Конец цикла обработки");

	}
	
	/*
	 * перекладывает обработанный файл в директорию ARCHIVE либо в ERROR
	 */
	private Path moveFile(Path p, String target) throws IOException {
		Path targetDir = Paths.get(target);
		return Files.move(p, targetDir.resolve(p.getFileName()));		
	}

	/*
	 * В обработанном файле, который уже лежит в ARCHIVE либо в ERROR удаляет пароли (данные после последнего "|" )
	 */
	void deletePasswords(Path path) throws IOException {
		try (Stream<String> lines = Files.lines(path)) {
			   List<String> replaced = lines
			       .map(line-> line.substring(0,StringUtils.ordinalIndexOf(line,delimiter,2)))
			       .collect(Collectors.toList());
			   Files.write(path, replaced);
			}		
	}
	
	/*
	 * Обработка файла
	 * Читает файл посторчно и на каждую строку вызывает INSERT в БД
	 */
	void processFile(Path path) throws Exception {
		List<Credentials> credList=new ArrayList<>();			
		List<String> strList=Files.readAllLines(path);
		
		int lineNUmber = 0;
		for (String buf : strList) {
			try {
				lineNUmber++;
				StringTokenizer st = new StringTokenizer(buf, delimiter);
				if (st.countTokens()!=3)
					throw new Exception("Ошибка обработки файла " + path.getFileName()+" Номер строки: "+lineNUmber+" : количество полей не равно 3");
				
				credList.add(new Credentials(Long.parseLong(st.nextToken().trim()), st.nextToken().trim(), st.nextToken().trim()));
			} catch (NumberFormatException e) {				
				throw new Exception("Ошибка обработки файла " + path.getFileName()+" Номер строки: "+lineNUmber+" "+e.getMessage());
			}
		}	

		//с файлом все ОК, пишем в базу
		for (Credentials credentials : credList) {
			DatabaseService.insert(credentials);
		}
		
	}	

}
