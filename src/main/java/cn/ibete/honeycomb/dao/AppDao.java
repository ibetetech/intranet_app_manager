package cn.ibete.honeycomb.dao;

import cn.ibete.honeycomb.model.App;
import cn.ibete.honeycomb.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface AppDao extends CrudRepository<App, String> {

    @Query("select a from App a where a.bundleID=:bundleID and a.platform=:platform and a.owner=:owner")
    App getByBundleIDAndPlatformAndOwner(@Param("bundleID") String bundleID, @Param("platform") String platform, @Param("owner") User owner);

    @Query("select a from App a where a.shortCode=:shortCode")
    App findByShortCode(@Param("shortCode") String shortCode);

    @Override
    @Query("select a from App a order by a.currentPackage.createTime desc ")
    Iterable<App> findAll();

    @Query("select a from App a where a.owner=:user order by a.currentPackage.createTime desc ")
    Iterable<App> findByUser(@Param("user") User user);
}
