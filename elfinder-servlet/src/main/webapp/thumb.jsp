<%@page import="my.sample.MySampleConfig"%>
<%@page import="java.io.ByteArrayInputStream"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<%@ page language="java" contentType="image/png" pageEncoding="UTF-8"%>
<%@page import="java.util.Calendar"%>
<%@page import="org.apache.commons.lang.time.DateUtils"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="com.mortennobel.imagescaling.DimensionConstrain"%>
<%@page import="com.mortennobel.imagescaling.ResampleOp"%>
<%@page import="javax.imageio.ImageIO"%>
<%@page import="java.awt.image.BufferedImage"%>
<%@page import="java.io.File"%>
<%
	int width = (!StringUtils.isEmpty(request.getParameter("w"))) ? Integer.valueOf(request.getParameter("w")) : 150;
	String path = request.getParameter("p");
	
	BufferedImage b =null;

	BufferedImage image = ImageIO.read(new File(MySampleConfig.HOME_SHARED_DOCS+path));

	ResampleOp rop = new ResampleOp(DimensionConstrain.createMaxDimension(width, -1));
	rop.setNumberOfThreads(4);
	b = rop.filter(image, null);
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	ImageIO.write(b, "png", baos);
	byte[] bytesOut = baos.toByteArray();
		
	response.setHeader("Last-Modified", DateUtils.addDays(Calendar.getInstance().getTime(), 2*360).toGMTString());
	response.setHeader("Expires", DateUtils.addDays(Calendar.getInstance().getTime(), 2*360).toGMTString());
	out.clear();
	ImageIO.write(b, "png", response.getOutputStream());
%>
