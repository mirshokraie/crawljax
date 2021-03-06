window.xhr = new XMLHttpRequest();
window.buffer = new Array();
var idCounter = new Date().getTime();
var globalCounter=0;

function send(value) {
	window.buffer.push(value);
	if(window.buffer.length == 200){

		sendReally();	
	}
	
}

function sendReally() {
	window.xhr.open('POST', document.location.href + '?thisisaclickabletracingcall', false);
	window.xhr.send(JSON.stringify(window.buffer));
	window.buffer = new Array();
}


function addFunctionNodeTrack(functionName, functionInfo) {

	return new Array("addFunctionNodeTrack",functionName, functionInfo);
				
}

function giveUniqueId(elem, eventHandler, eventType ) {
	
	var element=$(elem).clone();
	var idCounterList=new Array();
	for(var i=0;i<$(element).get().length;i++){
		 if($($(element).get(i)).prop("id")==""){
		  $($(element).get(i)).prop("id","assignedId"+"_"+idCounter);
		  idCounterList[i]="assignedId"+"_"+idCounter;
		  idCounter++;
		 }
		 else{
			 idCounterList[i]=$($(element).get(i)).prop("id");
		 }
	}

	
	var eventHandlerToBeSent=functionName(eventHandler);
	if(eventHandlerToBeSent==""){
		eventHandlerToBeSent="someFunction"+globalCounter;
		globalCounter++;
	}

	 return  new Array("giveUniqueId",idCounterList,  eventHandlerToBeSent, eventType);
}

function functionName(fun) {
	  var ret = fun.toString();
	  ret = ret.substr('function '.length);
	  ret = ret.substr(0, ret.indexOf('('));
	  return ret;
};
