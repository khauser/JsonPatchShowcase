package com.example.demo;

import static java.util.Collections.singletonList;
import static org.springframework.data.rest.webmvc.RestMediaTypes.JSON_PATCH_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PersonControllerTest {

    private static final Long EXPECTED_PERSON_ID = 1L;
    private static final String EXPECTED_PERSON_FIRST_NAME = "Santa";
    private static final String EXPECTED_PERSON_LAST_NAME = "Clause";

    private static final String EXPECTED_PERSON_FIRST_NAME_2 = "Max";
    private static final String EXPECTED_PERSON_LAST_NAME_2 = "Muster";

    @Autowired
    private MockMvc mvc;

    private String patchPayload;

    @Test
    public void test_patch() throws Exception {
        Person person = new Person();
        person.setFirstName(EXPECTED_PERSON_FIRST_NAME);
        person.setLastName(EXPECTED_PERSON_LAST_NAME);

        mvc.perform(MockMvcRequestBuilders.post("/person/").contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(person))).andDo(print()).andExpect(status().isCreated());

        mvc.perform(MockMvcRequestBuilders.get("/person/{id}", EXPECTED_PERSON_ID).accept(MediaTypes.HAL_JSON_VALUE))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value(EXPECTED_PERSON_FIRST_NAME))
                .andExpect(jsonPath("$.lastName").value(EXPECTED_PERSON_LAST_NAME));

        givenPatchItem();

        mvc.perform(MockMvcRequestBuilders.patch("/person/" + EXPECTED_PERSON_ID).contentType(JSON_PATCH_JSON)
                .content(patchPayload)).andDo(print()).andExpect(status().isNoContent());

        mvc.perform(MockMvcRequestBuilders.get("/person/" + EXPECTED_PERSON_ID).accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(jsonPath("$.firstName").value(EXPECTED_PERSON_FIRST_NAME_2))
                .andExpect(jsonPath("$.lastName").value(EXPECTED_PERSON_LAST_NAME));
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

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
