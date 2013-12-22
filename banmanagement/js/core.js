function showLoading(element) {
	var cl = new CanvasLoader(element);
	cl.setColor('#4e68d9'); // default is '#000000'
	cl.setDiameter(16); // default is 40
	cl.setDensity(59); // default is 40
	cl.setRange(1.6); // default is 1.3
	cl.setSpeed(4); // default is 2
	cl.show(); // Hidden by default
}

function hideLoading() {
	$("#ajaxLoading").remove();
}
$(function() {
	jQuery.validator.setDefaults({
		errorPlacement: function(error, placement) {
			error.wrap('<span class="help-inline" />');
			$(placement).after(error.parent());
			error.parent().parent().parent().removeClass('success').addClass('error');
		},
		success: function(label) {
			label.parent().parent().parent().removeClass('error').addClass('success');
		}
	});
	$("form").validate();
	$("time").each(function() {
		$(this).countdown({
			until: new Date($(this).attr('datetime')),
			format: 'yowdhms', layout: '{y<} {yn} {yl}, {y>} {o<} {on} {ol}, {o>} {w<} {wn} {wl}, {w>} {d<} {dn} {dl}, {d>} {h<} {hn} {hl}, {h>} {m<} {mn} {ml}, {m>} {s<} {sn} {sl} {s>}',
			onExpiry: function() {
				location.reload();
			}
		});
	});
	
	if (jQuery().minecraftSkin) $('.skin').minecraftSkin();
	
	if (jQuery().mCustomScrollbar) {
		$('#player_ban_info').mCustomScrollbar({
			theme: 'dark-thick'
		});
	}
	
	$.extend($.tablesorter.themes.bootstrap, {
		// these classes are added to the table. To see other table classes available,
		// look here: http://twitter.github.com/bootstrap/base-css.html#tables
		table      : 'table table-bordered',
		header     : 'bootstrap-header', // give the header a gradient background
		footerRow  : '',
		footerCells: '',
		icons      : '', // add "icon-white" to make them white; this icon class is added to the <i> in the header
		sortNone   : 'bootstrap-icon-unsorted',
		sortAsc    : 'glyphicon glyphicon-chevron-up',
		sortDesc   : 'glyphicon glyphicon-chevron-down',
		active     : '', // applied when column is sorted
		hover      : '', // use custom css here - bootstrap class may not override it
		filterRow  : '', // filter row class
		even       : '', // odd row zebra striping
		odd        : ''  // even row zebra striping
	});
	
	$.tablesorter.addParser({
		// set a unique id
		id: 'expires',
		is: function(s, table, cell) {
			// return false so this parser is not auto detected
			return false;
		},
		format: function(s, table, cell, cellIndex) {
			// format your data for normalization
			return $(cell).data("expires");
		},
		// set type, either numeric or text
		type: 'numeric'
	});
	
	$("table.sortable").tablesorter({
		theme : "bootstrap", // this will 

		widthFixed: true,

		headers: { 4: { sorter: 'expires' } },
		
		sortList: [[0,0]],
		
		headerTemplate : '{content} {icon}', // new in v2.7. Needed to add the bootstrap icon!

		// widget code contained in the jquery.tablesorter.widgets.js file
		// use the zebra stripe widget if you plan on hiding any rows (filter widget)
		widgets : [ "uitheme", "filter", "zebra", "saveSort" ],

		widgetOptions : {
		  // using the default zebra striping class name, so it actually isn't included in the theme variable above
		  // this is ONLY needed for bootstrap theming if you are using the filter widget, because rows are hidden
		  zebra : ["even", "odd"],

		  // reset filters button
		  filter_reset : ".reset",

		  // set the uitheme widget to use the bootstrap theme class names
		  // uitheme : "bootstrap"
		  filter_searchDelay : 1000

		}
	}).tablesorterPager({
		// target the pager markup - see the HTML block below
		container: $(".pager"),
		
		ajaxUrl: 'index.php?action=' + $("#container form input[name='action']").val() + '&player=' + $("#container form input[name='player']").val() + '&server=' + $("#container form input[name='server']").val() + '&excluderecords=' + $("#container form input[name='excluderecords']").val() + '&ajax=true&size={size}&page={page}&sortby={sortList:column}&filter={filterList:filter}',
		ajaxObject: {
			dataType: 'json',
			success: function(data, textStatus, jqXHR) {
				hideLoading();
			},
			error: function(jqXHR, textStatus, errorThrown) {
				hideLoading();
			},
			beforeSend: function() {
				$("table.sortable").before('<div id="ajaxLoading"><span id="loadingSmall"></span><br />Fetching...</div>');
				showLoading('loadingSmall');
			}
		},
		ajaxProcessing: function(data){
			if (data && data.hasOwnProperty('rows')) {
			  var r, row, c, d = data.rows,
			  // total number of rows (required)
			  total = data.total_rows,
			  // array of header names (optional)
			  headers = data.headers,
			  // all rows: array of arrays; each internal array has the table cell data for that row
			  rows = [],
			  // len should match pager set size (c.size)
			  len = d.length;
			  // this will depend on how the json is set up - see City0.json
			  // rows
			  for ( r=0; r < len; r++ ) {
				row = []; // new row array
				// cells
				for ( c in d[r] ) {
				  if (typeof(c) === "string") {
					row.push(d[r][c]); // add each table cell data to row array
				  }
				}
				rows.push(row); // add new row array to rows array
			  }
			  if(rows.length == 0)
				row.push("None");
			  
			  return [ total, rows, headers ];
			}
		},

		// target the pager page select dropdown - choose a page
		cssGoto: ".pagenum",

		// remove rows from the table to speed up the sort of large tables.
		// setting this to false, only hides the non-visible rows; needed if you plan to add/remove rows with the pager enabled.
		removeRows: false,

		// output string - default is '{page}/{totalPages}';
		// possible variables: {page}, {totalPages}, {filteredPages}, {startRow}, {endRow}, {filteredRows} and {totalRows}
		output: '{startRow} - {endRow} / {filteredRows} ({totalRows})'
	});
});