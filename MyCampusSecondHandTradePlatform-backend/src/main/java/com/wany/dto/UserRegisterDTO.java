package com.wany.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
public class UserRegisterDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    // 注册时必填（由控制器/服务层区分场景），更新时可为空（不修改密码）
    private String password;

    @NotBlank(message = "学号不能为空")
    private String studentId;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @NotBlank(message = "学院不能为空")
    private String college;

    @NotBlank(message = "宿舍号不能为空")
    private String dorm;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}