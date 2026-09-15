package com.salesmanager.core.business.services.user;

import java.util.List;
import jakarta.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.helper.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.repositories.user.UserRepository;
import com.salesmanager.core.business.services.common.generic.SalesManagerEntityServiceImpl;
import com.salesmanager.core.model.common.Criteria;
import com.salesmanager.core.model.common.GenericEntityList;
import com.salesmanager.core.model.user.User;
import com.salesmanager.core.model.user.UserCriteria;

@Service("UserService")
public class UserServiceImpl extends SalesManagerEntityServiceImpl<Long, User> implements UserService {

	private UserRepository userRepository;

	@Inject
	public UserServiceImpl(UserRepository userRepository) {
		super(userRepository);
		this.userRepository = userRepository;

	}

	@Override
	public User getByUserName(String userName) throws ServiceException {
		return userRepository.findByUserName(userName).orElse(null);
	}

	@Override
	public void delete(User user) throws ServiceException {
		User u = this.getById(user.getId());
		super.delete(u);

	}

	@Override
	public List<User> listUser() throws ServiceException {
		try {
			return userRepository.findAll();
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public void saveOrUpdate(User user) throws ServiceException {
		userRepository.save(user);
	}

	@Override
    public Page<User> listByCriteria(UserCriteria criteria, int page, int count) throws ServiceException {
        Pageable pageRequest = PageRequest.of(page, count);
        String email = criteria != null ? criteria.getAdminEmail() : null;
        return userRepository.listAll(email, pageRequest);
    }

	@Override
	public User findByResetPasswordToken(String userName, String token) throws ServiceException {
		Validate.notNull(userName, "User name cannot be null");
		Validate.notNull(token, "Token cannot be null");
		return null;
	}

	@Override
	public User getByPasswordResetToken( String token) {
		return userRepository.findByResetPasswordToken(token).orElse(null);
	}

}
