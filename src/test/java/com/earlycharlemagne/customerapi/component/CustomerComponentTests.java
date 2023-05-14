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

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.earlycharlemagne.customerapi.customer.dto.AddressRequest;
import com.earlycharlemagne.customerapi.customer.dto.CustomerDto;
import com.earlycharlemagne.customerapi.customer.dto.CustomerIdResponse;
import com.earlycharlemagne.customerapi.customer.entity.Customer;
import com.earlycharlemagne.customerapi.customer.repository.CustomerRepository;
import com.fasterxml.jackson.core.type.TypeReference;

@AutoConfigureMockMvc
@WithMockUser(username = "api_user", password = "verysecurepassword")
class CustomerComponentTests extends AbstractComponentTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    CustomerRepository customerRepository;

    @Test
    void createNewCustomerSuccessfully() throws Exception {
        var requestBody = OBJECT_MAPPER.writeValueAsString(newCustomerRequest());
        var response = mockMvc.perform(post("/api/customers").contentType(APPLICATION_JSON)
                                                             .content(requestBody))
                              .andExpect(status().isCreated())
                              .andReturn();

        var savedCustomers = customerRepository.findAll();
        assertThat(savedCustomers).hasSize(1)
                                  .first()
                                  .usingRecursiveComparison()
                                  .ignoringFields("id", "globalId")
                                  .isEqualTo(newCustomer());

        var responseBody = OBJECT_MAPPER.readValue(response.getResponse()
                                                           .getContentAsString(), CustomerIdResponse.class);
        var savedCustomerGlobalId = savedCustomers.get(0)
                                                  .getGlobalId();
        assertThat(responseBody.id()).isEqualTo(savedCustomerGlobalId);
    }

    @Test
    void createNewCustomerAlreadyExists() throws Exception {
        customerRepository.save(newCustomer());

        var requestBody = OBJECT_MAPPER.writeValueAsString(newCustomerRequest());

        mockMvc.perform(post("/api/customers").contentType(APPLICATION_JSON)
                                              .content(requestBody))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.errorCode", is("CUSTOMER_EXISTS")));

    }

    @Test
    void getCustomerByIdIsFound() throws Exception {
        givenExistingCustomers();
        var response = mockMvc.perform(get("/api/customers/aae20c05-4e09-4048-92b2-cd0557409950"))
                              .andExpect(status().isOk())
                              .andReturn();
        var expectedResponse = """
            {
                "id": "aae20c05-4e09-4048-92b2-cd0557409950",
                "firstName": "Jennifer",
                "lastName": "Charles",
                "email": "jennifer.charles@example.com",
                "age": 18,
                "address": "10th street, Amsterdam"
            }
            """;
        assertThat(response.getResponse()
                           .getContentAsString()).isEqualToIgnoringWhitespace(expectedResponse);
    }

    @Test
    void getCustomerByIdIsNotFound() throws Exception {
        mockMvc.perform(get("/api/customers/3149927e-85db-4875-b1eb-f97df52a4ab6"))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.errorCode", is("CUSTOMER_NOT_FOUND")));
        ;
    }

    @Test
    void getAllCustomersSuccessfully() throws Exception {
        givenExistingCustomers();

        mockMvc.perform(get("/api/customers"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(10)));
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

        mockMvc.perform(get("/api/customers").queryParam("firstName", "bob"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(2)))
               // first customer
               .andExpect(jsonPath("$[0].firstName", is("Bob")))
               .andExpect(jsonPath("$[0].lastName", is("Jenkins")))
               .andExpect(jsonPath("$[0].email", is("bob.jenkins@example.com")))
               .andExpect(jsonPath("$[0].age", is(33)))
               .andExpect(jsonPath("$[0].address", is("6th street, Almere")))
               .andExpect(jsonPath("$[0].id", is("ae820462-8827-49d6-9fa0-be9a0a68231f")))
               // second customer
               .andExpect(jsonPath("$[1].firstName", is("Bob")))
               .andExpect(jsonPath("$[1].lastName", is("Smith")))
               .andExpect(jsonPath("$[1].email", is("bob.smith@example.com")))
               .andExpect(jsonPath("$[1].age", is(55)))
               .andExpect(jsonPath("$[1].address", is("1st street, Amsterdam")))
               .andExpect(jsonPath("$[1].id", is("1b685e82-3743-42c3-a6ca-30f187121283")));
    }

    @Test
    void findCustomersByFirstNameReturnsEmpty() throws Exception {
        givenExistingCustomers();

        mockMvc.perform(get("/api/customers").queryParam("firstName", "Non existent first name"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void findCustomersByLastNameReturnsResults() throws Exception {
        givenExistingCustomers();

        mockMvc.perform(get("/api/customers").queryParam("lastName", "rice"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(2)))
               // first customer
               .andExpect(jsonPath("$[0].firstName", is("Sue")))
               .andExpect(jsonPath("$[0].lastName", is("Rice")))
               .andExpect(jsonPath("$[0].email", is("sue.rice@example.com")))
               .andExpect(jsonPath("$[0].age", is(42)))
               .andExpect(jsonPath("$[0].address", is("3rd street, Tilburg")))
               .andExpect(jsonPath("$[0].id", is("c9940b8c-680b-4e5b-95c3-b97a1f1ef692")))
               // second customer
               .andExpect(jsonPath("$[1].firstName", is("Tim")))
               .andExpect(jsonPath("$[1].lastName", is("Rice")))
               .andExpect(jsonPath("$[1].email", is("tim.rice@example.com")))
               .andExpect(jsonPath("$[1].age", is(45)))
               .andExpect(jsonPath("$[1].address", is("3rd street, Tilburg")))
               .andExpect(jsonPath("$[1].id", is("c40cc016-4296-4c8c-8589-43f57adb5038")));
    }

    @Test
    void findCustomersByLastNameReturnsEmpty() throws Exception {
        givenExistingCustomers();

        mockMvc.perform(get("/api/customers").queryParam("lastName", "Non existent last name"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void findCustomersByFirstNameAndLastNameIsFound() throws Exception {
        givenExistingCustomers();

        mockMvc.perform(get("/api/customers")
                   .queryParam("firstName", "jackie")
                   .queryParam("lastName", "jackson"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].firstName", is("Jackie")))
               .andExpect(jsonPath("$[0].lastName", is("Jackson")))
               .andExpect(jsonPath("$[0].email", is("jackie.jackson@example.com")))
               .andExpect(jsonPath("$[0].age", is(46)))
               .andExpect(jsonPath("$[0].address", is("7th street, Zeist")))
               .andExpect(jsonPath("$[0].id", is("7c8371fe-d085-432d-a66d-7ccb90e48c28")));
    }

    @Test
    void findCustomersByFirstNameAndLastNameReturnsEmpty() throws Exception {
        mockMvc.perform(get("/api/customers").queryParam("firstName", "Jen")
                                             .queryParam("lastName", "Jen"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void updateCustomerAddressSuccessfully() throws Exception {
        givenExistingCustomers();

        var addressRequest = OBJECT_MAPPER.writeValueAsString(new AddressRequest("New address"));
        mockMvc.perform(put("/api/customers/{id}/address", "df7acb3d-414c-4951-a227-ea1e18a1d4f0")
                   .content(addressRequest)
                   .contentType(APPLICATION_JSON))
               .andExpect(status().isNoContent());

        var updatedCustomer = customerRepository.findByGlobalId("df7acb3d-414c-4951-a227-ea1e18a1d4f0")
                                                .get();
        assertThat(updatedCustomer.getAddress()).isEqualTo("New address");
    }

    @Test
    void updateCustomerAddressNotFound() throws Exception {
        givenExistingCustomers();

        var addressRequest = OBJECT_MAPPER.writeValueAsString(new AddressRequest("New address"));
        mockMvc.perform(put("/api/customers/{id}/address", "non_existent_global_Id")
                   .content(addressRequest)
                   .contentType(APPLICATION_JSON))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.errorCode", is("CUSTOMER_NOT_FOUND")));
    }

    private void givenExistingCustomers() {
        try {
            var customers = OBJECT_MAPPER.readValue(new ClassPathResource("data/customers.json").getInputStream(), new TypeReference<List<Customer>>(){});

            customerRepository.saveAll(customers);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private CustomerDto newCustomerRequest() {
        return CustomerDto.builder()
                          .firstName("Jane")
                          .lastName("Doe")
                          .age(31)
                          .email("jane.doe@example.com")
                          .address("123 street, Amsterdam")
                          .build();
    }

    private Customer newCustomer() {
        var customer = new Customer();

        customer.setFirstName("Jane");
        customer.setLastName("Doe");
        customer.setAge(31);
        customer.setEmail("jane.doe@example.com");
        customer.setGlobalId(UUID.randomUUID().toString());
        customer.setAddress("123 street, Amsterdam");

        return customer;
    }
}
