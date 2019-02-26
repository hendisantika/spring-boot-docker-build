package com.hendisantika.springbootdockerbuild.controller;

import com.hendisantika.springbootdockerbuild.entity.Customer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by IntelliJ IDEA.
 * Project : spring-boot-docker-build
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Date: 2019-02-27
 * Time: 05:41
 */
@RestController
@RequestMapping("/customer")
public class CustomerController {

    @GetMapping("/{id}")
    public Customer GetCustomer(@PathVariable Long id) {
        return new Customer(id, "Customer" + id);
    }
}