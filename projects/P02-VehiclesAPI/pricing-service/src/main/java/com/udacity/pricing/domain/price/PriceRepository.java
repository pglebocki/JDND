package com.udacity.pricing.domain.price;

import com.udacity.pricing.service.PriceException;
import org.springframework.data.repository.CrudRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class PriceRepository implements CrudRepository<Price, Long> {

    /**
     * Holds {ID: Price} pairings (current implementation allows for 20 vehicles)
     */
    private final Map<Long, Price> PRICES = LongStream
            .range(1, 20)
            .mapToObj(i -> new Price("USD", randomPrice(), i))
            .collect(Collectors.toMap(Price::getVehicleId, p -> p));

    /**
     * If a valid vehicle ID, gets the price of the vehicle from the stored array.
     * @param vehicleId ID number of the vehicle the price is requested for.
     * @return price of the requested vehicle
     * @throws PriceException vehicleID was not found
     */
    private Price getPrice(Long vehicleId) throws PriceException {
        if (!PRICES.containsKey(vehicleId)) {
            throw new PriceException("Cannot find price for Vehicle " + vehicleId);
        }

        return PRICES.get(vehicleId);
    }

    /**
     * Gets a random price to fill in for a given vehicle ID.
     * @return random price for a vehicle
     */
    private BigDecimal randomPrice() {
        return new BigDecimal(ThreadLocalRandom.current().nextDouble(1, 5))
                .multiply(new BigDecimal(5000d)).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public <S extends Price> S save(S s) {
        return null;
    }

    @Override
    public <S extends Price> Iterable<S> saveAll(Iterable<S> iterable) {
        return null;
    }

    @Override
    public Optional<Price> findById(Long vehicleId) {
        try {
            return Optional.of(getPrice(vehicleId));
        } catch (PriceException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public Iterable<Price> findAll() {
        return null;
    }

    @Override
    public Iterable<Price> findAllById(Iterable<Long> iterable) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Long aLong) {

    }

    @Override
    public void delete(Price price) {

    }

    @Override
    public void deleteAll(Iterable<? extends Price> iterable) {

    }

    @Override
    public void deleteAll() {

    }
}
