package com.connect.rest;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.NotesException;
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.domino.services.util.JsonWriter;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonParser;
import com.openntf.xsnippets.HTMLEMail;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

public class ApprovalsService {

	static public final int POST = 0;
	static public final int PUT = 1;
	static public final int DELETE = 2;

	static public void renderService() {
		FacesContext ctx = FacesContext.getCurrentInstance();
		ExternalContext exCon = ctx.getExternalContext();
		HttpServletRequest request = (HttpServletRequest) exCon.getRequest();

		String method = request.getMethod();
		if ("GET".equalsIgnoreCase(method)) {
			renderServiceJSONGet();
		} else if ("POST".equalsIgnoreCase(method)) {
			renderServiceJSONUpdate(POST);
		} else if ("PUT".equalsIgnoreCase(method)) {
			renderServiceJSONUpdate(PUT);
		} else if ("DELETE".equalsIgnoreCase(method)) {
			renderServiceJSONDelete();
		}
	}

	@SuppressWarnings("unchecked")
	static public void renderServiceJSONUpdate(int method) {
		try {
			FacesContext ctx = FacesContext.getCurrentInstance();
			ExternalContext exCon = ctx.getExternalContext();
			HttpServletRequest request = (HttpServletRequest) exCon.getRequest();
			HttpServletResponse response = (HttpServletResponse) exCon.getResponse();

			String reqContentType = request.getContentType();
			if (!reqContentType.contains("application/json")) {
				response.sendError(204, "application/json Data Required");
				return;
			}

			String id = null;
			String pathInfo = exCon.getRequestPathInfo();
			try{
				id = pathInfo.substring(pathInfo.lastIndexOf("/") + 1);
			}catch(Exception e){
				id = "";
			}
			
			Database database = ExtLibUtil.getCurrentDatabase();
			Document document = null;
			if (id.length() == 32){
				document = database.getDocumentByUNID(id);
			}
			if (document == null && method == POST){
				response.sendError(404, "Couldn't locate Request " + id + ", use PUT if this is a new request");
				return;
			}else if(document != null && method == PUT){
				response.sendError(403, "Request already exists, use POST to update");
				return;
			}else if (document == null){
				document = database.createDocument();
			}

			JsonJavaObject json = null;
			JsonJavaFactory factory = JsonJavaFactory.instanceEx;

			try {
				Reader r = request.getReader();
				try {
					json = (JsonJavaObject) JsonParser.fromJson(factory, r);
					Iterator<String> props = json.getJsonProperties();
					while (props.hasNext()){
						String key = (String)props.next();
						Object value = (Object)json.getJsonProperty(key);
						if (value instanceof ArrayList){
							ArrayList list = (ArrayList)value;
							//We need to create the item and then append the values
							Item item = document.replaceItemValue(key, "");
							Iterator values = list.iterator();
							while(values.hasNext())
								item.appendToTextList((String)values.next());
						}else{
							document.replaceItemValue(key, value);
						}
					}
				} finally {
					r.close();
				}
				document.computeWithForm(false, false);
				document.save();
				HTMLEMail.sendNotification(document);
				
			} catch (Exception ex) {
				response.sendError(500, "renderServiceJSONGet: " + ex.getMessage());
				ex.printStackTrace();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	static public void renderServiceJSONGet() {
		
		try {
			FacesContext ctx = FacesContext.getCurrentInstance();
			ExternalContext exCon = ctx.getExternalContext();
			ResponseWriter writer = ctx.getResponseWriter();
			HttpServletResponse response = (HttpServletResponse) exCon.getResponse();
			try {

				response.setContentType("application/json");
				response.setHeader("Cache-Control", "no-cache");
				response.setCharacterEncoding("utf-8");

				String id = null;
				String pathInfo = exCon.getRequestPathInfo();
				id = pathInfo.substring(pathInfo.lastIndexOf("/") + 1);

				boolean compact = false;
				JsonWriter g = new JsonWriter(writer, compact);
				
				Database database = ExtLibUtil.getCurrentDatabase();
				try{
					Document document = database.getDocumentByUNID(id);
					if (document != null) {
						g.startObject();
						Vector<Item> items = document.getItems();
						Iterator<Item> it = items.iterator();
						while (it.hasNext()) {
							Item item = it.next();
							g.startProperty(item.getName().toLowerCase());
							try {
								if (item.getValues()!= null) {
									if (item.getValues().size() == 1){
										g.outStringLiteral(item.getText());
									}else{
										g.startArray();
										Iterator<?> values = item.getValues().iterator();
										while (values.hasNext()) {
											g.startArrayItem();
											g.outStringLiteral(values.next().toString());
											g.endArrayItem();
										}
										g.endArray();
									}
								} else {
									g.outStringLiteral("");
								}
							} catch (Exception e) {
								g.outStringLiteral("");
							}
							g.endProperty();
						}
						g.endObject();
					}else{
						response.setStatus(404);
					}
				}catch(NotesException e){
					response.setStatus(404);
				}

			} catch (Exception ex) {
				response.sendError(500, "renderServiceJSONGet: " + ex.getMessage());
				ex.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static public void renderServiceJSONDelete(){
		try {
			FacesContext ctx = FacesContext.getCurrentInstance();
			ExternalContext exCon = ctx.getExternalContext();
			HttpServletResponse response = (HttpServletResponse) exCon.getResponse();
			try {

				response.setHeader("Cache-Control", "no-cache");

				String id = null;
				String pathInfo = exCon.getRequestPathInfo();
				id = pathInfo.substring(pathInfo.lastIndexOf("/") + 1);

				Database database = ExtLibUtil.getCurrentDatabase();
				Document document = database.getDocumentByUNID(id);
				if (document != null){
					document.remove(true);
					response.setStatus(200);
				}else{
					response.setStatus(404);
				}
			}catch(Exception e){
				response.sendError(500, "renderServiceJSONDelete: " + e.getMessage());
				e.printStackTrace();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	

}