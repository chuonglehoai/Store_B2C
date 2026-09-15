package com.salesmanager.core.business.services.user;

import java.util.List;

import org.springframework.data.domain.Page;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.common.generic.SalesManagerEntityService;
import com.salesmanager.core.model.user.User;
import com.salesmanager.core.model.user.UserCriteria;



public interface UserService extends SalesManagerEntityService<Long, User> {

  User getByUserName(String userName) throws ServiceException;

  List<User> listUser() throws ServiceException;
  
  User getByPasswordResetToken(String token);

  void saveOrUpdate(User user) throws ServiceException;
  
  Page<User> listByCriteria(UserCriteria criteria, int page, int count) throws ServiceException;
  
  User findByResetPasswordToken (String userName, String token) throws ServiceException;




}
