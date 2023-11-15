package com.springwebapp.app.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MessageController {

  @GetMapping("/")
  public String displayMessage(Model model) {

    model.addAttribute("message", "My Web App v0.1");
    return "messageView";
  }
}
