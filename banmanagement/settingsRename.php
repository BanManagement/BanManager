<?php
/**
* This is the general configuaration file for Ban Management.
* In here you can control your encoding for server transfers,
* define what tables you want enabled, set your password for ACP,
* and more. 
**/

$settings['utf8'] 			= false; // Encoding (Recommended TRUE)
$settings['latest_bans'] 	= true;  // Latest Bans table
$settings['latest_mutes']	= true; // Latest Mutes table
$settings['latest_warnings'] = true; // Latest warnings table
$settings['servers']		= 'a:1:{i:0;a:13:{s:4:"name";s:4:"Test";s:4:"host";s:9:"localhost";s:8:"database";s:4:"bans";s:8:"username";s:4:"root";s:8:"password";s:0:"";s:9:"bansTable";s:7:"bm_bans";s:11:"recordTable";s:14:"bm_ban_records";s:7:"ipTable";s:10:"bm_ip_bans";s:13:"ipRecordTable";s:13:"bm_ip_records";s:10:"mutesTable";s:8:"bm_mutes";s:16:"mutesRecordTable";s:16:"bm_mutes_records";s:10:"kicksTable";s:8:"bm_kicks";s:13:"warningsTable";s:11:"bm_warnings";}}';
$settings['password']		= 'password'; // ACP Password (Keep it strong)
$settings['footer'] 		= '&copy; Your Server '.date('Y'); // Footer for all pages
$settings['admin_link']     = true; // Show the admin link in the footer of all page
$settings['bm_info']		= true; // Show ban management infomation aside 'Account Status'
	$settings['bm_info_icon'] = false; // Show the 'info' icon next to the title of bm_info

/**
* These are the language options for Ban Management
**/

$language['brand']			   = 'Ban Management'; // The branding of all pages
$language['header-title']	   = 'Account Status'; // Edit the 'Account Status' text above the search
$language['description'] 	   = ''; // Meta Description for search engines
$language['title']			   = 'Ban Management by Frostcast'; // Title of all pages
$language['latest_bans_title'] = 'Recent Bans'; // The text displayed over the latest bans table
$language['latest_mutes_title'] = 'Recent Mutes'; // The text displayed over the latest mutes table
$language['latest_warnings_title'] = 'Recent Warnings'; // The text displayed over the latest warnings table
$language['nav-home']		   = 'Home'; // The text displayed in the navbar for 'Home'
$language['nav-stats']	   = 'Statistics'; // The text displayed in the navbar for 'Servers'
$language['bm_info_text']  	   = // The text displayed if bm_info is set to true. Enter your text below, HTML elements supported
' 
	Ban Management is powerful ban and mute application that allows players to check if and why they were banned or muted and who
	it was by. You can also see how much time is left of your ban or mute.

';

/**
* These are the settings for editing the layout of Ban Management
**/

$theme['navbar-dark']       = false; // Enable dark theme for the navbar

?>