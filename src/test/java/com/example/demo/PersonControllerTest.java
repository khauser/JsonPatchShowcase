package com.example.demo;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.data.rest.webmvc.RestMediaTypes.JSON_PATCH_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.TimeZone;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.SneakyThrows;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PersonControllerTest {

    private static final Long EXPECTED_PERSON_ID = 1L;
    private static final String EXPECTED_PERSON_FIRST_NAME = "Santa";
    private static final String EXPECTED_PERSON_LAST_NAME = "Clause";
    private static final String EXPECTED_PERSON_BIRTHDAY = "1000-12-24";

    private static final String EXPECTED_PERSON_FIRST_NAME_2 = "Max";
    private static final String EXPECTED_PERSON_LAST_NAME_2 = "Muster";

    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void init() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    private String patchPayload;

    @BeforeAll
    static void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Test
    public void test_findAllByFirstName() throws Exception {
        Person person = new Person();
        person.setFirstName(EXPECTED_PERSON_FIRST_NAME);
        person.setLastName(EXPECTED_PERSON_LAST_NAME);
        person.setBirthday(LocalDate.parse(EXPECTED_PERSON_BIRTHDAY, DATE_FORMAT));

        mvc.perform(MockMvcRequestBuilders.post(PersonController.REQUEST_MAPPING)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(person)))
                .andDo(print()).andExpect(status().isCreated());

        mvc.perform(MockMvcRequestBuilders.get(
                PersonController.REQUEST_MAPPING + "/search/findByFirstName?firstName=" + EXPECTED_PERSON_FIRST_NAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.person[0]._links.self.href", is("http://localhost/person/1")))
                .andExpect(jsonPath("$._embedded.person[0].firstName", is(EXPECTED_PERSON_FIRST_NAME)))
                .andExpect(jsonPath("$._embedded.person[0].birthday", is(EXPECTED_PERSON_BIRTHDAY)));
    }

    @Test
    public void test_patch() throws Exception {
        Person person = new Person();
        person.setFirstName(EXPECTED_PERSON_FIRST_NAME);
        person.setLastName(EXPECTED_PERSON_LAST_NAME);
        person.setBirthday(LocalDate.parse(EXPECTED_PERSON_BIRTHDAY, DATE_FORMAT));

        mvc.perform(MockMvcRequestBuilders.post(PersonController.REQUEST_MAPPING)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(person))).andDo(print())
                .andExpect(status().isCreated());

        mvc.perform(MockMvcRequestBuilders.get(PersonController.REQUEST_MAPPING + "/{id}", EXPECTED_PERSON_ID)
                .accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value(EXPECTED_PERSON_FIRST_NAME))
                .andExpect(jsonPath("$.lastName").value(EXPECTED_PERSON_LAST_NAME));

        givenPatchItem();

        mvc.perform(MockMvcRequestBuilders.patch(PersonController.REQUEST_MAPPING + "/" + EXPECTED_PERSON_ID)
                .contentType(JSON_PATCH_JSON).content(patchPayload)).andDo(print()).andExpect(status().isNoContent());

        mvc.perform(MockMvcRequestBuilders.get("/person/" + EXPECTED_PERSON_ID).accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(jsonPath("$.firstName").value(EXPECTED_PERSON_FIRST_NAME_2))
                .andExpect(jsonPath("$.lastName").value(EXPECTED_PERSON_LAST_NAME))
                .andExpect(jsonPath("$.birthday", is(EXPECTED_PERSON_BIRTHDAY)));
    }

    @SneakyThrows
    private void givenPatchItem() {
        HashMap<String, Object> newGtin = new HashMap<>();
        newGtin.put("firstName", EXPECTED_PERSON_FIRST_NAME_2);
        newGtin.put("lastName", EXPECTED_PERSON_LAST_NAME_2);

        HashMap<String, Object> patchItem = new HashMap<>();
        patchItem.put("op", "replace");
        patchItem.put("path", "/firstName");
        patchItem.put("value", EXPECTED_PERSON_FIRST_NAME_2);
        patchPayload = new ObjectMapper().writeValueAsString(singletonList(patchItem));
    }

}
