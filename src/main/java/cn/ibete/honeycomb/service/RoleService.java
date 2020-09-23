package cn.ibete.honeycomb.service;


import org.springframework.stereotype.Service;
import cn.ibete.honeycomb.dao.RoleDao;
import cn.ibete.honeycomb.model.Role;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@Service
public class RoleService {
    @Resource
    private RoleDao roleDao;

    @Transactional
    public Role save(Role role) {
        return this.roleDao.save(role);
    }

}
