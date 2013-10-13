<?php
/*  BanManagement � 2012, a web interface for the Bukkit plugin BanManager
    by James Mortemore of http://www.frostcast.net
	is licenced under a Creative Commons
	Attribution-NonCommercial-ShareAlike 2.0 UK: England & Wales.
	Permissions beyond the scope of this licence 
	may be available at http://creativecommons.org/licenses/by-nc-sa/2.0/uk/.
	Additional licence terms at https://raw.github.com/confuser/Ban-Management/master/banmanagement/licence.txt
*/
$nav = array(
	$language['nav-home'] => 'index.php',
	$language['nav-stats'] => 'index.php?action=servers'
);

$path = $_SERVER['HTTP_HOST'].str_replace('index.php', '', $_SERVER['SCRIPT_NAME']);
?>
<!DOCTYPE html>
<html lang="en">
	<head>
		<meta charset="utf-8" />
			<title><?php echo $language['title']; ?></title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0" />
		<meta name="description" content="<?php echo $language['description']; ?>" />
		<meta name="author" content="Frostcast" />
		<link rel="stylesheet" type="text/css" href="css/bootstrap.css" media="screen" />
		<link rel="stylesheet" type="text/css" href="css/core.css" />
		<link rel="stylesheet" type="text/css" href="css/theme.css" />
		<!--[if lt IE 9]><script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script><![endif]-->
	</head>
<body>
	<nav class="navbar navbar-fixed-top <?php if(isset($theme['navbar-dark']) && $theme['navbar-dark']){echo "navbar-inverse";} else {echo "navbar-default";} ?>" role="navigation">
		<div class="container">
		    <div class="navbar-header">
		    <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-nav-collapse">
		      <span class="sr-only">Toggle navigation</span>
		      <span class="glyphicon glyphicon-th-large"></span>
		    </button>
		    <a class="navbar-brand" href="index.php"><?php echo $language['brand']; ?></a>
		  </div>
		  <div class="collapse navbar-collapse navbar-nav-collapse">
		    <ul class="nav navbar-nav">
				<?php
					$request = basename($_SERVER['REQUEST_URI']);
					foreach($nav as $name => $link) {
				?>
				<li <?php if($request == $link) echo 'class="active"'; ?>><a href="<?php echo $link; ?>"><?php echo $name ?></a></li>
				<?php
					}
				?>
		    </ul>
		    	<?php 
		    		if(isset($_SESSION['admin']) && $_SESSION['admin']) {
				?>
				<ul class="nav navbar-nav navbar-right">
					<li>
			    		<div class="btn-group">
			    			<button type="button" class="btn <?php if(isset($theme['navbar-dark']) && $theme['navbar-dark']) {echo "btn-inverse";} else {echo "btn-info";} ?> navbar-btn" id="acp">Admin CP</button>
			    			<button type="button" class="btn <?php if(isset($theme['navbar-dark']) && $theme['navbar-dark']) {echo "btn-inverse";} else {echo "btn-info";} ?> navbar-btn" id="logout">Logout</button>
			    		</div>
	    			</li>
	    		</ul>
		    	<?php
		    	}
		    	?>
		  </div>
		</div>
	</nav>
<div id="container" class="container">
<?php
	if(!empty($errors)){
?>

<div class="alert alert-danger">
	<h1>Oh no! We've found an error</h1>
	You forgot to rename your settingsRename.php file to settings.php. Without this file, you cannot configure anything.
</div>

<?php
	}
?>