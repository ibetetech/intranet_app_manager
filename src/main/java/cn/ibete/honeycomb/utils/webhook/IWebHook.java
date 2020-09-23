package cn.ibete.honeycomb.utils.webhook;

import cn.ibete.honeycomb.model.App;
import cn.ibete.honeycomb.storage.StorageUtil;

public interface IWebHook {
    void sendMessage(App app, String baseURL, StorageUtil storageUtil);
}
