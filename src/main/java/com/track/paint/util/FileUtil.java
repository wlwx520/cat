package com.track.paint.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class FileUtil {

	public static String getAppRoot() {
		return new File("").getAbsolutePath();
	}

	public static void createDirAndFileIfNotExists(File file) {
		File parentFile = file.getParentFile();
		if (!parentFile.exists()) {
			parentFile.mkdirs();
		}
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void deleteFile(File f, boolean flg) {
		subDir(f).forEach(item -> {
			deleteFile(item, true);
		});

		subFile(f).forEach(item -> {
			item.delete();
		});
		if (flg) {
			f.delete();
		}
	}

	public static List<File> subDir(File parent) {
		File[] listFiles = parent.listFiles(file -> {
			return file.isDirectory();
		});
		if (listFiles != null) {
			return Arrays.asList(listFiles);
		}
		return new ArrayList<>();
	}

	public static List<File> subFile(File parent) {
		File[] listFiles = parent.listFiles(file -> {
			return file.isFile();
		});
		if (listFiles != null) {
			return Arrays.asList(listFiles);
		}
		return new ArrayList<>();
	}

	public static String fileToBase64(File file) {
		try (FileInputStream inputFile = new FileInputStream(file);) {
			byte[] buffer = new byte[(int) file.length()];
			inputFile.read(buffer);
			inputFile.close();
			return new String(Base64.getEncoder().encode(buffer));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
