// Setup basic express server
var express = require('express');
var app = express();
var server = require('http').createServer(app);
var io = require('socket.io').listen(server);
var port = process.env.OPENSHIFT_NODEJS_PORT || 3000;
var ip = process.env.OPENSHIFT_NODEJS_IP || "0.0.0.0";
require('./db.js');

server.listen(port,ip,function () {
  var adr = server.address();
  console.log('Server listening at '+ adr.address +':' +  adr.port);
});

// Routing
app.use(express.static(__dirname + '/public'));

// Chatroom
io.sockets.on('connection', function (socket) {
  require('./socket_chat').listen(io, socket);
});


