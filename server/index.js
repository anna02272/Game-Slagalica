const express = require('express');
const socket = require('socket.io');
const fs = require('fs');
const app = express();
const port = 3000;

const server = app.listen(port);
app.use(express.static('public'));
console.log('Server is running');
const io = socket(server);

io.on('connection', (socket) => {
    console.log("Player connected: " + socket.id);

    socket.on('disconnect',function(){
   		console.log("Player disconnected!");
   	});
});
