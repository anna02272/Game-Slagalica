const express = require('express');
const socket = require('socket.io');
const fs = require('fs');
const app = express();
const port = 3000;

const server = app.listen(port);
app.use(express.static('public'));
console.log('Server is running');
const io = socket(server);

const connectedUsers = {};
const playingUsers = {};
let userReadyCount = 0;
let isGameStarting = false;


io.on('connection', (socket) => {
//    console.log("Player connected: " + socket.id);

     socket.on('userConnected', (userInfo) => {
            const { username } = userInfo;
            connectedUsers[socket.id] = { username: username, socket: socket };
//            console.log(`User ${username} connected.`);
           const usernamesArray = Object.values(connectedUsers).map(user => user.username);
              io.emit('updateConnectedUsers', usernamesArray);
              console.log("Connected Users:",usernamesArray)
        });
         socket.on('userPlaying', (userInfo) => {
                    const { username } = userInfo;
                    playingUsers[socket.id] = { username: username, socket: socket };
                   const playingUsernamesArray = Object.values(playingUsers).map(user => user.username);
                      io.emit('updatePlayingUsers', playingUsernamesArray);
                      console.log("Playing Users:",playingUsernamesArray)
                });
        socket.on('userDisconnected', (userInfo) => {
         const { username } = userInfo;
             delete connectedUsers[socket.id];
            const usernamesArray = Object.values(connectedUsers).map(user => user.username);
               io.emit('updateConnectedUsers', usernamesArray);
                 console.log("Connected Users:",usernamesArray)
                 userReadyCount = 0;
                 isGameStarting = false;
        });
           socket.on('playerDisconnected', (userInfo) => {
                 const { username } = userInfo;
                      delete playingUsers[socket.id];
                    const playingUsernamesArray = Object.values(playingUsers).map(user => user.username);
                          io.emit('updatePlayingUsers', playingUsernamesArray);
                          console.log("Playing Users:",playingUsernamesArray)
                         userReadyCount = 0;
                         isGameStarting = false;
                });

          socket.on('startGame', () => {
                userReadyCount++;
                if (userReadyCount === 2 && !isGameStarting) {
                    isGameStarting = true;
                    io.emit('gameStarting');
                    setTimeout(() => {
                        io.emit('startActualGame');
                        userReadyCount = 0;
                        isGameStarting = false;
                    }, 4000);
                }
            });

    socket.on('disconnect', (userInfo) =>{
       delete connectedUsers[socket.id];
       delete playingUsers[socket.id];

       const usernamesArray = Object.values(connectedUsers).map(user => user.username);
       io.emit('updateConnectedUsers', usernamesArray);
   	   console.log("Connected Users:",usernamesArray)

   	    const playingUsernamesArray = Object.values(playingUsers).map(user => user.username);
        io.emit('updatePlayingUsers', playingUsernamesArray);
        console.log("Playing Users:",playingUsernamesArray)

   	   userReadyCount = 0;
       isGameStarting = false;
   	});

});


