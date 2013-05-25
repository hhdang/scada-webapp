package com.ht.scada.web.service.impl;

import com.ht.scada.security.dao.UserDao;
import com.ht.scada.web.entity.UserExtInfo;
import com.ht.scada.web.dao.UserExtInfoDao;
import com.ht.scada.web.service.UserExtInfoService;
import java.util.List;
import javax.inject.Inject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author admin
 */
@Transactional
@Service("userExtInfoService")
public class UserExtInfoServiceImpl implements UserExtInfoService {

    @Inject
    private UserDao userDao;
    @Inject
    private UserExtInfoDao userExtInfoDao;

    @Override
    public List<UserExtInfo> getAllUserExtInfo() {

        return userExtInfoDao.findAll();
    }

    @Override
    public void saveUserExtInfo(UserExtInfo newUser) {
        userExtInfoDao.save(newUser);
    }

    @Override
    public UserExtInfo findUserExtInfoByUserID(int id) {
        return userExtInfoDao.findByUserID(id);
    }
}
