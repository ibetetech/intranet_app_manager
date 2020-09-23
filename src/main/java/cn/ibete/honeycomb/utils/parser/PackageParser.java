package cn.ibete.honeycomb.utils.parser;

import cn.ibete.honeycomb.model.Package;

public interface PackageParser {
    // 解析包
    public Package parse(String filePath);
}
