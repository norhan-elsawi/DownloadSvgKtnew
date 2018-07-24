package com.ibtikar.downloadsvgkt.utils;

/**
 * Created by norhan.elsawi on 7/19/2018.
 */

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author jon
 */
public class Decompress {
    ProgressHelper progressHelper;

    public Decompress(String zipFile, String location, ProgressHelper progressHelper) {
        this.progressHelper = progressHelper;
        extractZip(zipFile, location);
    }

    private boolean extractZip(String pathOfZip, String pathToExtract) {

        int BUFFER_SIZE = 1024;
        int size;
        byte[] buffer = new byte[BUFFER_SIZE];

        try {
            File f = new File(pathToExtract);
            if (!f.isDirectory()) {
                f.mkdirs();
            }
            ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(pathOfZip), BUFFER_SIZE));
            try {
                ZipEntry ze = null;
                while ((ze = zin.getNextEntry()) != null) {
                    String path = pathToExtract + "/" + ze.getName();

                    if (ze.isDirectory()) {
                        File unzipFile = new File(path);
                        if (!unzipFile.isDirectory()) {
                            unzipFile.mkdirs();
                        }
                    } else {
                        FileOutputStream out = new FileOutputStream(path, false);
                        BufferedOutputStream fout = new BufferedOutputStream(out, BUFFER_SIZE);
                        try {
                            int total = 0;
                            while ((size = zin.read(buffer, 0, BUFFER_SIZE)) != -1) {
                                total += size;
                                fout.write(buffer, 0, size);
                                progressHelper.addProgress(total, ze.getSize());
                            }

                            zin.closeEntry();
                        } catch (Exception e) {
                            Log.e("Exception", "Unzip exception 1:" + e.toString());
                        } finally {
                            fout.flush();
                            fout.close();

                        }
                    }
                }
            } catch (Exception e) {
                Log.e("Exception", "Unzip exception2 :" + e.toString());
            } finally {
                zin.close();
            }
            return true;
        } catch (Exception e) {
            Log.e("Exception", "Unzip exception :" + e.toString());
        }
        return false;

    }
}