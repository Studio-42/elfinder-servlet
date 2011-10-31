<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    	               "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<!-- jQuery and jQuery UI -->
<script src="js/jquery-1.6.1.min.js" type="text/javascript"
	charset="utf-8"></script>
<script src="js/jquery-ui-1.8.13.custom.min.js" type="text/javascript"
	charset="utf-8"></script>
<link rel="stylesheet" href="css/smoothness/jquery-ui-1.8.13.custom.css"
	type="text/css" media="screen" charset="utf-8">

<!-- elRTE -->
<script src="js/elrte.min.js" type="text/javascript" charset="utf-8"></script>
<link rel="stylesheet" href="css/elrte.min.css" type="text/css"
	media="screen" charset="utf-8">

<!-- elRTE translation messages -->
<script src="js/i18n/elrte.ru.js" type="text/javascript" charset="utf-8"></script>

 <script src="js/elfinder.min.js" type="text/javascript" charset="utf-8"></script>
 <link rel="stylesheet" href="css/elfinder.css" type="text/css" media="screen" charset="utf-8">

<script type="text/javascript" charset="utf-8">
	$().ready(function() {
	
		var opts = {
			cssClass : 'el-rte',
			height : 450,
			toolbar : 'complete',
			cssfiles : [ 'css/elrte-inner.css' ],
			fmOpen : function(callback) {
				$('<div id="myelfinder" />').elfinder({
					url : '/servlet/ElrteConnector',
					lang : 'en',
					dialog : { width : 900, modal : true, title : 'elFinder - file manager for web' },
					closeOnEditorCallback : true,
					editorCallback : callback
				})
			}
		}
		$('#editor').elrte(opts);
	})
</script>

<style type="text/css" media="screen">
body {
	padding: 20px;
}
</style>
</head>
<body>
	<h1>Hello World!</h1>
	<div id="editor"></div>
</body>
</html>
