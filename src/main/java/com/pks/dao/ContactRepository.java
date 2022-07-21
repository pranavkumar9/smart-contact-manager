package com.pks.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pks.Entities.Contact;


public interface ContactRepository extends JpaRepository<Contact, Integer>{
	
	@Query("from Contact c where c.user.id =:userId")
	public Page<Contact> findContactsByUser(@Param("userId")int userId, Pageable pageable);
	// Pageable contains 2 info
	//Current page, contact per page
	
	

	
}
