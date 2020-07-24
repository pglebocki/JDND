package com.udacity.vehicles;

import com.udacity.vehicles.api.CarResourceAssembler;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.service.CarService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class VehiclesApiApplicationTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CarService carService;

    @Test
    public void contextLoads() {
    }


}
