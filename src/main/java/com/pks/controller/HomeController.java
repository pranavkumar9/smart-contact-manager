package com.pks.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.pks.Entities.User;
import com.pks.dao.UserRepository;
import com.pks.helper.Message;

@Controller
public class HomeController {
	
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder PasswordEncoder;
	
	@RequestMapping("/")
	public String home(Model model)
	{
		model.addAttribute("title","Home-SmartContactManager");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model model)
	{
		model.addAttribute("title","About-SmartContactManager");
		return "about";
	}
	
	@RequestMapping("/signup")
	public String register(Model model)
	{
		model.addAttribute("title","Register-SmartContactManager");
		model.addAttribute("user",new User());
		return "signup";
	}
	
	@RequestMapping(value="/do_register", method=RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("User") User user ,BindingResult res, @RequestParam(value="agreement",defaultValue="false") 
								boolean agreement, Model model, HttpSession session)
	{
		try 
		{
			
			
			if(!agreement)
			{
				System.out.println("Not Accepted the terms and conditions");
				throw new Exception("Not Accepted the terms and conditions");
			
			}
			if(user.getEmail() == "") {
				System.out.println("name can't be blank");
				throw new Exception("name can't be blank");
			}
			
			
			if(res.hasErrors())
			{
				System.out.println("Error "+ res.toString());
				model.addAttribute("user",user);
				return "signup";
			}
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(PasswordEncoder.encode(user.getPassword()));
			
			
			User result = this.userRepository.save(user);
			model.addAttribute("user",new User());
			System.out.println("User "+ result);
			session.setAttribute("message",new Message("Successfully registered", "alert-success"));
			return "signup";
			

		} 
		catch (Exception e)
		{
			model.addAttribute("user",user);
			e.printStackTrace();
			session.setAttribute("message", new Message("something went wrong ", "alert-danger"));
			return "signup";
		}
	}
	
	

}
