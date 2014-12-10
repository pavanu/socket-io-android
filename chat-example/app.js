var fs = require('fs');
var privateKey  = fs.readFileSync('ssl/server.key', 'utf8');
var certificate = fs.readFileSync('ssl/server.crt', 'utf8');
var options = {key: privateKey, cert: certificate};

var app = require('express')();
var https = require('https').createServer(options, app);
var io = require('socket.io')(https);

app.get('/', function(req, res){
  res.sendFile(__dirname + '/index.html');
});

io.on('connection', function(socket){
  socket.on('send:message', function(msg){
    io.emit('message', msg);
  });
});

port = 443
https.listen(port, function(){
  console.log('listening on *:' + port);
});
