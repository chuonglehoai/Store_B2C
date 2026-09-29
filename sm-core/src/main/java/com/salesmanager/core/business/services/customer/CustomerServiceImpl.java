package com.salesmanager.core.business.services.customer;

import java.util.List;

import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.repositories.customer.CustomerRepository;
import com.salesmanager.core.business.services.common.generic.SalesManagerEntityServiceImpl;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.customer.CustomerCriteria;

@Service("customerService")
public class CustomerServiceImpl extends SalesManagerEntityServiceImpl<Long, Customer> implements CustomerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerServiceImpl.class);
    
    private final CustomerRepository customerRepository;

    @Inject
    public CustomerServiceImpl(CustomerRepository customerRepository) {
        super(customerRepository);
        this.customerRepository = customerRepository;
    }

    @Override
    public List<Customer> getByName(String fullName) {
        return customerRepository.findByName(fullName);
    }

    @Override
    public Customer getByUserName(String userName) {
        return customerRepository.findByUserName(userName).orElse(null);
    }

    @Override
    public Customer getById(Long id) {
        return customerRepository.findById(id).orElse(null);      
    }
    
    @Override
    public Customer getByNick(String nick) {
        return customerRepository.findByNick(nick).orElse(null); 
    }

    // Bổ sung phương thức getByEmail bị thiếu so với Interface
    @Override
    public Customer getByEmail(String email) {
        return customerRepository.findByEmailAddress(email).orElse(null);
    }
    
    @Override
    public Page<Customer> listByCriteria(CustomerCriteria criteria, int page, int count) {
        Pageable pageable = PageRequest.of(page, count);
        String search = criteria != null ? criteria.getSearch() : null;
        return customerRepository.listAll(search, pageable);
    }

    @Override   
    public void saveOrUpdate(Customer customer) throws ServiceException {
        LOGGER.debug("Saving Customer");
        if (customer.getId() != null && customer.getId() > 0) {
            super.update(customer);
        } else {            
            super.create(customer);
        }
    }

    @Override
    public void delete(Customer customer) throws ServiceException {
        customer = getById(customer.getId());
        if (customer != null) {
            customerRepository.delete(customer);
        }
    }

    @Override
    public Customer getByPasswordResetToken(String token) {
        return customerRepository.findByResetPasswordToken(token);
    }
}