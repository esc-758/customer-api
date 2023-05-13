package com.earlycharlemagne.customerapi.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.earlycharlemagne.customerapi.dto.AddressRequest;
import com.earlycharlemagne.customerapi.dto.CustomerDto;
import com.earlycharlemagne.customerapi.repository.CustomerRepository;
import com.earlycharlemagne.customerapi.entity.Customer;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class CustomerComponentTests {
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Container
    private static final PostgreSQLContainer<?> POSTGRES_SQL_CONTAINER = new PostgreSQLContainer<>("postgres:15.3");

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        POSTGRES_SQL_CONTAINER.start();

        registry.add("spring.datasource.url", POSTGRES_SQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_SQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_SQL_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES_SQL_CONTAINER::getDriverClassName);
    }

    @Autowired
    MockMvc mockMvc;
    @Autowired
    CustomerRepository customerRepository;

    @Test
    void createNewCustomerSuccessfully() throws Exception {
        var requestBody = OBJECT_MAPPER.writeValueAsString(newCustomerRequest());
        var response = mockMvc.perform(post("/api/customers")
                                  .contentType(APPLICATION_JSON)
                                  .content(requestBody))
                              .andExpect(status().isCreated())
                              .andReturn();

        var savedCustomers = customerRepository.findAll();
        assertThat(savedCustomers).hasSize(1)
                                  .first()
                                  .usingRecursiveComparison()
                                  .ignoringFields("id", "globalId")
                                  .isEqualTo(newCustomer());

        var responseBody = response.getResponse().getContentAsString();
        var savedCustomerGlobalId = savedCustomers.get(0).getGlobalId();
        assertThat(responseBody).isEqualTo(savedCustomerGlobalId);
    }

    @Test
    void createNewCustomerAlreadyExists() throws Exception {
        customerRepository.save(newCustomer());

        var requestBody = OBJECT_MAPPER.writeValueAsString(newCustomerRequest());

        mockMvc.perform(post("/api/customers")
                   .contentType(APPLICATION_JSON)
                   .content(requestBody))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.errorCode", is("EMAIL_EXISTS")));

    }

    @Test
    void getCustomerByIdIsFound() throws Exception {
        givenExistingCustomers();
        var response = mockMvc.perform(get("/api/customers/3149927e-85db-4875-b1eb-f97df52a4ab6"))
                              .andExpect(status().isOk())
                              .andReturn();
        var expectedResponse = """
            {
                "id": "3149927e-85db-4875-b1eb-f97df52a4ab6",
                "firstName": "Jane",
                "lastName": "Doe",
                "email": "jane.doe@example.com",
                "age": 31,
                "address": "123 street, Amsterdam"
            }
            """;
        assertThat(response.getResponse().getContentAsString()).isEqualToIgnoringWhitespace(expectedResponse);
    }

    @Test
    void getCustomerByIdIsNotFound() throws Exception {
        mockMvc.perform(get("/api/customers/3149927e-85db-4875-b1eb-f97df52a4ab6"))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.errorCode", is("CUSTOMER_NOT_FOUND")));;
    }

    @Test
    void getAllCustomersSuccessfully() throws Exception {
        givenExistingCustomers();

        mockMvc.perform(get("/api/customers"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void getAllCustomersReturnsEmpty() throws Exception {
        mockMvc.perform(get("/api/customers"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void findCustomersByFirstNameReturnsResults() throws Exception {
        givenExistingCustomers();

        mockMvc.perform(get("/api/customers")
                   .queryParam("firstName", "jane"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].firstName", is("Jane")))
               .andExpect(jsonPath("$[0].lastName", is("Doe")))
               .andExpect(jsonPath("$[0].email", is("jane.doe@example.com")))
               .andExpect(jsonPath("$[0].age", is(31)))
               .andExpect(jsonPath("$[0].address", is("123 street, Amsterdam")))
               .andExpect(jsonPath("$[0].id", is("3149927e-85db-4875-b1eb-f97df52a4ab6")));
    }

    @Test
    void findCustomersByFirstNameReturnsEmpty() throws Exception {
        givenExistingCustomers();

        mockMvc.perform(get("/api/customers")
                   .queryParam("firstName", "Non existent first name"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void findCustomersByLastNameReturnsResults() throws Exception {
        givenExistingCustomers();

        mockMvc.perform(get("/api/customers")
                   .queryParam("lastName", "doe"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].firstName", is("Jane")))
               .andExpect(jsonPath("$[0].lastName", is("Doe")))
               .andExpect(jsonPath("$[0].email", is("jane.doe@example.com")))
               .andExpect(jsonPath("$[0].age", is(31)))
               .andExpect(jsonPath("$[0].address", is("123 street, Amsterdam")))
               .andExpect(jsonPath("$[0].id", is("3149927e-85db-4875-b1eb-f97df52a4ab6")));
    }

    @Test
    void findCustomersByLastNameReturnsEmpty() throws Exception {
        givenExistingCustomers();

        mockMvc.perform(get("/api/customers")
                   .queryParam("lastName", "Non existent last name"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void findCustomersByFirstNameAndLastNameIsFound() throws Exception {
        givenExistingCustomers(
            newCustomer("Jane", "Doe", "jane.doe@example.com", "3149927e-85db-4875-b1eb-f97df52a4ab6"),
            newCustomer("John", "Doe", "john.doe@example.com", "c388d8ed-acf4-4dee-a63d-85ed9dde0bb0"),
            newCustomer("Jen", "Jen", "jen.jen@example.com", "d435f409-69d8-4bae-ab61-92a585d2c27a"),
            newCustomer("Jen", "Jansen", "jen.jansen@example.com", "9be65b33-e82a-4a62-b801-288e75ee16a2")
        );

        mockMvc.perform(get("/api/customers")
                   .queryParam("firstName", "jen")
                   .queryParam("lastName", "jen"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].firstName", is("Jen")))
               .andExpect(jsonPath("$[0].lastName", is("Jen")))
               .andExpect(jsonPath("$[0].email", is("jen.jen@example.com")))
               .andExpect(jsonPath("$[0].age", is(31)))
               .andExpect(jsonPath("$[0].address", is("123 street, Amsterdam")))
               .andExpect(jsonPath("$[0].id", is("d435f409-69d8-4bae-ab61-92a585d2c27a")));
    }

    @Test
    void findCustomersByFirstNameAndLastNameReturnsEmpty() throws Exception {
        mockMvc.perform(get("/api/customers")
                   .queryParam("firstName", "Jen")
                   .queryParam("lastName", "Jen"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void updateCustomerAddressSuccessfully() throws Exception {
        givenExistingCustomers();

        var addressRequest = OBJECT_MAPPER.writeValueAsString(new AddressRequest("New address"));
        mockMvc.perform(put("/api/customers/{id}/address", "3149927e-85db-4875-b1eb-f97df52a4ab6")
                   .contentType(APPLICATION_JSON)
                   .content(addressRequest))
               .andExpect(status().isNoContent());

        var updatedCustomer = customerRepository.findByGlobalId("3149927e-85db-4875-b1eb-f97df52a4ab6").get();
        assertThat(updatedCustomer.getAddress()).isEqualTo("New address");
    }

    @Test
    void updateCustomerAddressNotFound() throws Exception {
        givenExistingCustomers();

        var addressRequest = OBJECT_MAPPER.writeValueAsString(new AddressRequest("New address"));
        mockMvc.perform(put("/api/customers/{id}/address", "non_existent_global_Id")
                   .contentType(APPLICATION_JSON)
                   .content(addressRequest))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.errorCode", is("CUSTOMER_NOT_FOUND")));
    }

    private void givenExistingCustomers() {
        givenExistingCustomers(
            newCustomer("Jane", "Doe", "jane.doe@example.com", "3149927e-85db-4875-b1eb-f97df52a4ab6"),
            newCustomer("Jen", "Jen", "jen.jen@example.com", "d435f409-69d8-4bae-ab61-92a585d2c27a"),
            newCustomer("Jackie", "Jack", "jackie.jack@example.com", "9be65b33-e82a-4a62-b801-288e75ee16a2")
        );
    }

    private void givenExistingCustomers(Customer... customers) {
        customerRepository.saveAll(List.of(customers));
    }

    private CustomerDto newCustomerRequest() {
        return  CustomerDto.builder()
                           .firstName("Jane")
                           .lastName("Doe")
                           .age(31)
                           .email("jane.doe@example.com")
                           .address("123 street, Amsterdam")
                           .build();
    }

    private Customer newCustomer() {
        return newCustomer("jane.doe@example.com", UUID.randomUUID().toString());
    }

    private Customer newCustomer(String email, String globalId) {
        return newCustomer("Jane", email, globalId);
    }

    private Customer newCustomer(String firstName, String email, String globalId) {
        return newCustomer(firstName, "Doe", email, globalId);
    }

    private Customer newCustomer(String firstName, String lastName, String email, String globalId) {
        var customer = new Customer();

        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setAge(31);
        customer.setEmail(email);
        customer.setGlobalId(globalId);
        customer.setAddress("123 street, Amsterdam");

        return customer;
    }
}
