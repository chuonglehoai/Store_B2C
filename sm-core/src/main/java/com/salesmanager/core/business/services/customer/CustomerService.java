package com.salesmanager.core.business.services.customer;

import org.springframework.data.domain.Page;
import java.util.List;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.common.generic.SalesManagerEntityService;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.customer.CustomerCriteria;
import com.salesmanager.core.model.customer.CustomerList;



public interface CustomerService  extends SalesManagerEntityService<Long, Customer> {

	List<Customer> getByName(String fullName);

	Customer getByUserName(String userName);

	Customer getByNick(String nick);

	Customer getByEmail(String email);

	void saveOrUpdate(Customer customer) throws ServiceException ;
	
	Page<Customer> listByCriteria(CustomerCriteria criteria, int page, int count);

	Customer getByPasswordResetToken(String token);

}
