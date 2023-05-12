package com.earlycharlemagne.customerapi.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import com.earlycharlemagne.customerapi.dto.CustomerDto;
import com.earlycharlemagne.customerapi.repository.CustomerRepository;
import com.earlycharlemagne.customerapi.entity.Customer;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class CustomerControllerComponentTests {
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Container
    private static final PostgreSQLContainer<?> POSTGRES_SQL_CONTAINER = new PostgreSQLContainer<>("postgres:latest");

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
               .andExpect(content().string("EMAIL_EXISTS"));

    }

    @Test
    void getCustomerByIdIsFound() throws Exception {
        givenExistingCustomers();
        var response = mockMvc.perform(get("/api/customers/3149927e-85db-4875-b1eb-f97df52a4ab6"))
                              .andExpect(status().isOk())
                              .andReturn();
        var expectedResponse = """
            {
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
               .andExpect(content().string("CUSTOMER_NOT_FOUND"));;
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
    void findCustomersByFirstNameIsFound() throws Exception {

    }

    @Test
    void findCustomersByFirstNameIsNotFound(){

    }

    @Test
    void findCustomersByLastNameIsFound(){

    }

    @Test
    void findCustomersByLastNameIsNotFound(){

    }

    @Test
    void findCustomersByFirstNameAndLastNameIsFound(){

    }
    @Test
    void findCustomersByFirstNameAndLastNameIsNotFound(){

    }

    @Test
    void updateCustomerAddressSuccessfully() {

    }

    @Test
    void updateCustomerAddressNotFound() {

    }

    private void givenExistingCustomers() {
        customerRepository.saveAll(List.of(
            newCustomer("jane.doe@example.com", "3149927e-85db-4875-b1eb-f97df52a4ab6"),
            newCustomer("jen.jen@example.com", "d435f409-69d8-4bae-ab61-92a585d2c27a"),
            newCustomer("jackie.jack@example.com", "9be65b33-e82a-4a62-b801-288e75ee16a2"))
        );
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
        var customer = new Customer();

        customer.setFirstName("Jane");
        customer.setLastName("Doe");
        customer.setAge(31);
        customer.setEmail(email);
        customer.setGlobalId(globalId);
        customer.setAddress("123 street, Amsterdam");

        return customer;
    }
}
