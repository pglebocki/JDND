package com.udacity.vehicles.service;

import com.udacity.vehicles.client.maps.Address;
import com.udacity.vehicles.client.prices.Price;
import com.udacity.vehicles.domain.Location;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.CarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements the car service create, read, update or delete
 * information about vehicles, as well as gather related
 * location and price data when desired.
 */
@Service
public class CarService {

    private final CarRepository repository;
    private final WebClient mapsWebClient;
    private final WebClient pricingWebClient;

    @Autowired
    public CarService(
            CarRepository repository,
            @Qualifier("maps") WebClient mapsWebClient,
            @Qualifier("pricing") WebClient pricingWebClient
    ) {
        this.repository = repository;
        this.mapsWebClient = mapsWebClient;
        this.pricingWebClient = pricingWebClient;
    }

    /**
     * Gathers a list of all vehicles
     *
     * @return a list of all vehicles in the CarRepository
     */
    public List<Car> list() {
        return repository.findAll();
    }

    /**
     * Gets car information by ID (or throws exception if non-existent)
     *
     * @param id the ID number of the car to gather information on
     * @return the requested car's information, including location and price
     */
    public Car findById(Long id) {
        final Car car = repository
                .findById(id)
                .orElseThrow(CarNotFoundException::new);

        final Map<String, Object> priceParams = new HashMap<>();
        priceParams.put("vehicleId", id);
        final Price price = pricingWebClient.get()
                .uri("/services/price", priceParams)
                .retrieve()
                .bodyToMono(Price.class)
                .block();
        car.setPrice(price.getPrice() + price.getCurrency());

        final Location location = car.getLocation();
        final Map<String, Object> mapsParams = new HashMap<>();
        mapsParams.put("lat", location.getLat());
        mapsParams.put("lon", location.getLon());
        final Address address = mapsWebClient.get()
                .uri("/", mapsParams)
                .retrieve()
                .bodyToMono(Address.class)
                .block();

        location.setCity(address.getCity());
        location.setState(address.getState());
        location.setZip(address.getZip());
        location.setAddress(address.toString());
        car.setLocation(location);

        return car;
    }

    /**
     * Either creates or updates a vehicle, based on prior existence of car
     *
     * @param car A car object, which can be either new or existing
     * @return the new/updated car is stored in the repository
     */
    public Car save(Car car) {
        if (car.getId() != null) {
            return repository.findById(car.getId())
                    .map(carToBeUpdated -> {
                        carToBeUpdated.setDetails(car.getDetails());
                        carToBeUpdated.setLocation(car.getLocation());
                        return repository.save(carToBeUpdated);
                    }).orElseThrow(CarNotFoundException::new);
        }

        return repository.save(car);
    }

    /**
     * Deletes a given car by ID
     *
     * @param id the ID number of the car to delete
     */
    public void delete(Long id) {
        final Car car = repository
                .findById(id)
                .orElseThrow(CarNotFoundException::new);

        repository.delete(car);
    }
}
