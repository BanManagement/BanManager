/*  
	Serverless 3D Minecraft Skin
	by James Mortemore of http://www.frostcast.net
	is licenced under a Creative Commons
	Attribution-NonCommercial-ShareAlike 2.0 UK: England & Wales.
	Based on minecraft skins in html5 canvas tags by Kent Rasmussen @ earthiverse.ath.cx
	Permissions beyond the scope of this licence 
	may be available at http://creativecommons.org/licenses/by-nc-sa/2.0/uk/.
 */
(function($) {
	var pluginName = 'minecraftSkin';	

	var methods = {
		init: function(options) {
			return this.each(function(index){
				var $this = $(this);
                var data = $this.data(pluginName);

				// If the plugin hasn't been initialized yet
                if (!data){
                    var settings = {
						'scale': 6,
						'hat': true,
						'draw' : 'model'
					};
                    if(options) { $.extend(true, settings, options); }
                }
				
				settings.username = $this.data('minecraft-username');
				if($this.data('minecraft-scale'))
					settings.scale = $this.data('minecraft-scale');
				if($this.data('minecraft-draw'))
					settings.draw = $this.data('minecraft-draw');
					
				// Check if valid drawing set
				if(settings.draw != 'head' && settings.draw != 'model')
					settings.draw = 'model';

				// Request the data
				methods.requestData('http://s3.amazonaws.com/MinecraftSkins/'+settings.username+'.png', $this, settings);
			});
		},
		buildImage: function(imgData, $this, settings) {
			// Failed to respond
			if(!imgData)
				return;

			// Create the canvas
			var canvas = document.createElement('canvas');
			var scratch = document.createElement('canvas');
			
			// Convert MIME
			imgData = imgData.replace('application/octet-stream', 'image/png');
			
			// Draw the skin
			if(settings.draw == 'model')
				methods.draw_model(canvas, scratch, settings.scale, settings.hat, $this, imgData);
			else if(settings.draw == 'head')
				methods.draw_head(canvas, scratch, settings.scale, settings.hat, $this, imgData);
		},
		requestData: function(username, $this, settings) {
			var result;
			var yql = 'http://query.yahooapis.com/v1/public/yql?q=' + encodeURIComponent('SELECT * FROM data.uri WHERE url = "' + username + '"') + '&format=json&callback=?';

			$.getJSON(yql, function(data, result) {
				if (data.query.results.url)
					methods.buildImage(data.query.results.url, $this, settings);
			});				
		},
		draw_head: function(canvas, scratchCanv, scale, hat, $this, imgData) {	
			//Draws an isometric model of the given minecraft username
			var model = canvas.getContext('2d');
			var scratch = scratchCanv.getContext('2d');

			//Resize Scratch
			scratchCanv.setAttribute('width', 64 * scale);
			scratchCanv.setAttribute('height', 32 * scale);
			scratchCanv.setAttribute('class', 'scratch');
			
			//Resize Isometric Area (Found by trial and error)
			canvas.setAttribute('width', 20 * scale);
			canvas.setAttribute('height', 17.6 * scale);
			canvas.setAttribute('class', 'model');
			
			$this.append(canvas);
			$this.append(scratchCanv);
			
			var skin = new Image();

			skin.onload = function() {
				scratch.drawImage(skin,0,0,64,32,0,0,64,32);
				//Scale it
				scale_image(scratch.getImageData(0,0,64,32), scratch, 0, 0, scale);
				
				//Head
				//Head - Front
				model.setTransform(1,-0.5,0,1.2,0,0);
				model.drawImage(scratchCanv, 8*scale, 8*scale, 8*scale, 8*scale, 10*scale, 13/1.2*scale, 8*scale, 8*scale);
				//Head - Right
				model.setTransform(1,0.5,0,1.2,0,0);
				model.drawImage(scratchCanv, 0, 8*scale, 8*scale, 8*scale, 2*scale, 3/1.2*scale, 8*scale, 8*scale);
				//Head - Top
				model.setTransform(-1,0.5,1,0.5,0,0);
				model.scale(-1,1);
				model.drawImage(scratchCanv, 8*scale, 0, 8*scale, 8*scale, -3*scale, 5*scale, 8*scale, 8*scale);
				
				if(hat == true) {
					if(!is_one_color(scratch.getImageData(40*scale,8*scale,8*scale,8*scale))) {
						//Hat
						//Hat - Front
						model.setTransform(1,-0.5,0,1.2,0,0);
						model.drawImage(scratchCanv, 40*scale, 8*scale, 8*scale, 8*scale, 10*scale, 13/1.2*scale, 8*scale, 8*scale);
						//Hat - Right
						model.setTransform(1,0.5,0,1.2,0,0);
						model.drawImage(scratchCanv, 32*scale, 8*scale, 8*scale, 8*scale, 2*scale, 3/1.2*scale, 8*scale, 8*scale);
						//Hat - Top
						model.setTransform(-1,0.5,1,0.5,0,0);
						model.scale(-1,1);
						model.drawImage(scratchCanv, 40*scale, 0, 8*scale, 8*scale, -3*scale, 5*scale, 8*scale, 8*scale);
					}
				}
			}
			
			skin.src = imgData;
		},
		draw_model: function(canvas, scratchCanv, scale, hat, $this, imgData) {
			//Draws an isometric model of the given minecraft username
			var model = canvas.getContext('2d');
			var scratch = scratchCanv.getContext('2d');

			//Resize Scratch
			scratchCanv.setAttribute('width', 64 * scale);
			scratchCanv.setAttribute('height', 32 * scale);
			scratchCanv.setAttribute('class', 'scratch');
			
			//Resize Isometric Area (Found by trial and error)
			canvas.setAttribute('width', 20 * scale);
			canvas.setAttribute('height', 44.8 * scale);
			canvas.setAttribute('class', 'model');
			
			$this.append(canvas);
			$this.append(scratchCanv);
			
			var skin = new Image();

			skin.onload = function() {
				scratch.drawImage(skin,0,0,64,32,0,0,64,32);
				//Scale it
				scale_image(scratch.getImageData(0,0,64,32), scratch, 0, 0, scale);
				//Left Leg
				//Left Leg - Front
				model.setTransform(1,-0.5,0,1.2,0,0);
				model.scale(-1,1);
				model.drawImage(scratchCanv, 4*scale, 20*scale, 4*scale, 12*scale, -16*scale, 34.4/1.2*scale, 4*scale, 12*scale);
				
				//Right Leg
				//Right Leg - Right
				model.setTransform(1,0.5,0,1.2,0,0);
				model.drawImage(scratchCanv, 0*scale, 20*scale, 4*scale, 12*scale, 4*scale, 26.4/1.2*scale, 4*scale, 12*scale);
				//Right Leg - Front
				model.setTransform(1,-0.5,0,1.2,0,0);
				model.drawImage(scratchCanv, 4*scale, 20*scale, 4*scale, 12*scale, 8*scale, 34.4/1.2*scale, 4*scale, 12*scale);
				
				//Arm Left
				//Arm Left - Front
				model.setTransform(1,-0.5,0,1.2,0,0);
				model.scale(-1,1);
				model.drawImage(scratchCanv, 44*scale, 20*scale, 4*scale, 12*scale, -20*scale, 20/1.2*scale, 4*scale, 12*scale);
				//Arm Left - Top
				model.setTransform(-1,0.5,1,0.5,0,0);
				model.drawImage(scratchCanv, 44*scale, 16*scale, 4*scale, 4*scale, 0, 16*scale, 4*scale, 4*scale);
				
				//Body
				//Body - Front
				model.setTransform(1,-0.5,0,1.2,0,0);
				model.drawImage(scratchCanv, 20*scale, 20*scale, 8*scale, 12*scale, 8*scale, 20/1.2*scale, 8*scale, 12*scale);
				
				//Arm Right
				//Arm Right - Right
				model.setTransform(1,0.5,0,1.2,0,0);
				model.drawImage(scratchCanv, 40*scale, 20*scale, 4*scale, 12*scale, 0, 16/1.2*scale, 4*scale, 12*scale);
				//Arm Right - Front
				model.setTransform(1,-0.5,0,1.2,0,0);
				model.drawImage(scratchCanv, 44*scale, 20*scale, 4*scale, 12*scale, 4*scale, 20/1.2*scale, 4*scale, 12*scale);
				//Arm Right - Top
				model.setTransform(-1,0.5,1,0.5,0,0);
				model.scale(-1,1);
				model.drawImage(scratchCanv, 44*scale, 16*scale, 4*scale, 4*scale, -16*scale, 16*scale, 4*scale, 4*scale);
				
				//Head
				//Head - Front
				model.setTransform(1,-0.5,0,1.2,0,0);
				model.drawImage(scratchCanv, 8*scale, 8*scale, 8*scale, 8*scale, 10*scale, 13/1.2*scale, 8*scale, 8*scale);
				//Head - Right
				model.setTransform(1,0.5,0,1.2,0,0);
				model.drawImage(scratchCanv, 0, 8*scale, 8*scale, 8*scale, 2*scale, 3/1.2*scale, 8*scale, 8*scale);
				//Head - Top
				model.setTransform(-1,0.5,1,0.5,0,0);
				model.scale(-1,1);
				model.drawImage(scratchCanv, 8*scale, 0, 8*scale, 8*scale, -3*scale, 5*scale, 8*scale, 8*scale);
				
				if(hat == true) {
					if(!is_one_color(scratch.getImageData(40*scale,8*scale,8*scale,8*scale))) {
						//Hat
						//Hat - Front
						model.setTransform(1,-0.5,0,1.2,0,0);
						model.drawImage(scratchCanv, 40*scale, 8*scale, 8*scale, 8*scale, 10*scale, 13/1.2*scale, 8*scale, 8*scale);
						//Hat - Right
						model.setTransform(1,0.5,0,1.2,0,0);
						model.drawImage(scratchCanv, 32*scale, 8*scale, 8*scale, 8*scale, 2*scale, 3/1.2*scale, 8*scale, 8*scale);
						//Hat - Top
						model.setTransform(-1,0.5,1,0.5,0,0);
						model.scale(-1,1);
						model.drawImage(scratchCanv, 40*scale, 0, 8*scale, 8*scale, -3*scale, 5*scale, 8*scale, 8*scale);
					}
				}
			}
			
			skin.src = imgData;
		}
	};
	
	//Scales using nearest neighbour
	function scale_image(imageData, context, d_x, d_y, scale) {
		var width = imageData.width;
		var height = imageData.height;
		context.clearRect(0,0,width,height); //Clear the spot where it originated from
		for(y=0; y<height; y++) { //height original
			for(x=0; x<width; x++) { //width original
				//Gets original colour, then makes a scaled square of the same colour
				var index = (x + y * width) * 4;
				context.fillStyle = "rgba(" + imageData.data[index+0] + "," + imageData.data[index+1] + "," + imageData.data[index+2] + "," + imageData.data[index+3] + ")";
				context.fillRect(d_x + x*scale, d_y + y*scale, scale, scale);
			}
		}
	}
	
	//Checks if the provided imageData is one color
	function is_one_color(imageData) {
		var width = imageData.width;
		var height = imageData.height;
		var is_one_color = true;
		
		//Get First Pixel Color
		var pixel_data = "" + imageData.data[0] + imageData.data[1] + imageData.data[2]
		for(y=0; y<height; y++) { //height original
			for(x=0; x<width; x++) { //width original
				//Gets original colour, then makes a rectangle of it
				var index = (x + y * width) * 4;
				var compare = "" + imageData.data[index+0] + imageData.data[index+1] + imageData.data[index+2]
				if (compare !== pixel_data) {
					//Break loop if not one color
					is_one_color = false;
					break;
				}
			//Break loop if not one color
			if(is_one_color == false) break;
			}
		}
		return is_one_color;
	}
	
	$.fn[pluginName] = function( method ) {
		if ( methods[method] ) {
			return methods[method].apply( this, Array.prototype.slice.call( arguments, 1 ));
		} else if ( typeof method === 'object' || !method ) {
			return methods.init.apply( this, arguments );
		} else {
			$.error( 'Method ' + method + ' does not exist in jQuery.' + pluginName );
		}
	};
})( jQuery );
