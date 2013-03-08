if (top.location != self.location) { top.location = self.location.href; }
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
	
	$.extend($.tablesorter.themes.bootstrap, {
		// these classes are added to the table. To see other table classes available,
		// look here: http://twitter.github.com/bootstrap/base-css.html#tables
		table      : 'table table-bordered',
		header     : 'bootstrap-header', // give the header a gradient background
		footerRow  : '',
		footerCells: '',
		icons      : '', // add "icon-white" to make them white; this icon class is added to the <i> in the header
		sortNone   : 'bootstrap-icon-unsorted',
		sortAsc    : 'icon-chevron-up',
		sortDesc   : 'icon-chevron-down',
		active     : '', // applied when column is sorted
		hover      : '', // use custom css here - bootstrap class may not override it
		filterRow  : '', // filter row class
		even       : '', // odd row zebra striping
		odd        : ''  // even row zebra striping
	});
	
	$("table.sortable").tablesorter({
		theme : "bootstrap", // this will 

		widthFixed: true,

		headerTemplate : '{content} {icon}', // new in v2.7. Needed to add the bootstrap icon!

		// widget code contained in the jquery.tablesorter.widgets.js file
		// use the zebra stripe widget if you plan on hiding any rows (filter widget)
		widgets : [ "uitheme", "filter", "zebra" ],

		widgetOptions : {
		  // using the default zebra striping class name, so it actually isn't included in the theme variable above
		  // this is ONLY needed for bootstrap theming if you are using the filter widget, because rows are hidden
		  zebra : ["even", "odd"],

		  // reset filters button
		  filter_reset : ".reset",

		  // set the uitheme widget to use the bootstrap theme class names
		  // uitheme : "bootstrap"

		}
	}).tablesorterPager({
		// target the pager markup - see the HTML block below
		container: $(".pager"),

		// target the pager page select dropdown - choose a page
		cssGoto  : ".pagenum",

		// remove rows from the table to speed up the sort of large tables.
		// setting this to false, only hides the non-visible rows; needed if you plan to add/remove rows with the pager enabled.
		removeRows: false,

		// output string - default is '{page}/{totalPages}';
		// possible variables: {page}, {totalPages}, {filteredPages}, {startRow}, {endRow}, {filteredRows} and {totalRows}
		output: '{startRow} - {endRow} / {filteredRows} ({totalRows})'
	});
});