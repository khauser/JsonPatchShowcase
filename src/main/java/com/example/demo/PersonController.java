package com.example.demo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RepositoryRestController
public class PersonController {

    public static final String REQUEST_MAPPING = "/person";

    @Autowired
    private PersonRepository personRepository;

    @RequestMapping(value = REQUEST_MAPPING
            + "/search/findByFirstName", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<CollectionModel<PersistentEntityResource>> findByFirstName(@RequestParam String firstName,
            PersistentEntityResourceAssembler assembler) {
        List<Person> persons = personRepository.findByFirstName(firstName);

        if (persons != null) {
            return ResponseEntity.ok(assembler.toCollectionModel(persons));
        }
        return ResponseEntity.ok(null);
    }
}
