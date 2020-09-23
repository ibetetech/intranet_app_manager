package cn.ibete.honeycomb.service;


import cn.ibete.honeycomb.storage.StorageUtil;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import cn.ibete.honeycomb.dao.AppDao;
import cn.ibete.honeycomb.dao.PackageDao;
import cn.ibete.honeycomb.dao.UserDao;
import cn.ibete.honeycomb.model.App;
import cn.ibete.honeycomb.model.Package;
import cn.ibete.honeycomb.model.Storage;
import cn.ibete.honeycomb.model.User;
import cn.ibete.honeycomb.utils.CharUtil;
import cn.ibete.honeycomb.utils.file.PathManager;
import cn.ibete.honeycomb.utils.image.ImageUtils;
import cn.ibete.honeycomb.utils.parser.ParserClient;
import cn.ibete.honeycomb.vo.AppViewModel;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AppService {
    @Resource
    private AppDao appDao;
    @Resource
    private UserDao userDao;
    @Resource
    private PackageDao packageDao;
    @Resource
    private StorageUtil storageUtil;
    @Resource
    private PackageService packageService;

    @Transactional
    public App save(App app, User user) {
        user = this.userDao.findById(user.getId()).get();
        app.setOwner(user);
        app = this.appDao.save(app);
        app.getCurrentPackage();
        try {
            // 触发级联查询
            app.getWebHookList().forEach(webHook -> {
            });
        } catch (Exception e) {
        }
        return app;
    }

    @Transactional
    public List<AppViewModel> findAll(HttpServletRequest request) {
        Iterable<App> apps = this.appDao.findAll();
        List<AppViewModel> list = new ArrayList<>();
        for (App app : apps) {
            AppViewModel appViewModel = new AppViewModel(app, request, false);
            list.add(appViewModel);
        }
        return list;
    }

    @Transactional
    public AppViewModel getById(String appID, User user, HttpServletRequest request) {
        Optional<App> optionalApp = this.appDao.findById(appID);
        App app = optionalApp.get();
        if (!app.getOwner().getId().equalsIgnoreCase(user.getId())) {
            return null;
        }

        if (app != null) {
            app.getPackageList().forEach(aPackage -> {
                try {
                    aPackage.getSourceFile().getKey();
                    aPackage.getIconFile().getKey();
                } catch (Exception e) {
                }
            });
            AppViewModel appViewModel = new AppViewModel(app, request, true);
            return appViewModel;
        }
        return null;
    }

    @Transactional
    public App savePackage(Package aPackage, User user) {
        aPackage = this.packageDao.save(aPackage);
        user = this.userDao.findByUsername(user.getUsername());
        App app = this.appDao.getByBundleIDAndPlatformAndOwner(aPackage.getBundleID(), aPackage.getPlatform(), user);
        if (app == null) {
            app = new App();
            String shortCode = CharUtil.generate(4);
            while (this.appDao.findByShortCode(shortCode) != null) {
                shortCode = CharUtil.generate(4);
            }
            BeanUtils.copyProperties(aPackage, app);
            app.setShortCode(shortCode);
            app.setOwner(user);
        } else {
            // 级联查询
            app.getPackageList().forEach(aPackage1 -> {
            });
            app.getWebHookList().forEach(webHook -> {
            });
        }
        app.setName(aPackage.getName());
        app.getPackageList().add(aPackage);
        app.setCurrentPackage(aPackage);
        app = this.appDao.save(app);
        return app;
    }

    @Transactional
    public void deleteById(String id) {
        App app = this.appDao.findById(id).get();
        if (app != null) {
            app.getPackageList().forEach(aPackage -> {
                this.packageService.deletePackage(aPackage);
            });
            this.appDao.deleteById(id);
        }
    }

    /**
     * 通过 code 和 packageId 查询
     *
     * @param code
     * @param packageId
     * @return
     */
    @Transactional
    public AppViewModel findByCode(String code, String packageId, HttpServletRequest request) {
        App app = this.appDao.findByShortCode(code);
        AppViewModel viewModel = new AppViewModel(app, request, packageId);
        return viewModel;
    }

    @Transactional
    public List<AppViewModel> findByUser(User user, HttpServletRequest request) {
        Iterable<App> apps = this.appDao.findByUser(user);
        List<AppViewModel> list = new ArrayList<>();
        for (App app : apps) {
            AppViewModel appViewModel = new AppViewModel(app, request, false);
            list.add(appViewModel);
        }
        return list;
    }

    @Transactional
    public App addPackage(String filePath, Map<String, String> extra, User user) throws Exception {
        // 1. 构建包
        Package aPackage = ParserClient.parse(filePath);
        // 2. 文件转存
        storeFiles(filePath, aPackage);
        // 获取包对应的APP是否存在
        App app = getApp(user, aPackage);
        // 3. 更新包信息
        aPackage.setApp(app);
        // 4. 更新包信息
        aPackage = this.packageDao.save(aPackage);
        // 5. 更新app信息
        app.setName(aPackage.getName());
        app.getPackageList().add(aPackage);
        app.setCurrentPackage(aPackage);
        this.appDao.save(app);
        return app;
    }

    /**
     * 获取APP信息
     * @param user
     * @param aPackage
     * @return
     */
    @NotNull
    private App getApp(User user, Package aPackage) {
        App app = this.appDao.getByBundleIDAndPlatformAndOwner(aPackage.getBundleID(), aPackage.getPlatform(), user);
        if (app == null) {
            app = new App();
            String shortCode = CharUtil.generate(4);
            while (this.appDao.findByShortCode(shortCode) != null) {
                shortCode = CharUtil.generate(4);
            }
            BeanUtils.copyProperties(aPackage, app);
            app.setShortCode(shortCode);
            // 获取用户信息
            user = this.userDao.findById(user.getId()).get();
            app.setOwner(user);
        } else {
            // 级联查询
            app.getPackageList().forEach(aPackage1 -> {
            });
            app.getWebHookList().forEach(webHook -> {
            });
        }
        return app;
    }

    /**
     * 转存文件
     * @param filePath
     * @param aPackage
     * @throws IOException
     */
    private void storeFiles(String filePath, Package aPackage) throws IOException {
        // 2.1 源文件
        File sourceFile = new File(filePath);
        Storage storage = storageUtil.store(new FileInputStream(sourceFile), sourceFile.length(), "application/octet-stream", sourceFile.getName());
        FileUtils.forceDelete(sourceFile);
        aPackage.setSourceFile(storage);
        // 2.2 图标文件
        String iconFilePath = PathManager.getTempFilePath("png");
        ImageUtils.resize(aPackage.getIconFile().getUrl(), iconFilePath, 192, 192);
        File iconFile = new File(iconFilePath);
        Storage iconStorage = storageUtil.store(new FileInputStream(iconFile), iconFile.length(), "application/png", iconFile.getName());
        FileUtils.forceDelete(new File(aPackage.getIconFile().getUrl()));
        FileUtils.forceDelete(iconFile);
        aPackage.setIconFile(iconStorage);
    }
}
