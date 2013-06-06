<?php
/*  BanManagement © 2012, a web interface for the Bukkit plugin BanManager
    by James Mortemore of http://www.frostcast.net
	is licenced under a Creative Commons
	Attribution-NonCommercial-ShareAlike 2.0 UK: England & Wales.
	Permissions beyond the scope of this licence 
	may be available at http://creativecommons.org/licenses/by-nc-sa/2.0/uk/.
	Additional licence terms at https://raw.github.com/confuser/Ban-Management/master/banmanagement/licence.txt
*/
if(!isset($_GET['server']) || !is_numeric($_GET['server']))
	redirect('index.php');
else if(!isset($settings['servers'][$_GET['server']]))
	redirect('index.php');
else if(!isset($_GET['player']) || empty($_GET['player']))
	redirect('index.php');
else if(isset($_GET['player']) && preg_match('/[^a-z0-9_]{2,16}/i', $_GET['player']))
	redirect('index.php');
else {
	// Get the server details
	$server = $settings['servers'][$_GET['server']];

	// Clear old players
	clearCache($_GET['server'].'/players', 300);
	clearCache($_GET['server'].'/mysqlTime', 300);
	
	// Check if they are logged in as an admin
	if(isset($_SESSION['admin']) && $_SESSION['admin'])
		$admin = true;
	else
		$admin = false;
	
	// Check if the player exists
	$currentBans = cache("SELECT * FROM ".$server['bansTable']." WHERE banned = '".$_GET['player']."'", 300, $_GET['server'].'/players', $server);
	$pastBans = cache("SELECT * FROM ".$server['recordTable']." WHERE banned = '".$_GET['player']."'", 300, $_GET['server'].'/players', $server);
	$currentMutes = cache("SELECT * FROM ".$server['mutesTable']." WHERE muted = '".$_GET['player']."'", 300, $_GET['server'].'/players', $server);
	$pastMutes = cache("SELECT * FROM ".$server['mutesRecordTable']." WHERE muted = '".$_GET['player']."'", 300, $_GET['server'].'/players', $server);
	$pastKicks = cache("SELECT * FROM ".$server['kicksTable']." WHERE kicked = '".$_GET['player']."'", 300, $_GET['server'].'/players', $server);
	$pastWarnings = cache("SELECT * FROM ".$server['warningsTable']." WHERE warned = '".$_GET['player']."'", 300, $_GET['server'].'/players', $server);

	if(count($currentBans) == 0 && count($pastBans) == 0 && count($currentMutes) == 0 && count($pastMutes) == 0 && count($pastKicks) == 0 && $pastWarnings == 0) {
		errors('Player does not exist');
		?><a href="index.php" class="btn btn-primary">New Search</a><?php
	} else {
		// They have been banned, naughty!
		// Now check the time differences!
		$timeDiff = cache('SELECT ('.time().' - UNIX_TIMESTAMP(now()))/3600 AS mysqlTime', 5, $_GET['server'].'/mysqlTime', $server); // Cache it for a few seconds
		
		$mysqlTime = $timeDiff['mysqlTime'];
		$mysqlTime = ($mysqlTime > 0)  ? floor($mysqlTime) : ceil ($mysqlTime);
		$mysqlSecs = ($mysqlTime * 60) * 60;
		?>
		<div class="hero-unit">
			<h2><img src="https://minotar.net/avatar/<?php echo $_GET['player']; ?>/40" alt="<?php echo $_GET['player']; ?>" /> <?php echo $_GET['player']; ?></h2>
			<h3>Server: <?php echo $server['name']; ?></h3>
		<?php
		$id = array_keys($settings['servers']);
		$i = 0;
		$html = '';
		if(count($settings['servers']) > 1) {
			echo '
			<h5>Change Server: ';
			foreach($settings['servers'] as $serv) {
				if($serv['name'] != $server['name']) {
					$html .= '<a href="index.php?action=viewplayer&player='.$_GET['player'].'&server='.$id[$i].'">'.$serv['name'].'</a>, ';
				}
				++$i;
			}
			echo substr($html, 0, -2).'
			</h5>';
		}
		
		if((isset($settings['player_current_ban']) && $settings['player_current_ban']) || !isset($settings['player_current_ban'])) {
			?>
			<br />
			<table id="current-ban" class="table table-striped table-bordered">
				<caption>Current Ban</caption>
				<tbody>
				<?php
			if(count($currentBans) == 0) {
				echo '
					<tr>
						<td colspan="2">None</td>
					</tr>';
			} else {
				$reason = str_replace(array('&quot;', '"'), array('&#039;', '\''), $currentBans['ban_reason']);
				echo '
					<tr>
						<td>Expires in:</td>
						<td class="expires">';
				if($currentBans['ban_expires_on'] == 0)
					echo '<span class="label label-important">Never</span>';
				else {
					$currentBans['ban_expires_on'] = $currentBans['ban_expires_on'] + $mysqlSecs;
					$currentBans['ban_time'] = $currentBans['ban_time'] + $mysqlSecs;
					$expires = $currentBans['ban_expires_on'] - time();
					
					if($expires > 0)
						echo '<time datetime="'.date('c', $currentBans['ban_expires_on']).'">'.secs_to_h($expires).'</time>';
					else
						echo 'Now';
				}
				
				echo '</td>
					</tr>
					<tr>
						<td>Banned by:</td>
						<td>'.$currentBans['banned_by'].'</td>
					</tr>
					<tr>
						<td>Banned at:</td>
						<td>'.date('jS F Y h:i:s A', $currentBans['ban_time']).'</td>
					</tr>
					<tr>
						<td>Reason:</td>
						<td class="reason">'.$currentBans['ban_reason'].'</td>
					</tr>';
				if(!empty($currentBans['server'])) {
					echo '
					<tr>
						<td>Server:</td>
						<td>'.$currentBans['server'].'</td>
					</tr>';
				}
			}
				?>
				</tbody><?php
			if($admin && count($currentBans) != 0) {
				echo '
				<tfoot>
					<tr>
						<td colspan="2">
							<a class="btn btn-warning edit" title="Edit" href="#editban" data-toggle="modal"><i class="icon-pencil icon-white"></i> Edit</a>
							<a class="btn btn-danger delete" title="Unban" data-role="confirm" href="index.php?action=deleteban&ajax=true&authid='.sha1($settings['password']).'&server='.$_GET['server'].'&id='.$currentBans['ban_id'].'" data-confirm-title="Unban '.$_GET['player'].'" data-confirm-body="Are you sure you want to unban '.$_GET['player'].'?<br />This cannot be undone"><i class="icon-trash icon-white"></i> Unban</a>
						</td>
					</tr>
				</tfoot>';
			}
				?>
			</table><?php
			if($admin && count($currentBans) != 0) {?>
			<div class="modal hide fade" id="editban">
				<form class="form-horizontal" action="" method="post">
					<div class="modal-header">
						<button type="button" class="close" data-dismiss="modal">&times;</button>
						<h3>Edit Ban</h3>
					</div>
					<div class="modal-body">
						<fieldset>
							<div class="control-group">
								<label class="control-label" for="yourtime">Your Time:</label>
								<div class="controls">
									<span class="yourtime"></span>
								</div>
							</div>
							<div class="control-group">
								<label class="control-label" for="servertime">Server Time:</label>
								<div class="controls">
									<span class="servertime"><?php echo date('d/m/Y H:i:s', time() + $mysqlSecs); ?></span>
								</div>
							</div>
							<div class="control-group">
								<label class="control-label" for="bandatetime">Expires Server Time:</label>
								<div class="controls">
									<div class="input-append datetimepicker date"><?php
				echo '						
										<div class="input-prepend">
											<button class="btn btn-danger bantype" type="button">';
				if($currentBans['ban_expires_on'] == 0)
					echo 'Never';
				else
					echo 'Temp';
			
				echo '</button>
											<input type="text" class="required';
			
				if($currentBans['ban_expires_on'] == 0)
					echo ' disabled" disabled="disabled"';
				else
					echo '"'; 
			
				echo ' name="expires" data-format="dd/MM/yyyy hh:mm:ss" value="';

				if($currentBans['ban_expires_on'] == 0)
					echo '';
				else
					echo date('d/m/Y H:i:s', $currentBans['ban_expires_on']);
				
				echo '" id="bandatetime" />';
										?>
											<span class="add-on">
												<i data-time-icon="icon-time" data-date-icon="icon-calendar"></i>
											</span>
										</div>
									</div>
								</div>
							</div>
							<div class="control-group">
								<label class="control-label" for="banreason">Reason:</label>
								<div class="controls">
									<textarea id="banreason" name="reason" rows="4"><?php echo $currentBans['ban_reason']; ?></textarea>
								</div>
							</div>
						</fieldset>
					</div>
					<div class="modal-footer">
						<a href="#" class="btn" data-dismiss="modal">Close</a>
						<input type="submit" class="btn btn-primary" value="Save" />
					</div>
					<input type="hidden" name="id" value="<?php echo $currentBans['ban_id']; ?>" />
					<input type="hidden" name="server" value="<?php echo $_GET['server']; ?>" />
					<input type="hidden" name="expiresTimestamp" value="" />
				</form>
			</div><?php
			}
		}
		
		if((isset($settings['player_current_mute']) && $settings['player_current_mute']) || !isset($settings['player_current_mute'])) {
			?>
			<br />
			<table id="current-mute" class="table table-striped table-bordered">
				<caption>Current Mute</caption>
				<tbody>
				<?php
			if(count($currentMutes) == 0) {
				echo '
					<tr>
						<td colspan="2">None</td>
					</tr>';
			} else {
				$reason = str_replace(array('&quot;', '"'), array('&#039;', '\''), $currentMutes['mute_reason']);
				echo '
					<tr>
						<td>Expires in:</td>
						<td class="expires">';
				if($currentMutes['mute_expires_on'] == 0)
					echo '<span class="label label-important">Never</span>';
				else {
					$currentMutes['mute_expires_on'] = $currentMutes['mute_expires_on'] + $mysqlSecs;
					$currentMutes['mute_time'] = $currentMutes['mute_time'] + $mysqlSecs;
					$expires = $currentMutes['mute_expires_on'] - time();
				
					if($expires > 0)
						echo '<time datetime="'.date('c', $currentMutes['mute_expires_on']).'">'.secs_to_h($expires).'</time>';
					else
						echo 'Now';
				}
				echo '</td>
					</tr>
					<tr>
						<td>Muted by:</td>
						<td>'.$currentMutes['muted_by'].'</td>
					</tr>
					<tr>
						<td>Muted at:</td>
						<td>'.date('jS F Y h:i:s A', $currentMutes['mute_time']).'</td>
					</tr>
					<tr>
						<td>Reason:</td>
						<td class="reason">'.$currentMutes['mute_reason'].'</td>
					</tr>
					<tr>';
				if(!empty($currentMutes['server'])) {
					echo '
					<tr>
						<td>Server:</td>
						<td>'.$currentMutes['server'].'</td>
					</tr>';
				}
			}
				?>
				
				</tbody>
				<?php
			if($admin && count($currentMutes) != 0) {
				echo '
				<tfoot>
					<tr>
						<td colspan="2">
							<a class="btn btn-warning edit" title="Edit" href="#editmute" data-toggle="modal"><i class="icon-pencil icon-white"></i> Edit</a>
							<a class="btn btn-danger delete" title="Unban" data-role="confirm" href="index.php?action=deletemute&ajax=true&authid='.sha1($settings['password']).'&server='.$_GET['server'].'&id='.$currentMutes['mute_id'].'" data-confirm-title="Unban '.$_GET['player'].'" data-confirm-body="Are you sure you want to unmute '.$_GET['player'].'?<br />This cannot be undone"><i class="icon-trash icon-white"></i> Unmute</a>
						</td>
					</tr>
				</tfoot>';
			}
				?>
			
			</table><?php
			if($admin && count($currentMutes) != 0) {?>
			
			<div class="modal hide fade" id="editmute">
				<form class="form-horizontal" action="" method="post">
					<div class="modal-header">
						<button type="button" class="close" data-dismiss="modal">&times;</button>
						<h3>Edit Ban</h3>
					</div>
					<div class="modal-body">
						<fieldset>
							<div class="control-group">
								<label class="control-label" for="yourtime">Your Time:</label>
								<div class="controls">
									<span class="yourtime"></span>
								</div>
							</div>
							<div class="control-group">
								<label class="control-label" for="servertime">Server Time:</label>
								<div class="controls">
									<span class="servertime"><?php echo date('d/m/Y H:i:s', time() + $mysqlSecs); ?></span>
								</div>
							</div>
							<div class="control-group">
								<label class="control-label" for="mutedatetime">Expires Server Time:</label>
								<div class="controls">
									<div class="input-append datetimepicker date"><?php
				echo '						
										<div class="input-prepend">
											<button class="btn btn-danger bantype" type="button">';
				if($currentMutes['mute_expires_on'] == 0)
					echo 'Never';
				else
					echo 'Temp';
			
				echo '</button>
											<input type="text" class="required';
			
				if($currentMutes['mute_expires_on'] == 0)
					echo ' disabled" disabled="disabled"';
				else
					echo '"'; 
			
				echo ' name="expires" data-format="dd/MM/yyyy hh:mm:ss" value="';

				if($currentMutes['mute_expires_on'] == 0)
					echo '';
				else
					echo date('d/m/Y H:i:s', $currentMutes['mute_expires_on']);
				
				echo '" id="mutedatetime" />';
										?>
											<span class="add-on">
												<i data-time-icon="icon-time" data-date-icon="icon-calendar"></i>
											</span>
										</div>
									</div>
								</div>
							</div>
							<div class="control-group">
								<label class="control-label" for="mutereason">Reason:</label>
								<div class="controls">
									<textarea id="mutereason" name="reason" rows="4"><?php echo $currentMutes['mute_reason']; ?></textarea>
								</div>
							</div>
						</fieldset>
					</div>
					<div class="modal-footer">
						<a href="#" class="btn" data-dismiss="modal">Close</a>
						<input type="submit" class="btn btn-primary" value="Save" />
					</div>
					<input type="hidden" name="id" value="<?php echo $currentMutes['mute_id']; ?>" />
					<input type="hidden" name="server" value="<?php echo $_GET['server']; ?>" />
					<input type="hidden" name="expiresTimestamp" value="" />
				</form>
			</div><?php
			}
		}
		
		if((isset($settings['player_previous_bans']) && $settings['player_previous_bans']) || !isset($settings['player_previous_bans'])) {
		?>
			<br />
			<table class="table table-striped table-bordered" id="previous-bans">
				<caption>Previous Bans</caption>
				<thead>
					<tr>
						<th>ID</th>
						<th>Reason</th>
						<th>By</th>
						<th>On</th>
						<th>Length</th>
						<th>Unbanned By</th>
						<th>At</th><?php
			if(!isset($pastBans[0]) || (isset($pastBans[0]) && !is_array($pastBans[0])))
				$pastBans = array($pastBans);
			$serverName = false;
			foreach($pastBans as $r) {
				if(!empty($r['server'])) {
					$serverName = true;
					break;
				}
			}
			if($serverName) {
				echo '
						<th>Server</th>';
			}
			if($admin)
				echo '
						<th></th>';
				?>
				
					</tr>
				</thead>
				<tbody><?php
			if(isset($pastBans[0]) && count($pastBans[0]) == 0) {
				echo '
					<tr>
						<td colspan="8">None</td>
					</tr>';
			} else {
				$i = 1;
				foreach($pastBans as $r) {
					$r['ban_reason'] = str_replace(array('&quot;', '"'), array('&#039;', '\''), $r['ban_reason']);
					$r['ban_expired_on'] = ($r['ban_expired_on'] != 0 ? $r['ban_expired_on'] + $mysqlSecs : $r['ban_expired_on']);
					$r['ban_time'] = $r['ban_time'] + $mysqlSecs;
					$r['unbanned_time'] = $r['unbanned_time'] + $mysqlSecs;

					echo '
					<tr>
						<td>'.$i.'</td>
						<td>'.$r['ban_reason'].'</td>
						<td>'.$r['banned_by'].'</td>
						<td>'.date('H:i:s d/m/y', $r['ban_time']).'</td>
						<td>'.($r['ban_expired_on'] == 0 ? 'Never' : secs_to_h($r['ban_expired_on'] - $r['ban_time'])).'</td>
						<td>'.$r['unbanned_by'].'</td>
						<td>'.date('H:i:s d/m/y', $r['unbanned_time']).'</td>'.($serverName ? '
						<td>'.$r['server'].'</td>' : '').($admin ? '
						<td class="admin-options"><a href="#" class="btn btn-danger delete" title="Remove" data-server="'.$_GET['server'].'" data-record-id="'.$r['ban_record_id'].'"><i class="icon-trash icon-white"></i></a></td>' : '').'
					</tr>';
					++$i;
				}
			}
				?>
				
				</tbody>
			</table><?php
		} 
		
		if((isset($settings['player_previous_mutes']) && $settings['player_previous_mutes']) || !isset($settings['player_previous_mutes'])) {
		?>
			<br />
			<table class="table table-striped table-bordered" id="previous-mutes">
				<caption>Previous Mutes</caption>
				<thead>
					<tr>
						<th>ID</th>
						<th>Reason</th>
						<th>By</th>
						<th>On</th>
						<th>Length</th>
						<th>Unbanned By</th>
						<th>At</th><?php
			if(isset($pastMutes[0]) && !is_array($pastMutes[0]))
				$pastMutes = array($pastMutes);
			$serverName = false;
			foreach($pastMutes as $r) {
				if(!empty($r['server'])) {
					$serverName = true;
					break;
				}
			}
			if($serverName) {
					echo '
						<th>Server</th>';
			}
			if($admin)
				echo '
						<th></th>';
				?>
					
					</tr>
				</thead>
				<tbody><?php
			if(count($pastMutes) == 0) {
				echo '
					<tr>
						<td colspan="8">None</td>
					</tr>';
			} else {
				$i = 1;
				foreach($pastMutes as $r) {
					$r['mute_reason'] = str_replace(array('&quot;', '"'), array('&#039;', '\''), $r['mute_reason']);
					$r['mute_expired_on'] = ($r['mute_expired_on'] != 0 ? $r['mute_expired_on'] + $mysqlSecs : $r['mute_expired_on']);
					$r['mute_time'] = $r['mute_time'] + $mysqlSecs;
					echo '
					<tr>
						<td>'.$i.'</td>
						<td>'.$r['mute_reason'].'</td>
						<td>'.$r['muted_by'].'</td>
						<td>'.date('d/m/y', $r['mute_time']).'</td>
						<td>'.($r['mute_expired_on'] == 0 ? 'Never' : secs_to_h($r['mute_expired_on'] - $r['mute_time'])).'</td>
						<td>'.$r['unmuted_by'].'</td>
						<td>'.date('d/m/y', $r['unmuted_time']).'</td>'.($serverName ? '
						<td>'.$r['server'].'</td>' : '').($admin ? '
						<td class="admin-options"><a href="#" class="btn btn-danger delete" title="Remove" data-server="'.$_GET['server'].'" data-record-id="'.$r['mute_record_id'].'"><i class="icon-trash icon-white"></i></a></td>' : '').'
					</tr>';
					++$i;
				}
			}
				?>
				
				</tbody>
			</table><?php
		}
		
		if((isset($settings['player_warnings']) && $settings['player_warnings']) || !isset($settings['player_warnings'])) {
			?>
			<br />
			<table class="table table-striped table-bordered" id="previous-warnings">
				<caption>Warnings</caption>
				<thead>
					<tr>
						<th>ID</th>
						<th>Reason</th>
						<th>By</th>
						<th>On</th><?php
			if(!isset($pastWarnings[0]) || (isset($pastWarnings[0]) && !is_array($pastWarnings[0])))
				$pastWarnings = array($pastWarnings);
			$serverName = false;
			foreach($pastWarnings as $r) {
				if(!empty($r['server'])) {
					$serverName = true;
					break;
				}
			}
			if($serverName) {
				echo '
						<th>Server</th>';
			}
			if($admin)
				echo '
						<th></th>';
				?>
				
					</tr>
				</thead>
				<tbody><?php
			if(isset($pastWarnings[0]) && count($pastWarnings[0]) == 0) {
				echo '
					<tr>
						<td colspan="8">None</td>
					</tr>';
			} else {
				$i = 1;
				foreach($pastWarnings as $r) {
					$r['warn_reason'] = str_replace(array('&quot;', '"'), array('&#039;', '\''), $r['warn_reason']);
					$r['warn_time'] = $r['warn_time'] + $mysqlSecs;

					echo '
					<tr>
						<td>'.$i.'</td>
						<td>'.$r['warn_reason'].'</td>
						<td>'.$r['warned_by'].'</td>
						<td>'.date('H:i:s d/m/y', $r['warn_time']).'</td>'.($serverName ? '
						<td>'.$r['server'].'</td>' : '').($admin ? '
						<td class="admin-options"><a href="#" class="btn btn-danger delete" title="Remove" data-server="'.$_GET['server'].'" data-record-id="'.$r['warn_id'].'"><i class="icon-trash icon-white"></i></a></td>' : '').'
					</tr>';
					++$i;
				}
			}
				?>
				
				</tbody>
			</table><?php
		}
		
		if((isset($settings['player_kicks']) && $settings['player_kicks']) || !isset($settings['player_kicks'])) {		
		?>
			<br />
			<table class="table table-striped table-bordered" id="previous-kicks">
				<caption>Kicks</caption>
				<thead>
					<tr>
						<th>ID</th>
						<th>Reason</th>
						<th>By</th>
						<th>At</th><?php
			if(isset($pastKicks[0]) && !is_array($pastKicks[0]))
				$pastKicks = array($pastKicks);
			$serverName = false;
			foreach($pastKicks as $r) {
				if(!empty($r['server'])) {
					$serverName = true;
					break;
				}
			}
			if($serverName) {
					echo '
						<th>Server</th>';
			}
			if($admin)
				echo '
						<th></th>';
				?>
					
					</tr>
				</thead>
				<tbody><?php
			if(count($pastKicks) == 0) {
				echo '
					<tr>
						<td colspan="8">None</td>
					</tr>';
			} else {
				$i = 1;
				foreach($pastKicks as $r) {
					$r['kick_reason'] = str_replace(array('&quot;', '"'), array('&#039;', '\''), $r['kick_reason']);
					$r['kick_time'] = $r['kick_time'] + $mysqlSecs;
					echo '
					<tr>
						<td>'.$i.'</td>
						<td>'.$r['kick_reason'].'</td>
						<td>'.$r['kicked_by'].'</td>
						<td>'.date('d/m/y', $r['kick_time']).'</td>'.($serverName ? '
						<td>'.$r['server'].'</td>' : '').($admin ? '
						<td class="admin-options"><a href="#" class="btn btn-danger delete" title="Remove" data-server="'.$_GET['server'].'" data-record-id="'.$r['kick_id'].'"><i class="icon-trash icon-white"></i></a></td>' : '').'
					</tr>';
					++$i;
				}
			}
				?>
				
				</tbody>
			</table><?php
		} ?>
		</div>
		<?php
	}
}
?>