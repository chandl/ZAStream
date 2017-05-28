@(room: String, userId: Integer)

function d2(n) {
    if(n<9) return "0"+n;
    return n;
}

function updateUserList(users){
    var list = users.split("||");

    document.getElementById("user-list").innerHTML = "";

    for(var i=0; i<list.length; i++){
        var li = document.createElement("li");
        li.className = "user-list-user";

        var a = document.createElement("a");
        a.innerHTML=list[i];
        a.href="/"+list[i];

        li.appendChild(a);

        document.getElementById("user-list").appendChild(li);
    }
}

function userListJoin(user){
    var li = document.createElement("li");
    li.className = "user-list-user";

    var a = document.createElement("a");
    a.innerHTML=user;
    a.href="/"+user;

    li.appendChild(a);

    document.getElementById("user-list").appendChild(li);
}

function userListLeave(user){
    var ul = document.getElementById("user-list");

    var userList = ul.getElementsByTagName("li");

    for(var i = 0; i < userList.length; i++){

        if(userList[i].innerText === user){
            ul.removeChild(userList[i]);
            return ;
        }
    }
}

$(function(){
    var WS = window['MozWebSocket'] ? window['MozWebSocket'] : WebSocket;
    var socket = new WS('@routes.ChatController.chatInterface(room).webSocketURL(request)');

    var handleMessage = function(event){
        var msg = JSON.parse(event.data);
        var d = new Date().toTimeString().split(' ')[0];
        var sender = msg.sender;
        var message = msg.message;

        var userList =  document.getElementById('user-list');
        if (typeof(userList) !== 'undefined' && userList !== null)
        {
            switch(sender){
                case "?chatUpdate":
                    updateUserList(message);
                    break;
                case "?chatJoin":
                    userListJoin(message);
                    break;
                case "?chatLeave":
                    userListLeave(message);
                    break;
                default:
                    writeMessages(event);
            }
        }else{
            if(!sender.startsWith("?")){ //code meaning a user list update.
                writeMessages(event);
            }
        }

    };

    var writeMessages = function(event){
        var msg = JSON.parse(event.data);
        var d = new Date().toTimeString().split(' ')[0];
        var sender = msg.sender;
        var message = msg.message;


        $('#chatbox').prepend('<p><small>['+d+']</small><span class="chat-user">'+sender+'</span>: <span class="chat-msg">'+ message +'</span></p>');
    };

    socket.onmessage = handleMessage;


    var sendMessage = function(message){
        if(message.trim()!='')
        {
            message = message.replace(/\"/gi, "&#34;");
            socket.send("{\"sender\":\"@userId\", \"message\":\"" + message + "\"}");
        }
    };

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