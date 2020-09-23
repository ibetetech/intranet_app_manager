package cn.ibete.honeycomb.utils.parser;

import cn.ibete.honeycomb.model.Package;
import org.apache.commons.io.FilenameUtils;

public class ParserClient {

    /**
     * 解析包
     * @param filePath 文件路径
     * @return
     */
    public static Package parse(String filePath) throws ClassNotFoundException {
        PackageParser parser = getParser(filePath);
        if (parser != null) {
            return parser.parse(filePath);
        }
        return null;
    }

    /**
     * 根据文件后缀名获取解析器
     * @param filePath
     * @return
     */
    private static PackageParser getParser(String filePath) throws ClassNotFoundException {
        String extension = FilenameUtils.getExtension(filePath);
        // 动态获取解析器
        Class aClass = Class.forName("cn.ibete.utils.parser." + extension.toUpperCase()+"Parser");
        try {
            PackageParser packageParser = (PackageParser)aClass.newInstance();
            return packageParser;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
