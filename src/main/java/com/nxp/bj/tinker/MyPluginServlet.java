package com.nxp.bj.tinker;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.google.common.collect.ImmutableMap;
// import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Maps;

public class MyPluginServlet extends HttpServlet {
	/**
     * 
     */
    private static final long serialVersionUID = -4405982449194776737L;

    private final Logger logger = LoggerFactory.getLogger(MyPluginServlet.class);

	private static final String PLUGIN_STORAGE_KEY = "com.nxp.bj.tinker.configui";

	private final SoyTemplateRenderer soyTemplateRenderer;
	private final UserManager userManager;
	private final LoginUriProvider loginUriProvider;
//	private final TemplateRenderer templateRenderer;
	private final PluginSettingsFactory pluginSettingsFactory;
	private Map<String, String> fields;
	
	
	public MyPluginServlet(UserManager userManager,
			LoginUriProvider loginUriProvider,
			SoyTemplateRenderer soyTemplateRenderer,
			PluginSettingsFactory pluginSettingsFactory) {
		this.userManager = userManager;
		this.loginUriProvider = loginUriProvider;
		this.soyTemplateRenderer = soyTemplateRenderer;
		this.pluginSettingsFactory = pluginSettingsFactory;
		
		fields = new HashMap<String, String>();
	}

	@Override
    public void doGet(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
		/*String username = userManager.getRemoteUsername(request);
    	if (username == null || !userManager.isSystemAdmin(username)) {
    		redirectToLogin(request, resp);
    		return;
    	}*/

    	/*Map<String, Object> context = Maps.newHashMap();
    	PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
    	
    	if (pluginSettings.get(PLUGIN_STORAGE_KEY + ".age") == null) {
    		pluginSettings.put(PLUGIN_STORAGE_KEY, "Enter an age here");
    	}

    	context.put("name", pluginSettings.get(PLUGIN_STORAGE_KEY + ".name"));
    	context.put("age", pluginSettings.get(PLUGIN_STORAGE_KEY + ".age"));*/

		resp.setContentType("text/html;charset=utf-8");
		
		fields.put("name", "Tomcat");
		fields.put("age", "22");
		
		soyTemplateRenderer.render(
	        resp.getWriter(),
	        PLUGIN_STORAGE_KEY + ":tinker-settings-serverside",
	        "com.atlassian.stash.repository.hook.ref.config",
	        ImmutableMap.<String, Object>builder().put("config", fields).build()
        );
		
//    	templateRenderer.render("admin.vm", resp.getWriter());
    }
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse response)
		throws ServletException, IOException {
		PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
		pluginSettings.put(PLUGIN_STORAGE_KEY + ".name", req.getParameter("name"));
		pluginSettings.put(PLUGIN_STORAGE_KEY + ".age", req.getParameter("age"));
		response.sendRedirect("test");
	}

    /*private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
//    	response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
    }
    
    private URI getUri(HttpServletRequest request) {	
    	StringBuffer builder = request.getRequestURL();
    	if (request.getQueryString() != null) {
    		builder.append("?");
    		builder.append(request.getQueryString());
    	}
    	return URI.create(builder.toString());
    }*/
}
