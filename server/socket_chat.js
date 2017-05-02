var serv = require('./server.js');
var db= require('./db.js');

var numUsers=0;
var users={};

// rooms which are currently available in chat
var rooms = ['global','privat','room3'];


module.exports.listen = function(io, socket){
    var addedUser = false;
    var clientIp = socket.request.connection.remoteAddress;
    //console.log(socket); //all info from connection

    socket.on('new message',function (msg) {
        var newMsg= new db.tbMsg({ip: clientIp, username: socket.username, message: msg,socId: socket.id});

        newMsg.save(function(err){
            if(err){
                console.log(err);
            }else{
                //console.log("msg saved in db");
            }
        });

        socket.broadcast.emit('new message', {
            username: socket.username,
            message: msg,
            id: socket.id
        });
    });

    socket.on('private message',function(msg,sid,callback){

        for(var key in users){
            if(users[key] == sid){
                callback(true);
                io.sockets.connected[sid].emit('priv message',{
                    username:socket.username,
                    message:'(priv) '+msg,
                    id:socket.id
                });
                console.log('priv msg sent...');
            }else{
                callback(false);
                console.log('user offline');
            }
        }


    });

    socket.on('typing', function () {
        socket.broadcast.emit('typing', {
            username: socket.username
        });
    });

    socket.on('stop typing', function () {
        socket.broadcast.emit('stop typing', {
            username: socket.username
        });
    });

    socket.on('add user', function (username,callback) {
        if (addedUser) return;

        if (username in users){
            callback(false);
        }else{
            callback(true);

            socket.username = username;
            users[socket.username]=socket.id;
            ++numUsers;
            addedUser = true;

            socket.emit('login', {
                numUsers: numUsers,
                userList: users
            });

            socket.broadcast.emit('user joined', {
                username: socket.username,
                numUsers: numUsers,
                id: socket.id
            });

            console.log(socket.username+' joined.');
        }
    });

    socket.on('add fbuser', function (fbName, fbId) {
        if (addedUser) return;

        socket.username = fbName;
        socket.fbId=fbId;
        users[fbId] = socket.id;
        ++numUsers;
        addedUser = true;

        socket.emit('login', {
            numUsers: numUsers,
            userList: users
        });

        socket.broadcast.emit('user joined', {
            username: socket.username,
            numUsers: numUsers,
            id: socket.id
        });

        console.log(socket.username + ' joined.');
    });

    socket.on('get messages',function () {
        //send last 5 message if db online
        try{
            var query =db.tbMsg.find({}).select({username:1,message:1,created:1,socId:1});
            query.sort('-created').limit(5).exec(function(err,docs){
                if(err) throw err;
                socket.emit('load old msgs',docs);
            });
            console.log('get messages...');
        }catch (err){
            console.log(err);
        }
    });

    socket.on('disconnect', function () {
        if (addedUser) {
            --numUsers;

            if(socket.hasOwnProperty("fbId")){
                delete users[socket.fbId];
            }else{
                delete users[socket.username];
            }

            socket.broadcast.emit('user left', {
                username: socket.username,
                numUsers: numUsers,
                id: socket.id
            });

            console.log(socket.username+' left.');
        }
    });

};