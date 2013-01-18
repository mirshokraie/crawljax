window.xhr = new XMLHttpRequest();
window.buffer = new Array();
var idCounter = new Date().getTime();

function send(value) {
	window.buffer.push(value);
	if(window.buffer.length == 200) {
		sendReally();	
	}
}

function sendReally() {
	window.xhr.open('POST', document.location.href + '?thisisaclickabletracingcall', false);
	window.xhr.send(JSON.stringify(window.buffer));
	window.buffer = new Array();
}



function giveUniqueId(element, eventHanlder ) {
	var idCounterList=new Array();
	for(var i=0;i<element.get().length;i++){
		 if($(element.get(i)).prop("id")==""){
		  $(element.get(i)).prop("id",idCounter);
		  idCounterList.push(idCounter);
		  idCounter++;
		 }
	}
	 return  new Array(idCounterList, eventHandler);
};