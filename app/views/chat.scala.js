@(channel: String, userId: Integer)

function d2(n) {
    if(n<9) return "0"+n;
    return n;
}

$(function(){
    var WS = window['MozWebSocket'] ? window['MozWebSocket'] : WebSocket;
    var socket = new WS('@routes.ChannelController.chatInterface(channel).webSocketURL(request)');

    socket.onopen = function(e){

    }

    var writeMessages = function(event){

        var msg = JSON.parse(event.data);
        var d = new Date().toTimeString().split(' ')[0];
        var sender = msg.sender;
        var message = msg.message;



        $('#chatbox').prepend('<p><small>('+d+')</small>'+sender+': '+ message +'</p>');
    }

    socket.onmessage = writeMessages;


    var sendMessage = function(message){
        if(message.trim()!='')
        {
            socket.send("{\"sender\":\"@userId\", \"message\":\"" + message + "\"}");
        }
    }

    document.getElementById("submitmsg").onclick = function(event){
        var message = document.getElementById("usermsg").value;
        sendMessage(message);

        $("#usermsg").val('');

    };

    $('#usermsg').keyup(function(event){
        var charCode = (event.which) ? event.which : event.keyCode ;

        if(charCode === 13 && charCode!=null){
            sendMessage($(this).val());
            $(this).val('');
        }
    });


});