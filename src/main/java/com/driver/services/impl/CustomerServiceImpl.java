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
		TripBooking tripBooking=null;
		for(Driver driver:drivers){
			if(driver.getCab().getAvailable()){
				Customer customer = customerRepository2.findById(customerId).get();
				tripBooking=new TripBooking();
				tripBooking.setCustomer(customer);
				tripBooking.setDriver(driver);
				tripBooking.setStatus(TripStatus.CONFIRMED);
				tripBooking.setFromLocation(fromLocation);
				tripBooking.setToLocation(toLocation);
				tripBooking.setDistanceInKm(distanceInKm);
				tripBooking.setBill(distanceInKm * driver.getCab().getPerKmRate());
				driver.getTripBookingList().add(tripBooking);
				customer.getTripBookingList().add(tripBooking);
				driver.getCab().setAvailable(false);
				driverRepository2.save(driver);
				customerRepository2.save(customer);
				tripBookingRepository2.save(tripBooking);
				break;
			}
		}
		if(tripBooking==null){
			throw new RuntimeException("No cab available!");
		}
		return tripBooking;
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking bookedTrip=tripBookingRepository2.findById(tripId).get();
		bookedTrip.getDriver().getCab().setAvailable(true);
		bookedTrip.setStatus(TripStatus.CANCELED);
		bookedTrip.getCustomer().getTripBookingList().remove(bookedTrip);
		bookedTrip.setBill(0);
		tripBookingRepository2.save(bookedTrip);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking bookedTrip=tripBookingRepository2.findById(tripId).get();
		bookedTrip.getDriver().getCab().setAvailable(true);
		bookedTrip.setStatus(TripStatus.COMPLETED);
		tripBookingRepository2.save(bookedTrip);
	}
}
