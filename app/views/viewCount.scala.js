@(channel: String)

viewCount("@channel");



function viewCount(){

    var WS = window['MozWebSocket'] ? window['MozWebSocket'] : WebSocket;

    var socket = new WS('@routes.ChannelController.viewCountInterface(channel).webSocketURL(request)');


    var changeViewCount = function(event){

        document.getElementById('viewCount').innerHTML = event.data;
    }
    socket.onmessage = changeViewCount
}