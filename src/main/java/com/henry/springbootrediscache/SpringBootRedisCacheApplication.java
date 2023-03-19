package com.henry.springbootrediscache;


import com.henry.springbootrediscache.model.Person;
import com.henry.springbootrediscache.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SpringBootRedisCacheApplication implements CommandLineRunner {

	private final Logger LOG = LoggerFactory.getLogger(getClass());

	private final PersonRepository personRepository;

	public SpringBootRedisCacheApplication(PersonRepository personRepository) {
		this.personRepository = personRepository;
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringBootRedisCacheApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

        //Populating embedded database here
		LOG.info("Current user count is {}.", personRepository.count());
		Person p1 = new Person("p1","test", 25);
		Person p2 = new Person("p2", "test",28);
		Person p3 = new Person("p3", "test", 60);

		personRepository.save(p1);
		personRepository.save(p2);
		personRepository.save(p3);
		LOG.info("Data: {}.", personRepository.findAll());

	}
}
