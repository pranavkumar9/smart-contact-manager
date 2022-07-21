package com.pks.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.pks.Entities.User;
import com.pks.dao.UserRepository;

public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	public UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		//fetching user from database
		User user = userRepository.getUserByUserName(username);
		
		if(user == null)
		{
			throw new UsernameNotFoundException("Please enter valid username");
		}
		
		CustomUserDetails customUserDetails = new CustomUserDetails(user);
		
		return customUserDetails;
	}

}
