package com.henry.springbootrediscache.controller;

import com.henry.springbootrediscache.model.Person;
import com.henry.springbootrediscache.repository.PersonRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
public class PersonController {

    private final RedisTemplate<String, Object> redisTemplate;

    private final PersonRepository PersonRepository;

    public PersonController(RedisTemplate<String, Object> redisTemplate, PersonRepository PersonRepository) {
        this.redisTemplate = redisTemplate;
        this.PersonRepository = PersonRepository;
    }

    @GetMapping("/personByRedisTemplate")
    public Person getPersonByRedisTemplate(){
        var person =  (Person) redisTemplate.opsForValue().get("personId");
        return person!=null?person:null;
    }

    @Cacheable(value = "persons", key = "#personId", unless = "#result.age < 29")
    @GetMapping("/{personId}")
    public Person getPerson(@PathVariable Long personId) {

       var person = PersonRepository.findById(personId).get();
       redisTemplate.opsForValue().set("personId", person);
       return person;

    }

    @CachePut(value = "persons", key = "#person.id")
    @PutMapping("/update")
    public Person updatePersonByID(@RequestBody Person person) {
        PersonRepository.save(person);
        return person;
    }

    @CacheEvict(value = "persons", allEntries=true)
    @DeleteMapping("/{personId}")
    public void deletePersonByID(@PathVariable Long personId) {
       PersonRepository.delete(new Person(personId));
    }
}
