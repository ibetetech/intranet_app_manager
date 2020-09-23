package cn.ibete.honeycomb.dao;

import cn.ibete.honeycomb.model.Package;
import org.springframework.data.repository.CrudRepository;

public interface PackageDao extends CrudRepository <Package, String > {

}
