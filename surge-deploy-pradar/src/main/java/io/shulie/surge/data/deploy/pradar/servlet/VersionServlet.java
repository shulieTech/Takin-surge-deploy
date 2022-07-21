package io.shulie.surge.data.deploy.pradar.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;

import com.google.inject.Singleton;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

@Singleton
public class VersionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json;charset=utf-8");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Resource resource = new DefaultResourceLoader().getResource("classpath:git.properties");
        Properties gitVersion = new Properties();
        if (resource.exists()) {
            try (InputStream stream = resource.getInputStream()) {
                gitVersion.load(stream);
            }
        }
        resp.getWriter().println(JSON.toJSONString(gitVersion));
    }
}
