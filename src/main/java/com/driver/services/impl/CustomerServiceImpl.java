package com.driver.services.impl;

import com.driver.model.TripBooking;
import com.driver.model.TripStatus;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.model.Customer;
import com.driver.model.Driver;
import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		Customer customer=customerRepository2.findById(customerId).get();
		customerRepository2.delete(customer);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		List<Driver> drivers=driverRepository2.findAll();
		int driverId =Integer.MAX_VALUE;
		for(Driver driver:drivers){
			if(driver.getDriverId() < driverId && driver.getCab().isAvailable()){
				driverId =driver.getDriverId();
				}
			}
		if(driverId == Integer.MAX_VALUE){
			throw new RuntimeException("No cab available!");
		}
		Driver driver = driverRepository2.findById(driverId).get();
		Customer customer=customerRepository2.findById(customerId).get();
		int rate = distanceInKm * driver.getCab().getPerKmRate();
		TripBooking tripBooked=new TripBooking(fromLocation,toLocation,distanceInKm,rate);
		tripBooked.setDriver(driver);
		tripBooked.setCustomer(customer);
		tripBooked.setTripStatus(TripStatus.CONFIRMED);
		driver.getCab().setAvailable(false);
		List<TripBooking> driverTrips=driver.getTripBookingList();
		List<TripBooking> customerTrips=customer.getTripBookingList();
		if(driverTrips==null){
			driverTrips=new ArrayList<>();
		}
		if(customerTrips==null){
			customerTrips=new ArrayList<>();
		}
		driverTrips.add(tripBooked);
		customerTrips.add(tripBooked);
		driver.setTripBookingList(driverTrips);
		customer.setTripBookingList(customerTrips);
		tripBookingRepository2.save(tripBooked);
		driverRepository2.save(driver);
		customerRepository2.save(customer);
		return tripBooked;
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking bookedTrip=tripBookingRepository2.findById(tripId).get();
		Customer customer=bookedTrip.getCustomer();
		Driver driver=bookedTrip.getDriver();
		driver.getCab().setAvailable(true);
		bookedTrip.setTripStatus(TripStatus.CANCELED);
		driverRepository2.save(driver);
		customerRepository2.save(customer);
		tripBookingRepository2.save(bookedTrip);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking bookedTrip=tripBookingRepository2.findById(tripId).get();
		Customer customer=bookedTrip.getCustomer();
		Driver driver=bookedTrip.getDriver();
		driver.getCab().setAvailable(true);
		bookedTrip.setTripStatus(TripStatus.COMPLETED);
		driverRepository2.save(driver);
		customerRepository2.save(customer);
		tripBookingRepository2.save(bookedTrip);
	}
}
