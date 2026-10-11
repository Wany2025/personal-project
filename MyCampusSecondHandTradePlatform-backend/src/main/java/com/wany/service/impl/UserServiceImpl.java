package com.wany.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wany.dto.UserLoginDTO;
import com.wany.dto.UserRegisterDTO;
import com.wany.entity.User;
import com.wany.exception.BusinessException;
import com.wany.mapper.UserMapper;
import com.wany.service.UserService;
import com.wany.utils.JwtUtil;
import com.wany.vo.UserInfoVO;
import com.wany.vo.UserLoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    @Override
    public String register(UserRegisterDTO registerDTO) {
        // 注册场景：密码必填
        if (StrUtil.isBlank(registerDTO.getPassword())) {
            throw new BusinessException("密码不能为空");
        }

        // 检查用户名是否存在
        LambdaQueryWrapper<User> usernameWrapper = new LambdaQueryWrapper<>();
        usernameWrapper.eq(User::getUsername, registerDTO.getUsername());
        if (userMapper.selectOne(usernameWrapper) != null) {
            throw new BusinessException("用户名已存在");
        }

        // 检查学号是否存在
        LambdaQueryWrapper<User> studentIdWrapper = new LambdaQueryWrapper<>();
        studentIdWrapper.eq(User::getStudentId, registerDTO.getStudentId());
        if (userMapper.selectOne(studentIdWrapper) != null) {
            throw new BusinessException("学号已存在");
        }

        // 创建用户
        User user = new User();
        BeanUtil.copyProperties(registerDTO, user);
        user.setPassword(MD5.create().digestHex(registerDTO.getPassword()));
        user.setCreditScore(100);
        user.setStatus(0);

        userMapper.insert(user);
        return "注册成功";
    }

    @Override
    public UserLoginVO login(UserLoginDTO loginDTO) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 验证密码
        String encryptedPassword = MD5.create().digestHex(loginDTO.getPassword());
        if (!encryptedPassword.equals(user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 1) {
            throw new BusinessException("用户已被禁用");
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getUsername(), user.getId());

        // 构建返回结果
        UserLoginVO loginVO = new UserLoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setRealName(user.getRealName());
        loginVO.setAvatar(user.getAvatar());
        loginVO.setCreditScore(user.getCreditScore());

        return loginVO;
    }

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtil.copyProperties(user, userInfoVO);
        return userInfoVO;
    }

    @Override
    public void updateUser(Long userId, UserRegisterDTO updateDTO) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 检查用户名是否被其他用户使用
        if (!user.getUsername().equals(updateDTO.getUsername())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, updateDTO.getUsername())
                   .ne(User::getId, userId);
            if (userMapper.selectOne(wrapper) != null) {
                throw new BusinessException("用户名已存在");
            }
        }

        // 更新用户信息
        BeanUtil.copyProperties(updateDTO, user, "id", "creditScore", "createTime", "password");
        if (updateDTO.getPassword() != null && !updateDTO.getPassword().isEmpty()) {
            user.setPassword(MD5.create().digestHex(updateDTO.getPassword()));
        }

        userMapper.updateById(user);
    }

    @Override
    public UserInfoVO getUserProfile(Long userId, Long profileId) {
        if (userId.equals(profileId)) {
            return getUserInfo(userId);
        }

        User user = userMapper.selectById(profileId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setId(user.getId());
        userInfoVO.setUsername(user.getUsername());
        userInfoVO.setRealName(user.getRealName());
        userInfoVO.setCollege(user.getCollege());
        userInfoVO.setAvatar(user.getAvatar());
        userInfoVO.setCreditScore(user.getCreditScore());
        userInfoVO.setCreateTime(user.getCreateTime());

        return userInfoVO;
    }
}