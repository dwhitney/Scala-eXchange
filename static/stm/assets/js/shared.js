// Manages the presentation's state
var PIK5 = function(){

	var self         = this;
	var win          = $(window);
	this.slides      = null;
	this.current     = 0;
	this.hidden      = 0;
	this.location    = null;
	this.isPresenter = (location.href.substr(-14) == 'presenter.html');
	this.inPresenter = (!this.isPresenter && /presenter\.html$/.test(parent.location + ''));

	// Setup location
	if(!this.isPresenter){
		this.location = location.href;
	}

	// Setup worker
	if(typeof SharedWorker == 'function' && !this.inPresenter){
		try {
			var path = 'assets/js/worker.js';
			path = (typeof pik5_base_dir != 'undefined') ? pik5_base_dir + path : path;
			this.worker = new SharedWorker(path, 'Pik5');
		}
		catch(e){
			this.worker = null;
			console.log('Failed to create Web Worker - multi window presentations and presenter view disabled');
			console.log(e);
		}
	}
	else {
		this.worker = null;
	}

	// Wrapper for worker.postMessage that does nothing if no worker is available
	this.postMessage = function(data){
		if(this.worker !== null){
			this.worker.port.postMessage(data);
		}
	}

	// Main presentation control functions
	this.slideNext = function(propagate){
		var newIndex = this.current + 1;
		if(this.slides && this.slides[newIndex]){
			this.slideTo(newIndex, propagate);
		}
	}
	this.slideBack = function(propagate){
		var newIndex = this.current - 1;
		if(this.slides && this.slides[newIndex]){
			this.slideTo(newIndex, propagate);
		}
	}
	this.slideTo = function(index, propagate){
		if(!this.inPresenter && this.slides){
			if(this.slides[this.current]){
				$(this.slides[this.current]).trigger('deactivate');
			}
			if(this.slides[index]){
				$(this.slides[index]).trigger('activate');
			}
			win.trigger('change', index);
		}
		this.current = index;
		win.trigger('slideTo', index);
		if(propagate){
			this.postMessage({ 'current': this.current });
		}
	}
	this.hide = function(propagate){
		this.hidden = 1;
		win.trigger('hide');
		if(propagate){
			this.postMessage({ 'hidden': this.hidden });
		}
	}
	this.show = function(propagate){
		this.hidden = 0;
		win.trigger('show');
		if(propagate){
			this.postMessage({ 'hidden': this.hidden });
		}
	}
	this.setHidden = function(value){
		(value == 1) ? this.hide() : this.show();
	}
	this.toggleHidden = function(propagate){
		if(this.hidden == 0){
			this.hide(propagate);
		}
		else {
			this.show(propagate);
		}
	}
	this.setLocation = function(url){
		win.trigger('location', url);
	}
	this.goTo = function(url, propagate){
		win.trigger('goTo', url);
		if(propagate){
			this.postMessage({ 'goTo': url });
		}
	}

	// Recieve events from the worker
	if(this.worker !== null){
		this.worker.port.addEventListener('message', function(evt){
			if(evt.data){
				// Slide number
				if(typeof evt.data.current != 'undefined'){
					if(evt.data.current !== self.current){
						self.slideTo(evt.data.current, false);
					}
				}
				// Hidden state
				if(typeof evt.data.hidden != 'undefined'){
					if(evt.data.hidden !== self.hidden){
						self.setHidden(evt.data.hidden, false);
					}
				}
				// Location
				if(typeof evt.data.location != 'undefined'){
					if(evt.data.location !== null && evt.data.location != self.location){
						self.setLocation(evt.data.location);
					}
				}
				// goTo event
				if(typeof evt.data.goTo != 'undefined'){
					self.goTo(evt.data.goTo, false);
				}
			}
		});
		this.worker.port.start();
	}

	// Initial sync
	this.postMessage({ 'location': this.location });

}
var pik5 = new PIK5();


// Catch keypress events
jQuery(document).ready(function($){
	$(document).keydown(function(evt){
		var code = evt.keyCode;
		if(code == 39 || code == 34){
			pik5.slideNext(true);
		}
		else if(code == 37 || code == 33){
			pik5.slideBack(true);
		}
		else if(code == 116 || code == 190 || code == 27){
			pik5.toggleHidden(true);
			evt.preventDefault();
		}
	});
});
