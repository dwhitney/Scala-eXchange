// Connection pool, current slide and hidden state
var pool     = [],
    current  = 0,
    hidden   = 0,
    location = null;

self.onconnect = function(evt){

	// Add to the connection pool
	var port = evt.ports[0];
	pool.push(port);

	// Recieve messages
	port.onmessage = function(msg){
		if(msg.data){

			// Request state
			if(typeof msg.data.request != 'undefined'){
				port.postMessage({
					'current' : current,
					'hidden'  : hidden,
					'location': location
				});
			}

			// goTo-Event
			else if(typeof msg.data.goTo != 'undefined'){
				current = 0;
				for(var i = 0; i < pool.length; i++){
					pool[i].postMessage({
						'current' : current,
						'goTo': msg.data.goTo
					});
				}
			}

			// Update state
			else {
				if(typeof msg.data.current != 'undefined'){
					current = msg.data.current;
				}
				if(typeof msg.data.hidden != 'undefined'){
					hidden = msg.data.hidden;
				}
				if(typeof msg.data.location != 'undefined' && msg.data.location !== null){
					location = msg.data.location;
				}
				for(var i = 0; i < pool.length; i++){
					pool[i].postMessage({
						'current' : current,
						'hidden'  : hidden,
						'location': location
					});
				}
			}

		}
	};

};
