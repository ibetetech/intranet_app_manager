package cn.ibete.honeycomb.dao;

import cn.ibete.honeycomb.model.WebHook;
import org.springframework.data.repository.CrudRepository;

public interface WebHookDao extends CrudRepository <WebHook, String > {

}
