# Mobile Game Slagalica Project

## System Purpose:
The project task involves developing an Android mobile application inspired by the quiz game "Slagalica." The mobile application aims to facilitate player competition through one-on-one gaming across several different games.

## Technologies Used:
The mobile application is developed using Java programming language for Android, utilizing Firebase for database creation and management, also incorporates the use of WebSockets to enable real-time communication and updates. Implemented the application using a three-layer architecture for clear separation of the presentation layer, business logic, and data management.

## Launch Guide

### Prerequisites

1. Android Studio installed on your machine.
2. Node.js installed for running the server.

### Steps to Launch the Application
1. **Clone the Repository:**
   ```bash
   git clone https://github.com/anna02272/Game-Slagalica
   ```
2. **Open in Android Studio:**
   Open Android Studio and choose "Open an existing Android Studio project." Navigate to the cloned repository and select the project folder.

3. **Sync Gradle:**
   Click on the "Sync Project with Gradle Files" button in Android Studio to ensure all dependencies are resolved.

4. **Run the Socket Server:**
   Navigate to the `server` folder in the terminal:
   ```bash
   cd mobile-application/server
   ```
   Install dependencies:
   ```bash
   npm install
   ```
   Start the server:
   ```bash
   node index.js
   ```
   The server will start, and you should see a message indicating that the server is running.

5. **Update SocketHandler with IP Address:**
   Open `app/src/main/java/com/example/slagalica/config/SocketHandler.java`. Find the following line:
   ```java
    socket = IO.socket("http://" + BuildConfig.IP_ADDR + ":3000");
   ```
   Replace `"IP_ADDR"` with your machine's IPv4 address.
    - If running the server on a different machine, make sure the Android device/emulator and the server machine are on the same local network.

6. **Run the Application:**
   Connect your Android device or use an emulator and click on the "Run" button in Android Studio. The application should build and launch on the device.

7. **Enjoy the Game:**
   You are now ready to enjoy the mobile application! Play games, view statistics, and compete with friends.

## Users:
The mobile application caters to both registered and unregistered players. A registered player can perform all application functions (play games, access a profile with game statistics, view rankings, etc.), while an unregistered player can only play games.

## Games:
Each round consists of six games played consecutively, with each game having a duration and a specific point value. Each game is explained separately below:

1. **Ko zna zna (Who Knows, Knows):**
    - 1 round, 25 seconds duration, max 50 points, min -25 points.
    - General knowledge-based, 5 questions with four possible answers.
    - Players have 5 seconds to answer each question.
    - Correct answers earn 10 points, incorrect answers result in -5 points.
    - If both players answer correctly, the faster player gets the points.
    - If no one answers, the game moves to the next question, and the point state remains unchanged.

2. **Spojnice (Connections):**
    - 2 rounds, 60 seconds duration, max 20 points, min 0 points.
    - Players connect five terms from the left column with terms from the right column based on a specified criterion.
    - Each connected term earns 2 points, totaling 10 points per round.

3. **Asocijacije (Associations):**
    - 2 rounds, 4 minutes duration, max 60 points, min 0 points.
    - Players associate four columns with hidden words or expressions that correspond to the solution in the fifth field.
    - Opening a field allows the player to guess the column or the final solution.
    - Each column's solution is scored: 2 points + 1 point for each unopened field.

4. **Skočko:**
    - 2 rounds, 60 seconds duration, max 35 points, min 0 points.
    - Players attempt to find a given combination of 4 symbols in 6 attempts with 6 symbols.
    - Points are awarded based on the attempts.

5. **Korak po korak (Step by Step):**
    - 2 rounds, 140 seconds duration, max 25 points, min 0 points.
    - Players guess a term in a maximum of 7 steps, with each step reducing the points.

6. **Moj broj (My Number):**
    - 2 rounds, 2 minutes duration, max 40 points, min 0 points.
    - Players create an expression using 6 numbers and basic operations to reach the target number.
    - Points are awarded based on the correctness of the expression.

## Application Requirements:
1. **Player Registration and Login:**
    - Registration requires email, username, password, and password confirmation.
    - Players log in using their email/username and password.

2. **Profile Display for Registered Users:**
    - Display username, email address, ability to add/change a profile picture.
    - Show player statistics: success in each game, total games played, win/loss percentage.
    - Logout option.

3. **Gameplay:**
    - Players use tokens to start a game; 5 tokens are awarded daily and upon registration.
    - Start a game by connecting to a randomly selected online player or a friend.
    - Game mechanics are explained above.
    - After a game, winners gain stars (10 stars + 1 star for every 40 points), and losers lose stars (10 stars - 1 star for every 40 points).
    - 50 stars earn a player 1 token.

4. **Leaderboard:**
    - Rank players on a weekly and monthly basis.
    - Display a leaderboard with usernames and earned stars.
    - Reward players with tokens based on their weekly and monthly rankings.

5. **Adding Friends:**
    - Players can search for others by username to add them to their friends list.
    - View friend profiles, including profile picture, username, current monthly rank, and stars.

6. **Application Layout:**
    - Allow players to access their profile, statistics, friends list, leaderboards, and start games (except during a game).
    - Display the current number of tokens, stars, and rank (except during a game).
    - During a game, restrict access to other pages; the game should be in focus.

7. **Sensor Interaction:**
    - Uses the shake sensor for stopping numbers in the "My Number" game.

## Images of project (light and dark mode)
### Splash screen: 
<img src="https://github.com/anna02272/MobilneAplikacije2023-projekat/assets/96575598/f8045cf3-6992-448f-90e8-7698d85d49c0" alt="Splash screen light" width="30%"> <img src="https://github.com/anna02272/MobilneAplikacije2023-projekat/assets/96575598/ceddccf6-9ace-4a92-8a9c-ba8c35b552df" alt="Splash screen dark" width="30%">
### Home page (guest and user):
<img src="https://github.com/anna02272/MobilneAplikacije2023-projekat/assets/96575598/010128f4-0a7b-4388-84e0-f72a1a1fd760" alt="Home page guest" width="30%"> <img src="https://github.com/anna02272/MobilneAplikacije2023-projekat/assets/96575598/b1792b91-280e-43d2-a969-ba647bcbe702" alt="Home page user" width="30%">
### Start game :
<img src="https://github.com/anna02272/MobilneAplikacije2023-projekat/assets/96575598/7842cae7-b1eb-4162-a9ae-d381bd711dd8" alt="Start game light" width="30%"> <img src="https://github.com/anna02272/MobilneAplikacije2023-projekat/assets/96575598/f36359fd-f60c-4c77-80ff-1ea6de5c734a" alt="Start game dark" width="30%">
### Game Spojnice :
<img src="https://github.com/anna02272/MobilneAplikacije2023-projekat/assets/96575598/bcf56342-b88c-42c8-8917-8e5412c8a56c" alt="Spojnice light" width="30%"> <img src="https://github.com/anna02272/MobilneAplikacije2023-projekat/assets/96575598/1fc87dd5-b746-45a6-a6e9-7b28bd3054d9" alt="Spojnice dark" width="30%">
### Game Korak po korak :
<img src="https://github.com/anna02272/MobilneAplikacije2023-projekat/assets/96575598/24e6a521-783f-461d-af02-a3c4d9ce1f4b" alt="Korak po korak light" width="30%"> <img src="https://github.com/anna02272/MobilneAplikacije2023-projekat/assets/96575598/b5fc43f7-82d5-4f26-b46d-5bfcac8adf8e" alt="Korak po korak dark" width="30%">
### Game Moj broj :
<img src="https://github.com/anna02272/MobilneAplikacije2023-projekat/assets/96575598/1c2b840b-fb1d-4835-bf5f-2f13faea3f6b" alt="Moj broj light" width="30%"> <img src="https://github.com/anna02272/MobilneAplikacije2023-projekat/assets/96575598/674d89b1-d6b4-44c1-af7b-eeff33bf5b80" alt="Moj broj dark" width="30%">
### Game Asocijacije :
<img src="https://github.com/anna02272/MobilneAplikacije2023-projekat/assets/96575598/a8be9e5f-57e1-4d9e-a5f0-440aa0f0a417" alt="Asocijacije light" width="30%"> <img src="https://github.com/anna02272/MobilneAplikacije2023-projekat/assets/96575598/fe4e4d38-734d-4edd-bc72-7299d14059c5" alt="Asocijacije dark" width="30%">
### Game Ko zna zna :
<img src="https://github.com/anna02272/MobilneAplikacije2023-projekat/assets/96575598/fd4c95b4-43c0-4257-80ed-4019258a3368" alt="Ko zna zna light" width="30%"> <img src="https://github.com/anna02272/MobilneAplikacije2023-projekat/assets/96575598/0886f6ca-ebe5-40b7-b3f7-fdc758d81fdb" alt="Ko zna zna dark" width="30%">
### Game Skocko:
<img src="https://github.com/anna02272/MobilneAplikacije2023-projekat/assets/96575598/24fb2a98-b486-4f0c-b535-58c8da3d1332" alt="Skocko light" width="30%"> <img src="https://github.com/anna02272/MobilneAplikacije2023-projekat/assets/96575598/eba14181-14e5-4814-a0d3-7de047d1be6b" alt="Skocko dark" width="30%">
### Login and registration :
<img src="https://github.com/anna02272/MobilneAplikacije2023-projekat/assets/96575598/77a5bd3a-6ea4-47e8-94be-e3023e0b3a23" alt="Login" width="30%"> <img src="https://github.com/anna02272/MobilneAplikacije2023-projekat/assets/96575598/a64e83fe-f799-4d94-b2ed-079325d3846a" alt="Registration" width="30%">
### Menu :
<img src="https://github.com/anna02272/MobilneAplikacije2023-projekat/assets/96575598/cbd5c15d-9ac5-41b6-aef8-4e004dbaf22d" alt="Menu light" width="30%"> <img src="https://github.com/anna02272/MobilneAplikacije2023-projekat/assets/96575598/453c02a8-665d-4a05-94ee-8688c0312567" alt="Menu dark" width="30%">
### Profile :
<img src="https://github.com/anna02272/MobilneAplikacije2023-projekat/assets/96575598/7c0f7da3-ec54-4a96-9058-aaf4edb880e3" alt="Profile" width="30%"> 




