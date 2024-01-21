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
let answerIndex1 = 0;
let confirmClicked = 0;
let isContinued;
let roundIndex1 = 0;
let roundIndex2 = 0;
let confirmClicked1 = 0;
let isContinued1;
let currentGame = 0;
let currentGame1 = 0;
let currentGame2 = 0;
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
                io.emit('updatePlayingUsers1', playingUsernamesArray, playingSocketsArray);
                io.emit('updatePlayingUsers2', playingUsernamesArray, playingSocketsArray);
                io.emit('updatePlayingUsers3', playingUsernamesArray, playingSocketsArray);
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
          socket.on('timerStart1', (targetSocketId) => {
                        io.to(targetSocketId).emit('timerStarted1');
               });

           socket.on('1timerStart10', (targetSocketId) => {
                       io.to(targetSocketId).emit('1timerStarted10');
               });
            socket.on('timerStart2', (targetSocketId) => {
                        io.to(targetSocketId).emit('timerStarted2');
               });

           socket.on('2timerStart10', (targetSocketId) => {
                       io.to(targetSocketId).emit('2timerStarted10');
               });


//SPOJNICE

         socket.on('retrieveSteps', (targetSocketId) => {
                  io.to(targetSocketId).emit('retrieveSteps');
         });
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
                  console.log("roundIndex: " + roundIndex)
                 io.emit('updateRoundIndex', roundIndex);
               });

               socket.on('decrementRoundIndex', () => {
                 roundIndex--;
                  console.log("roundIndex: " + roundIndex)
                 io.emit('updateRoundIndex', roundIndex);
               });
            socket.on('incrementCurrentGame', () => {
                 currentGame++;
                 console.log("currentGame: " + currentGame)
                 io.emit('updateCurrentGame', currentGame);
               });

                socket.on('continuedTrue', () => {
                  isContinued = true;
                  io.emit('updateContinued', isContinued);
                });

                socket.on('continuedFalse', () => {
                  isContinued = false;
                  io.emit('updateContinued', isContinued);
                });
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
         socket.on('buttonClickableSpojnice', (buttonId, clickable) => {
                          io.emit('buttonClickableSpojnice', buttonId, clickable);
                     });
//KORAK PO KORAK
             socket.on('startNextGame1', () => {
                     io.emit('startNextGame1');
                 });

              socket.on('continueGame1', () => {
                      io.emit('continueGame1');
             });
               socket.on('endGame1', () => {
                    io.emit('endGame1');
               });
                socket.on('startActivity1', () => {
                     io.emit('startActivity1');
                });

              socket.on('showToast1', (message) => {
                io.emit('showToast1', message);
              });

              socket.on('disableTouch1', (targetSocketId) => {
                 io.to(targetSocketId).emit('touchDisabled1');
             });

             socket.on('enableTouch1', (targetSocketId) => {
                 io.to(targetSocketId).emit('touchEnabled1');
             });

             socket.on('incrementRoundIndex1', () => {
                 roundIndex1++;
                 console.log("roundIndex1: " + roundIndex1)
                 io.emit('updateRoundIndex1', roundIndex1);
               });

               socket.on('decrementRoundIndex1', () => {
                 roundIndex1--;
                 console.log("roundIndex1: " + roundIndex1)
                 io.emit('updateRoundIndex1', roundIndex1);
               });
              socket.on('incrementAnswerIndex1', () => {
                  answerIndex1++;
                  console.log("answerIndex1: " + answerIndex1)
                  io.emit('updateAnswerIndex1', answerIndex1);
                });

                socket.on('decrementAnswerIndex1', () => {
                  answerIndex1--;
                   console.log("answerIndex1: " + answerIndex1)
                  io.emit('updateAnswerIndex1', answerIndex1);
                });

                socket.on('continuedTrue1', () => {
                  isContinued1 = true;
                  io.emit('updateContinued1', isContinued1);
                });

                socket.on('continuedFalse1', () => {
                  isContinued1 = false;
                  io.emit('updateContinued1', isContinued1);
                });
            socket.on('incrementCurrentGame1', () => {
                 currentGame1++;
                 console.log("currentGame1: " + currentGame1)
                 io.emit('updateCurrentGame1', currentGame1);
               });

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
              socket.on('startNextGame2', () => {
                     io.emit('startNextGame2');
                 });
               socket.on('endGame2', () => {
                    io.emit('endGame2');
               });
                socket.on('startActivity2', () => {
                     io.emit('startActivity2');
                });

              socket.on('showToast2', (message) => {
                io.emit('showToast2', message);
              });

              socket.on('disableTouch2', (targetSocketId) => {
                 io.to(targetSocketId).emit('touchDisabled2');
             });

             socket.on('enableTouch2', (targetSocketId) => {
                 io.to(targetSocketId).emit('touchEnabled2');
             });

             socket.on('incrementRoundIndex2', () => {
                 roundIndex2++;
                  console.log("roundIndex2: " + roundIndex2)
                 io.emit('updateRoundIndex2', roundIndex2);
               });

               socket.on('decrementRoundIndex2', () => {
                 roundIndex2--;
                  console.log("roundIndex2: " + roundIndex2)
                 io.emit('updateRoundIndex2', roundIndex2);
               });
            socket.on('incrementCurrentGame2', () => {
                 currentGame2++;
                 console.log("currentGame2: " + currentGame2)
                 io.emit('updateCurrentGame2', currentGame2);
               });
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

             socket.on('buttonClickable2', (buttonId, clickable) => {
                  io.emit('buttonClickable2', buttonId, clickable);
             });

          socket.on("inputText", (text) => {
                io.emit("inputText", text);
          });
          socket.on("buttonAnswerText", (text) => {
              io.emit("buttonAnswerText", text);
          });

          socket.on("buttonAnswerText2", (text) => {
                io.emit("buttonAnswerText2", text);
          });
          socket.on('showToast', (message, targetSocketId) => {
                   io.to(targetSocketId).emit('showToast', message);
          });
           socket.on('incrementConfirmCount', () => {
                confirmClicked++;
                 console.log("confirmClicked: " + confirmClicked);
            io.emit('updateConfirmClicked', confirmClicked);
           });

           socket.on('decrementConfirmCount', () => {
              confirmClicked--;
               console.log(confirmClicked);
           io.emit('updateConfirmClicked', confirmClicked);
           });

           socket.on('checkTwoAnswers', () => {
              io.emit('checkTwoAnswers');
           });

            socket.on("setFinalAnswer", (receivedAnswer, ack) => {
                    io.emit('setFinalAnswer', receivedAnswer);
                ack.call(null, receivedAnswer);
            });

            socket.on("setFinalAnswer2", (receivedAnswer, ack) => {
                io.emit('setFinalAnswer2', receivedAnswer);
              ack.call(null, receivedAnswer);
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
                 answerIndex1 = 0;
                 confirmClicked = 0;
                  roundIndex1 = 0;
                   roundIndex2 = 0;
                  confirmClicked1 = 0;
                  currentGame = 0
                  currentGame1 = 0
                  currentGame2 = 0
        });
           socket.on('playerDisconnected', (userInfo) => {
                 const { username } = userInfo;
                      delete playingUsers[socket.id];
                     playingUsernamesArray = Object.values(playingUsers).map(user => user.username);
                      playingSocketsArray = Object.values(playingUsers).map(user => user.socket.id);
                      io.emit('updatePlayingUsers', playingUsernamesArray, playingSocketsArray);
                       io.emit('updatePlayingUsers1', playingUsernamesArray, playingSocketsArray);
                        io.emit('updatePlayingUsers2', playingUsernamesArray, playingSocketsArray);
                         io.emit('updatePlayingUsers3', playingUsernamesArray, playingSocketsArray);
                          console.log("Playing Users:",playingUsernamesArray)
                     playingSocketsArray = Object.values(playingUsers).map(user => user.socket.id);
                         userReadyCount = 0;
                         isGameStarting = false;
                         roundIndex = 0;
                         answerIndex1 = 0;
                         confirmClicked = 0;
                         roundIndex1 = 0;
                         roundIndex2 = 0;
                          confirmClicked1 = 0;
                           currentGame = 0
                           currentGame1 = 0
                           currentGame2 = 0
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
          io.emit('updatePlayingUsers1', playingUsernamesArray, playingSocketsArray);
           io.emit('updatePlayingUsers2', playingUsernamesArray, playingSocketsArray);
            io.emit('updatePlayingUsers3', playingUsernamesArray, playingSocketsArray);
        console.log("Playing Users:",playingUsernamesArray)

   	   userReadyCount = 0;
       isGameStarting = false;
       roundIndex = 0;
       answerIndex1 = 0;
       confirmClicked = 0;
        roundIndex1 = 0;
       roundIndex2 = 0;
        confirmClicked1 = 0;
         currentGame = 0
        currentGame1 = 0
        currentGame2 = 0
   	});


});

