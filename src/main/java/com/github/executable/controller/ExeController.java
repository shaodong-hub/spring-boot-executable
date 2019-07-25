package com.github.executable.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 创建时间为 14:11 2019-07-25
 * 项目名称 spring-boot-executable
 * </p>
 *
 * @author 石少东
 * @version 0.0.1
 * @since 0.0.1
 */
@RestController
public class ExeController {

    @GetMapping("/test")
    public String test() {
        return "Fine";
    }


}
