package gifview.aven.gifviewdemo;

import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * 文件操作类
 * @author Aven
 **/
public class FileUtils {

    private static final String TAG = "FileUtils";
    private static final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/GifView";

    /**
     * <br>功能简述:判断SD卡是否存在
     * <br>功能详细描述:
     * <br>注意:
     **/
    public static boolean isSdcardExist() {
        boolean isSdcard = Environment.getExternalStorageDirectory().getAbsolutePath() != null;
        if (!isSdcard) {
            Log.d(TAG, "SD Card is not exist");
        }
        return isSdcard;
    }

    /**
     * <br>功能简述:判断文件是否存在
     * @param fileName 文件名
     * @return true : false
     */
    public static boolean isFileExist(String fileName) {
        if(!isSdcardExist()) {
            return false;
        }
        File file = new File(FILE_PATH,fileName);
        if (file.exists()) {
            return true;
        }
        return false;
    }

    /**
     * <br>功能简述:将 inputStream 写到文件
     * @param fileName 文件名
     * @param inputStream inputStream
     * @return true 成功 false 失败
     */
    public synchronized static boolean writeInputStreamToSdcard(String fileName, InputStream inputStream) {
        if(!isSdcardExist()) {
            return false;
        }
        File dir = new File(FILE_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String filePath = FILE_PATH + "/" + fileName;
        try {
            File newFile = new File(filePath);
            if (!newFile.exists()) {
                newFile.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            byte[] buffer = inputStreamTobyte(inputStream);
            fileOutputStream.write(buffer);
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * <br>功能简述:从SD Card中读出 inputStream
     * @param fileName 文件名
     * @return 返回 inputStream，如果读取失败，则返回null
     */
    public synchronized static InputStream getInputStreamFromSdcard(String fileName) {
        if(!isSdcardExist()) {
            return null;
        }
        String filePath = FILE_PATH + "/" + fileName;
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        int length = 0;
        ByteArrayInputStream is = null;
        try {
            FileInputStream fin = new FileInputStream(filePath);
            length = fin.available();
            byte [] buffer = new byte[length];
            fin.read(buffer);
            is = new ByteArrayInputStream(buffer);
            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return is;
    }

    /**
     * MD5加码 32位
     * @param inStr
     * @return
     */
    public static String transStringToMd5(String inStr) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
            return "";
        }
        char[] charArray = inStr.toCharArray();
        byte[] byteArray = new byte[charArray.length];
        for (int i = 0; i < charArray.length; i++) {
            byteArray[i] = (byte) charArray[i];
        }

        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();

        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }

        return hexValue.toString();
    }

    public static byte[] inputStreamTobyte(InputStream inStream)
            throws IOException {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while ((rc = inStream.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        byte[] in2b = swapStream.toByteArray();
        return in2b;
    }


}
