package com.earlycharlemagne.customerapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.earlycharlemagne.customerapi.dto.ErrorResponse;
import com.earlycharlemagne.customerapi.dto.ValidationError;
import com.earlycharlemagne.customerapi.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(CustomerController.class)
class CustomerControllerValidationTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @MockBean
    CustomerService customerService;
    @Autowired
    MockMvc mockMvc;

    @Test
    void customerRequestBodyIsValid() throws Exception {
        var requestBody = """
            {
                "firstName": "Jane",
                "lastName": "Doe",
                "email": "jane.doe@example.com",
                "age": 31,
                "address": "123 street, Amsterdam"
            }
            """;
        mockMvc.perform(post("/api/customers")
                                  .contentType(APPLICATION_JSON)
                                  .content(requestBody))
                              .andExpect(status().isCreated());
    }

    @Test
    void customerRequestBodyIsNotValid() throws Exception {
        var emptyRequestBody = "{}";

        var response = mockMvc.perform(post("/api/customers")
                                  .contentType(APPLICATION_JSON)
                                  .content(emptyRequestBody))
                              .andExpect(status().isBadRequest())
                              .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                              .andReturn().getResponse();

        var validationErrors = OBJECT_MAPPER.readValue(response.getContentAsString(), ErrorResponse.class)
                                            .errors();

        assertThat(validationErrors).containsExactlyInAnyOrder(
            new ValidationError("firstName", "First name is required"),
            new ValidationError("lastName", "Last name is required"),
            new ValidationError("email", "Email is required"),
            new ValidationError("age", "Age is required"),
            new ValidationError("address", "Address is required")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "e",
        "More_than_50_characters_KweWbrUSzcYCkVoVc2FrHFV6L1J"
    })
    void firstNameLengthIsInvalid(String firstName) throws Exception {
        var invalidFirstName = """
            {
              "firstName": "%s"
            }
            """.formatted(firstName);

        mockMvc.perform(post("/api/customers")
                   .contentType(APPLICATION_JSON)
                   .content(invalidFirstName))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
               .andExpect(jsonPath("$.errors[?(@.field=='firstName' && @.message=='Length of first name must be between 2 and 50 characters')]").exists());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "e",
        "More_than_50_characters_KweWbrUSzcYCkVoVc2FrHFV6L1J"
    })
    void lastNameLengthIsInvalid(String lastName) throws Exception {
        var invalidLastName = """
            {
              "lastName": "%s"
            }
            """.formatted(lastName);

        mockMvc.perform(post("/api/customers")
                   .contentType(APPLICATION_JSON)
                   .content(invalidLastName))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
               .andExpect(jsonPath("$.errors[?(@.field=='lastName' && @.message=='Length of last name must be between 2 and 50 characters')]").exists());
    }

    @Test
    void emailIsInvalid() throws Exception {
        var invalidEmail = """
            {
              "email": "invalidEmail@"
            }
            """;

        mockMvc.perform(post("/api/customers")
                   .contentType(APPLICATION_JSON)
                   .content(invalidEmail))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
               .andExpect(jsonPath("$.errors[?(@.field=='email' && @.message=='Email is not valid')]").exists());
    }

    @Test
    void ageIsLessThan18() throws Exception {
        var under18 = """
            {
              "age": "17"
            }
            """;

        mockMvc.perform(post("/api/customers")
                   .contentType(APPLICATION_JSON)
                   .content(under18))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
               .andExpect(jsonPath("$.errors[?(@.field=='age' && @.message=='Age must be 18 and above')]").exists());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "e",
        "More_than_255_characters_2nD0SU4TEaVv0aeEy1K5DmGdWB2qmmX2XMegplbwnMtXuzNCoUBjG7RFW4yopbdsP4H6j9vhcIaO6RjECFrb6P4LXQw9PgIvXvHCYeIPC3GRGxhe0wUiwKbFl8SskmI3pERQAMlQY91SGtJ3iSupcEDWZb727ScqBnGJCcsJ0hukt8xNIRPeKuPd5pek9KFhkbc8Qsbn8xFG5mqUBuA6XWerkXee7p70SCOiHT9"
    })
    void addressLengthIsInvalid(String address) throws Exception {
        var invalidAddress = """
            {
              "address": "%s"
            }
            """.formatted(address);

        mockMvc.perform(post("/api/customers")
                   .contentType(APPLICATION_JSON)
                   .content(invalidAddress))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
               .andExpect(jsonPath("$.errors[?(@.field=='address' && @.message=='Length of address must be between 2 and 255 characters')]").exists());
    }

    @Test
    void updatingAddressWithEmptyRequestIsInvalid() throws Exception {
        var emptyRequest = "{}";

        mockMvc.perform(put("/api/customers/123/address")
                   .contentType(APPLICATION_JSON)
                   .content(emptyRequest))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
               .andExpect(jsonPath("$.errors[?(@.field=='address' && @.message=='Address is required')]").exists());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "e",
        "More_than_255_characters_2nD0SU4TEaVv0aeEy1K5DmGdWB2qmmX2XMegplbwnMtXuzNCoUBjG7RFW4yopbdsP4H6j9vhcIaO6RjECFrb6P4LXQw9PgIvXvHCYeIPC3GRGxhe0wUiwKbFl8SskmI3pERQAMlQY91SGtJ3iSupcEDWZb727ScqBnGJCcsJ0hukt8xNIRPeKuPd5pek9KFhkbc8Qsbn8xFG5mqUBuA6XWerkXee7p70SCOiHT9"
    })
    void updatingAddressLengthIsInvalid(String address) throws Exception {
        var invalidAddress = """
            {
              "address": "%s"
            }
            """.formatted(address);

        mockMvc.perform(put("/api/customers/123/address")
                   .contentType(APPLICATION_JSON)
                   .content(invalidAddress))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
               .andExpect(jsonPath("$.errors[?(@.field=='address' && @.message=='Length of address must be between 2 and 255 characters')]").exists());
    }
}