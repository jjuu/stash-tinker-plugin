package com.nxp.bj.tinker;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

// TODO remove this class
public class TinkerBatchUploadIPOwnerServlet extends HttpServlet {
    
    @Override
    public void init() throws ServletException {
        
    }
    
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if(!ServletFileUpload.isMultipartContent(req)){
            throw new ServletException("Content type is not multipart/form-data");
        }
        
//        resp.setContentType("text/plain;charset=utf-8");
        req.setCharacterEncoding("utf-8");
        
        DiskFileItemFactory dff=new DiskFileItemFactory();
        dff.setSizeThreshold(1024000);
        ServletFileUpload sfu=new ServletFileUpload(dff);
        sfu.setFileSizeMax(5000000);
        sfu.setSizeMax(10000000);
        
        try {
            FileItemIterator fii=sfu.getItemIterator(req);
            StringBuffer stringBuffer=new StringBuffer();
            while(fii.hasNext()) {
                FileItemStream fis=fii.next();
                if(!fis.isFormField() && fis.getName().length() > 0) {
                    BufferedInputStream in=new BufferedInputStream(fis.openStream());
                    BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(in));
                    String lineTxt=null;
                    
                    while((lineTxt=bufferedReader.readLine()) != null) {
                        stringBuffer.append(lineTxt + ",");
                    }
                   
                }
            }
        } catch (FileUploadException e) {
            e.printStackTrace();
        }
    }
}
