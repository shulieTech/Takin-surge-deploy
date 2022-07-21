package io.shulie.surge.data.deploy.pradar.servlet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;

import com.google.inject.Singleton;
import io.shulie.surge.data.deploy.pradar.common.VersionRegister;

@Singleton
public class VersionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json;charset=utf-8");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Properties gitVersion = VersionRegister.readGitVersion();
        resp.getWriter().println(JSON.toJSONString(gitVersion));
    }
}
