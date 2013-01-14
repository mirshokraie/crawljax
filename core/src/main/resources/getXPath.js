window.xhr = new XMLHttpRequest();
window.buffer = new Array();

function send(value) {
	window.buffer.push(value);
	if(window.buffer.length == 200) {
		sendReally();	
	}
}

function sendReally() {
	window.xhr.open('POST', document.location.href + '?thisisafuncexectracingcall', false);
	window.xhr.send(JSON.stringify(window.buffer));
	window.buffer = new Array();
}

function addFunctionCallTrack(funcCallerName, funcCalleeName) {
	
	return new Array(funcCallerName, funcCalleeName);
				
}

function addFunctionNodeTrack(functionName, functionInfo) {
	
	return new Array(functionName, functionInfo);
				
}


function getXPath( element, eventHanlder ) {
    var xpath = '';
    for ( ; element && element.nodeType == 1; element = element.parentNode )
    {
       var id = $(element.parentNode).children(element.tagName).index(element) + 1;
       id > 1 ? (id = '[' + id + ']') : (id = '');
       xpath = '/' + element.tagName.toLowerCase() + id + xpath;
    }
    return  new Array(xpath, eventHandler);
};