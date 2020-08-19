package com.example.demo;

import com.example.demo.controllers.UserController;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.model.requests.ModifyCartRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.transaction.Transactional;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class SareetaApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserController userController;

    @Autowired
    private JacksonTester<User> userTester;

    @Autowired
    private JacksonTester<ModifyCartRequest> modifyCartRequestTester;

    @Test
    public void testLogin() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setPassword("testpass");
        request.setConfirmPassword("testpass");
        ResponseEntity<User> response = userController.createUser(request);

        assertEquals("testuser", response.getBody().getUsername());

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("testpass");

        mvc.perform(post(new URI("/login"))
                .content(userTester.write(user).getJson())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void testAddingItems() throws Exception {
        CreateUserRequest createUserReq = new CreateUserRequest();
        createUserReq.setUsername("testuser");
        createUserReq.setPassword("testpass");
        createUserReq.setConfirmPassword("testpass");

        ResponseEntity<User> response = userController.createUser(createUserReq);
        assertEquals("testuser", response.getBody().getUsername());

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("testpass");

        MvcResult loginResult = mvc.perform(post(new URI("/login"))
                .content(userTester.write(user).getJson())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
                .andReturn();

        String token = loginResult.getResponse().getHeader("Authorization");

        ModifyCartRequest request = new ModifyCartRequest();
        request.setUsername("testuser");
        request.setItemId(1);
        request.setQuantity(1);

        MvcResult result = mvc.perform(post(new URI("/api/cart/addToCart"))
                .content(modifyCartRequestTester.write(request).getJson())
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk()).andReturn();

        byte[] content = result.getResponse().getContentAsByteArray();
        Cart cart = new ObjectMapper().readValue(content, Cart.class);
        assertEquals(1, cart.getItems().size());
    }

    @Test
    public void testAuthorization() throws Exception {
        mvc.perform(get(new URI("/api/item"))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().is4xxClientError());

        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setPassword("testpass");
        request.setConfirmPassword("testpass");

        ResponseEntity<User> response = userController.createUser(request);
        assertEquals("testuser", response.getBody().getUsername());

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("testpass");

        MvcResult loginResult = mvc.perform(post(new URI("/login"))
                .content(userTester.write(user).getJson())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
                .andReturn();

        String token = loginResult.getResponse().getHeader("Authorization");

        mvc.perform(get(new URI("/api/item"))
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());
    }


    @Test
    public void testTooShortPasswordException() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setPassword("short");
        request.setConfirmPassword("short");

        ResponseEntity<User> response = userController.createUser(request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}