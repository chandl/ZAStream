@(channel: String)



$(function(){
    var WS = window['MozWebSocket'] ? window['MozWebSocket'] : WebSocket;
    var socket = new WS('@routes.ChannelController.chatInterface(channel).webSocketURL(request)');

    var text = '{ "employees" : [' +
        '{ "firstName":"John" , "lastName":"Doe" },' +
        '{ "firstName":"Anna" , "lastName":"Smith" },' +
        '{ "firstName":"Peter" , "lastName":"Jones" } ]}';

    socket.onopen = function(e){
        socket.send(text);
    }

    var writeMessages = function(event){
        $('#chatbox').prepend('<p>'+event.data+'</p>');
    }

    socket.onmessage = writeMessages;

    // if enter (charcode 13) is pushed, send message, then clear input field
    document.getElementById("submitmsg").onclick = function(event){

        socket.send("test");


        // var charCode = (event.which) ? event.which : event.keyCode ;
        //
        // if(charCode === 13){
        //     socket.send($(this).val());
        //     $(this).val('');
        // }
    };
}