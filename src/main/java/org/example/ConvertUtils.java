package org.example;

import com.sun.jna.Platform;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.jar.JarFile;


public class ConvertUtils {

    private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
    private static final String TEMP_DIR = System.getProperty(JAVA_IO_TMPDIR);
    private static final String JNA_LIBRARY_PATH = "jna.library.path";

    private static final String SPRING_JAR = "BOOT-INF/classes";

    private static final String JNA_LIB_PATH = getJnaLibraryPath();

    public static void main(String[] args) throws URISyntaxException, IOException {
        System.setProperty("jna.encoding", "UTF-8");
        String jnaLibraryPath = getJnaLibraryPath();
        System.out.println("jnaLibraryPath = " + jnaLibraryPath);
        System.out.println(File.separator);
        //windows
        //ConvertUtils.convertTiff("D:\\20230706131629.tiff", "d:\\tiff");
        //linux
        ConvertUtils.convertTiff("/home/hejian/gdal_test/20230706131629.tiff", "/home/hejian/gdal_test/tiff");
    }

    private static String getJnaLibraryPath() {
        try {
            URL current_jar_dir = ConvertUtils.class.getProtectionDomain().getCodeSource().getLocation();
            System.out.println("===getJnaLibraryPath===.current_jar_dir = " + current_jar_dir);
            Path jar_path;
            String path = Platform.RESOURCE_PREFIX;
            if (current_jar_dir.getPath().contains(SPRING_JAR)) {
                jar_path = Paths.get(current_jar_dir.toString().substring(10, current_jar_dir.toString().indexOf(SPRING_JAR) - 2));
                path = SPRING_JAR + "/" + Platform.RESOURCE_PREFIX;
            } else {
                jar_path = Paths.get(current_jar_dir.toURI());
            }
            System.out.println("===getJnaLibraryPath===.jar_path = " + jar_path);
            String folderContainingJar = jar_path.getParent().toString();
            System.out.println("===getJnaLibraryPath===.folderContainingJar = " + folderContainingJar);
            ResourceCopy r = new ResourceCopy();
            Optional<JarFile> jar = r.jar(ConvertUtils.class);
            if (jar.isPresent()) {
                System.out.println("JAR detected");
                File target_dir = new File(folderContainingJar);
                System.out.println(String.format("Trying copy from %s %s to %s", jar.get().getName(), path, target_dir));
                // perform dir copy
                r.copyResourceDirectory(jar.get(), path, target_dir);
                // add created folders to JNA lib loading path
                System.setProperty(JNA_LIBRARY_PATH, target_dir.getCanonicalPath());
                return target_dir.getCanonicalPath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Thread.currentThread().getContextClassLoader().getResource("").getPath().substring(1) + Platform.RESOURCE_PREFIX;
    }

    public static boolean convertTiff(String input, String output) {
        System.out.println("jnaLibraryPath = " + JNA_LIB_PATH);
        String cmdStr = "%s  -v -r bilinear -levels 8 -ps 512 512 -co “TILED=YES”  -targetDir %s %s";
        if (Platform.RESOURCE_PREFIX.contains("win")) {
            openExe(String.format(cmdStr, JNA_LIB_PATH + "/gdal_retile.exe", output, input));
        } else if (Platform.RESOURCE_PREFIX.contains("linux")) {
            String shell = JNA_LIB_PATH + "/gdal_retile";
            openExe("chmod u+x " + shell);
            openExe(String.format(cmdStr, shell, output, input));
        }
        return true;
    }

    private static void openExe(String cmd) {
        BufferedReader br = null;
        BufferedReader brError = null;
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            String line = null;
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            brError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = br.readLine()) != null || (line = brError.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
