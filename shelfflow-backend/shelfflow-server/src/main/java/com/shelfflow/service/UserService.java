package com.shelfflow.service;

import com.shelfflow.dto.UserLoginDTO;
import com.shelfflow.entity.User;

public interface UserService {

    User login(UserLoginDTO userLoginDTO);
}
