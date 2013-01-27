window.xhr = new XMLHttpRequest();
window.buffer = new Array();
var idCounter = new Date().getTime();

function send(value) {
	window.buffer.push(value);

		sendReally();	
	
}

function sendReally() {
	window.xhr.open('POST', document.location.href + '?thisisaclickabletracingcall', false);
	window.xhr.send(JSON.stringify(window.buffer));
	window.buffer = new Array();
}


function addFunctionNodeTrack(functionName, functionInfo) {

	return new Array("addFunctionNodeTrack",functionName, functionInfo);
				
}

function giveUniqueId(element, eventHandler ) {
	
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

	//	document.write(idCounterList[1]);
	 return  new Array("giveUniqueId",idCounterList, eventHandler);
};