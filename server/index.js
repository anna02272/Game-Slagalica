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
let roundIndex = 0;
let answerIndex = 0;
let confirmClicked = 0;
let isContinued;

app.get('/getSocketId', (req, res) => {
    const socketId = req.query.socketId;
    res.send(socketId);
});

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
                io.emit('updatePlayingUsers', playingUsernamesArray, playingSocketsArray);
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
                           playingUsernamesArray: playingUsernamesArray,
                           playingSocketsArray: playingSocketsArray
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

         socket.on('timerStart', (targetSocketId) => {
                  io.to(targetSocketId).emit('timerStarted');
         });

          socket.on('timerStart10', (targetSocketId) => {
                           io.to(targetSocketId).emit('timerStarted10');
                  });

//GAMES
            socket.on('startNextGame', () => {
                    io.emit('startNextGame');
                });

             socket.on('continueGame', () => {
                     io.emit('continueGame');
            });
              socket.on('endGame', () => {
                   io.emit('endGame');
              });
               socket.on('startActivity', () => {
                    io.emit('startActivity');
               });

             socket.on('showToast', (message) => {
               io.emit('showToast', message);
             });

             socket.on('disableTouch', (targetSocketId) => {
                io.to(targetSocketId).emit('touchDisabled');
            });

            socket.on('enableTouch', (targetSocketId) => {
                io.to(targetSocketId).emit('touchEnabled');
            });

            socket.on('incrementRoundIndex', () => {
                roundIndex++;
                io.emit('updateRoundIndex', roundIndex);
              });

              socket.on('decrementRoundIndex', () => {
                roundIndex--;
                io.emit('updateRoundIndex', roundIndex);
              });
             socket.on('incrementAnswerIndex', () => {
                 answerIndex++;
                 io.emit('updateAnswerIndex', answerIndex);
               });

               socket.on('decrementAnswerIndex', () => {
                 answerIndex--;
                 io.emit('updateAnswerIndex', answerIndex);
               });

               socket.on('continuedTrue', () => {
                 isContinued = true;
                 io.emit('updateContinued', isContinued);
               });

               socket.on('continuedFalse', () => {
                 isContinued = false;
                 io.emit('updateContinued', isContinued);
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

            socket.on("message_received", (message) => {
                 io.emit('message_received', message);
             });

//KORAK PO KORAK
            socket.on('stepChange', ( buttonId, step, answer) => {
                   io.emit('stepChange', buttonId, step, answer);
               });

            socket.on("answer", (answer) => {
                  io.emit("answer", answer);
           });
            socket.on('timerText', (buttonIndex, text) => {
                   io.emit('timerText', buttonIndex, text);
               });
             socket.on('buttonText', (buttonId, enabled, step) => {
                    io.emit('buttonText', buttonId, enabled, step);
             });
              socket.on('buttonClickable', (buttonId, clickable, step) => {
                  io.emit('buttonClickable', buttonId, clickable, step);
             });


//MOJ BROJ
            socket.on('numberChange', ( buttonId, number) => {
                io.emit('numberChange', buttonId, number);
            });

           socket.on('answerChange', ( answer, finalAnswer) => {
                 io.emit('answerChange', answer, finalAnswer);
           });

            socket.on('setButtonText', (buttonId, buttonText) => {
               io.emit('setButtonText', buttonId, buttonText);
            });

            socket.on('buttonEnabled', (buttonId, enabled) => {
                io.emit('buttonEnabled', buttonId, enabled);
            });



           socket.on('inputGone', (targetSocketId) => {
               io.to(targetSocketId).emit('inputGone');
           });

          socket.on('input2Gone', (targetSocketId) => {
                io.to(targetSocketId).emit('input2Gone');
          });
          socket.on("inputText", (text) => {
                io.emit("inputText", text);
          });
           socket.on("input1Text", (text, targetSocketId) => {
                   io.to(targetSocketId).emit("input1Text", text);
           });
           socket.on("input2Text", (text, targetSocketId) => {
                 io.to(targetSocketId).emit("input2Text", text);
           });
          socket.on('showToast', (message, targetSocketId) => {
                   io.to(targetSocketId).emit('showToast', message);
          });
           socket.on('incrementConfirmCount', () => {
                confirmClicked++;
            io.emit('updateConfirmClicked', confirmClicked);
           });

           socket.on('decrementConfirmCount', () => {
              confirmClicked--;
           io.emit('updateConfirmClicked', confirmClicked);
           });

           socket.on('checkTwoAnswers', () => {
              io.emit('checkTwoAnswers');
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
                 roundIndex = 0;
                 answerIndex = 0;
                 confirmClicked = 0;
        });
           socket.on('playerDisconnected', (userInfo) => {
                 const { username } = userInfo;
                      delete playingUsers[socket.id];
                     playingUsernamesArray = Object.values(playingUsers).map(user => user.username);
                      playingSocketsArray = Object.values(playingUsers).map(user => user.socket.id);
                      io.emit('updatePlayingUsers', playingUsernamesArray, playingSocketsArray);
                          console.log("Playing Users:",playingUsernamesArray)
                     playingSocketsArray = Object.values(playingUsers).map(user => user.socket.id);
                         userReadyCount = 0;
                         isGameStarting = false;
                         roundIndex = 0;
                         answerIndex = 0;
                         confirmClicked = 0;

                });

    socket.on('disconnect', (userInfo) =>{
       delete connectedUsers[socket.id];
       delete playingUsers[socket.id];

        usernamesArray = Object.values(connectedUsers).map(user => user.username);
       io.emit('updateConnectedUsers', usernamesArray);
   	   console.log("Connected Users:",usernamesArray)

   	     playingUsernamesArray = Object.values(playingUsers).map(user => user.username);
   	      playingSocketsArray = Object.values(playingUsers).map(user => user.socket.id);
         io.emit('updatePlayingUsers', playingUsernamesArray, playingSocketsArray);
        console.log("Playing Users:",playingUsernamesArray)

   	   userReadyCount = 0;
       isGameStarting = false;
       roundIndex = 0;
       answerIndex = 0;
       confirmClicked = 0;
   	});


});

