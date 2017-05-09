@(channel: String)

channelWS("@channel");

var WS;
var socket;
function channelWS(){
    WS = window['MozWebSocket'] ? window['MozWebSocket'] : WebSocket;

    socket = new WS('@routes.ChannelController.viewCountInterface(channel).webSocketURL(request)');


    var changeViewCount = function(count){
        document.getElementById('viewCount').innerHTML = count;
    }

    var changeTitle = function(title){
        var titleElement = document.getElementById('stream-title');
        titleElement.innerHTML = title;
    }

    var updateChannel = function(event){
        console.log(event.data);
        if(event.data.match(/^\[[0-9]\]/i)){
            var count = event.data.replace(/[\[\]']+/g ,"");
            changeViewCount(count);
        }else if(event.data.match(/^\[title\].*/i)){
            var title = event.data.substring(7, event.data.length);
            changeTitle(title);
        }
    }

    socket.onmessage = updateChannel;
}

function getSocket(){
    return socket;
}