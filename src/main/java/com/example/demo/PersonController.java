package com.example.demo;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RepositoryRestController
@RequestMapping(value = "/person")
@EnableHypermediaSupport(type = HypermediaType.HAL)
public class PersonController {


    // START: Removing this block would fix the issue
    @Autowired
    private PersonRepository personRepository;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<PersistentEntityResource> findOneById(@PathVariable final Long id,
            PersistentEntityResourceAssembler assembler) {
        Optional<Person> person = personRepository.findById(id);
        return person.map(person1 -> new ResponseEntity<>(assembler.toFullResource(person1), HttpStatus.OK))
                .orElse(ResponseEntity.notFound().build());
    }
    // END
}
