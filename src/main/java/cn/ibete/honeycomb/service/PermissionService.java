package cn.ibete.honeycomb.service;


import org.springframework.stereotype.Service;
import cn.ibete.honeycomb.dao.PermissionDao;
import cn.ibete.honeycomb.model.Permission;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@Service
public class PermissionService {
    @Resource
    private PermissionDao permissionDao;

    @Transactional
    public Permission save(Permission permission) {
        return this.permissionDao.save(permission);
    }
}
