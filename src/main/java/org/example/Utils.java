package org.example;

import java.io.File;
import java.io.FileInputStream;

public class Utils {
    public static String readFullFile(String path) {
        String result = null;
        File file = new File(path);
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            result = new String(data, "UTF-8");
        }
        catch(Exception e) { e.printStackTrace(); }

        return result;
    }
}
