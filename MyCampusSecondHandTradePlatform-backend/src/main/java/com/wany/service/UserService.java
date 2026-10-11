package com.wany.service;

import com.wany.dto.UserLoginDTO;
import com.wany.dto.UserRegisterDTO;
import com.wany.vo.UserInfoVO;
import com.wany.vo.UserLoginVO;

public interface UserService {

    String register(UserRegisterDTO registerDTO);

    UserLoginVO login(UserLoginDTO loginDTO);

    UserInfoVO getUserInfo(Long userId);

    void updateUser(Long userId, UserRegisterDTO updateDTO);

    UserInfoVO getUserProfile(Long userId, Long profileId);
}