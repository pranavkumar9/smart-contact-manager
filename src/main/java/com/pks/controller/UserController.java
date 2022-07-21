package com.pks.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.pks.Entities.Contact;
import com.pks.Entities.User;
import com.pks.dao.ContactRepository;
import com.pks.dao.UserRepository;
import com.pks.helper.Message;


@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	private List<Contact> findContactsByUser;
	
	//method to add common data
	@ModelAttribute
	public void addContact(Model model,Principal principal)
	{	
		
		
		//Takes email in username
		String Username = principal.getName();
		System.out.println("Username = "+Username);
		
		//getting name of user from repository with the email
		User user = userRepository.getUserByUserName(Username);
		System.out.println("User "+user);
		
		model.addAttribute("user",user);
		model.addAttribute("title",user.getName()+" - Dashboard");
		
	}
	
	//Dashboard home------------------------------------------------------------
	
	
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal)
	{	
		
		return "user/userDashboard";
	}
	
	
//Add contact handler----normal------------------------------------------------------------
	
	
	@GetMapping("/addContact")
	public String addContactForm(Model model)
	{
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		return "/user/addContact";
	}
	
//	Add contact handler------------------------------------------------------------------
	
	@PostMapping("/processContact")
	public String processContact(
			@ModelAttribute Contact contact,						 //fetches all data from contacts,where name matches with entity
			@RequestParam("profileImage") MultipartFile file,		 //RequestParam fetches specific field from form
			Principal principal,                         			 // fetch User data, here we fetches username by getName
			HttpSession session)									 // using session attribute to send alert in basehtml
	{
		try {
			String name = principal.getName();
			
			User user = this.userRepository.getUserByUserName(name);
			
			//putting image in folder and 	update the name to contact data
			if(file.isEmpty())
			{
				System.out.println("Image File is empty");
				contact.setImageUrl("led bulbs.jfif");
				
			}
			else
			{
				contact.setImageUrl(file.getOriginalFilename());//inbuild method to get original file
				
				File imgFile = new ClassPathResource("static/img").getFile(); //file get upload in this folder
				
				Path path = Paths.get(imgFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				//path to take file  from
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);//copy the file and replace if existing
			
				System.out.println("Image is uploaded");
			}
			
			
			user.getContacts().add(contact);
			
			contact.setUser(user);
			
			this.userRepository.save(user);
			
			System.out.println("Data = "+ contact);
			
			System.out.println("Contact saved");
			
//showing contact saved successfully on page	
			
			session.setAttribute("message", new Message("Contact saved successfully!!","success"));
			System.out.println(session.getAttribute("message"));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			e.printStackTrace();
//showing error in saving contact on page
			
			session.setAttribute("message", new Message("Something went wrong. Please try again","danger"));
		}
	
		return "user/addContact";
	}
	
//showing all contacts----------------------------------------------------------------------------
	
	
	@GetMapping("/viewContacts/{page}")
	public String viewContacts(@PathVariable("page") Integer page, Model model,Principal principal)
	{
		model.addAttribute("title","Your Contacts");
		String name = principal.getName();
		
		User user = this.userRepository.getUserByUserName(name);
		
		//two info - current page and contact per page
		Pageable pageable = PageRequest.of(page,5);
		
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		model.addAttribute("contacts",contacts);
		model.addAttribute("currentPage",page);
		model.addAttribute("totalPages", contacts.getTotalPages());
		
		
		return "user/viewContacts";
	}
	
//showing full details of contact -----------------------------------------------------------------------
	
	
	@GetMapping("/contact/{cId}")
	public String showDetails(@PathVariable("cId") Integer cId, Model model,Principal principal)
	{
		
		Optional<Contact> contactfound = this.contactRepository.findById(cId);
		Contact contact = contactfound.get();
		
		String username = principal.getName();
		User user = this.userRepository.getUserByUserName(username);
		
		
		if(user.getId()== contact.getUser().getId())
		{
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName()+" - Contact Detail");
		}
		return "/user/contactDetail";
	}
	
	//Delete the contact------------------------------------------------------------------
	
	
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId,Model model,Principal principal,HttpSession session)
	{
//		Optional<Contact> findById = this.contactRepository.findById(cId);
//		Contact contact = findById.get();
		Contact contact= this.contactRepository.findById(cId).get();  //getting contact by using id
		
		String username = principal.getName();
		User user = this.userRepository.getUserByUserName(username);
		
		
		if(user.getId()== contact.getUser().getId())
		{
			
			user.getContacts().remove(contact);
			this.userRepository.save(user);
			
			session.setAttribute("message", new Message("Contact deleted successfully!!","success"));
			System.out.println(session.getAttribute("message"));
		}
		
		return "redirect:/user/viewContacts/0";
	}
	
	// Update contact handler-------------------------------------------------------------------
	
	@PostMapping("/updateContact/{cId}")
	public String updateContact(@PathVariable("cId") Integer cId,Model model)
	{
		model.addAttribute("title","Update contact");
		
		Contact contact= this.contactRepository.findById(cId).get();
		model.addAttribute("contact",contact); 
		return "/user/updateContact";
	}
	
	
	//Update controller
	@PostMapping("/updatingContact/{cId}")
	public String updateContact(
			@ModelAttribute Contact contact,						 //fetches all data from contacts,where name matches with entity
			@RequestParam("profileImage") MultipartFile file,		 //RequestParam fetches specific field from form
			Model model, Principal principal,                         			 // fetch User data, here we fetches username by getName
			HttpSession session,
			@PathVariable("cId") Integer cId)									 // using session attribute to send alert in basehtml
	{
		try {
			
			

			
			// old contact details
			Contact oldContactDetail = this.contactRepository.findById(cId).get();
			System.out.println("-----------------------------Contact updated "+ oldContactDetail.getCid());
			if(!file.isEmpty())
			{
				//delete old file
				
				File deleteFile = new ClassPathResource("static/img").getFile(); 
				File file1 = new File(deleteFile, oldContactDetail.getImage());
				file1.delete();
					
				//update image file
				
				File saveFile = new ClassPathResource("static/img").getFile(); //file get upload in this folder
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				//path to take file  from
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);//copy the file and replace if existing
				
				contact.setImageUrl(file.getOriginalFilename());
			}
			else
			{
				contact.setImageUrl(oldContactDetail.getImage()); 
			}
			
			// User user = this.userRepository.getUserByUserName(principal.getName());
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			
			contact.setCid(cId);  //form doesn't contain cId,so setting it while updating form
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
			
			
			System.out.println("Data = "+ contact);
			
			System.out.println("-----------------------------Contact updated "+ contact.getName());
			System.out.println("-----------------------------Contact updated "+ contact.getCid());
			
//showing contact saved successfully on page	
			
			session.setAttribute("message", new Message("Contact updated successfully!!","success"));
			System.out.println(session.getAttribute("message"));
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
//showing error in saving contact on page
			
			session.setAttribute("message", new Message("Something went wrong. Please try again","danger"));
		}
	
		return "redirect:/user/contact/{cId}";
	}
	
	@GetMapping("/profile")
	public String profile(Model model)
	{
		model.addAttribute("title", "Profile");
		return "/user/profile";
	}
	
//	Handling setting controller
	@RequestMapping("/settings")
	public String setting(Model model)
	{
		model.addAttribute("title", "Settings");
		return "/user/settingPage";
	}
	
	@PostMapping("/passwordChange")
	public String passwordChange(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, Principal principal, HttpSession session)
	{
		User currentUser = this.userRepository.getUserByUserName(principal.getName());
		
		System.out.println("old password "+ oldPassword);
		System.out.println("new password "+ newPassword);
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword()))
		{
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("Password Changed", "success"));
		}
		else
		{
			session.setAttribute("message", new Message("Old Password is incorrect","warning"));
			
		}
			
		
		
		return "/user/settingPage";
	}
	
}
