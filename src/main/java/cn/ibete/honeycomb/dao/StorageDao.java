package cn.ibete.honeycomb.dao;

import cn.ibete.honeycomb.model.Storage;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface StorageDao extends CrudRepository<Storage, String> {

    @Query("select s from Storage s where s.key=:key")
    public Storage findByKey(@Param("key") String key);
}
