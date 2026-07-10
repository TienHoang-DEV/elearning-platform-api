package vn.edu.fpt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ManagerGrantAccessController {

    @GetMapping("/manager/grant-access")
    public String managerGrantAccess() {
        return "manager/grant-access/grant-access";
    }
}
