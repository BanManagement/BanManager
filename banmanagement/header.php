<?php
$nav = array(
	'Home' => 'index.php',
	'Servers' => 'index.php?action=servers'
);
if(isset($_SESSION['admin']) && $_SESSION['admin']) {
	$nav['Admin'] = 'index.php?action=admin';
	$nav['Logout'] = 'index.php?action=logout';
}
?>
<!DOCTYPE html>
<html lang="en">
	<head>
		<meta charset="utf-8">
		<title>Ban Management, from Frostcast</title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<meta name="description" content="">
		<meta name="author" content="">

		<!-- Le styles -->
		<link href="css/bootstrap.min.css" rel="stylesheet">
		<link href="css/bootstrap-responsive.min.css" rel="stylesheet">

		<link href="css/style.css" rel="stylesheet">
		
		<!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
		<!--[if lt IE 9]>
		  <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
		<![endif]-->
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
		<script src="js/bootstrap.min.js"></script>
		<script src="http://ajax.aspnetcdn.com/ajax/jquery.validate/1.9/jquery.validate.min.js"></script>
		<script src="js/heartcode-canvasloader-min.js"></script>
		<script src="js/jquery.countdown.min.js"></script>
		<script src="js/core.js"></script>
	</head>
	<body>
		<div class="navbar navbar-fixed-top">
			<div class="navbar-inner">
				<div class="container">
					<a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
						<span class="icon-bar"></span>
						<span class="icon-bar"></span>
						<span class="icon-bar"></span>
					</a>
					<a class="brand" href="index.php">Ban Management</a>
					<div class="nav-collapse">
						<ul class="nav">
						<?php
						$request = basename($_SERVER['REQUEST_URI']);
						foreach($nav as $name => $link) {
							?><li<?php
							if($request == $link)
								echo ' class="active"';
							?>><a href="<?php echo $link; ?>"><?php echo $name ?></a><?php
						}
						?>
						</ul>
					</div><!--/.nav-collapse -->
				</div>
			</div>
		</div>
		<div id="container" class="container">