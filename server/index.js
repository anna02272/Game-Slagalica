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
let playingUsernamesArray =  [];
let usernamesArray =  [];
let socketsArray =  [];

io.on('connection', (socket) => {
//CONNECTION

     socket.on('userConnected', (userInfo) => {
            const { username } = userInfo;
            connectedUsers[socket.id] = { username: username, socket: socket };
            usernamesArray = Object.values(connectedUsers).map(user => user.username);
            socketsArray = Object.values(connectedUsers).map(user => user.socket.id);
              io.emit('updateConnectedUsers', usernamesArray);
              console.log("Connected Users:",usernamesArray)
        });
         socket.on('userPlaying', (userInfo) => {
                const { username } = userInfo;
                playingUsers[socket.id] = { username: username, socket: socket };
                playingUsernamesArray = Object.values(playingUsers).map(user => user.username);
                playingSocketsArray = Object.values(playingUsers).map(user => user.socket.id);
                io.emit('updatePlayingUsers', playingUsernamesArray);
                console.log("playingSocketsArray",playingSocketsArray)
                console.log("Playing Users:",playingUsernamesArray)
         });

//START GAME
         socket.on('startGame', () => {
         userReadyCount++;
         if (userReadyCount === 2 && !isGameStarting) {
               isGameStarting = true;
                playingUsernamesArray = [];
                playingSocketsArray = [];
                io.emit('gameStarting');
                 setTimeout(() => {
                           io.emit('startActualGame', {
                           playingUsernamesArray: usernamesArray,
                           playingSocketsArray: socketsArray
                           });
                           userReadyCount = 0;
                           isGameStarting = false;
                   }, 4000);
                         }
          });

//TIMER
        socket.on('startTimer', (timerData) => {
                io.emit('syncTimer', timerData);
            });


//SPOJNICE
           socket.on('stepChanged', (stepIndex, step) => {
                   io.emit('stepChanged', stepIndex, step);
               });

            socket.on("answerChanged", ( shuffledIndex, answerIndex, answer) => {
                              io.emit("answerChanged",shuffledIndex, answerIndex , answer);
                        });

            socket.on("buttonStateChanged", (data) => {
                const enableState = data.enableState;
                io.emit("buttonStateChanged", enableState);
            });

            socket.on('colorChange', (eventData) => {
                   io.emit('colorChange', eventData);
               });

             socket.on("reset_received", (reset) => {
                io.emit('reset_received', reset);
             });

             socket.on('startNextGame', () => {
                    io.emit('startNextGame');
                });

             socket.on('continueGame', () => {
                     io.emit('continueGame');
            });

             socket.on('showToast', (message) => {
               io.emit('showToast', message);
             });

            socket.on('disableTouch', (targetSocketId) => {
                console.log(`Disabling touch for socket ID: ${targetSocketId}`);
                io.to(targetSocketId).emit('touchDisabled');
            });

            socket.on('enableTouch', (targetSocketId) => {
                console.log(`Enabling touch for socket ID: ${targetSocketId}`);
                io.to(targetSocketId).emit('touchEnabled');
            });



//DISCONNECT
        socket.on('userDisconnected', (userInfo) => {
         const { username } = userInfo;
             delete connectedUsers[socket.id];
             usernamesArray = Object.values(connectedUsers).map(user => user.username);
               io.emit('updateConnectedUsers', usernamesArray);
                 console.log("Connected Users:",usernamesArray)
                 userReadyCount = 0;
                 isGameStarting = false;
        });
           socket.on('playerDisconnected', (userInfo) => {
                 const { username } = userInfo;
                      delete playingUsers[socket.id];
                     playingUsernamesArray = Object.values(playingUsers).map(user => user.username);
                          io.emit('updatePlayingUsers', playingUsernamesArray);
                          console.log("Playing Users:",playingUsernamesArray)
                     playingSocketsArray = Object.values(playingUsers).map(user => user.socket.id);
                         userReadyCount = 0;
                         isGameStarting = false;

                });

    socket.on('disconnect', (userInfo) =>{
       delete connectedUsers[socket.id];
       delete playingUsers[socket.id];

        usernamesArray = Object.values(connectedUsers).map(user => user.username);
       io.emit('updateConnectedUsers', usernamesArray);
   	   console.log("Connected Users:",usernamesArray)

   	     playingUsernamesArray = Object.values(playingUsers).map(user => user.username);
   	      playingSocketsArray = Object.values(playingUsers).map(user => user.socket.id);
        io.emit('updatePlayingUsers', playingUsernamesArray);
        console.log("Playing Users:",playingUsernamesArray)

   	   userReadyCount = 0;
       isGameStarting = false;
   	});


});

