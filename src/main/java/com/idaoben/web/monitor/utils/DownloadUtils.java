package com.idaoben.web.monitor.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class DownloadUtils {

    public static void sendFileToClient(File file, HttpServletResponse response) throws IOException {
        sendFileToClient(file, file.getName(), response);
    }

    public static void sendFileToClient(File file, String name, HttpServletResponse response) throws IOException {
        if(file.exists()){
            String type = URLConnection.getFileNameMap().getContentTypeFor(name);
            response.setContentType(type);
            response.setContentLength((int)file.length());
            if(StringUtils.isNotEmpty(name)){
                response.addHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", URLEncoder.encode(name, "UTF-8")));
            }

            BufferedInputStream in = null;
            BufferedOutputStream out = null;
            try{
                in = new BufferedInputStream(new FileInputStream(file));
                out = new BufferedOutputStream(response.getOutputStream());
                IOUtils.copy(in, out);
                out.close();
            } finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    public static void sendErrorFileToClient(String error, HttpServletResponse response) throws IOException {
        String fileName = "error.txt";
        byte[] errorContent = error.getBytes(StandardCharsets.UTF_8);
        String type = URLConnection.getFileNameMap().getContentTypeFor(fileName);
        response.setContentType(type);
        response.setContentLength(errorContent.length);
        if(StringUtils.isNotEmpty(fileName)){
            response.addHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", URLEncoder.encode(fileName, "UTF-8")));
        }

        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        try{
            in = new BufferedInputStream(new ByteArrayInputStream(errorContent));
            out = new BufferedOutputStream(response.getOutputStream());
            IOUtils.copy(in, out);
            out.close();
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }
}
