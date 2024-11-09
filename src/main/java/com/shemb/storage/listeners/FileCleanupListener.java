package com.shemb.storage.listeners;

import com.shemb.storage.utils.FileUtils;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class FileCleanupListener implements ServletRequestListener {
    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        FileUtils.deleteTempPaths();
    }
}
