<?php
/*  BanManagement © 2012, a web interface for the Bukkit plugin BanManager
    by James Mortemore of http://www.frostcast.net
	is licenced under a Creative Commons
	Attribution-NonCommercial-ShareAlike 2.0 UK: England & Wales.
	Permissions beyond the scope of this licence 
	may be available at http://creativecommons.org/licenses/by-nc-sa/2.0/uk/.
	Additional licence terms at https://raw.github.com/confuser/Ban-Management/master/banmanagement/licence.txt
*/
if($settings['password'] == '')
	$errors[] = 'You have not yet set a password!';
else if(isset($_SESSION['failed_attempts']) && $_SESSION['failed_attempts'] > 4) {
	die(errors('You have reached the maximum number of attempts. Please try again in 30 minutes.'));
	if($_SESSION['failed_attempt'] < time())
		unset($_SESSION['failed_attempts']);
} else if(!isset($_SESSION['admin']) && !isset($_POST['password'])) {
	?><form action="" method="post" class="well form-inline">
	<h3>Admin Control Panel <small>&mdash; If you forgot your password please refer to settings.php to change it.</small></h3>
	<div class="row">
		<div class="col-lg-6">
		<?php
			if(!empty($errors)){
				foreach ($errors as $error) {
					echo $error;
				}
			}
		?>
			<div class="input-group">
			<input type="password" class="form-control" name="password" placeholder="Password">
				<span class="input-group-btn">
				<button class="btn btn-info" type="submit">Sign In</button>
	    		</span>
	    	</div>
	    </div>
    </div>
    </form><?php
} else if(isset($_POST['password']) && !isset($_SESSION['admin'])) {
	if(htmlspecialchars_decode($_POST['password'], ENT_QUOTES) != $settings['password']) {
		//set how long we want them to have to wait after 5 wrong attempts
		$time = 1800; //make them wait 30 mins
		if(isset($_SESSION['failed_attempts']))
			++$_SESSION['failed_attempts']; 
		else
			$_SESSION['failed_attempts'] = 1;
		$_SESSION['failed_attempt'] = time() + $time;
		redirect('index.php?action=admin');
	} else {
		$_SESSION['admin'] = true;
		redirect('index.php?action=admin');
	}
} else if(isset($_SESSION['admin']) && $_SESSION['admin']) {
	?>
	<table class="table table-striped table-bordered" id="servers">
		<thead>
			<tr>
				<th>Server Name</th>
				<th>Options</th>
			</tr>
		</thead>
		<tbody><?php
	if(empty($settings['servers']))
		echo '<tr id="noservers"><td colspan="2">No Servers Defined</td></tr>';
	else {
		$id = array_keys($settings['servers']);
		$i = 0;
		$count = count($settings['servers']) - 1;
		
		foreach($settings['servers'] as $server) {
			echo '
				<tr>
					<td>'.$server['name'].'</td>
					<td>
						<a href="#" class="btn btn-danger deleteServer" data-serverid="'.$id[$i].'"><span class="glyphicon glyphicon-trash"></span></a>';
			if($count > 0) {
				if($i == 0)
					echo '
					<a href="#" class="btn reorderServer" data-order="down" data-serverid="'.$id[$i].'"><span class="glyphicon glyphicon-arrow-down"></i></a>';
				else if($i == $count)
					echo '
					<a href="#" class="btn reorderServer" data-order="up" data-serverid="'.$id[$i].'"><span class="glyphicon glyphicon-arrow-up"></span></a>';
				else {
					echo '
					<a href="#" class="btn reorderServer" data-order="up" data-serverid="'.$id[$i].'"><span class="glyphicon glyphicon-arrow-up"></span></a>
					<a href="#" class="btn reorderServer" data-order="down" data-serverid="'.$id[$i].'"><span class="glyphicon glyphicon-arrow-down"></span></a>';
				}
			}
			echo '
					</td>
				</tr>';
			++$i;
		}
	}
		?>
		
		</tbody>
		<tfoot>
			<tr>
				<td colspan="2">
		<?php
	if(!is_writable('settings.php')) {
		echo '<a class="btn btn-primary btn-large disabled" href="#addserver" title="Settings file not writable">Add Server</a>';
	} else
		echo '<a class="btn btn-primary btn-large" href="#addserver" data-toggle="modal">Add Server</a>';
	?>
	
				</td>
			</tr>
		</tfoot>
	</table>
	<div class="modal fade" id="addserver">
		<div class="modal-dialog">
			<div class="modal-content">
				<form class="form-horizontal" action="" method="post">
					<div class="modal-header">
						<button type="button" class="close" data-dismiss="modal">&times;</button>
						<h3>Add Server</h3>
					</div>
					<div class="modal-body">
						<div class="container">
							<div class="form-group">
								<label class="control-label" for="servername">Server Name:</label>
								<div class="controls">
									<input type="text" class="form-control required" name="servername" id="servername">
								</div>
							</div>
							<div class="form-group">
								<label class="control-label" for="host">MySQL Host:</label>
								<div class="controls">
									<input type="text" class="form-control required" name="host" id="host">
								</div>
							</div>
							<div class="form-group">
								<label class="control-label" for="database">MySQL Database:</label>
								<div class="controls">
									<input type="text" class="form-control required" name="database" id="database">
								</div>
							</div>
							<div class="form-group">
								<label class="control-label" for="username">MySQL Username:</label>
								<div class="controls">
									<input type="text" class="form-control required" name="username" id="usernme">
								</div>
							</div>
							<div class="form-group">
								<label class="control-label" for="password">MySQL Password:</label>
								<div class="controls">
									<input type="password" class="form-control" name="password" id="password">
								</div>
							</div>
							<div class="form-group">
								<label class="control-label" for="banstable">Bans Table:</label>
								<div class="controls">
									<input type="text" class="form-control required" name="banstable" id="banstable" value="bm_bans">
								</div>
							</div>
							<div class="form-group">
								<label class="control-label" for="recordtable">Bans Record Table:</label>
								<div class="controls">
									<input type="text" class="form-control required" name="recordtable" id="recordtable" value="bm_ban_records">
								</div>
							</div>
							<div class="form-group">
								<label class="control-label" for="iptable">IP Bans Table:</label>
								<div class="controls">
									<input type="text" class="form-control required" name="iptable" id="iptable" value="bm_ip_bans">
								</div>
							</div>
							<div class="form-group">
								<label class="control-label" for="iprecordtable">IP Record Table:</label>
								<div class="controls">
									<input type="text" class="form-control required" name="iprecordtable" id="iprecordtable" value="bm_ip_records">
								</div>
							</div>
							<div class="form-group">
								<label class="control-label" for="mutestable">Mutes Table:</label>
								<div class="controls">
									<input type="text" class="form-control required" name="mutestable" id="mutestable" value="bm_mutes">
								</div>
							</div>
							<div class="form-group">
								<label class="control-label" for="mutesrecordtable">Mutes Record Table:</label>
								<div class="controls">
									<input type="text" class="form-control required" name="mutesrecordtable" id="mutesrecordtable" value="bm_mutes_records">
								</div>
							</div>
							<div class="form-group">
								<label class="control-label" for="kickstable">Kicks Table:</label>
								<div class="controls">
									<input type="text" class="form-control required" name="kickstable" id="kickstable" value="bm_kicks">
								</div>
							</div>
							<div class="form-group">
								<label class="control-label" for="warningstable">Warnings Table:</label>
								<div class="controls">
									<input type="text" class="form-control required" name="warningstable" id="warningstable" value="bm_warnings">
								</div>
							</div>
						</div>
					</div>
					<div class="modal-footer">
						<a href="#" class="btn" data-dismiss="modal">Close</a>
						<input type="submit" class="btn btn-primary" value="Save" />
					</div>
				</form>
			</div>
		</div>
	</div>
	<br />
	<br />
	<h3>Homepage Settings <small>You may find more settings in settings.php</small></h3>
	<form class="form-horizontal settings" action="" method="post">
		<table class="table table-striped table-bordered table-hover">
			<thead>
				<tr>
					<th>Option</th>
					<th>Value</th>
				</tr>
			</thead>
			<tbody>
	<?php
	if(!is_writable('settings.php')) {
		echo '
				<tr>
					<td colspan="2">settings.php can not be written to</td>
				</tr>';
	} else {
		echo '
				<tr>
					<td>iFrame Protection (Recommended)</td>
					<td><input type="hidden" name="type" value="mainsettings" /><input type="checkbox" name="iframe"'.((isset($settings['iframe_protection']) && $settings['iframe_protection']) || !isset($settings['iframe_protection']) ? ' checked="checked"' : '').' /></td>
				</tr>
				<tr>
					<td>UTF8</td>
					<td><input type="checkbox" name="utf8"'.(isset($settings['utf8']) && $settings['utf8'] ? ' checked="checked"' : '').' /></td>
				</tr>
				<tr>
					<td>Footer</td>
					<td><input type="text" class="form-control" name="footer" value="'.$settings['footer'].'" /></td>
				</tr>
				<tr>
					<td>Latest Bans</td>
					<td><input type="checkbox" name="latestbans"'.((isset($settings['latest_bans']) && $settings['latest_bans']) || !isset($settings['latest_bans']) ? ' checked="checked"' : '').' /></td>
				</tr>
				<tr>
					<td>Latest Mutes</td>
					<td><input type="checkbox" name="latestmutes"'.(isset($settings['latest_mutes']) && $settings['latest_mutes'] ? ' checked="checked"' : '').' /></td>
				</tr>
				<tr>
					<td>Latest Warnings</td>
					<td><input type="checkbox" name="latestwarnings"'.(isset($settings['latest_warnings']) && $settings['latest_warnings'] ? ' checked="checked"' : '').' /></td>
				</tr>
				<tr>
					<td>HTML Before Buttons</td>
					<td><input type="text" class="form-control" name="buttons_before" value="'.(isset($settings['submit_buttons_before_html']) ? $settings['submit_buttons_before_html'] : '').'" /></td>
				</tr>
				<tr>
					<td>HTML After Buttons</td>
					<td><input type="text" class="form-control" name="buttons_after" value="'.(isset($settings['submit_buttons_after_html']) ? $settings['submit_buttons_after_html'] : '').'" /></td>
				</tr>';
	} ?>
	
			</tbody>
			<tfoot>
				<tr>
					<td colspan="2">
	<?php
	if(!is_writable('settings.php')) {
		echo '<input type="submit" class="btn btn-primary btn-large disabled" disabled="disabled" value="Save" />';
	} else {
		echo '<input type="submit" class="btn btn-primary btn-large" value="Save" />';
	} ?>
			
					</td>
				</tr>
			</tfoot>
		</table>
	</form>
	<br />
	<br />
	<h3>View Player Settings</h3>
	<form class="form-horizontal settings" action="" method="post">
		<table class="table table-striped table-bordered table-hover">
			<thead>
				<tr>
					<th>Visible</th>
					<th>Value</th>
				</tr>
			</thead>
			<tbody>
	<?php
	if(!is_writable('settings.php')) {
		echo '
				<tr>
					<td colspan="2">settings.php can not be written to</td>
				</tr>';
	} else {
		echo '
				<tr>
					<td>Current Ban</td>
					<td><input type="hidden" name="type" value="viewplayer" /><input type="checkbox" name="ban"'.((isset($settings['player_current_ban']) && $settings['player_current_ban']) || !isset($settings['player_current_ban']) ? ' checked="checked"' : '').' /></td>
				</tr>
				<tr>
					<td>Current Ban HTML Extra</td>
					<td><input type="input" class="form-control" name="banextra"'.(isset($settings['player_current_ban_extra_html']) ? ' value="'.$settings['player_current_ban_extra_html'].'"' : '').' /></td>
				</tr>
				<tr>
					<td>Current Mute</td>
					<td><input type="checkbox" name="mute"'.((isset($settings['player_current_mute']) && $settings['player_current_mute']) || !isset($settings['player_current_mute']) ? ' checked="checked"' : '').' /></td>
				</tr>
				<tr>
					<td>Current Mute HTML Extra</td>
					<td><input type="input" class="form-control" name="muteextra"'.(isset($settings['player_current_mute_extra_html']) ? ' value="'.$settings['player_current_mute_extra_html'].'"' : '').' /></td>
				</tr>
				<tr>
					<td>Previous Bans</td>
					<td><input type="checkbox" name="prevbans"'.((isset($settings['player_previous_bans']) && $settings['player_previous_bans']) || !isset($settings['player_previous_bans']) ? ' checked="checked"' : '').' /></td>
				</tr>
				<tr>
					<td>Previous Mutes</td>
					<td><input type="checkbox" name="prevmutes"'.((isset($settings['player_previous_mutes']) && $settings['player_previous_mutes']) || !isset($settings['player_previous_mutes']) ? ' checked="checked"' : '').' /></td>
				</tr>
				<tr>
					<td>Warnings</td>
					<td><input type="checkbox" name="warnings"'.((isset($settings['player_warnings']) && $settings['player_warnings']) || !isset($settings['player_warnings']) ? ' checked="checked"' : '').' /></td>
				</tr>
				<tr>
					<td>Kicks</td>
					<td><input type="checkbox" name="kicks"'.((isset($settings['player_kicks']) && $settings['player_kicks']) || !isset($settings['player_kicks']) ? ' checked="checked"' : '').' /></td>
				</tr>';
	} ?>
	
			</tbody>
			<tfoot>
				<tr>
					<td colspan="2">
	<?php
	if(!is_writable('settings.php')) {
		echo '<input type="submit" class="btn btn-primary btn-large disabled" disabled="disabled" value="Save" />';
	} else {
		echo '<input type="submit" class="btn btn-primary btn-large" value="Save" />';
	} ?>
			
					</td>
				</tr>
			</tfoot>
		</table>
	</form>
	<br />
	<br />
	<h3>Miscellaneous</h3>
	<a href="index.php?action=deletecache&authid=<?php echo sha1($settings['password']); ?>" class="btn btn-primary">Clear Cache</a>
	<?php
}
?>
<script src="//<?php echo $path; ?>js/admin.js"></script>