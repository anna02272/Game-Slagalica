# Mobile Application Project

## System Purpose:
The project task involves developing an Android mobile application inspired by the quiz game "Slagalica." The mobile application aims to facilitate player competition through one-on-one gaming across several different games.

## Technologies Used:
The mobile application is developed using Java programming language for Android, utilizing Firebase for database creation and management. Implemented the application using a three-layer architecture for clear separation of the presentation layer, business logic, and data management.

# Mobile Application Project

## Launch Guide

### Prerequisites

1. Android Studio installed on your machine.
2. Node.js installed for running the server.

### Steps to Launch the Application
1. **Clone the Repository:**
   ```bash
   git clone https://github.com/your-username/mobile-application.git
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
