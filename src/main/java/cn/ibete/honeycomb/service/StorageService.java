package cn.ibete.honeycomb.service;


import org.springframework.stereotype.Service;
import cn.ibete.honeycomb.dao.StorageDao;
import cn.ibete.honeycomb.model.Storage;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@Service
public class StorageService {
    @Resource
    private StorageDao storageDao;

    @Transactional
    public Storage save(Storage storage) {
        return this.storageDao.save(storage);
    }

    @Transactional
    public Storage findByKey(String key) {
        return this.storageDao.findByKey(key);
    }
}
